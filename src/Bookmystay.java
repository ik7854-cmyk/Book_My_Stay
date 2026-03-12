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
    }

    Reservation getNextRequest() {
        return queue.poll();
    }

    boolean hasRequests() {
        return !queue.isEmpty();
    }
}

class BookingHistory {

    private List<Reservation> confirmedBookings = new ArrayList<>();

    void addReservation(Reservation reservation) {
        confirmedBookings.add(reservation);
    }

    List<Reservation> getBookings() {
        return confirmedBookings;
    }
}

class BookingService {

    private RoomInventory inventory;
    private HashMap<String, Set<String>> allocatedRooms = new HashMap<>();
    private int roomCounter = 1;
    private BookingHistory history;

    BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    void processBookings(BookingRequestQueue queue) {

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

                    history.addReservation(request);

                    System.out.println("Reservation Confirmed");
                    System.out.println("Guest: " + request.guestName);
                    System.out.println("Room Type: " + roomType);
                    System.out.println("Reservation ID: " + roomId);
                    System.out.println();
                }
            }
        }
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
}

class BookingReportService {

    private BookingHistory history;

    BookingReportService(BookingHistory history) {
        this.history = history;
    }

    void displayAllBookings() {

        System.out.println("Booking History\n");

        for (Reservation r : history.getBookings()) {
            System.out.println("Reservation ID: " + r.reservationId);
            System.out.println("Guest: " + r.guestName);
            System.out.println("Room Type: " + r.roomType);
            System.out.println();
        }
    }

    void generateSummaryReport() {

        Map<String, Integer> summary = new HashMap<>();

        for (Reservation r : history.getBookings()) {
            summary.put(r.roomType, summary.getOrDefault(r.roomType, 0) + 1);
        }

        System.out.println("Booking Summary Report\n");

        for (String roomType : summary.keySet()) {
            System.out.println(roomType + " bookings: " + summary.get(roomType));
        }

        System.out.println();
    }
}

public class Bookmystay {

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();

        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));
        queue.addRequest(new Reservation("Charlie", "Suite Room"));

        BookingService bookingService = new BookingService(inventory, history);

        bookingService.processBookings(queue);

        BookingReportService reportService = new BookingReportService(history);

        reportService.displayAllBookings();

        reportService.generateSummaryReport();
    }
}