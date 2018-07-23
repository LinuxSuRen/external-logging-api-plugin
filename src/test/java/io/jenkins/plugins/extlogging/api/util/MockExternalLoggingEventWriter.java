package io.jenkins.plugins.extlogging.api.util;

import io.jenkins.plugins.extlogging.api.Event;
import io.jenkins.plugins.extlogging.api.impl.ExternalLoggingEventWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
public class MockExternalLoggingEventWriter extends ExternalLoggingEventWriter {

    private static final Logger LOGGER =
            Logger.getLogger(MockExternalLoggingEventWriter.class.getName());

    public File dest;

    // Debug flags
    private boolean eventWritten;

    public MockExternalLoggingEventWriter(File dest) {
        this.dest = dest;
    }

    @Override
    public void writeEvent(Event event) throws IOException {
        eventWritten = true;
        FileWriter writer = new FileWriter(dest, true);
        writer.write(event.toString() + "\n");
        writer.flush();
        writer.close();
    }

    public File getDest() {
        return dest;
    }

    public boolean isEventWritten() {
        return eventWritten;
    }
}