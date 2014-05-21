package ru.ifmo.ctddev.stoyanov.task1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Grep {

    ArrayList<String> patterns;

    Grep(String[] arg) throws IOException {
        MyReader r;
        if ("-".equals(arg[0])) {
            ///r = ???
            r = new MyReaderConsole();
        } else {
            r = new MyReaderCommandLine(arg);
        }
        r.read();
        patterns = new ArrayList<>(r.getPatterns());
    }


    public void execute() throws IOException {
        MyFileVisitor myFileVisitor = new MyFileVisitor(patterns);
        Path currentPath = Paths.get(".");
        Files.walkFileTree(currentPath, myFileVisitor);
    }

    public static void allActions(String[] arg) throws IOException {
        if (arg.length == 0) {
            throw new IOException(SomeStrings.missingInput);
        }
        Grep grep = new Grep(arg);
        if (grep.patterns.size() == 0) {
            throw new IOException("No one pattern is found.");
        }
        grep.execute();
    }

    public static void main(String[] arg) {
        try {
            Grep.allActions(arg);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


}
