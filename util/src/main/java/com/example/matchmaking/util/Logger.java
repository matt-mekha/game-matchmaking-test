package com.example.matchmaking.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Logger {

    private final static ArrayList<String> lines = new ArrayList<>();
    private static String name = "";

    public static void logf(String format, Object... args) {
        log(String.format(format, args));
    }

    public static void log(String str) {
        System.out.println(str);
        synchronized (lines) {
            lines.add(str);
        }
    }

    public static void setName(String name) {
        Logger.name = name;
    }

    public static void save() {
        try {
            FileWriter fileWriter = new FileWriter(String.format("%s_%d.txt", name, System.currentTimeMillis()));
            for(String line : lines) {
                fileWriter.write(line + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void fatalException(Exception e) {
        log(e.getMessage());
        for(StackTraceElement stackTraceElement : e.getStackTrace()) {
            log("\t\t" + stackTraceElement.toString());
        }
        save();
        System.exit(-1);
    }
}