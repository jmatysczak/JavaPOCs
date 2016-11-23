package jmat.javapocs.library;

import jmat.javapocs.shading.Utils;

public final class Library {

    public static String getSomethingElse() {
        System.out.printf("In %s:%n", Library.class.getCanonicalName());
        System.out.printf("  Location: %s%n", Library.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.printf("  Using: %s%n", Utils.class.getCanonicalName());
        return Utils.getSomethingElse();
    }
}
