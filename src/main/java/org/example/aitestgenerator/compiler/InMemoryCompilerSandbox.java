package org.example.aitestgenerator.compiler;
//not sure if this is needed
import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import javax.tools.JavaFileObject.Kind;
import java.util.Map;

public class InMemoryCompilerSandbox {

    public static Class<?> compileAndLoad(String className, String sourceCode) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("JavaCompiler not found! Ensure you are running a JDK.");
        }

        // 1. Trick the compiler into reading from our String instead of a physical .java file
        JavaFileObject sourceFile = new SimpleJavaFileObject(
                URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension),
                Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return sourceCode;
            }
        };

        // 2. Trick the compiler into writing to RAM (a HashMap) instead of a physical .class file
        Map<String, byte[]> compiledBytecode = new HashMap<>();
        JavaFileManager fileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(
                compiler.getStandardFileManager(null, null, null)) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String name, Kind kind, FileObject sibling) {
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

        // 3. Run the compilation task
        boolean success = compiler.getTask(null, fileManager, null, null, null, List.of(sourceFile)).call();
        if (!success) {
            throw new RuntimeException("Compilation failed! Syntax error in the string.");
        }

        // 4. Load the compiled bytecode from our RAM directly into the JVM
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