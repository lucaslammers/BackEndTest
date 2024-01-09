package Ontdekstation013.ClimateChecker.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String customStatusCode) {
        super(String.valueOf(customStatusCode));
    }
}
