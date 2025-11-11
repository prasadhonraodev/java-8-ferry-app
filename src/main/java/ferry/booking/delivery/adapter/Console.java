package ferry.booking.delivery.adapter;


import ferry.booking.delivery.port.UserCommunication;

import java.io.PrintStream;

public class Console implements UserCommunication {
    private PrintStream printStream;

    private Console(PrintStream printStream) {
        this.printStream = printStream;
    }

    public static Console to(PrintStream printStream) {
        return new Console(printStream);
    }

    @Override
    public void display(String[] message) {
        for (String line : message) {
            this.println(line);
        }
    }

    private void println(String message) {
        printStream.println(message);
    }
}
