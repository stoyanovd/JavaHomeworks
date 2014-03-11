package ru.ifmo.ctddev.stoyanov.task1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class MyReader {


    static void readPatternsFromConsole(Grep grep) throws IOException {

        String s;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

        while((s = in.readLine()) != null)
        {
            if(!"".equals(s))
                for (int j = 0; j < Encodings.encodings.size(); j++)
                    try {
                        grep.patterns.add(new String(s.getBytes("UTF-8"), Encodings.encodings.get(j)));
                    } catch (UnsupportedEncodingException e) {
                        throw new IOException("Error - unknown encoding.");
                    }
        }
        in.close();
    }

    static void readPatternsFromArguments(Grep grep, String[] arg) throws IOException {
        for (String anArg : arg)
            for (int j = 0; j < Encodings.encodings.size(); j++)
                try {
                    grep.patterns.add(new String(anArg.getBytes("UTF-8"), Encodings.encodings.get(j)));
                } catch (UnsupportedEncodingException e) {
                    throw new IOException("Error in encoding changing.");
                }

    }
}
