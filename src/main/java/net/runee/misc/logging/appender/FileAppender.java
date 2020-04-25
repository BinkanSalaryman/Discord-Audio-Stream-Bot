package net.runee.misc.logging.appender;

import net.runee.misc.logging.LogFrame;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

public class FileAppender implements Appender {
    private File file;

    public FileAppender(File file) throws IOException {
        this.file = file;
        if(!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            file.createNewFile();
        }
    }

    @Override
    public void append(LogFrame frame) throws IOException {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            String d = frame.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            String t = frame.thread.getName();
            String l = frame.severity.name().toUpperCase();
            String s = frame.clazz.getSimpleName();
            String m = frame.message;
            String line = d + " [" + t + "] " + l + " " + s + " - " + m + "\n";
            out.write(line);
            if(frame.exception != null) {
                frame.exception.printStackTrace(out);
            }
        }
    }
}
