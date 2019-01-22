package messages;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Msg extends Message {

    public static final Set<String> STRUCTURE = new HashSet<>(Arrays.asList("message_type", "timestamp",
            "origin", "destination", "message_content", "message_status"));
    public static final String KEY_NAME = "MSG";
    private String message_content;
    private String message_status;

    public Msg(String message_type, int timestamp, int origin, int destination, String message_content, String message_status) {
        super(message_type, timestamp, origin, destination);
        this.message_content = message_content;
        this.message_status = message_status;

    }

    public String getMessage_content() {
        return message_content;
    }

    public String getMessage_status() {
        return message_status;
    }

    /**
     * Return the total number of fields with blank/empty values
     *
     * @return total number
     */
    @Override
    public Integer getBlankContentMessages() {

        int result = super.getBlankContentMessages();

        if (getMessage_content().isEmpty()) {
            result += 1;

        }
        if (getMessage_status().isEmpty()) {
            result += 1;

        }
        return result;
    }

    /**
     * Two values are valid: {DELIVERED|SEEN}
     *
     * @return boolean
     */
    @Override
    public boolean hasFieldErrors() {
        return !getMessage_status().equals("DELIVERED") && !getMessage_status().equals("SEEN");
    }

}
