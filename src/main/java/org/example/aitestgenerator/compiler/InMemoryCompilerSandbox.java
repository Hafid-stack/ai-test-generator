package org.example.aitestgenerator.compiler;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryCompilerSandbox {

    public static Class<?> compileAndLoad(String className, String sourceCode) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("JavaCompiler not found! Ensure you are running a JDK.");
        }

        // 1. Create a diagnostic collector to catch the EXACT compiler errors
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        JavaFileObject sourceFile = new SimpleJavaFileObject(
                URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return sourceCode;
            }
        };

        Map<String, byte[]> compiledBytecode = new HashMap<>();
        JavaFileManager fileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(
                compiler.getStandardFileManager(diagnostics, null, null)) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) {
                return new SimpleJavaFileObject(URI.create("bytes:///" + name.replace('.', '/') + kind.extension), kind) {
                    @Override
                    public OutputStream openOutputStream() {
                        return new ByteArrayOutputStream() {
                            @Override
                            public void close() {
                                compiledBytecode.put(name, this.toByteArray());
                            }
                        };
                    }
                };
            }
        };

        // 2. Grab your current project's classpath so the compiler can find JUnit!
        String classPath = System.getProperty("java.class.path");
        List<String> options = List.of("-classpath", classPath);

        // 3. Run the task, passing in our diagnostics and options
        boolean success = compiler.getTask(null, fileManager, diagnostics, options, null, List.of(sourceFile)).call();

        // 4. If it fails, print the ACTUAL reasons why
        if (!success) {
            StringBuilder errorMsg = new StringBuilder("Compilation failed!\n");
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                errorMsg.append("Line ").append(diagnostic.getLineNumber())
                        .append(": ").append(diagnostic.getMessage(null)).append("\n");
            }
            throw new RuntimeException(errorMsg.toString());
        }

        ClassLoader memoryClassLoader = new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                byte[] bytes = compiledBytecode.get(name);
                if (bytes == null) throw new ClassNotFoundException(name);
                return defineClass(name, bytes, 0, bytes.length);
            }
        };

        return memoryClassLoader.loadClass(className);
    }
}