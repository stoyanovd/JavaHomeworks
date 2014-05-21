package ru.ifmo.ctddev.stoyanov.task1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MyReaderConsole implements MyReader {

    public ArrayList<String> patterns = null;

    MyReaderConsole() {
        patterns = new ArrayList<>();
    }

    public ArrayList<String> getPatterns() {
        return patterns;
    }

    public void read() throws IOException {

        String s;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

        while ((s = in.readLine()) != null) {
            if (!"".equals(s)) {
                for (int j = 0; j < Encodings.encodings.size(); j++) {
                    try {
                        patterns.add(new String(s.getBytes("UTF-8"), Encodings.encodings.get(j)));
                    } catch (UnsupportedEncodingException e) {
                        throw new IOException("Error - unknown encoding.");
                    }
                }
            }
        }
        in.close();
    }
}
