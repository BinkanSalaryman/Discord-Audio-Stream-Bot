package net.runee.misc.logging.appender;

import net.runee.misc.logging.LogFrame;

import java.io.IOException;

public interface Appender {
    void append(LogFrame frame) throws IOException;
}
