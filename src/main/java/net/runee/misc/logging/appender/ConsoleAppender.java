package net.runee.misc.logging.appender;

import net.runee.misc.logging.LogFrame;

import java.time.format.DateTimeFormatter;

public class ConsoleAppender implements Appender {
    @Override
    public void append(LogFrame frame) {
        String d = frame.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String t = frame.thread.getName();
        String l = frame.severity.name().toUpperCase();
        String s = frame.clazz.getSimpleName();
        String m = frame.message;
        System.out.println(d + " [" + t + "] " + l + " " + s + " - " + m);
        if(frame.exception != null) {
            frame.exception.printStackTrace();
        }
    }
}
