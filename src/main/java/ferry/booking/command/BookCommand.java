package ferry.booking.command;

import ferry.booking.booking.Booking;
import ferry.booking.booking.JourneyBookingService;
import ferry.booking.delivery.port.ErrorMessage;
import ferry.booking.delivery.port.HelpMessage;
import ferry.booking.delivery.port.UserCommunication;
import ferry.booking.delivery.port.UserMessage;

public class BookCommand implements Command {
    private final UserCommunication out;
    private final JourneyBookingService bookingService;
    private String line;

    public BookCommand(String line, JourneyBookingService bookingService, UserCommunication out) {
        this.out = out;
        this.bookingService = bookingService;
        this.line = line;
    }


    public void run() {
        try {
            String parts[] = line.split(" ");
            int journeyId = Integer.parseInt(parts[1]);
            int passengers = Integer.parseInt(parts[2]);

            if (bookingService.bookIfPossible(new Booking(journeyId, passengers))) {
                new UserMessage("Booked").print(this.out);
            } else {
                new ErrorMessage(new String[]{"Cannot book that journey"}).print(this.out);
            }
        } catch (Exception e) {
            new HelpMessage(new String[]{
                    "Book is [book x y]",
                    "where x - journey id",
                    "where y - number of passenger"
            }).print(this.out);
        }
    }
}
