package Ontdekstation013.ClimateChecker.features.measurement.endpoint;

import java.time.Instant;
import java.time.format.*;
import java.util.List;
import java.time.*;
import java.time.format.DateTimeFormatter;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Ontdekstation013.ClimateChecker.features.measurement.endpoint.responses.DayMeasurementResponse;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/measurement")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;
    private Logger LOG = LoggerFactory.getLogger(MeasurementController.class);

    @GetMapping("/latest")
    public ResponseEntity<List<MeasurementDTO>> getLatestMeasurements() {
        try {
            List<MeasurementDTO> measurements = measurementService.getLatestMeasurements();
            return ResponseEntity.ok(measurements);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/history")
    public List<MeasurementDTO> getMeasurementsAtTime(
            @RequestParam(value = "timestamp") String timestamp) {
        try {
            Instant utcDateTime = Instant.parse(timestamp);
            List<MeasurementDTO> measurements = measurementService.getMeasurementsAtTime(utcDateTime);
            return measurements;
        } catch (DateTimeParseException e) {
            throw new InvalidArgumentException("Timestamp must be in ISO 8601 format");
        }
    }
    
    @GetMapping("/latest/{id}")
    public MeasurementDTO getLatestMeasurement(@PathVariable int id) {
        return measurementService.getLatestMeasurement(id);
    }

    @GetMapping("/history/all/{id}")
    public List<MeasurementDTO> getMeasurements(@PathVariable int id, @RequestParam String startDate, @RequestParam String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        LocalDateTime localDateTimeStart = LocalDateTime.parse(startDate, formatter);
        Instant startInstant = localDateTimeStart.atZone(ZoneId.systemDefault()).toInstant();

        LocalDateTime localDateTimeEnd = LocalDateTime.parse(endDate, formatter);
        Instant endInstant = localDateTimeEnd.atZone(ZoneId.systemDefault()).toInstant();

        if (startInstant.isAfter(endInstant)){
            throw new InvalidArgumentException("Start date is after end date");
        }

        return measurementService.getMeasurements(id, startInstant, endInstant);
    }

    @GetMapping("/history/average/{id}")
    public List<DayMeasurementResponse> getMeasurementsAverage(@PathVariable int id, @RequestParam String startDate, @RequestParam String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        LocalDateTime localDateTimeStart = LocalDateTime.parse(startDate, formatter);
        Instant startInstant = localDateTimeStart.atZone(ZoneId.systemDefault()).toInstant();

        LocalDateTime localDateTimeEnd = LocalDateTime.parse(endDate, formatter);
        Instant endInstant = localDateTimeEnd.atZone(ZoneId.systemDefault()).toInstant();

        if (startInstant.isAfter(endInstant)){
            throw new InvalidArgumentException("Start date is after end date");
        }

        return measurementService.getMeasurementsAverage(id, startInstant, endInstant);
    }
}
