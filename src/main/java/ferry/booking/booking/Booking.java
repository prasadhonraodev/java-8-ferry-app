package ferry.booking.booking;

public class Booking {

    public final int journeyId;
    public final int passengers;

    public Booking(int journeyId, int passengers) {
        this.journeyId = journeyId;
        this.passengers = passengers;
    }
}
