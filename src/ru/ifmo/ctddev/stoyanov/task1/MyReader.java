package ru.ifmo.ctddev.stoyanov.task1;

import java.io.IOException;
import java.util.ArrayList;

public interface MyReader {

    public void read() throws IOException;

    public ArrayList<String> getPatterns();

}
