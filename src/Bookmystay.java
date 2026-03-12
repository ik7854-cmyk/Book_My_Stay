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

    int getAvailability(String roomType) { return availability.getOrDefault(roomType, 0); }

    void decrementAvailability(String roomType) throws InvalidBookingException {
        int count = availability.getOrDefault(roomType, -1);
        if (count <= 0) throw new InvalidBookingException("No rooms available for " + roomType);
        availability.put(roomType, count - 1);
    }

    void incrementAvailability(String roomType) {
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

class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();
    void addRequest(Reservation r){ queue.add(r); }
    Reservation getNextRequest(){ return queue.poll(); }
    boolean hasRequests(){ return !queue.isEmpty(); }
}

class BookingHistory {
    private List<Reservation> confirmedBookings = new ArrayList<>();
    void addReservation(Reservation r){ confirmedBookings.add(r); }
    void removeReservation(Reservation r){ confirmedBookings.remove(r); }
    List<Reservation> getBookings(){ return confirmedBookings; }
    boolean containsReservation(Reservation r){ return confirmedBookings.contains(r); }
}

class InvalidBookingException extends Exception {
    InvalidBookingException(String msg){ super(msg); }
}

class BookingService {

    private RoomInventory inventory;
    private HashMap<String, Set<String>> allocatedRooms = new HashMap<>();
    private int roomCounter = 1;
    private BookingHistory history;
    private Stack<String> rollbackStack = new Stack<>();

    BookingService(RoomInventory inventory, BookingHistory history){
        this.inventory = inventory;
        this.history = history;
    }

    void processBookings(BookingRequestQueue queue){

        while(queue.hasRequests()){
            Reservation request = queue.getNextRequest();
            try {
                validateRequest(request);

                String roomType = request.roomType;
                inventory.decrementAvailability(roomType);

                String roomId = roomType.replace(" ","").toUpperCase() + "-" + roomCounter++;
                allocatedRooms.putIfAbsent(roomType,new HashSet<>());
                Set<String> roomSet = allocatedRooms.get(roomType);
                if(roomSet.contains(roomId)) throw new InvalidBookingException("Room ID collision for "+roomId);

                roomSet.add(roomId);
                rollbackStack.push(roomId);
                request.reservationId = roomId;
                history.addReservation(request);

                System.out.println("Reservation Confirmed: " + request.guestName + ", Room ID: " + roomId);

            } catch(InvalidBookingException e){
                System.out.println("Booking Failed: " + e.getMessage());
            }
        }
    }

    void cancelBooking(String reservationId){

        Reservation target = null;

        for(Reservation r : history.getBookings()){
            if(r.reservationId.equals(reservationId)) {
                target = r;
                break;
            }
        }

        if(target == null){
            System.out.println("Cancellation Failed: Reservation " + reservationId + " not found.");
            return;
        }

        String roomType = target.roomType;
        if(!allocatedRooms.containsKey(roomType) || !allocatedRooms.get(roomType).contains(reservationId)){
            System.out.println("Cancellation Failed: Room ID not allocated.");
            return;
        }

        allocatedRooms.get(roomType).remove(reservationId);
        inventory.incrementAvailability(roomType);
        history.removeReservation(target);

        rollbackStack.remove(reservationId);

        System.out.println("Reservation Cancelled: " + target.guestName + ", Room ID: " + reservationId);
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
        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));
        queue.addRequest(new Reservation("Charlie", "Suite Room"));

        BookingService bookingService = new BookingService(inventory, history);
        bookingService.processBookings(queue);

        System.out.println("\nCurrent Inventory:");
        System.out.println("Single Room: " + inventory.getAvailability("Single Room"));
        System.out.println("Double Room: " + inventory.getAvailability("Double Room"));
        System.out.println("Suite Room: " + inventory.getAvailability("Suite Room") + "\n");

        // Cancel Alice's booking
        bookingService.cancelBooking("SINGLEROOM-1");

        System.out.println("\nInventory After Cancellation:");
        System.out.println("Single Room: " + inventory.getAvailability("Single Room"));
        System.out.println("Double Room: " + inventory.getAvailability("Double Room"));
        System.out.println("Suite Room: " + inventory.getAvailability("Suite Room") + "\n");

        // Attempt to cancel non-existent booking
        bookingService.cancelBooking("NONEXISTENT-99");
    }
}