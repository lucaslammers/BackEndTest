package Ontdekstation013.ClimateChecker.features.measurement.endpoint.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayMeasurementResponse {
    @JsonProperty("timestamp")
    private String timestamp; // Format "dd-MM"
    @JsonProperty("avgTemp")
    private float avgTemp;
    @JsonProperty("minTemp")
    private float minTemp;
    @JsonProperty("maxTemp")
    private float maxTemp;
}
