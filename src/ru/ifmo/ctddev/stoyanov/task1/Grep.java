package ru.ifmo.ctddev.stoyanov.task1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Grep {

    ArrayList<String> patterns;

    Grep(String[] arg) throws IOException {
        patterns = new ArrayList<String>();
        if ("-".equals(arg[0]))
            MyReader.readPatternsFromConsole(this);
        else
            MyReader.readPatternsFromArguments(this, arg);
    }


    void execute() throws IOException {
        MyFileVisitor myFileVisitor = new MyFileVisitor(this);
        Path currentPath = Paths.get(".");
        Files.walkFileTree(currentPath, myFileVisitor);
    }

    public static void allActions(String[] arg) throws IOException {
        if (arg.length == 0)
            throw new IOException(SomeStrings.missingInput);
        Grep grep = new Grep(arg);
        if (grep.patterns.size() == 0)
            throw new IOException("No one pattern is found.");
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
