package measures;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import messages.Call;
import messages.Msg;
import org.json.JSONException;
import org.json.JSONObject;
import requests.DateRequestHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Metrics {

    private static final String MISSING_FIELD_KEY = "Missing Field";
    private static final String FIELD_ERROR_KEY = "Field Error";
    private static final String ORIGIN_DESTINATION_RELATION_FORMAT = "%s\\%s";
    private static final String[] WORDS = {"ARE", "YOU", "FINE", "HELLO", "NOT"};
    private String result;
    private File json;
    private Kpis kpis;
    private int missing_fields;
    private int blank_content;
    private int fields_errors;
    private int ok;
    private int ko;
    private Map<Integer, Map<String, Integer>> origin_destination_calls;
    private Map<Integer, List<Integer>> call_duration;
    private Map<String, Integer> word_occurrences;

    public Metrics(Kpis kpis) {
        this.kpis = kpis;

        initializeLocalVariables();

        BufferedReader in;

        try {
            long startTime = System.currentTimeMillis();

            this.json = getJsonFile();

            in = new BufferedReader(new FileReader(json));

            this.kpis.incrementTotal_json();

            try {
                String line;
                while ((line = in.readLine()) != null) {
                    this.kpis.incrementTotal_rows();

                    try {
                        JSONObject json = new JSONObject(line);
                        getJsonMetrics(json);

                    } catch (MeasureException e) {

                        String exception = e.getMessage();

                        if (exception.equals(FIELD_ERROR_KEY)) {
                            fields_errors += 1;

                        } else if (exception.equals(MISSING_FIELD_KEY)) {
                            missing_fields += 1;
                        }
                    } catch (JSONException e) {
                        //Json with syntax errors count as fields_errors
                        fields_errors += 1;
                    }

                }

            } finally {
                in.close();
            }

            createMetricResult();

            long endTime = System.currentTimeMillis();
            kpis.addJson_process_time(json.getName(), endTime - startTime);

        } catch (FileNotFoundException e) {
            result = "File not found. Please request the desired json by date";
        } catch (IOException e) {
            result = "Error Reading the File";
        }
    }

    /**
     * Get Current Json File
     *
     * @return json file
     */
    private File getJsonFile() throws FileNotFoundException {
        File directory = new File(DateRequestHandler.JSON_FILE_DIRECTORY_PATH);
        File[] files = directory.listFiles();

        if (files == null || files.length == 0)
            throw new FileNotFoundException();

        return files[0];
    }

    /**
     * Initialize local variables when instantiated
     */
    private void initializeLocalVariables() {
        result = "";
        missing_fields = 0;
        blank_content = 0;
        fields_errors = 0;
        ok = 0;
        ko = 0;
        call_duration = new HashMap<>();
        word_occurrences = new HashMap<>();
        origin_destination_calls = new HashMap<>();
    }

    /**
     * Get the current json metrics
     *
     * @param json
     */
    private void getJsonMetrics(JSONObject json) throws MeasureException {

        if (json.has("message_type")) {
            Object message_type_object = json.get("message_type");

            if (message_type_object instanceof String) {

                String message_type = ((String) message_type_object).toUpperCase();

                try {
                    if (Call.KEY_NAME.equals(message_type)) {

                        if (json.keySet().equals(Call.STRUCTURE)) {
                            kpis.incrementTotal_call();

                            Call call = new Gson().fromJson(json.toString(), Call.class);

                            uniqueCallHandler(call);

                            kpis.getOriginDestination(call);

                        } else {
                            //Does not match with CALL key structure
                            throw new MeasureException(MISSING_FIELD_KEY);
                        }

                    } else if (Msg.KEY_NAME.equals(message_type)) {

                        if (json.keySet().equals(Msg.STRUCTURE)) {
                            kpis.incrementTotal_msg();

                            Msg msg = new Gson().fromJson(json.toString(), Msg.class);

                            uniqueMsgHandler(msg);

                            kpis.getOriginDestination(msg);

                        } else {
                            //Does not match with MSG key structure
                            throw new MeasureException(MISSING_FIELD_KEY);
                        }

                    } else if (!message_type.isEmpty()) {
                        // Message type neither CALL nor MSG
                        throw new MeasureException(FIELD_ERROR_KEY);
                    }

                } catch (JsonSyntaxException e) {
                    //Fields with wrong types (Error initializing MSG or CALL Object)
                    throw new MeasureException(FIELD_ERROR_KEY);
                }

            } else {
                //Message type is not a string
                throw new MeasureException(FIELD_ERROR_KEY);
            }

        } else {
            //Does not have the message_type
            throw new MeasureException(MISSING_FIELD_KEY);
        }
    }

    /**
     * Handler Specific Call Metrics
     *
     * @param call
     */
    private void uniqueCallHandler(Call call) {
        blank_content += call.getBlankContentMessages();

        if (call.hasFieldErrors()) {
            fields_errors += 1;
        } else {
            incrementOKKO(call);
        }

        getDurationsByCC(call);

        getOriginDestinationCalls(call);
    }

    /**
     * Handler Specific Msg Metrics
     *
     * @param msg
     */
    private void uniqueMsgHandler(Msg msg) {
        blank_content += msg.getBlankContentMessages();

        if (msg.hasFieldErrors())
            fields_errors += 1;

        incrementWordsOccurrence(msg);
    }

    /**
     * Increment the total of OK and KO in Calls
     *
     * @param call
     */
    private void incrementOKKO(Call call) {
        if (call.getStatus_code().equals("OK")) {
            ok += 1;
        } else if (call.getStatus_code().equals("NO")) {
            ko += 1;
        }
    }

    /**
     * get relation between Origin/Destination, group by Origin Country Code
     * syntax: {CountryCode: { Relation : NumberOccurrences } }
     *
     * @param call
     */
    private void getOriginDestinationCalls(Call call) {

        int countryCode = getCountryCode(call.getOrigin());

        String relation = String.format(ORIGIN_DESTINATION_RELATION_FORMAT, call.getOrigin(), call.getDestination());

        if (origin_destination_calls.containsKey(countryCode)) {
            int count = origin_destination_calls.get(countryCode).getOrDefault(relation, 0);
            origin_destination_calls.get(countryCode).put(relation, count + 1);

        } else {
            Map<String, Integer> new_relation_count = new HashMap<>();
            new_relation_count.put(relation, 1);
            origin_destination_calls.put(countryCode, new_relation_count);
        }
    }

    /**
     * Get MSISDN Country Code, Assuming that are first 2 digits
     *
     * @param msisdn
     * @return counry code
     */
    private int getCountryCode(long msisdn) {
        return Integer.parseInt(String.valueOf(msisdn).substring(0, 2));
    }


    /**
     * Create object with all duration by Country code (Origin and Destination)
     *
     * @param call
     */
    private void getDurationsByCC(Call call) {
        incrementDurationByCC(getCountryCode(call.getOrigin()), call.getDuration());

        incrementDurationByCC(getCountryCode(call.getDestination()), call.getDuration());
    }

    /**
     * Increment the current duration to CC
     *
     * @param countryCode
     * @param duration
     */
    private void incrementDurationByCC(int countryCode, int duration) {
        if (call_duration.containsKey(countryCode)) {
            call_duration.get(countryCode).add(duration);

        } else {
            List<Integer> aux = new ArrayList<>();
            aux.add(duration);
            call_duration.put(countryCode, aux);
        }
    }

    /**
     * If msg contains any the WORDS increment in global counter
     *
     * @param msg
     */
    private void incrementWordsOccurrence(Msg msg) {
        String content = msg.getMessage_content();
        for (String WORD : WORDS) {
            if (content.contains(WORD)) {
                int count = word_occurrences.getOrDefault(WORD, 0);
                word_occurrences.put(WORD, count + 1);
            }
        }

    }

    private File getJson() {
        return json;
    }

    public String getResult() {
        return result;
    }

    private int getMissing_fields() {
        return missing_fields;
    }

    private int getBlank_content() {
        return blank_content;
    }

    private int getFields_errors() {
        return fields_errors;
    }

    private int getOk() {
        return ok;
    }

    private int getKo() {
        return ko;
    }

    private String getOkKoRelation() {
        return String.format("{OK: %d, KO: %d}", getOk(), getKo());
    }

    private Map<Integer, Map<String, Integer>> getOrigin_destination_calls() {
        return origin_destination_calls;
    }

    /**
     * get String relation origin/destination by CC
     *
     * @return
     */
    private String getOriginDestinationRelationString() {
        return new JSONObject(getOrigin_destination_calls()).toString();
    }

    private Map<Integer, List<Integer>> getCall_duration() {
        return call_duration;
    }

    /**
     * get the call duration average
     *
     * @return
     */
    private String getCallDurationAverageString() {

        Map<Integer, Double> duration_Average = new HashMap<>();

        for (Integer key : getCall_duration().keySet()) {

            Long total_duration = 0L;

            List<Integer> durations = getCall_duration().get(key);

            for (Integer duration : durations) {
                total_duration += duration;
            }

            double average = (double) (total_duration / getCall_duration().size());

            duration_Average.put(key, average);
        }

        return new JSONObject(duration_Average).toString();
    }

    private Map<String, Integer> getWord_occurrences() {
        return word_occurrences;
    }

    private String getWordsOccurrencesString() {
        return new JSONObject(getWord_occurrences()).toString();
    }

    /**
     * Create Result in the correctly structure
     */
    private void createMetricResult() {
        result = "{ Process Json File: " + getJson().getName() + ",\n" +
                "Number of rows with missing fields: " + getMissing_fields() + ",\n" +
                "Number of messages with blank content: " + getBlank_content() + ",\n" +
                "Number of rows with fields errors: " + getFields_errors() + ",\n" +
                "Number of calls origin/destination grouped by country code: " + getOriginDestinationRelationString() + ",\n" +
                "Relationship between OK/KO calls: " + getOkKoRelation() + ",\n" +
                "Average call duration grouped by country code: " + getCallDurationAverageString() + ",\n" +
                "Word occurrence ranking for the given words in message_content field: " + getWordsOccurrencesString() + "}";
    }

}
