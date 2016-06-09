package jmat.javapocs.bitsetvshashsetsizeandspeed;

import java.lang.instrument.Instrumentation;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import org.github.jamm.MemoryMeter;

public class Main {

    private static Instrumentation instrumentation;

    public static void premain(final String args, final Instrumentation inst) {
        instrumentation = inst;
    }

    public static void main(final String[] args) {
        final MemoryMeter meter = new MemoryMeter();

        final int numberOfTests = 1000;
        final int totalNumberOfReferences = 5000;
        final int numberOfReferencesToSet = 1000;

        int ellapseBitSet = 0;
        int ellapseHashSetString = 0;
        int ellapseHashSetInteger = 0;
        long sizeOfBitSetI = 0;
        long sizeOfBitSetM = 0;
        long sizeOfHashSetStringI = 0;
        long sizeOfHashSetStringM = 0;
        long sizeOfHashSetIntegerI = 0;
        long sizeOfHashSetIntegerM = 0;
        for (int i = 0; i < numberOfTests; i++) {
            final int[] items = new int[numberOfReferencesToSet];
            for (int j = 0; j < numberOfReferencesToSet; j++) {
                items[j] = (int) Math.round(Math.random() * totalNumberOfReferences);
            }

            final BitSet bitSet = new BitSet();
            long start = System.currentTimeMillis();
            for (final int item : items) {
                bitSet.set(item);
            }
            ellapseBitSet += System.currentTimeMillis() - start;
            sizeOfBitSetI += instrumentation.getObjectSize(bitSet);
            sizeOfBitSetM += meter.measureDeep(bitSet);

            final HashSet<String> hashSetString = new HashSet<>();
            start = System.currentTimeMillis();
            for (final int item : items) {
                hashSetString.add(String.valueOf(item));
            }
            ellapseHashSetString += System.currentTimeMillis() - start;
            sizeOfHashSetStringI += instrumentation.getObjectSize(hashSetString);
            sizeOfHashSetStringM += meter.measureDeep(hashSetString);

            final HashSet<Integer> hashSetInteger = new HashSet<>();
            start = System.currentTimeMillis();
            for (final int item : items) {
                hashSetInteger.add(item);
            }
            ellapseHashSetInteger += System.currentTimeMillis() - start;
            sizeOfHashSetIntegerI += instrumentation.getObjectSize(hashSetInteger);
            sizeOfHashSetIntegerM += meter.measureDeep(hashSetInteger);
        }

        System.out.format("BitSet                : Ellapsed: %3d, Size I: %d, Size M: %8d%n", ellapseBitSet, sizeOfBitSetI, sizeOfBitSetM);
        System.out.format("HashSet (String)      : Ellapsed: %3d, Size I: %d, Size M: %8d%n", ellapseHashSetString, sizeOfHashSetStringI, sizeOfHashSetStringM);
        System.out.format("HashSet (Integer)     : Ellapsed: %3d, Size I: %d, Size M: %8d%n", ellapseHashSetInteger, sizeOfHashSetIntegerI, sizeOfHashSetIntegerM);

        System.out.format("BitSet size empty     : I: %3d, M: %3d%n", instrumentation.getObjectSize(new BitSet()), meter.measureDeep(new BitSet()));
        System.out.format("HashSet size empty    : I: %3d, M: %3d%n", instrumentation.getObjectSize(new HashSet()), meter.measureDeep(new HashSet()));

        System.out.format("HashMap size empty    : I: %3d, M: %3d%n", instrumentation.getObjectSize(new HashMap()), meter.measureDeep(new HashMap()));

        System.out.format("Array size empty      : I: %3d, M: %3d%n", instrumentation.getObjectSize(new Container()), meter.measureDeep(new Container()));
        System.out.format("Array items size empty: I: %3d, M: %3d%n", instrumentation.getObjectSize(new Container().items), meter.measureDeep(new Container().items));
    }

    static class Container {

        private final String[] items = new String[170];
    }
}
