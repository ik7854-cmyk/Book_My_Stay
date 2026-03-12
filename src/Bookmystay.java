import java.io.*;
import java.util.*;

// Room domain classes
abstract class Room implements Serializable {
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

// Inventory class
class RoomInventory implements Serializable {
    private HashMap<String, Integer> availability = new HashMap<>();

    RoomInventory() {
        availability.put("Single Room", 5);
        availability.put("Double Room", 3);
        availability.put("Suite Room", 2);
    }

    int getAvailability(String roomType) { return availability.getOrDefault(roomType,0); }

    void decrementAvailability(String roomType) throws InvalidBookingException {
        int count = availability.getOrDefault(roomType, -1);
        if(count <= 0) throw new InvalidBookingException("No rooms available for " + roomType);
        availability.put(roomType, count-1);
    }

    void incrementAvailability(String roomType){
        int count = availability.getOrDefault(roomType,0);
        availability.put(roomType,count+1);
    }

    boolean isValidRoomType(String roomType) { return availability.containsKey(roomType); }

    HashMap<String,Integer> getSnapshot() { return new HashMap<>(availability); }
    void restoreSnapshot(HashMap<String,Integer> snapshot) { availability = snapshot; }
}

// Reservation class
class Reservation implements Serializable {
    String guestName;
    String roomType;
    String reservationId;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }
}

// Booking history
class BookingHistory implements Serializable {
    private List<Reservation> confirmedBookings = new ArrayList<>();
    synchronized void addReservation(Reservation r){ confirmedBookings.add(r); }
    synchronized void removeReservation(Reservation r){ confirmedBookings.remove(r); }
    synchronized List<Reservation> getBookings(){ return new ArrayList<>(confirmedBookings); }
}

// Custom exception
class InvalidBookingException extends Exception {
    InvalidBookingException(String msg){ super(msg); }
}

// Booking service
class BookingService {

    private RoomInventory inventory;
    private HashMap<String, Set<String>> allocatedRooms = new HashMap<>();
    private int roomCounter = 1;
    private BookingHistory history;

    BookingService(RoomInventory inventory, BookingHistory history){
        this.inventory = inventory;
        this.history = history;
    }

    synchronized void processBooking(Reservation request){
        try {
            validateRequest(request);

            inventory.decrementAvailability(request.roomType);

            String roomId = request.roomType.replace(" ","").toUpperCase() + "-" + roomCounter++;
            allocatedRooms.putIfAbsent(request.roomType,new HashSet<>());
            Set<String> roomSet = allocatedRooms.get(request.roomType);

            if(roomSet.contains(roomId)) throw new InvalidBookingException("Room ID collision for "+roomId);

            roomSet.add(roomId);
            request.reservationId = roomId;
            history.addReservation(request);

            System.out.println("Reservation Confirmed: " + request.guestName + ", Room ID: " + roomId);

        } catch(InvalidBookingException e){
            System.out.println("Booking Failed: " + e.getMessage());
        }
    }

    private void validateRequest(Reservation request) throws InvalidBookingException{
        if(request.guestName == null || request.guestName.isEmpty())
            throw new InvalidBookingException("Guest name is required.");
        if(!inventory.isValidRoomType(request.roomType))
            throw new InvalidBookingException("Invalid room type: " + request.roomType);
    }
}

// Persistence manager
class PersistenceService {

    private static final String FILE_NAME = "bookmystay_state.dat";

    static void saveState(RoomInventory inventory, BookingHistory history){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))){
            oos.writeObject(inventory.getSnapshot());
            oos.writeObject(history.getBookings());
            System.out.println("System state saved successfully.");
        } catch(IOException e){
            System.out.println("Failed to save system state: " + e.getMessage());
        }
    }

    static void restoreState(RoomInventory inventory, BookingHistory history){
        File file = new File(FILE_NAME);
        if(!file.exists()){
            System.out.println("No saved state found. Starting fresh.");
            return;
        }

        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))){
            HashMap<String,Integer> invSnapshot = (HashMap<String,Integer>) ois.readObject();
            List<Reservation> bookings = (List<Reservation>) ois.readObject();
            inventory.restoreSnapshot(invSnapshot);
            for(Reservation r : bookings) history.addReservation(r);
            System.out.println("System state restored successfully.");
        } catch(Exception e){
            System.out.println("Failed to restore state: " + e.getMessage());
        }
    }
}

// Main application
public class Bookmystay {

    public static void main(String[] args){

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();

        // Restore persisted state if available
        PersistenceService.restoreState(inventory, history);

        BookingService bookingService = new BookingService(inventory, history);

        // Sample bookings
        bookingService.processBooking(new Reservation("Alice","Single Room"));
        bookingService.processBooking(new Reservation("Bob","Double Room"));

        // Show current inventory
        System.out.println("\nCurrent Inventory:");
        System.out.println("Single Room: " + inventory.getAvailability("Single Room"));
        System.out.println("Double Room: " + inventory.getAvailability("Double Room"));
        System.out.println("Suite Room: " + inventory.getAvailability("Suite Room"));

        // Save state before exit
        PersistenceService.saveState(inventory, history);
    }
}