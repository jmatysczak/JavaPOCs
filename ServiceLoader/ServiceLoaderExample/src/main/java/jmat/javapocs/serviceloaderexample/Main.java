package jmat.javapocs.serviceloaderexample;

import jmat.javapocs.serviceinterfaceexample.SomeService;
import java.util.Iterator;
import java.util.ServiceLoader;

public class Main {
    public static void main(final String[] args) {
        System.out.println("Loading and calling services...");
        final Iterator<SomeService> iterator = ServiceLoader.load(SomeService.class).iterator();
        while(iterator.hasNext()) {
            final SomeService someService = iterator.next();
            someService.performSomeOperation();
        }
    }
}
