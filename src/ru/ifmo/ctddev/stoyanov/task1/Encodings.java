package ru.ifmo.ctddev.stoyanov.task1;

import java.util.ArrayList;

public class Encodings {

    public static final ArrayList<String> encodings;

    static {
        encodings = new ArrayList<String>();
        encodings.add("UTF-8");
        encodings.add("KOI8-R");
        encodings.add("CP1251");
        encodings.add("CP866");

        encodings.add("GB2312");
        encodings.add("Big5");
    }

    public static final int maxCharacterSizeInBytes = 4;             //TODO   make it correct
    //maybe it will be more good to set it big.
}
