package info.kgeorgiy.java.advanced.base;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 * @version $$Id$$
 */
public class BaseTester {
    private Map<String, Class<?>> tests = new HashMap<>();

    public void run(String[] args) {
        if (args.length != 2 && args.length != 3) {
            printUsage();
        }

        Class<?> token = tests.get(args[0]);
        if (token == null) {
            printUsage();
            return;
        }

        System.setProperty("cut", args[1]);
        Result result = new JUnitCore().run(token);
        if (!result.wasSuccessful()) {
            for (Failure failure : result.getFailures()) {
                System.err.println("Test " + failure.getDescription().getMethodName() + " failed: " + failure.getMessage());
                if (failure.getException() != null) {
                    failure.getException().printStackTrace();
                }
            }
        } else {
            System.out.println("OK " + token.getSimpleName());
        }
        certify(token, args.length > 2 ? args[2] : "");
    }

    protected void certify(Class<?> token, String salt) {
    }

    private void printUsage() {
        System.out.println("Usage:");
        for (String name : tests.keySet()) {
            System.out.println(String.format("    java %s %s full.class.name", getClass().getName(), name));
        }
        System.exit(1);
    }

    public BaseTester add(String name, Class<?> testClass) {
        tests.put(name, testClass);
        return this;
    }
}
