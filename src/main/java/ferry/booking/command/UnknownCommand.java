package ferry.booking.command;

import ferry.booking.delivery.port.UserCommunication;
import ferry.booking.delivery.port.UserMessage;

public class UnknownCommand implements Command {
    private UserCommunication out;

    public UnknownCommand(UserCommunication userCommunication) {
        this.out = userCommunication;
    }

    public void run() {
        new UserMessage(new String[]{
                "Commands are: [search x y hh:mm] book, or list bookings",
                "  search x y hh:mm",
                "  book x y",
                "  list bookings",
                "  list ports",
                "",
                "Book is [book x y]",
                "where x - journey id",
                "where y - number of passenger",
                "",
                "Search is [search x y hh:mm]",
                "where: x - origin port id",
                "where: y - destinationg port id",
                "where: hh:mm - time to search after"}).print(this.out);
    }
}
