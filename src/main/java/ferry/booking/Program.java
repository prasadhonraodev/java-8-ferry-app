package ferry.booking;

import ferry.booking.booking.Bookings;
import ferry.booking.booking.JourneyBookingService;
import ferry.booking.command.*;
import ferry.booking.delivery.TimeTablePrinter;
import ferry.booking.delivery.adapter.Console;
import ferry.booking.delivery.port.UserCommunication;
import ferry.booking.ferry.Ferries;
import ferry.booking.ferry.FerryAvailabilityService;
import ferry.booking.port.Port;
import ferry.booking.port.PortManager;
import ferry.booking.port.Ports;
import ferry.booking.timetable.TimeTableService;
import ferry.booking.timetable.TimeTableViewModelRow;
import ferry.booking.timetable.TimeTables;

import java.io.*;
import java.util.List;

public class Program {

    public static TimeTableService timeTableService;
    public static Ports ports;
    private static JourneyBookingService bookingService;
    private static FerryAvailabilityService ferryService;
    private static UserCommunication out;
    private static TimeTablePrinter timeTablePrinter;

    public static void wireUp() {
        TimeTables timeTables = new TimeTables();
        Ferries ferries = new Ferries();
        Bookings bookings = new Bookings();
        ports = new Ports();
        ferryService = new FerryAvailabilityService(timeTables, new PortManager(ports, ferries));
        bookingService = new JourneyBookingService(timeTables, bookings, ferryService);
        timeTableService = new TimeTableService(timeTables, bookings, ferryService);
    }

    public static void main(String[] args) {
        start(System.in, System.out);
    }

    public static void start(InputStream input, PrintStream output) {
        start(output);
        commandLoop(input);
    }

    public static void mainWithTestData(PrintStream ps) {
        start(ps);
        testCommands();
    }

    public static void start(PrintStream ps) {
        out = Console.to(ps);
        timeTablePrinter = TimeTablePrinter.to(out);
        wireUp();

        timeTablePrinter.displayWelcomeMessage();

        List<Port> allPorts = ports.all();
        List<TimeTableViewModelRow> timeTable = timeTableService.getTimeTable(ports);

        timeTablePrinter.displayTimetable(allPorts, timeTable);
    }

    private static void testCommands() {
        doCommand("help");
        doCommand("list ports");
        doCommand("search 2 3 00:00");
        doCommand("search 2 3 00:00");
        doCommand("book 10 2");
        doCommand("search 2 3 00:00");
        doCommand("book 10 10");
        doCommand("book 10 1");
        doCommand("search 1 2 01:00");
        doCommand("book 4 2");
        doCommand("book 6 8");
        doCommand("search 1 2 01:00");
        doCommand("search 1 3 01:00");
        doCommand("search 1 3 01:30");
        doCommand("book 5 16");
        doCommand("book 16 16");
        doCommand("search 1 3 00:00");
        doCommand("list bookings");
    }

    private static void commandLoop(InputStream in) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            line = br.readLine();
            while (!(line == null || line.toLowerCase().equals("quit"))) {
                doCommand(line);
                line = br.readLine();
            }
        } catch (IOException e) {
        }
    }

    private static void doCommand(String commandDescription) {
        Command command = new UnknownCommand(out);
        if (commandDescription.startsWith("search")) {
            command = new SearchCommand(commandDescription, timeTableService, out);
        } else if (commandDescription.startsWith("book")) {
            command = new BookCommand(commandDescription, bookingService, out);
        } else if (commandDescription.startsWith("list ports")) {
            command = new ListPortsCommand(ports, out);
        } else if (commandDescription.startsWith("list bookings")) {
            command = new ListBookingsCommand(bookingService, out);
        }
        command.run();
    }
}
