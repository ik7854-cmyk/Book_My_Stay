import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

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

    void updateAvailability(String roomType, int count) {
        availability.put(roomType, count);
    }
}

class SearchService {

    private RoomInventory inventory;

    SearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    void searchRooms(Room[] rooms) {
        System.out.println("Available Rooms:");
        System.out.println();

        for (Room room : rooms) {
            int available = inventory.getAvailability(room.getType());

            if (available > 0) {
                room.displayDetails();
                System.out.println("Available Rooms: " + available);
                System.out.println();
            }
        }
    }
}

class Reservation {
    String guestName;
    String roomType;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    void displayReservation() {
        System.out.println("Guest: " + guestName + " requested " + roomType);
    }
}

class BookingRequestQueue {

    private Queue<Reservation> queue;

    BookingRequestQueue() {
        queue = new LinkedList<>();
    }

    void addRequest(Reservation reservation) {
        queue.add(reservation);
        System.out.println("Request added to queue for " + reservation.guestName);
    }

    void displayQueue() {
        System.out.println("\nCurrent Booking Request Queue:");
        for (Reservation r : queue) {
            r.displayReservation();
        }
    }
}

public class Bookmystay {

    public static void main(String[] args) {

        System.out.println("Welcome to the Hotel Booking System");
        System.out.println("Application: BookMyStay");
        System.out.println("Version: v1.0\n");

        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        Room[] rooms = {single, doubleRoom, suite};

        RoomInventory inventory = new RoomInventory();
        SearchService searchService = new SearchService(inventory);

        searchService.searchRooms(rooms);

        BookingRequestQueue requestQueue = new BookingRequestQueue();

        Reservation r1 = new Reservation("Alice", "Single Room");
        Reservation r2 = new Reservation("Bob", "Double Room");
        Reservation r3 = new Reservation("Charlie", "Suite Room");

        requestQueue.addRequest(r1);
        requestQueue.addRequest(r2);
        requestQueue.addRequest(r3);

        requestQueue.displayQueue();
    }
}