package com.matchmaking.server.matchmaking;

import java.io.*;

public class StreamPipe extends Thread {
    private final InputStream inputStream;
    private final PrintStream outputStream;

    private StreamPipe(InputStream inputStream, PrintStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null)
                outputStream.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pipe(InputStream inputStream, PrintStream outputStream) {
        StreamPipe streamPipe = new StreamPipe(inputStream, outputStream);
        streamPipe.start();
    }
}
