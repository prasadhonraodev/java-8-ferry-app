package ferry.booking.booking;

import ferry.booking.ferry.Ferry;
import ferry.booking.ferry.FerryAvailabilityService;
import ferry.booking.timetable.TimeTable;
import ferry.booking.timetable.TimeTableEntry;
import ferry.booking.timetable.TimeTables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JourneyBookingService {

    private final FerryAvailabilityService ferryService;
    private TimeTables timeTables;
    private Bookings bookings;

    public JourneyBookingService(TimeTables timeTables, Bookings bookings, FerryAvailabilityService ferryService) {
        this.timeTables = timeTables;
        this.bookings = bookings;
        this.ferryService = ferryService;
    }

    public boolean bookIfPossible(Booking booking) {
        if(this.canBook(booking)){
            this.book(booking);
            return true;
        }
        return false;
    }

    public List<Booking> getAllBookings() {
        return bookings.all();
    }

    private boolean canBook(Booking booking) {
        List<TimeTable> timetables = timeTables.all();
        List<TimeTableEntry> allEntries = new ArrayList<>();
        for (TimeTable tt : timetables) {
            allEntries.addAll(tt.entries);
        }
        allEntries.sort(Comparator.comparingLong(tte -> tte.time));

        for (TimeTableEntry timetable : allEntries) {
            Ferry ferry = ferryService.nextFerryAvailableFrom(timetable.originId, timetable.time);

            if (timetable.id == booking.journeyId) {
                int pax = 0;
                for (Booking x : bookings.all()) {
                    pax += x.passengers;
                }
                int seatsLeft = ferry.passengers - pax;
                return seatsLeft >= booking.passengers;
            }
        }
        return false;
    }

    private void book(Booking booking) {
        bookings.add(booking);
    }
}
