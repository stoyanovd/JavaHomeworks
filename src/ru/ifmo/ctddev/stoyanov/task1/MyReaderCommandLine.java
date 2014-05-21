package ru.ifmo.ctddev.stoyanov.task1;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MyReaderCommandLine implements MyReader {

    String[] arg;
    public ArrayList<String> patterns;

    MyReaderCommandLine(String[] arg) {
        this.arg = arg;
        patterns = new ArrayList<>();
    }

    public void read() throws IOException {
        for (String anArg : arg) {
            for (int j = 0; j < Encodings.encodings.size(); j++) {
                try {
                    patterns.add(new String(anArg.getBytes("UTF-8"), Encodings.encodings.get(j)));
                } catch (UnsupportedEncodingException e) {
                    throw new IOException("Error in encoding changing.");
                }
            }
        }
    }

    public ArrayList<String> getPatterns() {
        return patterns;
    }
}
