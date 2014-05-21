package ru.ifmo.ctddev.stoyanov.task1;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class MyFileVisitor extends SimpleFileVisitor<Path> {

    Searcher searcher;

    MyFileVisitor(ArrayList<String> patterns) throws IOException {
        searcher = new Searcher(patterns);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        searcher.cleanTemps();
        searcher.search(file);
        return FileVisitResult.CONTINUE;
    }

}
