package messages;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Call extends Message {

    public static final Set<String> STRUCTURE = new HashSet<String>(Arrays.asList("message_type", "timestamp",
            "origin", "destination", "duration", "status_code", "status_description"));
    public static final String KEY_NAME = "CALL";
    private int duration;
    private String status_code;
    private String status_description;

    public Call(String message_type, int timestamp, int origin, int destination, int duration, String status_code, String status_description) {
        super(message_type, timestamp, origin, destination);
        this.duration = duration;
        this.status_code = status_code;
        this.status_description = status_description;

    }

    public int getDuration() {
        return duration;
    }

    public String getStatus_code() {
        return status_code;
    }

    public String getStatus_description() {
        return status_description;
    }

    /**
     * Return the total number of fields with blank/empty values
     *
     * @return total number
     */
    @Override
    public Integer getBlankContentMessages() {

        int result = super.getBlankContentMessages();

        if (getStatus_code().isEmpty()) {
            result += 1;

        }
        if (getStatus_description().isEmpty()) {
            result += 1;

        }
        if (getDuration() == 0) {
            result += 1;
        }

        return result;
    }


    /**
     * Two values are valid: {OK|KO}
     *
     * @return boolean
     */
    @Override
    public boolean hasFieldErrors() {
        return !getStatus_code().equals("OK") && !getStatus_code().equals("KO");
    }
}
