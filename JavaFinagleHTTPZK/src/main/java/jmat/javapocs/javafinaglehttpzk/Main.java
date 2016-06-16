/**
 * NOTE: Requires Zookeeper to be running on the standard port.
 */
package jmat.javapocs.javafinaglehttpzk;

import com.twitter.finagle.Http;
import com.twitter.finagle.ListeningServer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Methods;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.http.Status;
import com.twitter.util.Await;
import com.twitter.util.Future;
import com.twitter.util.FutureEventListener;

public class Main {

    public static void main(final String[] args) throws Exception {
        final String address = "zk!localhost:2181!/jmat/javapocs/javafinaglehttpzk/1.0/http";

        final ListeningServer server = Http.serve(
                "localhost:9000",
                new Service<Request, Response>() {

                    @Override
                    public Future<Response> apply(final Request request) {
                        System.out.println("Request:");
                        System.out.println("   Uri:     " + request.getUri());
                        System.out.println("   Method:  " + request.getMethod());
                        System.out.println("   Params:  " + request.getParamNames());
                        System.out.println("   Content: " + request.getContentString());

                        final Response response = Response.apply(request.version(), Status.Ok());
                        response.setContentString("Hello Response");

                        return Future.value(response);
                    }
                }
        );

        Await.ready(server.announce(address + "!0"));

        System.out.println("Serving on " + server.boundAddress() + "...");

        final Request request = Request.apply(Methods.POST, address);
        request.setContentString("Hello Request");
        request.setUri("/foo?bar=baz");

        final Service<Request, Response> client = Http.client().newService(address);

        final Future<Response> response = client.apply(request);

        response.addEventListener(new FutureEventListener<Response>() {

            @Override
            public void onSuccess(final Response response) {
                System.out.println("Response:");
                System.out.println("   Response: " + response);
                System.out.println("   Content:  " + response.getContentString());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                System.out.println(throwable);
            }

        });

        Await.ready(response);

        //Await.ready(server);
        System.out.println("Done.");
    }
}
