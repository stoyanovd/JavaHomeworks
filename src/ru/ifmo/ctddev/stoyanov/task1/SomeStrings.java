package ru.ifmo.ctddev.stoyanov.task1;

import java.util.ArrayList;

public class SomeStrings {


    public static final String lineSeparator = System.getProperty("line.separator");

    public static final String missingInput = "Empty input." + lineSeparator +
            "You can type '-' and put your patterns in console." + lineSeparator +
            "Or you can put your strings in launch arguments.";


    public static final boolean DEBUG = true;

    public static final ArrayList<Byte> separators = new ArrayList<Byte>();

    static {
        byte[] temp = lineSeparator.getBytes();
        for (byte x : temp)
            separators.add(x);
    }


}

