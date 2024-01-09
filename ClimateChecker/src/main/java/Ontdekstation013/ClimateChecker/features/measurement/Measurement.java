package Ontdekstation013.ClimateChecker.features.measurement;

import io.micrometer.core.lang.Nullable;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Measurement {
    private int id;
    private Instant timestamp;
    private float latitude;
    private float longitude;
    private Float temperature;
    private Float humidity;
}
