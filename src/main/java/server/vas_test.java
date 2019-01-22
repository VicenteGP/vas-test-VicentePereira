package server;

import com.sun.net.httpserver.HttpServer;
import measures.Kpis;
import requests.DateRequestHandler;
import requests.KpisRequestHandler;
import requests.MetricsRequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class vas_test {

    private static final int PORT = 8080;

    private static final int BACKLOG = 1;

    public static void main(String[] args) throws IOException {
        //Create a server in port 8080 with backlog TCP connection 1
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), BACKLOG);

        //Create a single Kpi instance
        Kpis kpis = Kpis.getInstance();

        //Create all http endpoints
        server.createContext("/", new DateRequestHandler());
        server.createContext("/metrics", new MetricsRequestHandler(kpis));
        server.createContext("/kpis", new KpisRequestHandler(kpis));

        server.start();
    }
}

