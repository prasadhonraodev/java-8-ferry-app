package ferry.booking.port;


import ferry.booking.ferry.Ferries;
import ferry.booking.ferry.Ferry;

import java.util.ArrayList;
import java.util.List;

public class PortManager {

    private final Ports ports;
    private final Ferries ferries;

    public PortManager(Ports ports, Ferries ferries) {
        this.ports = ports;
        this.ferries = ferries;
    }

    public List<Port> addAvailability() {
        for (Ferry ferry : ferries.all()) {
            ports.byId(ferry.homePortId).addBoat(10, ferry);
        }
        return ports.all();
    }
}
