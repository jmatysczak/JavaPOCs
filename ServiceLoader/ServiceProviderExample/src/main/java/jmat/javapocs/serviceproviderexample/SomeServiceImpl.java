package jmat.javapocs.serviceproviderexample;

import jmat.javapocs.serviceinterfaceexample.SomeService;

public class SomeServiceImpl implements SomeService {

    @Override
    public void performSomeOperation() {
        System.out.println("SomeServiceImpl.performSomeOperation() called!");
    }
}
