package ferry.booking.port;

import ferry.booking.ferry.Ferry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Port {
    private final Map<Integer, Long> boatAvailability = new HashMap<>();
    private final List<Ferry> boats = new ArrayList<>();
    public final int id;
    public final String name;

    public Port(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addBoat(long available, Ferry boat) {
        if (boat != null) {
            boats.add(boat);
            boatAvailability.put(boat.id, available);
        }
    }

    public Ferry getNextAvailable(long time) {
        for (Map.Entry<Integer, Long> entry : boatAvailability.entrySet()) {
            if (time >= entry.getValue()) {
                boatAvailability.remove(entry.getKey());
                for (Ferry boat : boats) {
                    if (boat.id == entry.getKey()) {
                        boats.remove(boat);
                        return boat;
                    }
                }
                return null;
            }
        }
        return null;
    }
}
