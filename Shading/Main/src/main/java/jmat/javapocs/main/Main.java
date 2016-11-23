package jmat.javapocs.main;

import jmat.javapocs.library.Library;
import jmat.javapocs.shading.Utils;

public final class Main {

    public static void main(final String[] args) {
        System.out.println();
        System.out.printf("Get Something: %s%n", Utils.getSomething());
        System.out.printf("Get Something Else: %s%n", Library.getSomethingElse());
        System.out.println("Done");
        System.out.println();
    }
}
