package Elevator.components;

import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class GuiLogHandler extends Handler {
    private final JTextArea area;

    public GuiLogHandler(JTextArea area) {
        this.area = area;
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null)
            return;
        SwingUtilities.invokeLater(() -> area.append(record.getMessage() + "\n"));
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
