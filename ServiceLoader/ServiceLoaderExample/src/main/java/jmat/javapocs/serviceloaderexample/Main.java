package jmat.javapocs.serviceloaderexample;

import jmat.javapocs.serviceinterfaceexample.SomeService;
import java.util.ServiceLoader;

public class Main {
    public static void main(final String[] args) {
        System.out.println("Loading and calling services...");

        final ServiceLoader<SomeService> someServices = ServiceLoader.load(SomeService.class);
        for(final SomeService someService : someServices) {
            someService.performSomeOperation();
        }
    }
}
