package measures;

import messages.Message;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kpis {

    private static Kpis single_instance = null;

    private int total_json;

    private long total_rows;

    private long total_call;

    private long total_msg;

    private int total_origin;

    private int total_destination;

    private Map<String, Long> json_process_time;
    private List<Long> originCountries;
    private List<Long> destinationCountries;

    private Kpis() {
        initializeVariables();

    }

    public static Kpis getInstance() {
        if (single_instance == null)
            single_instance = new Kpis();

        return single_instance;
    }

    private void initializeVariables() {
        total_json = 0;
        total_rows = 0L;
        total_call = 0L;
        total_msg = 0L;
        total_origin = 0;
        total_destination = 0;
        json_process_time = new HashMap<>();
        originCountries = new ArrayList<>();
        destinationCountries = new ArrayList<>();
    }

    public int getTotal_json() {
        return total_json;
    }

    public void incrementTotal_json() {
        this.total_json += 1;
    }

    public long getTotal_rows() {
        return total_rows;
    }

    public void incrementTotal_rows() {
        this.total_rows += 1L;
    }

    public long getTotal_call() {
        return total_call;
    }

    public void incrementTotal_call() {
        this.total_call += 1L;
    }

    public long getTotal_msg() {
        return total_msg;
    }

    public void incrementTotal_msg() {
        this.total_msg += 1L;
    }

    public int getTotal_origin() {
        return total_origin;
    }

    public void incrementTotal_origin() {
        this.total_origin += 1;
    }

    public int getTotal_destination() {
        return total_destination;
    }

    public void incrementTotal_destination() {
        this.total_destination += 1;
    }

    public String getJsonProcessTimeString() {
        return new JSONObject(json_process_time).toString();
    }

    public void addJson_process_time(String json, Long processTime) {
        this.json_process_time.put(json, processTime);
    }

    /**
     * get the different origin and destination from a message
     *
     * @param message
     */
    public void getOriginDestination(Message message) {
        if (!destinationCountries.contains(message.getDestination())) {
            incrementTotal_destination();
            destinationCountries.add(message.getDestination());
        }
        if (!originCountries.contains(message.getOrigin())) {
            incrementTotal_origin();
            originCountries.add(message.getOrigin());
        }
    }

    @Override
    public String toString() {
        return "{ Total number of processed JSON files: " + getTotal_json() + ",\n" +
                "Total number of rows: " + getTotal_rows() + ",\n" +
                "Total number of calls: " + getTotal_call() + ",\n" +
                "Total number of messages: " + getTotal_msg() + ",\n" +
                "Total number of different origin country codes: " + getTotal_origin() + ",\n" +
                "Total number of different destination country codes: " + getTotal_destination() + ",\n" +
                "Duration of each JSON proces: " + getJsonProcessTimeString() + " }";
    }
}
