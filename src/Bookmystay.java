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
    SingleRoom() { super("Single Room", 1, 20, 80); }
}

class DoubleRoom extends Room {
    DoubleRoom() { super("Double Room", 2, 35, 120); }
}

class SuiteRoom extends Room {
    SuiteRoom() { super("Suite Room", 3, 60, 250); }
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

    void decrementAvailability(String roomType) throws InvalidBookingException {
        int count = availability.getOrDefault(roomType, -1);
        if (count <= 0) {
            throw new InvalidBookingException("Cannot allocate " + roomType + ": No rooms available.");
        }
        availability.put(roomType, count - 1);
    }

    boolean isValidRoomType(String roomType) {
        return availability.containsKey(roomType);
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

            try {
                validateRequest(request);

                String roomType = request.roomType;
                inventory.decrementAvailability(roomType);

                String roomId = roomType.replace(" ", "").toUpperCase() + "-" + roomCounter++;

                allocatedRooms.putIfAbsent(roomType, new HashSet<>());
                Set<String> roomSet = allocatedRooms.get(roomType);

                if (roomSet.contains(roomId)) {
                    throw new InvalidBookingException("Room ID collision for " + roomId);
                }

                roomSet.add(roomId);
                request.reservationId = roomId;
                history.addReservation(request);

                System.out.println("Reservation Confirmed");
                System.out.println("Guest: " + request.guestName);
                System.out.println("Room Type: " + roomType);
                System.out.println("Reservation ID: " + roomId);
                System.out.println();

            } catch (InvalidBookingException e) {
                System.out.println("Booking Failed: " + e.getMessage() + "\n");
            }
        }
    }

    private void validateRequest(Reservation request) throws InvalidBookingException {
        if (request.guestName == null || request.guestName.isEmpty()) {
            throw new InvalidBookingException("Guest name is required.");
        }

        if (!inventory.isValidRoomType(request.roomType)) {
            throw new InvalidBookingException("Invalid room type: " + request.roomType);
        }
    }
}

class InvalidBookingException extends Exception {
    InvalidBookingException(String message) { super(message); }
}

public class Bookmystay {

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        BookingRequestQueue queue = new BookingRequestQueue();

        // Valid request
        queue.addRequest(new Reservation("Alice", "Single Room"));
        // Invalid room type
        queue.addRequest(new Reservation("Bob", "Penthouse"));
        // Empty guest name
        queue.addRequest(new Reservation("", "Double Room"));
        // Valid request
        queue.addRequest(new Reservation("Charlie", "Suite Room"));

        BookingService bookingService = new BookingService(inventory, history);
        bookingService.processBookings(queue);

        System.out.println("Booking history contains " + history.getBookings().size() + " confirmed reservations.");
    }
}