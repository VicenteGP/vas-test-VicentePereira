package requests;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class Utils {

    /**
     * send Http Response
     *
     * @param request
     * @param response
     * @throws IOException
     */
    public static void sendResponse(HttpExchange request, String response) throws IOException {
        request.sendResponseHeaders(200, response.length());
        try (OutputStream os = request.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
