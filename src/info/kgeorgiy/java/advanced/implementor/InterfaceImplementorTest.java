package info.kgeorgiy.java.advanced.implementor;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.annotation.Generated;
import javax.management.Descriptor;
import javax.sql.rowset.CachedRowSet;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.xml.bind.Element;
import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InterfaceImplementorTest {
//    private static final List<Class<?>> RETURN_TYPES = Arrays.asList(
//            byte.class, short.class, int.class, long.class,
//            char.class, boolean.class,
//            double.class, boolean.class,
//            void.class,
//            Object.class, String.class, List.class, Void.class,
//            String[].class, List[][].class, int[].class
//    );
//
//    private final Random random = new Random();

    private String methodName;
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        protected void starting(Description description) {
            methodName = description.getMethodName();
            System.out.println("== Running " + description.getMethodName());
        }
    };

    @Test
    public void test01_constructor() throws ClassNotFoundException, NoSuchMethodException {
        Class<?> token = loadClass();
        Assert.assertTrue(token.getName() + " should implement Impler interface", Impler.class.isAssignableFrom(token));
        Assert.assertTrue(token.getName() + " should implement JarImpler interface", JarImpler.class.isAssignableFrom(token));
        checkConstructor("public default constructor", token);
    }

    @Test
    public void test02_standardMethodlessInterfaces() {
        test(false, Element.class);
    }

    @Test
    public void test03_standardInterfaces() {
        test(false, Accessible.class, AccessibleAction.class, CachedRowSet.class, Descriptor.class, Generated.class);
    }

    public void test04_standardNonInterfaces() {
        test(true, void.class, String[].class, int[].class, String.class, boolean.class);
    }

    protected void test(boolean shouldFail, Class<?>... classes) {
        final File root = getRoot();
        try {
            implement(shouldFail, root, Arrays.asList(classes));
            if (!shouldFail) {
                compile(root, Arrays.asList(classes));
                check(root, Arrays.asList(classes));
            }
        } finally {
            clean(root);
        }
    }

    private File getRoot() {
        return new File(".", methodName);
    }

    private URLClassLoader getClassLoader(File root) {
        try {
            return new URLClassLoader(new URL[]{root.toURI().toURL()});
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    private void compile(File root, List<Class<?>> classes) {
        List<String> files = new ArrayList<>();
        for (Class<?> token : classes) {
            files.add(getFile(root, token).getPath());
        }
        compileFiles(root, files);
    }

    private void compileFiles(File root, List<String> files) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.addAll(files);
        args.add("-cp");
        args.add(root.getPath());
        int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        Assert.assertEquals("Compiler exit code", 0, exitCode);
    }

    private void clean(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    clean(child);
                }
            }
        }
        file.delete();
    }

    private void checkConstructor(String description, Class<?> token, Class<?>... params) {
        try {
            token.getConstructor(params);
        } catch (NoSuchMethodException e) {
            Assert.fail(token.getName() + " should have " + description);
        }
    }

//    protected void implement(boolean shouldFail, File root, List<String> classNames, File... classpath) throws MalformedURLException, ClassNotFoundException {
//        URL[] urls = new URL[classpath.length];
//        for (int i = 0; i < classpath.length; i++) {
//            urls[i] = classpath[i].toURI().toURL();
//        }
//        ClassLoader loader = new URLClassLoader(urls);
//        List<Class<?>> classes = new ArrayList<>();
//        for (String className : classNames) {
//            classes.add(loader.loadClass(className));
//        }
//        implement(shouldFail, root, classes);
//    }

    private void implement(boolean shouldFail, File root, List<Class<?>> classes) {
        JarImpler implementor;
        try {
            implementor = (JarImpler) loadClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Instantiation error");
            implementor = null;
        }
        for (Class<?> clazz : classes) {
            try {
                implementor.implement(clazz, root);
                File jarFile = new File(root, clazz.getName() + ".jar");
                implementor.implementJar(clazz, jarFile);
                checkJar(jarFile, clazz);
                Assert.assertTrue("You may not implement " + clazz, !shouldFail);
            } catch (ImplerException e) {
                if (shouldFail) {
                    return;
                }
                throw new AssertionError("Error implementing " + clazz, e);
            } catch (Throwable e) {
                throw new AssertionError("Error implementing " + clazz, e);
            }
            File file = getFile(root, clazz);
            Assert.assertTrue("Error implementing clazz: File '" + file + "' not found", file.exists());
        }
    }

    private File getFile(File root, Class<?> clazz) {
        String path = clazz.getCanonicalName().replace(".", "/") + "Impl.java";
        return new File(root, path).getAbsoluteFile();
    }

    private Class<?> loadClass() throws ClassNotFoundException {
        String className = System.getProperty("cut");
        Assert.assertTrue("Class name not specified", className != null);

        return Class.forName(className);
    }

    private void check(File root, List<Class<?>> classes) {
        URLClassLoader loader = getClassLoader(root);
        for (Class<?> token : classes) {
            check(loader, token);
        }
    }

    private void checkJar(File jarFile, Class<?> token) {
        check(getClassLoader(jarFile), token);
    }

    private void check(URLClassLoader loader, Class<?> token) {
        String name = token.getCanonicalName() + "Impl";
        try {
            Class<?> impl = loader.loadClass(name);

            if (token.isInterface()) {
                Assert.assertTrue(name + " should implement " + token, Arrays.asList(impl.getInterfaces()).contains(token));
            } else {
                Assert.assertEquals(name + " should extend " + token, token, impl.getSuperclass());
            }
            Assert.assertFalse(name + " should not be abstract", Modifier.isAbstract(impl.getModifiers()));
            Assert.assertFalse(name + " should not be interface", Modifier.isInterface(impl.getModifiers()));
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Error loading class " + name, e);
        }
    }

//    private List<Class<?>> loadClasses(List<String> classNames) {
//        List<Class<?>> classes = new ArrayList<>();
//        URLClassLoader loader = getClassLoader(getRoot());
//        for (String className : classNames) {
//            try {
//                classes.add(loader.loadClass(className));
//            } catch (ClassNotFoundException e) {
//                throw new AssertionError("Report bug in test", e);
//            }
//        }
//        return classes;
//    }
//
//
//    private class InterfaceBuilder {
//        private final String name;
//        private final String simpleName = randomIdentifier();
//
//        public InterfaceBuilder(int packages) {
//            String name = simpleName;
//            for (int i = 0; i < packages; i++) {
//                name = randomIdentifier() + "." + name;
//            }
//            this.name = name;
//        }
//
//        public File build() {
//            File file = new File(getRoot(), name.replace(".", "/") + ".java");
//            file.getParentFile().mkdirs();
//            try {
//                try (FileWriter writer = new FileWriter(file)) {
//                    writer.write("// This file was automatically generated.\n// Do not edit!\n");
//                    if (name.contains(".")) {
//                        writer.write("package " + name.substring(0, name.lastIndexOf('.')) + ";\n\n");
//                    }
//                    writer.write(String.format(
//                            "public interface %s {\n}\n",
//                            simpleName
//                    ));
//                }
//            } catch (IOException e) {
//                throw new AssertionError(e);
//            }
//            return file;
//        }
//
//        public String getClassName() {
//            return name;
//        }
////
////        public InterfaceBuilder addMethod(Class<?> returnType) {
////            return null;
////        }
//    }
//
//    private static final String FIRST_ID_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz$_";
//    private static final String ID_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$_";
//
//    protected String randomIdentifier() {
//        final StringBuilder sb = new StringBuilder();
//        sb.append(FIRST_ID_SYMBOLS.charAt(random.nextInt(FIRST_ID_SYMBOLS.length())));
//        for (int i = 0; i < 10; i++) {
//            sb.append(ID_SYMBOLS.charAt(random.nextInt(ID_SYMBOLS.length())));
//        }
//        return sb.toString();
//    }
}
