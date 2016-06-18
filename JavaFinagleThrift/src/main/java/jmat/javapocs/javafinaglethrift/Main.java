package jmat.javapocs.javafinaglethrift;

import com.twitter.finagle.Thrift;
import com.twitter.util.Await;
import com.twitter.util.Future;
import com.twitter.util.FutureEventListener;
import jmat.javapocs.javafinaglethrift.model.SomeRequest;
import jmat.javapocs.javafinaglethrift.model.SomeResponse;
import jmat.javapocs.javafinaglethrift.model.SomeService;

public class Main {

    public static void main(final String[] args) throws Exception {
        final String address = "localhost:9000";

        Thrift.serveIface(
            address,
            (SomeService.ServiceIface) (final SomeRequest request) -> {
                final String name = request.getFirstName() + " " + request.getLastName();
                System.out.println("Service (9000): " + name);
                return Future.value(new SomeResponse("Hello " + name + "!"));
            }
        );

        System.out.println("Running...");

        final SomeService.ServiceIface client = Thrift.newIface(address, SomeService.ServiceIface.class);
        final Future<SomeResponse> response = client.someOperation(new SomeRequest("John", "Doe"));
        Await.ready(response)
            .addEventListener(new FutureEventListener<SomeResponse>() {

                @Override
                public void onSuccess(final SomeResponse response) {
                    System.out.println("Response: " + response);
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    System.out.println("Error: " + throwable);
                }
            });

        System.out.println("Done.");
    }
}
