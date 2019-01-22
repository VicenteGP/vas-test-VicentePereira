package messages;

public abstract class Message {

    private String message_type;
    private int timestamp;
    private long origin;
    private long destination;


    public Message(String message_type, int timestamp, long origin, long destination) {
        this.message_type = message_type;
        this.timestamp = timestamp;
        this.origin = origin;
        this.destination = destination;
    }

    public String getMessage_type() {
        return message_type;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public long getOrigin() {
        return origin;
    }

    public long getDestination() {
        return destination;
    }

    /**
     * Return the total number of fields with blank/empty values
     *
     * @return total number
     */
    public Integer getBlankContentMessages() {
        int result = 0;

        if (getMessage_type().isEmpty()) {
            result += 1;

        }
        if (getTimestamp() == 0) {
            result += 1;

        }
        if (getOrigin() == 0) {
            result += 1;

        }
        if (getDestination() == 0) {
            result += 1;
        }
        return result;
    }

    abstract boolean hasFieldErrors();
}
