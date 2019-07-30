package ru.apbondarev;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        String address = args[0];
        File file = Path.of(args[1]).toFile();
        List<String> options;
        if (args.length > 1) {
            options = Arrays.stream(args, 2, args.length).collect(Collectors.toList());
        } else {
            options = null;
        }

        Map<String, URI> compiledClasses = new Compiler().compile(file, options);
        for (Map.Entry<String, URI> it : compiledClasses.entrySet()) {
            String className = it.getKey();
            URI uri = it.getValue();
            System.out.println("compiled className: " + className + ", uri: " + uri);
        }

        System.out.print("connecting to " + address + " ...");
        try (RedefineClass driver = new RedefineClass()) {
            driver.attach(address, RedefineClass.DEFAULT_TIMEOUT, RedefineClass.DEFAULT_TIMEOUT);
            System.out.println(" connected");

            for (Map.Entry<String, URI> it : compiledClasses.entrySet()) {
                String className = it.getKey();
                URI uri = it.getValue();
                driver.redefine(className, Files.readAllBytes(Path.of(uri)));
                System.out.println("hot swapped: " + className);
            }
        } catch (IOException e) {
            System.out.println();
            e.printStackTrace();
            System.exit(1);
        }
    }
}
