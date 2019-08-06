package ru.apbondarev;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class Compiler {

    public static void main(String[] args) {
        File file = Path.of(args[0]).toFile();

        List<String> options;
        if (args.length > 1) {
            options = Arrays.stream(args, 1, args.length).collect(Collectors.toList());
        } else {
            options = null;
        }

        Map<String, URI> output = new Compiler().compile(file, options);
        for (Map.Entry<String, URI> it : output.entrySet()) {
            String className = it.getKey();
            URI uri = it.getValue();
            System.out.println("className: " + className + ", uri: " + uri);
        }
    }

    public Map<String, URI> compile(File file, List<String> options) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        System.out.println("compiler source versions: " + compiler.getSourceVersions());
        System.out.println("VM options: " + options);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Map<String, URI> output = new LinkedHashMap<>();
        try (StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            JavaFileManager fileManager = new ForwardingJavaFileManager<>(stdFileManager) {
                @Override
                public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
                    JavaFileObject javaFileObject = super.getJavaFileForOutput(location, className, kind, sibling);
                    output.put(className, javaFileObject.toUri());
                    return javaFileObject;
                }
            };
            Iterable<? extends JavaFileObject> sources = stdFileManager.getJavaFileObjectsFromFiles(Collections.singletonList(file));
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, sources);
            task.call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (diagnostics.getDiagnostics().isEmpty()) {
            return output;
        } else {
            StringBuilder message = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> it : diagnostics.getDiagnostics()) {
                if (message.length() != 0) {
                    message.append('\n');
                }
                message.append(it.getSource()).append(':').append(it.getLineNumber()).append(' ').append(it.getMessage(Locale.getDefault()));
            }
            throw new IllegalArgumentException(message.toString());
        }
    }
}
