package jmat.javapocs.walkfiletree;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Main {

    public static void main(final String[] args) throws Exception {
        final Path tempDirectory = Files.createTempDirectory("WalkFileTree");
        final Path inputDirectory = Files.createDirectory(tempDirectory.resolve("input"));
        final Path currentDirectory = Files.createDirectory(inputDirectory.resolve("current"));
        final Path historicDirectory = Files.createDirectory(inputDirectory.resolve("historic"));
        final Path input0 = Files.createFile(inputDirectory.resolve("input_0.txt"));
        final Path current0 = Files.createFile(currentDirectory.resolve("current_0.txt"));
        final Path historic0 = Files.createFile(historicDirectory.resolve("historic_0.txt"));
        Files.write(input0, "input 0".getBytes());
        Files.write(current0, "current 0".getBytes());
        Files.write(historic0, "historic 0".getBytes());

        System.out.println("Files.newDirectoryStream:");
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(tempDirectory)) {
            for (final Path path : stream) {
                System.out.println("\t" + path);
            }
        }

        System.out.println("");

        System.out.println("Files.walk:");
        Files.walk(tempDirectory)
            .forEach(path -> System.out.println("\t" + path));

        System.out.println("");

        System.out.println("Files.walkFileTree:");
        Files.walkFileTree(inputDirectory, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(final Path path, final BasicFileAttributes attrs) throws IOException {
                System.out.format("\t%s PRE%n", path);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes) throws IOException {
                System.out.format("\t%s : %s : %s%n", path, inputDirectory.relativize(path), tempDirectory.resolve("trans").resolve(inputDirectory.relativize(path)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path path, final IOException ex) throws IOException {
                System.out.format("\t%s POST%n", path);
                return FileVisitResult.CONTINUE;
            }

        });

        System.out.println("Done");
    }
}
