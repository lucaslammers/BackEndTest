package Ontdekstation013.ClimateChecker.features.neighbourhood;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.responses.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodDTO;
import Ontdekstation013.ClimateChecker.utility.GpsTriangulation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NeighbourhoodService {
    // Latitude = Y
    // Longitude = X

    private final MeetJeStadService meetJeStadService;
    private final NeighbourhoodRepository neighbourhoodRepository;

    // Process measurements into neighbourhoods
    private List<NeighbourhoodDTO> getNeighbourhoodsAverageTemp(List<Neighbourhood> neighbourhoods, List<Measurement> measurements){
        List<NeighbourhoodDTO> neighbourhoodDTOS = new ArrayList<>();

        for (Neighbourhood neighbourhood : neighbourhoods) {
            NeighbourhoodDTO dto = new NeighbourhoodDTO();

            dto.setId(neighbourhood.getId());
            dto.setName(neighbourhood.getName());
            dto.setCoordinates(convertToFloatArray(neighbourhood.coordinates));

            // Get all measurements within this neighbourhood
            List<Measurement> tempMeasurements = new ArrayList<>();
            for (Measurement measurement : measurements) {
                float[] point = { measurement.getLatitude(), measurement.getLongitude() };
                if (GpsTriangulation.pointInPolygon(dto.getCoordinates(), point)) {
                    tempMeasurements.add(measurement);
                }
            }

            // Calculate average temperature of the measurements in this neighbourhood
            float totalTemp = 0.0f;
            int measurementCount = tempMeasurements.size();
            for (Measurement measurement : tempMeasurements) {
                if (measurement.getTemperature() != null) {
                    totalTemp += measurement.getTemperature();
                }
                else {
                    measurementCount--;
                }
            }
            dto.setAvgTemp(totalTemp / measurementCount);

            neighbourhoodDTOS.add(dto);
        }
        return neighbourhoodDTOS;
    }

    // Gets latest neighbourhood data with average temperature
    public List<NeighbourhoodDTO> getNeighbourhoodsLatest() {
        List<Neighbourhood> neighbourhoods = neighbourhoodRepository.findAll();
        List<Measurement> measurements = meetJeStadService.getLatestMeasurements();

        return getNeighbourhoodsAverageTemp(neighbourhoods, measurements);
    }

    // Gets neighbourhood data at given time with average temperature
    public List<NeighbourhoodDTO> getNeighbourhoodsAtTime(Instant dateTime){
        List<Neighbourhood> neighbourhoods = neighbourhoodRepository.findAll();

        int minuteMargin = meetJeStadService.getMinuteLimit();
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = dateTime.minus(Duration.ofMinutes(minuteMargin));
        params.EndDate = dateTime.plus(Duration.ofMinutes(minuteMargin));
        List<Measurement> allMeasurements = meetJeStadService.getMeasurements(params);

        return getNeighbourhoodsAverageTemp(neighbourhoods, allMeasurements);
    }

    public List<DayMeasurementResponse> getNeighbourhoodData(Long id, Instant startDate, Instant endDate) {
        Optional<Neighbourhood> neighbourhoodOptional = neighbourhoodRepository.findById(id);
        Neighbourhood neighbourhood;
        if (neighbourhoodOptional.isPresent())
              neighbourhood = neighbourhoodOptional.get();
        else
            return new ArrayList<>();

        // Get all measurements from 1 day to filter out stations that are irrelevant
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = endDate.minusSeconds(60 * 60); // 1 day subtraction
        params.EndDate = endDate;
        List<Measurement> measurements = meetJeStadService.getMeasurements(params);

        // Get all station id's within this neighbourhood
        float[][] neighbourhoodCoords = convertToFloatArray(neighbourhood.coordinates);
        List<Integer> stations = new ArrayList<>();
        for (Measurement measurement : measurements) {
            float[] point = { measurement.getLatitude(), measurement.getLongitude() };
            if (GpsTriangulation.pointInPolygon(neighbourhoodCoords, point) && !stations.contains(measurement.getId()))
                stations.add(measurement.getId());
        }

        // Get all measurements within timeframe from these stations
        params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds = stations;
        measurements = meetJeStadService.getMeasurements(params);

        // Get the daily average
        HashMap<LocalDate, List<Measurement>> dayMeasurements = new LinkedHashMap<>();
        for (Measurement measurement : measurements) {
            if (measurement.getTemperature() != null) {
                LocalDate date = LocalDate.ofInstant(measurement.getTimestamp(), ZoneId.systemDefault());
                if (!dayMeasurements.containsKey(date)) {
                    dayMeasurements.put(date, new ArrayList<>());
                }

                dayMeasurements.get(date).add(measurement);
            }
        }

        List<DayMeasurementResponse> responseList = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Measurement>> entry : dayMeasurements.entrySet()) {
            LocalDate date = entry.getKey();
            float minTemp = entry.getValue()
                    .stream()
                    .map(Measurement::getTemperature)
                    .min(Float::compare)
                    .orElse(Float.NaN);
            float maxTemp = entry.getValue()
                    .stream()
                    .map(Measurement::getTemperature)
                    .max(Float::compare)
                    .orElse(Float.NaN);
            float avgTemp = (float) entry.getValue()
                    .stream()
                    .mapToDouble(Measurement::getTemperature)
                    .average()
                    .orElse(Double.NaN);

            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd-MM");

            DayMeasurementResponse response = new DayMeasurementResponse(
                    date.format(pattern),
                    avgTemp,
                    minTemp,
                    maxTemp
            );

            responseList.add(response);
        }

        return responseList;
    }

    // Converting a list of coordinates to a two-dimensional float array
    private float[][] convertToFloatArray(List<NeighbourhoodCoords> coordinates) {
        return coordinates.stream()
                .map(coord -> new float[]{ coord.getLatitude(), coord.getLongitude() })
                .toArray(float[][]::new);
    }
}