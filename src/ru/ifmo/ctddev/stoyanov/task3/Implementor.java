package ru.ifmo.ctddev.stoyanov.task3;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author Dmitry Stoyanov
 * @version $$Id$$
 */

public class Implementor implements JarImpler {

    /**
     * Abbreviation to line separator.
     */
    private static final String separator = System.getProperty("line.separator");

    /**
     * Abbreviation to tab.
     */
    private static final String tab = "\t";

    /**
     * {@link java.io.PrintWriter} which is used in {@link #write} function.
     */
    private PrintWriter printWriter;

    /**
     * Constructor, which makes empty {@link ru.ifmo.ctddev.stoyanov.task3.Implementor}.
     */
    public Implementor() {
    }

    /**
     * Writes <tt>s</tt> with printWriter.
     *
     * @param s String to be written.
     */
    private void write(String s) {
        printWriter.print(s);
    }

    /**
     * Return String associated with <tt>m</tt>.
     *
     * @param m {@link java.lang.reflect.Method} to be associated with {@link java.lang.String}.
     * @return {@link java.lang.String} associated with <tt>m</tt>.
     */
    private static String getMethodWithArguments(Method m) {
        String ans = m.getName();
        for (Class p : m.getParameterTypes()) {
            ans += "#" + p.getCanonicalName();
        }
        return ans;
    }

    /**
     * Returns true if <tt>method</tt>'s {@link java.lang.reflect.Modifier} contains <tt>flag</tt> and false otherwise.
     *
     * @param method {@link java.lang.reflect.Method} which modifier is interested to contain a flag.
     * @param flag   bit mask of flag which is interested.
     * @return true if flag contains in <tt>method</tt>'s {@link java.lang.reflect.Modifier} and false otherwise.
     */
    private static boolean containsMethodFlag(Method method, int flag) {
        return (((method.getModifiers() & Modifier.methodModifiers()) & flag) > 0);
    }

    /**
     * Returns true if <tt>constructor</tt>'s {@link java.lang.reflect.Modifier} contains <tt>flag</tt> and false otherwise.
     *
     * @param constructor {@link java.lang.reflect.Constructor} which modifier is interested to contain a flag.
     * @param flag        bit mask of flag which is interested.
     * @return true if flag contains in <tt>constructor</tt>'s {@link java.lang.reflect.Modifier} and false otherwise.
     */
    private static boolean containsConstructorFlag(Constructor constructor, int flag) {
        return (((constructor.getModifiers() & Modifier.constructorModifiers()) & flag) > 0);
    }

    /**
     * Collects all classes and interfaces which are super to <tt>t</tt> to the {@link java.util.List} <tt>list</tt>.
     *
     * @param t    class for which dfs must be run.
     * @param list {@link java.util.List} to collect all classes and interfaces in dfs tour.
     */
    private void dfsMethods(Class t, List<Method> list) {
        if (t == null) {
            return;
        }
        list.addAll(Arrays.asList(t.getDeclaredMethods()));
        dfsMethods(t.getSuperclass(), list);
        for (Class i : t.getInterfaces()) {
            dfsMethods(i, list);
        }
    }

    /**
     * Generates a realization of interface or expansion of class <tt>t</tt> to the <tt>root</tt> directory.
     * <p/>
     * <tt>t</tt> must be full name of class or interface. <tt>root</tt> can be null, in this case answer will be generated to the path from which Implementor was run.
     *
     * @param t    class to be expanded or interface to be realized.
     * @param root directory to put .java file with generated class.
     * @throws java.io.IOException                                     if can't create or modify file in <tt>root</tt> directory.
     * @throws java.lang.ClassNotFoundException                        if can't find class <tt>t</tt>.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException if <tt>t</tt> can't be expanded and can't be realized.
     */
    private void makeImpl(Class t, File root) throws IOException, ImplerException, ClassNotFoundException {

        if (t.equals(void.class)) {
            throw new ImplerException();
        }
        if (((t.getModifiers() & Modifier.classModifiers()) & Modifier.FINAL) > 0) {
            throw new ImplerException();
        }

        if (!t.isInterface()) {
            boolean onlyPrivateConstructors = true;
            for (Constructor constructor : t.getDeclaredConstructors()) {
                if (!containsConstructorFlag(constructor, Modifier.PRIVATE)) {
                    onlyPrivateConstructors = false;
                    break;
                }
            }
            if (onlyPrivateConstructors) {
                throw new ImplerException();
            }
        }
        String rootString;

        if (root == null) {
            rootString = ".";
        } else {
            rootString = root.toString();
        }

        rootString += "/" + t.getPackage().getName().replace('.', '/');
        File file = new File(rootString);
        for (int i = 0; i < 5; i++) {
            if (file.mkdirs()) {
                break;
            }
        }
        if (!file.exists()) {
            System.out.println("Error in mkdirs. " + file);
            throw new FileNotFoundException();
        }

        printWriter = new PrintWriter(rootString + "/" + t.getSimpleName() + "Impl.java");

        String packageString = t.getPackage().getName();
        while (packageString.length() > 0 && packageString.charAt(0) == '.') {
            packageString = packageString.substring(1);
        }
        write("package " + packageString + ";" + separator + separator);


        if (t.isInterface()) {
            write("public class " + t.getSimpleName() + "Impl implements " + t.getName());
        } else {
            write("public class " + t.getSimpleName() + "Impl extends " + t.getName());
        }
        write(" {" + separator + separator);

        for (Constructor constructor : t.getDeclaredConstructors()) {

            if (containsConstructorFlag(constructor, Modifier.PRIVATE)) {
                continue;
            }
            for (Annotation annotation : constructor.getAnnotations()) {
                write(tab + annotation.toString() + separator);
            }
            write(tab);
            String modifiers = Modifier.toString((constructor.getModifiers() & Modifier.constructorModifiers()));
            if (!"".equals(modifiers)) {
                write(modifiers + " ");
            }

            write(t.getSimpleName() + "Impl(");

            int i = 0;
            for (Class p : constructor.getParameterTypes()) {
                i++;
                if (i > 1) {
                    write(", ");
                }
                if (p.isArray()) {
                    write(p.getComponentType().getName() + "[] a" + i);
                } else {
                    write(p.getName() + " a" + i);
                }
            }
            int argumentsCount = i;

            write(") ");
            i = 0;
            for (Class p : constructor.getExceptionTypes()) {
                i++;
                if (i > 1) {
                    write(", ");
                } else {
                    write("throws ");
                }
                write(p.getName());
            }

            if (containsConstructorFlag(constructor, Modifier.NATIVE)) {
                write(";" + separator + separator);
                continue;
            }

            write(" {" + separator);
            write(tab + tab + "super(");
            for (int j = 1; j <= argumentsCount; j++) {
                write("a" + j + ((j < argumentsCount) ? ", " : ""));
            }
            write(");" + separator + tab + "}" + separator);
        }

        write(separator + separator);

        HashMap<String, ArrayList<Method>> methods = new HashMap<>();
        HashSet<String> methodsNames = new HashSet<>();

        List<Method> methodList = new ArrayList<>();
        dfsMethods(t, methodList);

        for (Method method : methodList) {
            if (!methods.containsKey(getMethodWithArguments(method))) {
                methods.put(getMethodWithArguments(method), new ArrayList<Method>());
            }
            methods.get(getMethodWithArguments(method)).add(method);
            methodsNames.add(getMethodWithArguments(method));
        }

        for (String methodName : methodsNames) {
            ArrayList<Method> sameMethods = methods.get(methodName);

            boolean hasNotAbstract = false;
            for (Method method : sameMethods) {
                if (!containsMethodFlag(method, Modifier.ABSTRACT)) {
                    hasNotAbstract = true;
                    break;
                }
            }
            if (!hasNotAbstract) {
                writeMethod(sameMethods.get(0));
            }
        }

        write("}");
        printWriter.close();
    }

    /**
     * Writes {@link java.lang.reflect.Method} <tt>method</tt>.
     *
     * @param method {@link java.lang.reflect.Method} to be written.
     */
    private void writeMethod(Method method) {
        for (Annotation annotation : method.getAnnotations()) {
            write(tab + annotation.toString() + separator);
        }
        write(tab);
        String modifiers = Modifier.toString(((method.getModifiers() & Modifier.methodModifiers()) & (~Modifier.ABSTRACT)));
        if (!"".equals(modifiers)) {
            write(modifiers + " ");
        }

        if (method.getReturnType().isArray()) {
            write(method.getReturnType().getComponentType().getName() + "[] ");
        } else {
            write(method.getReturnType().getName() + " ");
        }
        write(method.getName() + "(");

        int i = 0;
        for (Class p : method.getParameterTypes()) {
            i++;
            if (i > 1) {
                write(", ");
            }
            if (p.isArray()) {
                write(p.getComponentType().getName() + "[] a" + i);
            } else {
                write(p.getName() + " a" + i);
            }
        }
        write(") ");
        i = 0;
        for (Class p : method.getExceptionTypes()) {
            i++;
            if (i > 1) {
                write(", ");
            } else {
                write("throws ");
            }
            write(p.getName());
        }
        if (containsMethodFlag(method, Modifier.NATIVE)) {
            write(";" + separator + separator);
            return;
        }

        write(" {" + separator);

        if (!method.getReturnType().equals(void.class)) {
            write(tab + tab + "return ");
            if (method.getReturnType().equals(boolean.class)) {
                write("false");
            } else if (method.getReturnType().isPrimitive()) {
                write("0");
            } else {
                write("null");
            }

            write(";");
        }
        write(separator + tab + "}" + separator + separator);
    }

    /**
     * Produces code implementing class or interface specified by provided <tt>token</tt>.
     * <p/>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to <tt>$root/java/util/ListImpl.java</tt>
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     *                                                                 generated.
     */
    public void implement(Class<?> token, File root) throws ImplerException {

        try {
            new Implementor().makeImpl(token, root);
        } catch (FileNotFoundException e) {
            System.out.println("Not file found.");
        } catch (IOException e) {
            System.out.println("IOException");
        } catch (ClassNotFoundException e) {
            System.out.println("Not found some class in tree.");
        }
    }

    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p/>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be generated.
     */
    public void implementJar(Class<?> token, File jarFile) throws ImplerException {

        implement(token, null);
        Class t = token;
        File jarFileOuput;
        if (jarFile != null) {
            jarFileOuput = jarFile;
        } else {
            jarFileOuput = new File(t.getSimpleName());
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();

        String className = t.getPackage().getName().replace('.', '/') + "/" + t.getSimpleName() + "Impl.class";
        String javaName = t.getPackage().getName().replace('.', '/') + "/" + t.getSimpleName() + "Impl.java";
        File compiledFile = new File(className);
        args.add(javaName);
        int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        if (exitCode != 0) {
            System.out.println("Error in compiling.");
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream jarOutputStream = null;
        try {
            jarOutputStream = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jarFileOuput)), manifest);

            JarEntry entry = new JarEntry(className);
            entry.setTime(jarFileOuput.lastModified());
            jarOutputStream.putNextEntry(entry);
            BufferedInputStream bufferedInputStream = null;
            try {
                bufferedInputStream = new BufferedInputStream(new FileInputStream(compiledFile));
                byte[] buffer = new byte[65536];
                while (true) {
                    int count = bufferedInputStream.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    jarOutputStream.write(buffer, 0, count);
                }
            } catch (IOException e) {
                System.out.println("Error in inner jar writing.");
                e.printStackTrace();
            } finally {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            }
            jarOutputStream.closeEntry();
        } catch (IOException e) {
            System.out.println("Error in outer jar writing.");
            e.printStackTrace();
        } finally {
            if (jarOutputStream != null) {
                try {
                    jarOutputStream.close();
                } catch (IOException e) {
                    System.out.println("Error in closing JarOutputStream.");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Generates class which expanded class or realizes interface which name is stored in zero element or <tt>arg</tt>.
     * <p/>
     * In zero element of <tt>arg</tt> must be stored full name of class or interface.
     * Generated class will be situated in the same directory as {@link ru.ifmo.ctddev.stoyanov.task3.Implementor} class.
     *
     * @param arg String array, it's zero element must contain name of class to be expanded or name of interface to be realized.
     */
    public static void main(String[] arg) {
        if (arg.length == 0) {
            System.out.println("Write full name of class or interface.");
            return;
        }
        String toClass;
        if ("-jar".equals(arg[0])) {
            if (arg.length < 2) {
                System.out.println("Write full name of class or interface after -jar.");
                return;
            }
            toClass = arg[1];
        } else {
            toClass = arg[0];
        }
        Class t;
        try {
            t = Class.forName(toClass);
        } catch (ClassNotFoundException e) {
            System.out.println("Class or interface not found.");
            return;
        }
        try {
            if ("-jar".equals(arg[0])) {
                new Implementor().implementJar(t, (arg.length > 2 ? new File(arg[2]) : null));
            } else {
                new Implementor().makeImpl(t, null);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File to output not found.");
        } catch (IOException e) {
            System.out.println("IOException");
        } catch (ImplerException e) {
            System.out.println("Impler Exception.");
        } catch (ClassNotFoundException e) {
            System.out.println("Not found some class in tree.");
        }
    }
}
