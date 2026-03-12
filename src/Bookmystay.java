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

    String getType() { return type; }
}

class SingleRoom extends Room { SingleRoom() { super("Single Room",1,20,80); } }
class DoubleRoom extends Room { DoubleRoom() { super("Double Room",2,35,120); } }
class SuiteRoom extends Room { SuiteRoom() { super("Suite Room",3,60,250); } }

class RoomInventory {
    private HashMap<String, Integer> availability = new HashMap<>();

    RoomInventory() {
        availability.put("Single Room", 5);
        availability.put("Double Room", 3);
        availability.put("Suite Room", 2);
    }

    synchronized int getAvailability(String roomType) {
        return availability.getOrDefault(roomType, 0);
    }

    synchronized void decrementAvailability(String roomType) throws InvalidBookingException {
        int count = availability.getOrDefault(roomType, -1);
        if (count <= 0) throw new InvalidBookingException("No rooms available for " + roomType);
        availability.put(roomType, count - 1);
    }

    synchronized void incrementAvailability(String roomType) {
        int count = availability.getOrDefault(roomType, 0);
        availability.put(roomType, count + 1);
    }

    boolean isValidRoomType(String roomType) { return availability.containsKey(roomType); }
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

class BookingHistory {
    private List<Reservation> confirmedBookings = new ArrayList<>();

    synchronized void addReservation(Reservation r){ confirmedBookings.add(r); }
    synchronized void removeReservation(Reservation r){ confirmedBookings.remove(r); }
    synchronized List<Reservation> getBookings(){ return new ArrayList<>(confirmedBookings); }
}

class InvalidBookingException extends Exception {
    InvalidBookingException(String msg){ super(msg); }
}

class BookingService {

    private RoomInventory inventory;
    private HashMap<String, Set<String>> allocatedRooms = new HashMap<>();
    private int roomCounter = 1;
    private BookingHistory history;

    BookingService(RoomInventory inventory, BookingHistory history){
        this.inventory = inventory;
        this.history = history;
    }

    void processBooking(Reservation request){
        try {
            validateRequest(request);

            synchronized(this){
                inventory.decrementAvailability(request.roomType);

                String roomId = request.roomType.replace(" ","").toUpperCase() + "-" + roomCounter++;

                allocatedRooms.putIfAbsent(request.roomType, new HashSet<>());
                Set<String> roomSet = allocatedRooms.get(request.roomType);

                if(roomSet.contains(roomId))
                    throw new InvalidBookingException("Room ID collision for " + roomId);

                roomSet.add(roomId);
                request.reservationId = roomId;
                history.addReservation(request);

                System.out.println("Reservation Confirmed: " + request.guestName + ", Room ID: " + roomId);
            }

        } catch(InvalidBookingException e){
            System.out.println("Booking Failed: " + e.getMessage() + " (" + request.guestName + ")");
        }
    }

    private void validateRequest(Reservation request) throws InvalidBookingException{
        if(request.guestName == null || request.guestName.isEmpty())
            throw new InvalidBookingException("Guest name is required.");
        if(!inventory.isValidRoomType(request.roomType))
            throw new InvalidBookingException("Invalid room type: " + request.roomType);
    }
}

public class Bookmystay {

    public static void main(String[] args){

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        BookingService bookingService = new BookingService(inventory, history);

        List<Thread> guestThreads = new ArrayList<>();

        // Simulate multiple concurrent guests
        guestThreads.add(new Thread(() -> bookingService.processBooking(new Reservation("Alice","Single Room"))));
        guestThreads.add(new Thread(() -> bookingService.processBooking(new Reservation("Bob","Single Room"))));
        guestThreads.add(new Thread(() -> bookingService.processBooking(new Reservation("Charlie","Suite Room"))));
        guestThreads.add(new Thread(() -> bookingService.processBooking(new Reservation("Diana","Double Room"))));
        guestThreads.add(new Thread(() -> bookingService.processBooking(new Reservation("Eve","Suite Room"))));

        // Start all threads
        guestThreads.forEach(Thread::start);

        // Wait for all threads to finish
        for(Thread t : guestThreads){
            try { t.join(); } catch(InterruptedException e){ e.printStackTrace(); }
        }

        System.out.println("\nFinal Inventory:");
        System.out.println("Single Room: " + inventory.getAvailability("Single Room"));
        System.out.println("Double Room: " + inventory.getAvailability("Double Room"));
        System.out.println("Suite Room: " + inventory.getAvailability("Suite Room"));

        System.out.println("\nConfirmed Reservations:");
        for(Reservation r : history.getBookings()){
            System.out.println(r.guestName + " - " + r.roomType + " - " + r.reservationId);
        }
    }
}