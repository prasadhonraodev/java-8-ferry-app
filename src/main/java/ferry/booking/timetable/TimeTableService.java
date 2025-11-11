package ferry.booking.timetable;

import ferry.booking.booking.AvailableCrossing;
import ferry.booking.booking.Booking;
import ferry.booking.booking.Bookings;
import ferry.booking.ferry.Ferry;
import ferry.booking.ferry.FerryAvailabilityService;
import ferry.booking.port.Port;
import ferry.booking.port.Ports;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimeTableService {

    private final TimeTables timeTables;
    private final Bookings bookings;
    private final FerryAvailabilityService ferryService;

    public TimeTableService(TimeTables timeTables, Bookings bookings, FerryAvailabilityService ferryService) {
        this.timeTables = timeTables;
        this.bookings = bookings;
        this.ferryService = ferryService;
    }

    public List<TimeTableViewModelRow> getTimeTable(Ports ports) {
        List<TimeTableEntry> allEntries = getTimeTableEntries();

        List<TimeTableViewModelRow> rows = new ArrayList<>();

        for (TimeTableEntry timetable : allEntries) {
            Port origin = ports.byId(timetable.originId);
            Port destination = ports.byId(timetable.destinationId);
            TimeTableViewModelRow row = buildRow(timetable, origin, destination);
            rows.add(row);
        }
        return rows;
    }

    private TimeTableViewModelRow buildRow(TimeTableEntry timetable, Port origin, Port destination) {
        String destinationName = destination.name;
        String originName = origin.name;
        Ferry ferry = ferryService.nextFerryAvailableFrom(origin.id, timetable.time);
        long arrivalTime = timetable.time + timetable.journeyTime;
        TimeTableViewModelRow row = new TimeTableViewModelRow();
        row.destinationPort = destinationName;
        row.ferryName = ferry == null ? "" : ferry.name;
        row.journeyLength = String.format("%02d:%02d", timetable.journeyTime / 60, timetable.journeyTime % 60);
        row.originPort = originName;
        row.startTime = String.format("%02d:%02d", timetable.time / 60, timetable.time % 60);
        row.arrivalTime = String.format("%02d:%02d", arrivalTime / 60, arrivalTime % 60);
        return row;
    }

    public List<AvailableCrossing> getAvailableCrossings(long time, int fromPort, int toPort) {
        Ports ports = new Ports();
        List<TimeTableEntry> allEntries = getTimeTableEntries();

        List<AvailableCrossing> result = new ArrayList<>();

        for (TimeTableEntry timetable : allEntries) {
            Port origin = ports.byId(timetable.originId);
            Port destination = ports.byId(timetable.destinationId);
            Ferry ferry = ferryService.nextFerryAvailableFrom(timetable.originId, timetable.time);

            if (toPort == destination.id && fromPort == origin.id) {
                if (timetable.time >= time) {
                    List<Booking> journeyBookings = new ArrayList<>();
                    bookings.byId(timetable.id).map(journeyBookings::add);
                    int pax = 0;
                    for (Booking x : journeyBookings) {
                        pax += x.passengers;
                    }
                    int seatsLeft = ferry.passengers - pax;
                    if (seatsLeft > 0) {
                        AvailableCrossing crossing = new AvailableCrossing();
                        crossing.arrive = timetable.time + timetable.journeyTime;
                        crossing.ferryName = ferry.name;
                        int pax2 = 0;
                        for (Booking x : journeyBookings) {
                            pax2 += x.passengers;
                        }
                        crossing.seatsLeft = ferry.passengers - pax2;
                        crossing.setOff = timetable.time;
                        crossing.originPort = origin.name;
                        crossing.destinationPort = destination.name;
                        crossing.journeyId = timetable.id;
                        result.add(crossing);
                    }
                }
            }
        }
        return result;
    }

    private List<TimeTableEntry> getTimeTableEntries() {
        List<TimeTable> timetables = timeTables.all();
        List<TimeTableEntry> allEntries = new ArrayList<>();
        for (TimeTable tt : timetables) {
            allEntries.addAll(tt.entries);
        }
        allEntries.sort(Comparator.comparingLong(entry -> entry.time));
        return allEntries;
    }
}
