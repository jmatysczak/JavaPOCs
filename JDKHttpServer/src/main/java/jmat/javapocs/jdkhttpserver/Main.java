package jmat.javapocs.jdkhttpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.twitter.finagle.Http;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Methods;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Await;
import com.twitter.util.Future;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(final String[] args) throws Exception {
        final int port = 9000;
        final String address = "localhost:" + port;
        final String route1 = "/test-service-1";
        final String route2 = "/test-service-2";

        try (final AutoCloseable server = startServer(port, route1, route2)) {
            System.out.println("Serving...");

            final Service<Request, Response> client = Http.newService(address);

            final Request request = Request.apply(Methods.GET, address);
            request.setUri("/");
            final Future<Response> response = client.apply(request);

            final Request request1 = Request.apply(Methods.GET, address);
            request1.setUri(route1);
            final Future<Response> response1 = client.apply(request1);

            final Request request2 = Request.apply(Methods.GET, address);
            request2.setUri(route2);
            final Future<Response> response2 = client.apply(request2);

            final Future<List<Response>> futureResponses = Future.collect(Arrays.asList(response, response1, response2));

            Await.ready(futureResponses);

            final List<Response> responses = Await.result(futureResponses);

            responses.forEach(r -> System.out.println(r.contentString()));
        }

        System.out.println("Done.");
    }

    private static AutoCloseable startServer(final int port, final String route1, final String route2) throws Exception {
        final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", (HttpExchange exchange) -> {
            final String response = "Hello from route /!";
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.createContext(route1, (HttpExchange exchange) -> {
            final String response = "Hello from route 1!";
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.createContext(route2, (HttpExchange exchange) -> {
            final String response = "Hello from route 2!";
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.start();

        return () -> server.stop(1);
    }
}
