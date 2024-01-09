package Ontdekstation013.ClimateChecker.features.measurement;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.List;

import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDTO;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.responses.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeasurementService {
    private final MeetJeStadService meetJeStadService;

    public List<MeasurementDTO> getLatestMeasurements() {
        List<Measurement> uniqueLatestMeasurements = meetJeStadService.getLatestMeasurements();
        return uniqueLatestMeasurements.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<MeasurementDTO> getMeasurementsAtTime(Instant dateTime) {
        // get measurements within a certain range of the dateTime
        int minuteMargin = meetJeStadService.getMinuteLimit();
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = dateTime.minus(Duration.ofMinutes(minuteMargin));
        params.EndDate = dateTime.plus(Duration.ofMinutes(minuteMargin));

        List<Measurement> allMeasurements = meetJeStadService.getMeasurements(params);

        // select closest measurements to datetime
        Map<Integer, Measurement> measurementHashMap = new HashMap<>();
        for (Measurement measurement : allMeasurements) {
            int id = measurement.getId();
            if (!measurementHashMap.containsKey(id))
                measurementHashMap.put(id, measurement);
            else {
                Duration existingDifference = Duration.between(dateTime, measurementHashMap.get(id).getTimestamp()).abs();
                Duration newDifference = Duration.between(dateTime, measurement.getTimestamp()).abs();
                if (existingDifference.toSeconds() > newDifference.toSeconds())
                    measurementHashMap.put(id, measurement);
            }
        }

        List<Measurement> closestMeasurements = new ArrayList<>(measurementHashMap.values());
        return closestMeasurements.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public MeasurementDTO getLatestMeasurement(int id) {
        Measurement latestMeasurement = meetJeStadService.getLatestMeasurement(id);

        if (latestMeasurement == null)
            throw new NotFoundException("Measurement with stationId " + id + "could not be found");
        else
            return convertToDTO(latestMeasurement);
    }

    public List<MeasurementDTO> getMeasurements(int id, Instant startDate, Instant endDate) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds.add(id);

        List<Measurement> measurements = meetJeStadService.getMeasurements(params);
        return measurements.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<DayMeasurementResponse> getMeasurementsAverage(int id, Instant startDate, Instant endDate) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds.add(id);
        List<Measurement> measurements = meetJeStadService.getMeasurements(params);

        HashMap<LocalDate, Set<Measurement>> dayMeasurements = new LinkedHashMap<>();
        for (Measurement measurement : measurements) {
            if (measurement.getTemperature()!= null){
                LocalDate date = LocalDate.ofInstant(measurement.getTimestamp(), ZoneId.systemDefault());
                if (!dayMeasurements.containsKey(date)) {
                    dayMeasurements.put(date, new HashSet<>());
                }
                dayMeasurements.get(date).add(measurement);
            }
        }

        List<DayMeasurementResponse> responseList = new ArrayList<>();

        for (Map.Entry<LocalDate, Set<Measurement>> entry : dayMeasurements.entrySet()) {
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

    private MeasurementDTO convertToDTO(Measurement entity) {
        MeasurementDTO dto = new MeasurementDTO();
        dto.setId(entity.getId());
        dto.setLongitude(entity.getLongitude());
        dto.setLatitude(entity.getLatitude());
        dto.setTemperature(entity.getTemperature());
        dto.setHumidity(entity.getHumidity());

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd-MM-yyyy HH:mm:ss")
                .withZone(ZoneId.of("Europe/Amsterdam"));
        dto.setTimestamp(formatter.format(entity.getTimestamp()));

        return dto;
    }
}

