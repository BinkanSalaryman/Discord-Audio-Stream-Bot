package net.runee.misc.logging;

import java.time.LocalDateTime;

public class LogFrame {
    public Class<?> clazz;
    public Thread thread;
    public Severity severity;
    public String message;
    public Throwable exception;
    public LocalDateTime timestamp;
}
