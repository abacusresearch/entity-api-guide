package auth;

public class EmptySystemPropertyException extends Exception {
    public EmptySystemPropertyException(String message) {
        super("System Property " + message + "is null or empty");
    }
}
