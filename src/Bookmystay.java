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

public class Bookmystay {

    public static void main(String[] args) {

        System.out.println("Welcome to the Hotel Booking System");
        System.out.println("Application: BookMyStay");
        System.out.println("Version: v1.0");
        System.out.println();

        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        int singleAvailability = 5;
        int doubleAvailability = 3;
        int suiteAvailability = 2;

        single.displayDetails();
        System.out.println("Available Rooms: " + singleAvailability);
        System.out.println();

        doubleRoom.displayDetails();
        System.out.println("Available Rooms: " + doubleAvailability);
        System.out.println();

        suite.displayDetails();
        System.out.println("Available Rooms: " + suiteAvailability);
    }
}