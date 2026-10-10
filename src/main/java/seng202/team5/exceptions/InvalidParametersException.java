package seng202.team5.exceptions;

public class InvalidParametersException extends RuntimeException {

    private final InvalidParametersReason reason;
    public InvalidParametersException(InvalidParametersReason reason) {
        super();
        this.reason = reason;
    }

    public InvalidParametersReason getReason() {
        return reason;
    }

}
