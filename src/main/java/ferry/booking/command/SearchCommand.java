package ferry.booking.command;

import ferry.booking.booking.AvailableCrossing;
import ferry.booking.delivery.port.UserCommunication;
import ferry.booking.delivery.port.UserMessage;
import ferry.booking.timetable.TimeTableService;

import java.util.ArrayList;
import java.util.List;

public class SearchCommand implements Command {
    private final UserCommunication out;
    private final TimeTableService timeTableService;
    private final String command;

    public SearchCommand(String command, TimeTableService timeTableService, UserCommunication out) {
        this.out = out;
        this.timeTableService = timeTableService;
        this.command = command;
    }

    public void run() {
        try {
            String parts[] = command.split(" ");
            int originPortId = Integer.parseInt(parts[1]);
            int destinationPortId = Integer.parseInt(parts[2]);
            String mins[] = parts[3].split(":");
            long time = Long.parseLong(mins[0]) * 60 + Long.parseLong(mins[1]);

            List<AvailableCrossing> search = timeTableService.getAvailableCrossings(time, originPortId,
                    destinationPortId);

            for (AvailableCrossing availableCrossing : search) {
                new UserMessage(String.format("[%02d:%02d] %s to %s -  %s (JourneyId : %d, spaces left %d)", availableCrossing.setOff / 60,
                        availableCrossing.setOff % 60, availableCrossing.originPort, availableCrossing.destinationPort, availableCrossing.ferryName,
                        availableCrossing.journeyId, availableCrossing.seatsLeft)).print(this.out);
            }
        } catch (Exception e) {
            List<String> message = new ArrayList<>();

            message.add("Search is [search x y hh:mm]");
            message.add("where: x - origin port id");
            message.add("where: y - destinationg port id");
            message.add("where: hh:mm - time to search after");
            new UserMessage(message.toArray(new String[0])).print(this.out);
        }
    }
}
