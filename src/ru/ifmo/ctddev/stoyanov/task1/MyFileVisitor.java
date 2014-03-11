package ru.ifmo.ctddev.stoyanov.task1;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class MyFileVisitor extends SimpleFileVisitor<Path> {

    Searcher searcher;

    MyFileVisitor(Grep grep) throws IOException {
        searcher = new Searcher(grep);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        //System.out.println("Visit file: " + file.toString());
        searcher.cleanTemps();
        searcher.search(file);
        return FileVisitResult.CONTINUE;
    }

}
