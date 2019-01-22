package requests;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateRequestHandler implements HttpHandler {

    public static final String JSON_FILE_DIRECTORY_PATH = "src/main/resources/current_json/";

    private static final String JSON_HOST = "https://raw.githubusercontent.com/vas-test/test1/master/logs/";

    private static final String JSON_FORMAT = "MCP_%s.json";

    private static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * handle the http requests
     *
     * @param request
     * @throws IOException
     */
    @Override
    public void handle(HttpExchange request) throws IOException {
        try {

            InputStreamReader isr = new InputStreamReader(request.getRequestBody(), StandardCharsets.UTF_8);

            BufferedReader br = new BufferedReader(isr);

            String query;

            try {
                query = br.readLine();

            } finally {
                isr.close();
                br.close();
            }

            String json_file = String.format(JSON_FORMAT, getDate(query));

            createCurrentJsonFile(json_file);

            getJsonFile(json_file);

            Utils.sendResponse(request, json_file);

        } catch (HttpRequestException e) {
            Utils.sendResponse(request, e.getMessage());
        }
    }

    /**
     * Return the date from Request String query
     *
     * @param query
     * @return date
     */
    private String getDate(String query) throws HttpRequestException {
        String date;

        if (query.isEmpty())
            throw new HttpRequestException("Empty Date");

        if (query.contains("&"))
            throw new HttpRequestException("The query has to have only one condition");

        try {

            date = query.split("=")[1];

            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            dateFormat.setLenient(false);
            dateFormat.parse(date.trim());

        } catch (ParseException pe) {
            throw new HttpRequestException("The date has to be defined with 'yyyyMMdd' syntax");

        } catch (ArrayIndexOutOfBoundsException e) {
            throw new HttpRequestException("The query has to be defined with 'key=date' syntax");
        }
        return date;
    }

    /**
     * Get json file from the date request from the host
     *
     * @param json_file_name
     */
    private void getJsonFile(String json_file_name) throws HttpRequestException {

        try {
            URL url = new URL(JSON_HOST + json_file_name);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            handleHttpResponseCode(connection, json_file_name);

            InputStream stream = connection.getInputStream();

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(stream)); FileWriter fw = new FileWriter(JSON_FILE_DIRECTORY_PATH + json_file_name)) {
                String line;
                while ((line = rd.readLine()) != null) {
                    fw.write(line);
                    fw.write("\n");
                }
            }

        } catch (HttpRequestException e) {
            throw e;

        } catch (Exception e) {
            throw new HttpRequestException("Error getting json from the host source");
        }
    }

    /**
     * Handle de http code response from get json request
     *
     * @param connection
     * @param json_file_name
     * @throws IOException
     * @throws HttpRequestException
     */
    private void handleHttpResponseCode(HttpsURLConnection connection, String json_file_name) throws IOException, HttpRequestException {

        int code = connection.getResponseCode();

        if (code == 404) {
            throw new HttpRequestException("404 - File " + json_file_name + " not found");
        } else if (code == 400) {
            throw new HttpRequestException("400 - Request to file " + json_file_name + " failed");
        } else if (code == 403) {
            throw new HttpRequestException("403 - Do you not have access to file " + json_file_name);
        }
    }

    /**
     * Create the new Json file, requested by the Client, if not exists
     *
     * @param json_file_name
     * @throws IOException
     */
    private void createCurrentJsonFile(String json_file_name) throws HttpRequestException {
        try {
            File newJsonFile = new File(JSON_FILE_DIRECTORY_PATH + json_file_name);

            if (!newJsonFile.exists()) {
                removeOldJson();
                if (!newJsonFile.createNewFile())
                    throw new Exception();
            }

        } catch (Exception e) {
            throw new HttpRequestException("Error creating the json file");
        }
    }

    /**
     * If the json file exists remove it, to replace with the current one
     */
    private void removeOldJson() throws HttpRequestException {
        File json_directory = new File(JSON_FILE_DIRECTORY_PATH);
        File[] filesList = json_directory.listFiles();

        if (filesList != null && filesList.length > 0)
            if (!filesList[0].delete()) {
                throw new HttpRequestException("The old json file was not removed correctly");
            }

    }
}
