package ferry.booking.command;

import ferry.booking.delivery.port.UserCommunication;
import ferry.booking.delivery.port.UserMessage;
import ferry.booking.port.Port;
import ferry.booking.port.Ports;

import java.util.ArrayList;
import java.util.List;

public class ListPortsCommand implements Command {
    private UserCommunication out;
    private Ports ports;

    public ListPortsCommand(Ports ports, UserCommunication out) {
        this.out = out;
        this.ports = ports;
    }

    public void run() {
        List<String> message = new ArrayList<>();
        message.add("Ports:");
        message.add("------");
        for (Port port : ports.all()) {
            message.add(String.format("%d - %s", port.id, port.name));
        }
        message.add("");
        new UserMessage(message.toArray(new String[0])).print(this.out);
    }
}
