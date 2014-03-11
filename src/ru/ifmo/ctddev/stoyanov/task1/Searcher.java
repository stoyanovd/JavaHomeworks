package ru.ifmo.ctddev.stoyanov.task1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;

public class Searcher {

    private static final int COUNTOFSHOW = 30;

    ArrayList<ArrayList<Byte>> patterns;
    private int maxTempSize;                                                           //7391

    Path currentPath;
    Grep currentGrep;

    byte[] temp;

    Searcher(Grep grep) throws IOException {
        patterns = new ArrayList<ArrayList<Byte>>();
        currentGrep = grep;
        for (String s : grep.patterns) {
            byte[] sArray = s.getBytes();
            patterns.add(new ArrayList<Byte>());
            for (byte x : sArray)
                patterns.get(patterns.size() - 1).add(x);
        }
        int maxPatternSize = 0;
        for (ArrayList<Byte> pattern : patterns)
            if (pattern.size() > maxPatternSize)
                maxPatternSize = pattern.size();

        if (maxPatternSize == 0)
            throw new IOException("Searcher: all patterns are null-size.");
        maxTempSize = Encodings.maxCharacterSizeInBytes * maxPatternSize * 4;

        temp = new byte[maxTempSize];
    }

    public void cleanTemps() throws IOException {
        temp = new byte[maxTempSize];
        if (file != null)
            file.close();
        file = null;
    }


    int bytesInTemp;
    long currentlyRead;
    long lastLineStart;
    long tempStartPosition;
    RandomAccessFile file;
    boolean findPattern;

    String lastFoundString;

    ArrayList<Byte> lastStart;


    private void checkLastStart() throws IOException {
        /*int k = consistsSeparator();
        if(k == -1)
            k = bytesInTemp;
        if(lastStart.size() - (int)(tempStartPosition - lastLineStart) < 0)
            throw  new IOException("Error in saving line start.");
        for(int i = lastStart.size() - (int)(tempStartPosition - lastLineStart); (i < k) && (lastStart.size() < COUNTOFSHOW); i++)
            lastStart.add(temp[i]);                           //form what point?    */
    }

    private long readBytes() throws IOException {

        int k = file.read(temp, bytesInTemp, temp.length - bytesInTemp);
        if (k == -1)
            return k;
        currentlyRead += k;
        bytesInTemp += k;
        checkLastStart();
        return bytesInTemp;
    }

    private int contains(byte[] a, int rightBound, ArrayList<Byte> p) throws IOException {
        if (rightBound > a.length)
            throw new IOException("Error in trying to search a string.");
        int[] pr = new int[p.size()];
        pr[0] = 0;
        int k = 0;
        for (int i = 1; i < p.size(); i++) {
            while (k > 0 && !p.get(k).equals(p.get(i)))
                k = pr[k - 1];
            if (p.get(k).equals(p.get(i)))
                k++;
            pr[i] = k;
        }

        k = 0;
        for (int i = 0; i < rightBound; i++) {
            if (k == p.size())
                k = pr[k - 1];
            while (k > 0 && p.get(k) != a[i])         //aaabsaas
                k = pr[k - 1];
            if (k < p.size() && p.get(k) == a[i])
                k++;
            if (k == p.size())
                return (i - p.size() + 1);
        }
        return -1;
    }

    private int consistsSeparator() throws IOException {
        return contains(temp, bytesInTemp, SomeStrings.separators);
    }

    private void moveTemp(int k) throws IOException {
        for (int i = 0; i < temp.length; i++) {
            if (i + k < temp.length)
                temp[i] = temp[i + k];
            else
                temp[i] = 0;
        }
        bytesInTemp -= k;
        if (bytesInTemp < 0)
            bytesInTemp = 0;
        tempStartPosition += k;
        checkLastStart();
    }

    private void endCurrentLine() throws IOException {
        int k = consistsSeparator();
        if (k == -1)
            throw new IOException("Error in trying to close unfinished line");
        if (findPattern)
            writeLine(lastLineStart, tempStartPosition + k);

        lastStart = new ArrayList<Byte>();
        findPattern = false;
        moveTemp(k + SomeStrings.separators.size());
        lastLineStart = tempStartPosition;
    }

    private void tryToEndLine() throws IOException {
        if (consistsSeparator() != -1)
            endCurrentLine();
        else if (bytesInTemp > temp.length / 2)
            moveTemp(bytesInTemp - temp.length / 2);
    }

    private boolean findEntry() throws IOException {

        int k = consistsSeparator();
        if (k == -1)
            k = bytesInTemp;
        for (int i = 0; i < patterns.size(); i++)
            if (contains(temp, k, patterns.get(i)) != -1) {
                lastFoundString = currentGrep.patterns.get(i);
                findPattern = true;
                return true;
            }
        return false;
    }

    private void tryToFindAndEnd() throws IOException {
        if (findEntry())
            findPattern = true;
        tryToEndLine();
    }

    public void search(Path filePath) throws IOException {
        currentPath = filePath;
        try {
            file = new RandomAccessFile(filePath.toString(), "r");
        } catch (FileNotFoundException e) {
            throw new IOException("Error in file opening.");
        }

        bytesInTemp = 0;
        currentlyRead = 0;
        lastLineStart = 0;
        tempStartPosition = 0;
        findPattern = false;

        while (readBytes() != -1) {
            if (findPattern) {
                tryToEndLine();
                continue;
            }
            if (bytesInTemp < temp.length)
                continue;
            tryToFindAndEnd();
        }

        while (consistsSeparator() != -1) {
            tryToFindAndEnd();
        }
        if (findEntry())
            writeLine(lastLineStart, tempStartPosition + bytesInTemp);

        try {
            file.close();
        } catch (IOException e) {
            throw new IOException("Error in closing file.");
        }
    }

    private void readEvenly(byte[] arr, int x) throws IOException {
        int k = 0;
        while (k < x) {
            int p = file.read(arr, k, x - k);
            if (p == -1)
                throw new IOException("Error in writing answer.");
            k += p;
        }
    }

    private void writeLine(long l, long r) throws IOException {                //  right bound is exclude.
        if (tryingWriteWithSeek(l, r))
            return;

        writeAnswer("Too large string.  Pattern that was found: " + lastFoundString);
    }

    private boolean tryingWriteWithSeek(long l, long r) throws IOException {
        long currentFilePosition = file.getFilePointer();
        file.seek(l);
        if (file.getFilePointer() != l)
            return false;
        if (r - l < COUNTOFSHOW * 3) {
            byte[] toOut = new byte[(int) (r - l)];
            file.read(toOut);
            writeAnswer(new String(toOut));
        } else {
            byte[] first = new byte[COUNTOFSHOW];
            byte[] second = new byte[COUNTOFSHOW];
            readEvenly(first, COUNTOFSHOW);

            file.seek((r - (long) COUNTOFSHOW));
            if (file.getFilePointer() != (r - (long) COUNTOFSHOW))
                return false;

            readEvenly(second, COUNTOFSHOW);

            writeAnswer((new String(first) + " ... " + (new String(second))));
        }
        file.seek(currentFilePosition);
        if (file.getFilePointer() != currentFilePosition)
            throw new IOException("Error in seek. File: " + currentPath);

        return true;

    }

    private void writeAnswer(String s) {
        System.out.println(currentPath.toString() + ": " + s);
    }

}