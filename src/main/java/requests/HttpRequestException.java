package requests;

public class HttpRequestException extends Exception {

    /**
     * Custom Exception Object to handle http request exceptions
     *
     * @param message
     */
    public HttpRequestException(String message) {
        super(message);
    }
}
