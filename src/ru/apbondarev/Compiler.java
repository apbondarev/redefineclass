package ru.apbondarev;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            JavaFileManager fileManager = new ForwardingJavaFileManager(stdFileManager) {
                @Override
                public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
                    System.out.println("output: " + className);
                    return super.getJavaFileForOutput(location, className, kind, sibling);
                }
            };
            Iterable<? extends JavaFileObject> sources = stdFileManager.getJavaFileObjectsFromFiles(Collections.singletonList(file));
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, sources);
            task.call();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        if (!diagnostics.getDiagnostics().isEmpty()) {
            for (Diagnostic<? extends JavaFileObject> it : diagnostics.getDiagnostics()) {
                System.err.println(it.getSource() + ":" + it.getLineNumber() + ' ' + it.getMessage(Locale.getDefault()));
            }
            System.exit(1);
        }
    }
}
