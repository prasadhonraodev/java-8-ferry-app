package ferry.booking.command;

import ferry.booking.booking.Booking;
import ferry.booking.booking.JourneyBookingService;
import ferry.booking.delivery.port.UserCommunication;
import ferry.booking.delivery.port.UserMessage;

import java.util.ArrayList;
import java.util.List;


public class ListBookingsCommand implements Command {
    private final UserCommunication out;
    private final JourneyBookingService bookingService;

    public ListBookingsCommand(JourneyBookingService bookingService, UserCommunication out) {
        this.out = out;
        this.bookingService = bookingService;
    }

    public void run() {
        List<Booking> bookings = bookingService.getAllBookings();

        List<String> message = new ArrayList<>();
        message.add("Bookings:");
        message.add("---------");
        StringBuilder stringBuilder = new StringBuilder();
        for (Booking b : bookings) {
            stringBuilder.append(String.format("journey %d - passengers %d", b.journeyId, b.passengers));
        }
        message.add(stringBuilder.toString());
        new UserMessage(message.toArray(new String[0])).print(this.out);
    }
}
