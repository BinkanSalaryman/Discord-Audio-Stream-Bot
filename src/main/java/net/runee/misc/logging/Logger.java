package net.runee.misc.logging;

import net.runee.misc.logging.appender.Appender;
import net.runee.misc.logging.appender.ConsoleAppender;
import net.runee.misc.logging.appender.FileAppender;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    public static final File logPath = new File("app.log");

    private static final Logger logger = new Logger(Logger.class);
    private static List<Appender> appenders = new ArrayList<>();

    static {
        appenders.add(new ConsoleAppender());
        try {
            appenders.add(new FileAppender(logPath));
        } catch (IOException ex) {
            logger.error("Failed to create file appender", ex);
        }
    }

    private Class<?> clazz;

    public Logger(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void debug(String msg) {
        log(Severity.debug, msg, null);
    }

    public void info(String msg) {
        log(Severity.info, msg, null);
    }

    public void warn(String msg) {
        log(Severity.warn, msg, null);
    }

    public void warn(String msg, Throwable ex) {
        log(Severity.warn, msg, ex);
    }

    public void error(String msg) {
        log(Severity.error, msg, null);
    }

    public void error(String msg, Throwable ex) {
        log(Severity.error, msg, ex);
    }

    public void log(Severity severity, String msg, Throwable ex) {
        LogFrame frame = new LogFrame();
        frame.clazz = clazz;
        frame.severity = severity;
        frame.message = msg;
        frame.exception = ex;
        frame.thread = Thread.currentThread();
        frame.timestamp = LocalDateTime.now();
        log(frame);
    }

    private void log(LogFrame frame) {
        for (Appender appender : appenders) {
            try {
                appender.append(frame);
            } catch (IOException ex) {
                logger.error("Failed to append a log frame (" + appender.getClass() + ")", ex);
            }
        }
    }
}
