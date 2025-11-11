package ferry.booking.delivery;

import ferry.booking.delivery.port.HelpMessage;
import ferry.booking.delivery.port.UserCommunication;
import ferry.booking.delivery.port.UserMessage;
import ferry.booking.port.Port;
import ferry.booking.timetable.TimeTableViewModelRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimeTablePrinter {
    private UserCommunication out;

    private TimeTablePrinter(UserCommunication userCommunication) {
        this.out = userCommunication;
    }

    public static TimeTablePrinter to(UserCommunication userCommunication) {
        return new TimeTablePrinter(userCommunication);
    }

    public void displayTimetable(List<Port> ports, List<TimeTableViewModelRow> rows) {
        for (Port port : ports) {
            printPortHeader(port.name);
            List<TimeTableViewModelRow> items = new ArrayList<>();
            for (TimeTableViewModelRow x : rows) {
                if (x.originPort.equals(port.name)) {
                    items.add(x);
                }
            }
            Collections.sort(items, Comparator.comparing(tt -> tt.startTime));

            for (TimeTableViewModelRow item : items) {
                new UserMessage(String.format("| %-8s | %-13s | %-13s | %-18s | %-8s |", item.startTime, item.destinationPort,
                        item.journeyLength, item.ferryName, item.arrivalTime)).print(this.out);
            }
        }
    }

    private void printPortHeader(String portName) {
        new UserMessage(new String[]{
                "",
                "Departures from " + portName,
                "",
                " --------------------------------------------------------------------------",
                String.format("| %-8s | %-13s | %-13s | %-18s | %-8s |", "Time", "Destination", "Journey Time", "Ferry", "Arrives"),
                " --------------------------------------------------------------------------"
        }).print(this.out);
    }

    public void displayWelcomeMessage() {
        new HelpMessage(new String[]{
                "Welcome to the Ferry Finding System",
                "=======",
                "Ferry Time Table"
        }).print(this.out);
    }
}
