package com.matchmaking.client;

import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private static Logger instance;

    private FileWriter fileWriter;

    public Logger() {
        try {
            fileWriter = new FileWriter(String.format("ClientLog_%d.txt", System.currentTimeMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getInstance() {
        if(instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void log(String s) {
        System.out.println(s);
        try {
            fileWriter.write(s);
            fileWriter.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
