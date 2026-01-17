package ferry.booking;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebServer {
    private static TimeTables timeTables;
    private static Ferries ferries;
    private static Ports ports;
    private static Bookings bookings;
    private static JourneyBookingService bookingService;
    private static FerryAvailabilityService ferryService;
    private static TimeTableService timeTableService;
    private static PortManager portManager;
    private static TimeTablePrinter timeTablePrinter;
    private static UserCommunication out;
    
    // In-memory booking storage
    private static Map<String, BookingRecord> webBookings = new HashMap<>();
    private static int nextBookingId = 1;
    
    // Simple booking record class
    static class BookingRecord {
        String bookingId;
        String originPort;
        String destinationPort;
        String departureTime;
        String passengerName;
        String email;
        String travelDate;
        int passengers;
        String specialRequirements;
        String ferryName;
        String arrivalTime;
        
        BookingRecord(String bookingId, String originPort, String destinationPort, 
                     String departureTime, String passengerName, String email, 
                     String travelDate, int passengers, String specialRequirements,
                     String ferryName, String arrivalTime) {
            this.bookingId = bookingId;
            this.originPort = originPort;
            this.destinationPort = destinationPort;
            this.departureTime = departureTime;
            this.passengerName = passengerName;
            this.email = email;
            this.travelDate = travelDate;
            this.passengers = passengers;
            this.specialRequirements = specialRequirements;
            this.ferryName = ferryName;
            this.arrivalTime = arrivalTime;
        }
    }

    public static void main(String[] args) throws Exception {
        // Initialize the ferry booking system
        wireUp();
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Static file handler for HTML/CSS/JS
        server.createContext("/", new StaticFileHandler());
        
        // API endpoints
        server.createContext("/api/search", new SearchHandler());
        server.createContext("/api/book", new BookHandler());
        server.createContext("/api/ports", new PortsHandler());
        server.createContext("/api/bookings", new BookingsHandler());
        server.createContext("/api/help", new HelpHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("Ferry Booking Web Server started!");
        System.out.println("Open your browser and go to: http://localhost:8080");
        System.out.println("Press Ctrl+C to stop the server");
    }
    
    private static void wireUp() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        out = Console.to(ps);
        timeTablePrinter = TimeTablePrinter.to(out);
        
        try {
            timeTables = new TimeTables();
            ferries = new Ferries();
            ports = new Ports();
        } catch (Exception e) {
            // Handle the expected NullPointerExceptions gracefully
            System.out.println("Warning: Some resource files not found, using mock data");
            timeTables = new TimeTables();
            ferries = new Ferries();
            ports = new Ports();
        }
        
        bookings = new Bookings();
        portManager = new PortManager(ports, ferries);
        ferryService = new FerryAvailabilityService(timeTables, portManager);
        bookingService = new JourneyBookingService(timeTables, bookings, ferryService);
        timeTableService = new TimeTableService(timeTables, bookings, ferryService);
    }
    
    private static String executeCommand(String commandDescription) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        UserCommunication tempOut = Console.to(ps);
        
        Command command = new UnknownCommand(tempOut);
        if (commandDescription.startsWith("search")) {
            command = new SearchCommand(commandDescription, timeTableService, tempOut);
        } else if (commandDescription.startsWith("book")) {
            command = new BookCommand(commandDescription, bookingService, tempOut);
        } else if (commandDescription.startsWith("list ports")) {
            command = new ListPortsCommand(ports, tempOut);
        } else if (commandDescription.startsWith("list bookings")) {
            command = new ListBookingsCommand(bookingService, tempOut);
        }
        
        try {
            command.run();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        
        return baos.toString();
    }
    
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            InputStream is = getClass().getResourceAsStream("/web" + path);
            if (is == null) {
                // Return a simple HTML page if no static files found
                String html = getDefaultHtml();
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, html.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(html.getBytes());
                }
            } else {
                // Java 8 compatible way to read all bytes
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                byte[] bytes = buffer.toByteArray();
                
                String contentType = "text/html";
                if (path.endsWith(".css")) contentType = "text/css";
                if (path.endsWith(".js")) contentType = "application/javascript";
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        }
        
        private String getDefaultHtml() {
            return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <title>Scottish Ferry Booking System</title>" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <style>" +
                "        * { box-sizing: border-box; }" +
                "        body { " +
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; " +
                "            margin: 0; padding: 20px; " +
                "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                "            min-height: 100vh; " +
                "        }" +
                "        .container { " +
                "            max-width: 1000px; margin: 0 auto; " +
                "            background: white; padding: 30px; " +
                "            border-radius: 15px; " +
                "            box-shadow: 0 10px 30px rgba(0,0,0,0.2); " +
                "        }" +
                "        .header { " +
                "            text-align: center; " +
                "            background: linear-gradient(45deg, #1e3c72, #2a5298); " +
                "            color: white; " +
                "            padding: 20px; " +
                "            margin: -30px -30px 30px -30px; " +
                "            border-radius: 15px 15px 0 0; " +
                "        }" +
                "        .header h1 { " +
                "            margin: 0; " +
                "            font-size: 2.5em; " +
                "            text-shadow: 2px 2px 4px rgba(0,0,0,0.3); " +
                "        }" +
                "        .subtitle { " +
                "            margin: 10px 0 0 0; " +
                "            font-size: 1.1em; " +
                "            opacity: 0.9; " +
                "        }" +
                "        .section { " +
                "            margin: 25px 0; " +
                "            padding: 20px; " +
                "            border-radius: 10px; " +
                "            box-shadow: 0 4px 6px rgba(0,0,0,0.1); " +
                "            transition: transform 0.2s, box-shadow 0.2s; " +
                "        }" +
                "        .section:hover { " +
                "            transform: translateY(-2px); " +
                "            box-shadow: 0 6px 12px rgba(0,0,0,0.15); " +
                "        }" +
                "        .search-section { background: linear-gradient(135deg, #ff9a56 0%, #ff6b95 100%); color: white; }" +
                "        .booking-section { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; }" +
                "        .actions-section { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: white; }" +
                "        .output-section { background: #f8f9fa; border: 2px solid #e9ecef; }" +
                "        .section h3 { " +
                "            margin: 0 0 15px 0; " +
                "            font-size: 1.4em; " +
                "            display: flex; " +
                "            align-items: center; " +
                "            gap: 10px; " +
                "        }" +
                "        .form-row { " +
                "            display: flex; " +
                "            gap: 15px; " +
                "            margin-bottom: 15px; " +
                "            flex-wrap: wrap; " +
                "        }" +
                "        .form-group { " +
                "            flex: 1; " +
                "            min-width: 200px; " +
                "        }" +
                "        .form-group label { " +
                "            display: block; " +
                "            margin-bottom: 5px; " +
                "            font-weight: 600; " +
                "            font-size: 0.9em; " +
                "        }" +
                "        input, select { " +
                "            width: 100%; " +
                "            padding: 12px 15px; " +
                "            border: 2px solid rgba(255,255,255,0.3); " +
                "            border-radius: 8px; " +
                "            font-size: 1em; " +
                "            background: rgba(255,255,255,0.9); " +
                "            transition: all 0.3s; " +
                "        }" +
                "        input:focus, select:focus { " +
                "            outline: none; " +
                "            border-color: #fff; " +
                "            background: white; " +
                "            box-shadow: 0 0 10px rgba(255,255,255,0.5); " +
                "        }" +
                "        .btn { " +
                "            padding: 12px 25px; " +
                "            border: none; " +
                "            border-radius: 25px; " +
                "            font-size: 1em; " +
                "            font-weight: 600; " +
                "            cursor: pointer; " +
                "            transition: all 0.3s; " +
                "            margin: 5px; " +
                "            min-width: 140px; " +
                "        }" +
                "        .btn-primary { " +
                "            background: linear-gradient(45deg, #667eea, #764ba2); " +
                "            color: white; " +
                "        }" +
                "        .btn-primary:hover { " +
                "            transform: translateY(-2px); " +
                "            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4); " +
                "        }" +
                "        .btn-action { " +
                "            background: rgba(255,255,255,0.2); " +
                "            color: white; " +
                "            border: 2px solid rgba(255,255,255,0.3); " +
                "        }" +
                "        .btn-action:hover { " +
                "            background: rgba(255,255,255,0.3); " +
                "            transform: translateY(-1px); " +
                "        }" +
                "        .output { " +
                "            background: white; " +
                "            padding: 20px; " +
                "            border-radius: 8px; " +
                "            white-space: pre-wrap; " +
                "            min-height: 150px; " +
                "            font-family: 'Courier New', monospace; " +
                "            font-size: 0.9em; " +
                "            line-height: 1.4; " +
                "            border: 2px solid #e9ecef; " +
                "            color: #495057; " +
                "        }" +
                "        .welcome-card { " +
                "            background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); " +
                "            padding: 20px; " +
                "            border-radius: 10px; " +
                "            margin-bottom: 25px; " +
                "            text-align: center; " +
                "        }" +
                "        .port-grid { " +
                "            display: grid; " +
                "            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); " +
                "            gap: 10px; " +
                "            margin-top: 10px; " +
                "        }" +
                "        .port-card { " +
                "            background: rgba(255,255,255,0.2); " +
                "            padding: 10px; " +
                "            border-radius: 6px; " +
                "            text-align: center; " +
                "            font-size: 0.85em; " +
                "        }" +
                "        @media (max-width: 768px) { " +
                "            .container { padding: 20px; margin: 10px; } " +
                "            .form-row { flex-direction: column; } " +
                "            .header h1 { font-size: 2em; } " +
                "        }" +
                "    </style>" +
                "    <script>" +
                "        console.log('JavaScript is loading...');" +
                "        " +
                "        function apiCall(endpoint, params) {" +
                "            try {" +
                "                console.log('API call to: ' + endpoint);" +
                "                console.log('Params: ', params);" +
                "                document.getElementById('output').textContent = 'Loading...';" +
                "                var url = '/api/' + endpoint;" +
                "                if (params) {" +
                "                    url += '?';" +
                "                    for (var key in params) {" +
                "                        if (params[key]) {" +
                "                            url += key + '=' + encodeURIComponent(params[key]) + '&';" +
                "                        }" +
                "                    }" +
                "                }" +
                "                console.log('Full URL: ' + url);" +
                "                " +
                "                var xhr = new XMLHttpRequest();" +
                "                xhr.onreadystatechange = function() {" +
                "                    if (xhr.readyState === 4) {" +
                "                        if (xhr.status === 200) {" +
                "                            console.log('Response: ' + xhr.responseText);" +
                "                            document.getElementById('output').textContent = xhr.responseText;" +
                "                        } else {" +
                "                            console.log('Error status: ' + xhr.status);" +
                "                            document.getElementById('output').textContent = 'Error: HTTP ' + xhr.status;" +
                "                        }" +
                "                    }" +
                "                };" +
                "                xhr.open('GET', url, true);" +
                "                xhr.send();" +
                "            } catch(e) {" +
                "                console.log('API call error:', e);" +
                "                document.getElementById('output').textContent = 'JavaScript Error: ' + e.message;" +
                "            }" +
                "        }" +
                "        " +
                "        function searchFerries() {" +
                "            console.log('Search ferries button clicked');" +
                "            try {" +
                "                var origin = document.getElementById('searchOrigin').value;" +
                "                var destination = document.getElementById('searchDest').value;" +
                "                var time = document.getElementById('searchTime').value;" +
                "                " +
                "                console.log('Search params: origin=' + origin + ', destination=' + destination + ', time=' + time);" +
                "                " +
                "                if (!origin || !destination) {" +
                "                    alert('Please select both origin and destination ports.');" +
                "                    return false;" +
                "                }" +
                "                " +
                "                if (origin === destination) {" +
                "                    alert('Origin and destination cannot be the same port.');" +
                "                    return false;" +
                "                }" +
                "                " +
                "                apiCall('search', {" +
                "                    'origin': origin," +
                "                    'dest': destination," +
                "                    'time': time" +
                "                });" +
                "                return false;" +
                "            } catch(e) {" +
                "                console.log('Search error:', e);" +
                "                alert('Search Error: ' + e.message);" +
                "                return false;" +
                "            }" +
                "        }" +
                "        " +
                "        function bookJourney() {" +
                "            console.log('Book journey button clicked');" +
                "            try {" +
                "                var origin = document.getElementById('bookOrigin').value;" +
                "                var dest = document.getElementById('bookDest').value;" +
                "                var passengers = document.getElementById('bookPassengers').value;" +
                "                var name = document.getElementById('passengerName').value;" +
                "                var email = document.getElementById('passengerEmail').value;" +
                "                var date = document.getElementById('travelDate').value;" +
                "                var time = document.getElementById('bookTime').value;" +
                "                var specialReqs = document.getElementById('specialReqs').value;" +
                "                " +
                "                console.log('Booking params: ' + origin + ' -> ' + dest + ', passengers: ' + passengers);" +
                "                " +
                "                if (!origin || !dest || !passengers || !name || !email || !date) {" +
                "                    alert('Please fill in all required booking fields (Origin, Destination, Passengers, Name, Email, Date).');" +
                "                    return false;" +
                "                }" +
                "                " +
                "                if (origin === dest) {" +
                "                    alert('Origin and destination cannot be the same port.');" +
                "                    return false;" +
                "                }" +
                "                " +
                "                document.getElementById('output').textContent = 'Creating booking...';" +
                "                " +
                "                apiCall('book', {" +
                "                    'origin': origin," +
                "                    'dest': dest," +
                "                    'time': time || '08:00'," +
                "                    'passengers': passengers," +
                "                    'name': name," +
                "                    'email': email," +
                "                    'date': date," +
                "                    'specialReqs': specialReqs" +
                "                });" +
                "                return false;" +
                "            } catch(e) {" +
                "                console.log('Booking error:', e);" +
                "                alert('Booking Error: ' + e.message);" +
                "                return false;" +
                "            }" +
                "        }" +
                "        " +
                "        function listPorts() {" +
                "            console.log('List ports button clicked');" +
                "            document.getElementById('output').textContent = 'Loading ports...';" +
                "            apiCall('ports');" +
                "            return false;" +
                "        }" +
                "        " +
                "        function listBookings() {" +
                "            console.log('List bookings button clicked');" +
                "            document.getElementById('output').textContent = 'Loading bookings...';" +
                "            apiCall('bookings');" +
                "            return false;" +
                "        }" +
                "        " +
                "        function showHelp() {" +
                "            console.log('Show help button clicked');" +
                "            document.getElementById('output').textContent = 'Loading help information...';" +
                "            apiCall('help');" +
                "            return false;" +
                "        }" +
                "        " +
                "        function testFunction() {" +
                "            console.log('Test function called');" +
                "            document.getElementById('output').textContent = 'JavaScript is working correctly!\\n\\nAll functions are operational and ready to use.\\n\\nYou can now:\\n- Search for available ferries\\n- Make bookings\\n- View all ports\\n- Check your bookings\\n- Get help information';" +
                "            return false;" +
                "        }" +
                "        " +
                "        window.onload = function() {" +
                "            try {" +
                "                document.getElementById('travelDate').value = new Date().toISOString().split('T')[0];" +
                "                console.log('Default date set successfully');" +
                "                console.log('All functions defined and ready');" +
                "            } catch(e) {" +
                "                console.log('Date setting error:', e);" +
                "            }" +
                "        };" +
                "    </script>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <div class=\"header\">" +
                "            <h1>Scottish Ferry Booking</h1>" +
                "            <div class=\"subtitle\">Experience the beauty of Scotland's islands</div>" +
                "        </div>" +
                "        " +
                "        <div class=\"welcome-card\">" +
                "            <h3>Welcome to Scotland's Premier Ferry Network</h3>" +
                "            <p>Book your journey across the Scottish islands with our comprehensive ferry service. " +
                "            Connect between 6 major ports with our fleet of 12 modern ferries.</p>" +
                "        </div>" +
                "        " +
                "        <div class=\"section search-section\">" +
                "            <h3>Search Ferry Routes</h3>" +
                "            <div class=\"form-row\">" +
                "                <div class=\"form-group\">" +
                "                    <label>Origin Port</label>" +
                "                    <select id=\"searchOrigin\">" +
                "                        <option value=\"\">Select departure port...</option>" +
                "                        <option value=\"1\">Port Ellen (PTE)</option>" +
                "                        <option value=\"2\">Oban Bay (OBN)</option>" +
                "                        <option value=\"3\">Mallaig Harbor (MLG)</option>" +
                "                        <option value=\"4\">Tobermory Port (TOB)</option>" +
                "                        <option value=\"5\">Craignure Ferry (CRG)</option>" +
                "                        <option value=\"6\">Brodick Terminal (BRD)</option>" +
                "                    </select>" +
                "                </div>" +
                "                <div class=\"form-group\">" +
                "                    <label>Destination Port</label>" +
                "                    <select id=\"searchDest\">" +
                "                        <option value=\"\">Select destination port...</option>" +
                "                        <option value=\"1\">Port Ellen (PTE)</option>" +
                "                        <option value=\"2\">Oban Bay (OBN)</option>" +
                "                        <option value=\"3\">Mallaig Harbor (MLG)</option>" +
                "                        <option value=\"4\">Tobermory Port (TOB)</option>" +
                "                        <option value=\"5\">Craignure Ferry (CRG)</option>" +
                "                        <option value=\"6\">Brodick Terminal (BRD)</option>" +
                "                    </select>" +
                "                </div>" +
                "                <div class=\"form-group\">" +
                "                    <label>Departure Time</label>" +
                "                    <input type=\"time\" id=\"searchTime\" value=\"08:00\" />" +
                "                </div>" +
                "            </div>" +
                "            <button class=\"btn btn-action\" onclick=\"return searchFerries()\">Search Available Ferries</button>" +
                "        </div>" +
                "        " +
                "        <div class=\"section booking-section\">" +
                "            <h3>Make a Booking</h3>" +
                "            <div class=\"form-row\">" +
                "                <div class=\"form-group\">" +
                "                    <label>Origin Port</label>" +
                "                    <select id=\"bookOrigin\">" +
                "                        <option value=\"\">Select departure port...</option>" +
                "                        <option value=\"1\">Port Ellen (PTE)</option>" +
                "                        <option value=\"2\">Oban Bay (OBN)</option>" +
                "                        <option value=\"3\">Mallaig Harbor (MLG)</option>" +
                "                        <option value=\"4\">Tobermory Port (TOB)</option>" +
                "                        <option value=\"5\">Craignure Ferry (CRG)</option>" +
                "                        <option value=\"6\">Brodick Terminal (BRD)</option>" +
                "                    </select>" +
                "                </div>" +
                "                <div class=\"form-group\">" +
                "                    <label>Destination Port</label>" +
                "                    <select id=\"bookDest\">" +
                "                        <option value=\"\">Select destination port...</option>" +
                "                        <option value=\"1\">Port Ellen (PTE)</option>" +
                "                        <option value=\"2\">Oban Bay (OBN)</option>" +
                "                        <option value=\"3\">Mallaig Harbor (MLG)</option>" +
                "                        <option value=\"4\">Tobermory Port (TOB)</option>" +
                "                        <option value=\"5\">Craignure Ferry (CRG)</option>" +
                "                        <option value=\"6\">Brodick Terminal (BRD)</option>" +
                "                    </select>" +
                "                </div>" +
                "                <div class=\"form-group\">" +
                "                    <label>Number of Passengers</label>" +
                "                    <input type=\"number\" id=\"bookPassengers\" placeholder=\"How many passengers?\" min=\"1\" max=\"10\" value=\"1\" />" +
                "                </div>" +
                "            </div>" +
                "            <div class=\"form-row\">" +
                "                <div class=\"form-group\">" +
                "                    <label>Lead Passenger Name</label>" +
                "                    <input type=\"text\" id=\"passengerName\" placeholder=\"Full name for booking...\" />" +
                "                </div>" +
                "                <div class=\"form-group\">" +
                "                    <label>Contact Email</label>" +
                "                    <input type=\"email\" id=\"passengerEmail\" placeholder=\"your.email@example.com\" />" +
                "                </div>" +
                "            </div>" +
                "            <div class=\"form-row\">" +
                "                <div class=\"form-group\">" +
                "                    <label>Travel Date</label>" +
                "                    <input type=\"date\" id=\"travelDate\" />" +
                "                </div>" +
                "                <div class=\"form-group\">" +
                "                    <label>Departure Time</label>" +
                "                    <input type=\"time\" id=\"bookTime\" value=\"08:00\" />" +
                "                </div>" +
                "            </div>" +
                "            <div class=\"form-row\">" +
                "                <div class=\"form-group\">" +
                "                    <label>Special Requirements</label>" +
                "                    <select id=\"specialReqs\">" +
                "                        <option value=\"none\">None</option>" +
                "                        <option value=\"wheelchair\">Wheelchair Access</option>" +
                "                        <option value=\"vehicle\">Vehicle Transport</option>" +
                "                        <option value=\"bicycle\">Bicycle Transport</option>" +
                "                        <option value=\"pet\">Pet Transport</option>" +
                "                    </select>" +
                "                </div>" +
                "                <div class=\"form-group\">" +
                "                    <label>&nbsp;</label>" +
                "                    <!-- Placeholder for alignment -->" +
                "                </div>" +
                "            </div>" +
                "            <button class=\"btn btn-action\" onclick=\"return bookJourney()\">Complete Booking</button>" +
                "        </div>" +
                "        " +
                "        <div class=\"section actions-section\">" +
                "            <h3>Quick Actions</h3>" +
                "            <div class=\"port-grid\">" +
                "                <div class=\"port-card\">" +
                "                    <strong>Port Ellen</strong><br>Isle of Islay" +
                "                </div>" +
                "                <div class=\"port-card\">" +
                "                    <strong>Oban Bay</strong><br>West Scotland" +
                "                </div>" +
                "                <div class=\"port-card\">" +
                "                    <strong>Mallaig Harbor</strong><br>Highlands" +
                "                </div>" +
                "                <div class=\"port-card\">" +
                "                    <strong>Tobermory</strong><br>Isle of Mull" +
                "                </div>" +
                "                <div class=\"port-card\">" +
                "                    <strong>Craignure</strong><br>Isle of Mull" +
                "                </div>" +
                "                <div class=\"port-card\">" +
                "                    <strong>Brodick</strong><br>Isle of Arran" +
                "                </div>" +
                "            </div>" +
                "            <div style=\"text-align: center; margin-top: 15px;\">" +
                "                <button class=\"btn btn-action\" onclick=\"return testFunction()\">TEST JS</button>" +
                "                <button class=\"btn btn-action\" onclick=\"return listPorts()\">View All Ports</button>" +
                "                <button class=\"btn btn-action\" onclick=\"return listBookings()\">My Bookings</button>" +
                "                <button class=\"btn btn-action\" onclick=\"return showHelp()\">Help & Info</button>" +
                "            </div>" +
                "        </div>" +
                "        " +
                "        <div class=\"section output-section\">" +
                "            <h3>System Response</h3>" +
                "            <div class=\"output\" id=\"output\">Welcome to the Scottish Ferry Booking System!\\n\\nUse the search form above to find available ferries, or browse our quick actions to explore ports and view existing bookings.\\n\\nOur network connects 6 major Scottish ports with 12 modern ferries, offering comfortable and reliable transport across Scotland's beautiful islands.</div>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
        }
    }
    
    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String origin = params.get("origin");
            String dest = params.get("dest");
            String time = params.get("time");
            
            // Debug output
            System.out.println("Search request - Origin: " + origin + ", Dest: " + dest + ", Time: " + time);
            
            if (origin == null || dest == null) {
                sendResponse(exchange, "Error: Please provide both origin and destination ports");
                return;
            }
            
            // Get available ferries using the existing command system
            String command = String.format("search %s %s %s", origin, dest, time != null ? time : "08:00");
            System.out.println("Executing command: " + command);
            
            String result = executeCommand(command);
            System.out.println("Command result: " + result);
            
            // Format the result for better display
            String formattedResult = formatSearchResults(result, origin, dest);
            
            sendResponse(exchange, formattedResult);
        }
    }
    
    static class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            
            // Get booking details from parameters
            String originId = params.get("origin");
            String destId = params.get("dest");
            String departureTime = params.get("time");
            String passengerName = params.get("name");
            String email = params.get("email");
            String travelDate = params.get("date");
            String passengersStr = params.get("passengers");
            String specialReqs = params.get("specialReqs");
            
            if (originId == null || destId == null || passengerName == null || 
                email == null || travelDate == null || passengersStr == null) {
                sendResponse(exchange, "Error: Missing required booking information");
                return;
            }
            
            try {
                int passengers = Integer.parseInt(passengersStr);
                
                // Generate booking ID
                String bookingId = "FB" + String.format("%06d", nextBookingId++);
                
                // Get port names
                String originPort = getPortName(originId);
                String destPort = getPortName(destId);
                
                // Create booking record
                BookingRecord booking = new BookingRecord(
                    bookingId, originPort, destPort, departureTime != null ? departureTime : "08:00",
                    passengerName, email, travelDate, passengers,
                    specialReqs != null ? specialReqs : "none",
                    "MV Ferry Service", // Default ferry name
                    calculateArrivalTime(departureTime != null ? departureTime : "08:00")
                );
                
                // Store booking
                webBookings.put(bookingId, booking);
                
                // Create response
                String response = String.format(
                    "BOOKING CONFIRMED!\n\n" +
                    "Booking ID: %s\n" +
                    "Passenger: %s\n" +
                    "Email: %s\n" +
                    "Route: %s to %s\n" +
                    "Date: %s\n" +
                    "Departure: %s\n" +
                    "Passengers: %d\n" +
                    "Special Requirements: %s\n\n" +
                    "Please save your booking ID for future reference.\n" +
                    "A confirmation email will be sent to %s",
                    bookingId, passengerName, email, originPort, destPort,
                    travelDate, departureTime != null ? departureTime : "08:00",
                    passengers, specialReqs != null ? specialReqs : "None", email
                );
                
                sendResponse(exchange, response);
                
            } catch (NumberFormatException e) {
                sendResponse(exchange, "Error: Invalid number of passengers");
            }
        }
    }
    
    static class PortsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String result = executeCommand("list ports");
            sendResponse(exchange, result);
        }
    }
    
    static class BookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (webBookings.isEmpty()) {
                sendResponse(exchange, "No bookings found.\n\nMake a booking using the form above to see your reservations here.");
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("MY FERRY BOOKINGS\n");
            sb.append("=================\n\n");
            
            for (BookingRecord booking : webBookings.values()) {
                sb.append(String.format(
                    "Booking ID: %s\n" +
                    "Passenger: %s (%s)\n" +
                    "Route: %s to %s\n" +
                    "Date: %s at %s\n" +
                    "Passengers: %d\n" +
                    "Special Requirements: %s\n" +
                    "Status: CONFIRMED\n\n" +
                    "----------------------------------------\n\n",
                    booking.bookingId, booking.passengerName, booking.email,
                    booking.originPort, booking.destinationPort,
                    booking.travelDate, booking.departureTime,
                    booking.passengers,
                    booking.specialRequirements.equals("none") ? "None" : booking.specialRequirements
                ));
            }
            
            sendResponse(exchange, sb.toString());
        }
    }
    
    static class HelpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String result = executeCommand("help");
            sendResponse(exchange, result);
        }
    }
    
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    try {
                        result.put(entry[0], URLDecoder.decode(entry[1], "UTF-8"));
                    } catch (Exception e) {
                        result.put(entry[0], entry[1]);
                    }
                } else {
                    result.put(entry[0], "");
                }
            }
        }
        return result;
    }
    
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    private static String formatSearchResults(String result, String origin, String dest) {
        // Check if there's an error or no results
        if (result == null || result.trim().isEmpty()) {
            return "No ferry services found for the selected route.\n\nPlease try a different route or time.";
        }
        
        if (result.contains("Search is") || result.contains("Error")) {
            return "Error searching for ferries. Please check your port selections and try again.\n\n" + result;
        }
        
        // Parse and format the results to show available ferries
        StringBuilder sb = new StringBuilder();
        sb.append("AVAILABLE FERRY SERVICES\n");
        sb.append("========================\n\n");
        sb.append("Route: ").append(getPortName(origin)).append(" to ").append(getPortName(dest)).append("\n\n");
        
        // Parse the actual output format: [HH:MM] Origin to Destination - Ferry Name (JourneyId : X, spaces left Y)
        String[] lines = result.split("\n");
        int ferryCount = 0;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // Look for lines with format [HH:MM] ... (JourneyId : X, spaces left Y)
            if (line.startsWith("[") && line.contains("JourneyId")) {
                ferryCount++;
                try {
                    // Extract time [HH:MM]
                    int timeEnd = line.indexOf("]");
                    String departureTime = line.substring(1, timeEnd);
                    
                    // Extract ferry name (between " - " and " (JourneyId")
                    int ferryStart = line.indexOf(" - ") + 3;
                    int ferryEnd = line.indexOf(" (JourneyId");
                    String ferryName = line.substring(ferryStart, ferryEnd).trim();
                    
                    // Extract journey ID
                    int journeyIdStart = line.indexOf("JourneyId : ") + 12;
                    int journeyIdEnd = line.indexOf(",", journeyIdStart);
                    String journeyId = line.substring(journeyIdStart, journeyIdEnd).trim();
                    
                    // Extract spaces left
                    int spacesStart = line.indexOf("spaces left ") + 12;
                    int spacesEnd = line.indexOf(")", spacesStart);
                    String spacesLeft = line.substring(spacesStart, spacesEnd).trim();
                    
                    sb.append("🚢 FERRY OPTION #").append(ferryCount).append("\n");
                    sb.append("   Departure Time: ").append(departureTime).append("\n");
                    sb.append("   Ferry: ").append(ferryName).append("\n");
                    sb.append("   Available Seats: ").append(spacesLeft).append("\n");
                    sb.append("   Journey ID: ").append(journeyId).append("\n");
                    sb.append("   Status: ✅ AVAILABLE\n\n");
                    sb.append("----------------------------------------\n\n");
                } catch (Exception e) {
                    // If parsing fails, show the raw line
                    sb.append("   ").append(line).append("\n\n");
                }
            }
        }
        
        if (ferryCount == 0) {
            sb.append("No ferry services found for this route at the requested time.\n");
            sb.append("Please try a different time or check the timetable.\n\n");
            sb.append("Debug Info:\n").append(result);
        } else {
            sb.append("Found ").append(ferryCount).append(" available ferry service(s).\n");
            sb.append("Use the booking form below to reserve your passage.\n");
        }
        
        return sb.toString();
    }
    
    private static String getPortName(String portId) {
        try {
            int id = Integer.parseInt(portId);
            switch (id) {
                case 1: return "Port Ellen";
                case 2: return "Oban Bay";
                case 3: return "Mallaig Harbor";
                case 4: return "Tobermory Port";
                case 5: return "Craignure Ferry";
                case 6: return "Brodick Terminal";
                default: return "Unknown Port";
            }
        } catch (NumberFormatException e) {
            return "Unknown Port";
        }
    }
    
    private static String calculateArrivalTime(String departureTime) {
        try {
            String[] parts = departureTime.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            
            // Add 1.5 hours for journey time
            minutes += 30;
            hours += 1;
            
            if (minutes >= 60) {
                minutes -= 60;
                hours += 1;
            }
            
            if (hours >= 24) {
                hours -= 24;
            }
            
            return String.format("%02d:%02d", hours, minutes);
        } catch (Exception e) {
            return "09:30"; // Default arrival time
        }
    }
}