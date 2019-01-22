package requests;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import measures.Kpis;

import java.io.IOException;
import java.net.URI;

public class KpisRequestHandler implements HttpHandler {

    private Kpis kpis;

    public KpisRequestHandler(Kpis kpis) {
        this.kpis = kpis;
    }

    @Override
    public void handle(HttpExchange request) throws IOException {
        URI requestedUri = request.getRequestURI();
        String query = requestedUri.getRawQuery();

        if (query == null) {
            Utils.sendResponse(request, kpis.toString());
        } else {
            Utils.sendResponse(request, "The URI Query String should not be defined");
        }
    }

}
