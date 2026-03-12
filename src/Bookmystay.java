import java.util.HashMap;

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

    void displayInventory() {
        for (String type : availability.keySet()) {
            System.out.println(type + " Available: " + availability.get(type));
        }
    }
}

public class Bookmystay {

    public static void main(String[] args) {

        System.out.println("Welcome to the Hotel Booking System");
        System.out.println("Application: BookMyStay");
        System.out.println("Version: v1.0");
        System.out.println();

        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        RoomInventory inventory = new RoomInventory();

        single.displayDetails();
        System.out.println("Available Rooms: " + inventory.getAvailability(single.getType()));
        System.out.println();

        doubleRoom.displayDetails();
        System.out.println("Available Rooms: " + inventory.getAvailability(doubleRoom.getType()));
        System.out.println();

        suite.displayDetails();
        System.out.println("Available Rooms: " + inventory.getAvailability(suite.getType()));
        System.out.println();

        System.out.println("Current Inventory:");
        inventory.displayInventory();
    }
}