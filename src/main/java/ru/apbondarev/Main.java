package ru.apbondarev;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(
                    "Compile and redefine java class via debugger protocol.\n" +
                    "https://docs.oracle.com/javase/8/docs/platform/jpda/jdwp/jdwp-protocol.html#JDWP_VirtualMachine_RedefineClasses\n" +
                    "Usage:\n" +
                    "\tjava -jar redefine.jar hostname:port javafile [compiler arguments]\n" +
                    "Example:\n" +
                    "\tjava -jar redefine.jar 127.0.0.1:50010 SampleApp.java -source 12 -target 12 -classpath ..."
            );
            System.exit(1);
            return;
        }

        String address = args[0];
        File file = Paths.get(args[1]).toFile();
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

            Map<String, byte[]> classToBytes = new LinkedHashMap<>();
            for (Map.Entry<String, URI> it : compiledClasses.entrySet()) {
                String className = it.getKey();
                URI uri = it.getValue();
                classToBytes.put(className, Files.readAllBytes(Paths.get(uri)));
                System.out.println("read file " + Paths.get(uri));
            }

            if (!classToBytes.isEmpty()) {
                driver.redefine(classToBytes);
                for (String className : classToBytes.keySet()) {
                    System.out.println("hot swapped: " + className);
                }
            }
        } catch (IOException e) {
            System.out.println();
            e.printStackTrace();
            System.exit(1);
        }
    }
}
