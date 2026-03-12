import java.util.*;

abstract class Room {
    String type;
    int beds;
    int size;
    double price;

    Room(String type, int beds, int size, double price) {
        this.type = type;
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    void displayDetails() {
        System.out.println("Room Type: " + type);
        System.out.println("Beds: " + beds);
        System.out.println("Size: " + size + " sqm");
        System.out.println("Price: $" + price);
    }

    String getType() {
        return type;
    }
}

class SingleRoom extends Room {
    SingleRoom() {
        super("Single Room", 1, 20, 80);
    }
}

class DoubleRoom extends Room {
    DoubleRoom() {
        super("Double Room", 2, 35, 120);
    }
}

class SuiteRoom extends Room {
    SuiteRoom() {
        super("Suite Room", 3, 60, 250);
    }
}

class RoomInventory {

    private HashMap<String, Integer> availability;

    RoomInventory() {
        availability = new HashMap<>();
        availability.put("Single Room", 5);
        availability.put("Double Room", 3);
        availability.put("Suite Room", 2);
    }

    int getAvailability(String roomType) {
        return availability.getOrDefault(roomType, 0);
    }

    void decrementAvailability(String roomType) {
        int count = availability.getOrDefault(roomType, 0);
        if (count > 0) {
            availability.put(roomType, count - 1);
        }
    }
}

class Reservation {
    String guestName;
    String roomType;
    String reservationId;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }
}

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    void addRequest(Reservation reservation) {
        queue.add(reservation);
        System.out.println("Request added for " + reservation.guestName);
    }

    Reservation getNextRequest() {
        return queue.poll();
    }

    boolean hasRequests() {
        return !queue.isEmpty();
    }
}

class BookingService {

    private RoomInventory inventory;
    private HashMap<String, Set<String>> allocatedRooms = new HashMap<>();
    private int roomCounter = 1;

    BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    List<String> processBookings(BookingRequestQueue queue) {

        List<String> confirmedReservations = new ArrayList<>();

        while (queue.hasRequests()) {

            Reservation request = queue.getNextRequest();
            String roomType = request.roomType;

            int available = inventory.getAvailability(roomType);

            if (available > 0) {

                String roomId = roomType.replace(" ", "").toUpperCase() + "-" + roomCounter++;

                allocatedRooms.putIfAbsent(roomType, new HashSet<>());
                Set<String> roomSet = allocatedRooms.get(roomType);

                if (!roomSet.contains(roomId)) {

                    roomSet.add(roomId);
                    inventory.decrementAvailability(roomType);

                    request.reservationId = roomId;
                    confirmedReservations.add(roomId);

                    System.out.println("Reservation Confirmed");
                    System.out.println("Guest: " + request.guestName);
                    System.out.println("Room Type: " + roomType);
                    System.out.println("Reservation ID: " + roomId);
                    System.out.println();
                }
            }
        }

        return confirmedReservations;
    }
}

class AddOnService {

    String name;
    double price;

    AddOnService(String name, double price) {
        this.name = name;
        this.price = price;
    }
}

class AddOnServiceManager {

    private Map<String, List<AddOnService>> reservationServices = new HashMap<>();

    void addService(String reservationId, AddOnService service) {

        reservationServices.putIfAbsent(reservationId, new ArrayList<>());
        reservationServices.get(reservationId).add(service);

        System.out.println(service.name + " added to reservation " + reservationId);
    }

    double calculateServiceCost(String reservationId) {

        double total = 0;

        List<AddOnService> services = reservationServices.get(reservationId);

        if (services != null) {
            for (AddOnService s : services) {
                total += s.price;
            }
        }

        return total;
    }

    void displayServices(String reservationId) {

        List<AddOnService> services = reservationServices.get(reservationId);

        if (services == null) return;

        System.out.println("Services for Reservation " + reservationId);

        for (AddOnService s : services) {
            System.out.println("- " + s.name + " ($" + s.price + ")");
        }

        System.out.println("Total Add-On Cost: $" + calculateServiceCost(reservationId));
        System.out.println();
    }
}

public class Bookmystay {

    public static void main(String[] args) {

        System.out.println("Welcome to the Hotel Booking System\n");

        RoomInventory inventory = new RoomInventory();

        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));

        BookingService bookingService = new BookingService(inventory);

        List<String> reservations = bookingService.processBookings(queue);

        AddOnServiceManager serviceManager = new AddOnServiceManager();

        AddOnService breakfast = new AddOnService("Breakfast", 15);
        AddOnService spa = new AddOnService("Spa Access", 40);
        AddOnService airportPickup = new AddOnService("Airport Pickup", 30);

        String res1 = reservations.get(0);

        serviceManager.addService(res1, breakfast);
        serviceManager.addService(res1, spa);
        serviceManager.addService(res1, airportPickup);

        serviceManager.displayServices(res1);
    }
}