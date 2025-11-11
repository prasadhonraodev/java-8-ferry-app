package ferry.booking.booking;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Bookings {

    private final List<Booking> bookings = new ArrayList<>();

    public void add(Booking booking) {
        bookings.add(booking);
    }

    public List<Booking> all() {
        return bookings;
    }

    public Optional<Booking> byId(int id) {
        return bookings.stream().filter(x -> x.journeyId == id).findFirst();
    }
}
