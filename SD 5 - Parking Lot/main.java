import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class ParkingLot {

    /******************************************************
     * ENUMS
     ******************************************************/

    enum VehicleType {
        CAR,
        BIKE,
        TRUCK
    }

    enum GateType {
        ENTRY,
        EXIT
    }

    enum PaymentStatus {
        PENDING,
        PAID
    }

    /******************************************************
     * VEHICLE
     ******************************************************/

    static class Vehicle {

        private final String vehicleNumber;
        private final VehicleType vehicleType;

        public Vehicle(String vehicleNumber, VehicleType vehicleType) {
            this.vehicleNumber = vehicleNumber;
            this.vehicleType = vehicleType;
        }

        public String getVehicleNumber() {
            return vehicleNumber;
        }

        public VehicleType getVehicleType() {
            return vehicleType;
        }

        @Override
        public String toString() {
            return vehicleType + " : " + vehicleNumber;
        }
    }

    /******************************************************
     * TICKET
     ******************************************************/

    static class Ticket {

        private final String ticketId;
        private final LocalDateTime entryTime;

        private final String floorId;
        private final String spotId;

        private final Vehicle vehicle;

        private PaymentStatus paymentStatus;

        public Ticket(String floorId,
                      String spotId,
                      Vehicle vehicle) {

            this.ticketId = UUID.randomUUID().toString();

            this.entryTime = LocalDateTime.now();

            this.floorId = floorId;
            this.spotId = spotId;
            this.vehicle = vehicle;

            this.paymentStatus = PaymentStatus.PENDING;
        }

        public String getTicketId() {
            return ticketId;
        }

        public LocalDateTime getEntryTime() {
            return entryTime;
        }

        public String getFloorId() {
            return floorId;
        }

        public String getSpotId() {
            return spotId;
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public PaymentStatus getPaymentStatus() {
            return paymentStatus;
        }

        public void markPaid() {
            paymentStatus = PaymentStatus.PAID;
        }

        @Override
        public String toString() {

            return "\n========== TICKET ==========\n" +
                    "Ticket Id     : " + ticketId +
                    "\nVehicle       : " + vehicle +
                    "\nFloor         : " + floorId +
                    "\nSpot          : " + spotId +
                    "\nEntry Time    : " + entryTime +
                    "\nPayment       : " + paymentStatus +
                    "\n============================";
        }
    }

    /******************************************************
     * PARKING SPOT
     ******************************************************/

    static class ParkingSpot {

        private final String spotId;

        private final VehicleType allowedVehicleType;

        private boolean occupied;

        public ParkingSpot(String spotId,
                           VehicleType allowedVehicleType) {

            this.spotId = spotId;
            this.allowedVehicleType = allowedVehicleType;
            this.occupied = false;
        }

        public String getSpotId() {
            return spotId;
        }

        public VehicleType getAllowedVehicleType() {
            return allowedVehicleType;
        }

        public boolean isOccupied() {
            return occupied;
        }

        public boolean canPark(VehicleType type) {
            return !occupied && allowedVehicleType == type;
        }

        public boolean occupy() {

            if (occupied)
                return false;

            occupied = true;
            return true;
        }

        public boolean vacate() {

            if (!occupied)
                return false;

            occupied = false;
            return true;
        }

        @Override
        public String toString() {

            return "Spot{" +
                    "id='" + spotId + '\'' +
                    ", type=" + allowedVehicleType +
                    ", occupied=" + occupied +
                    '}';
        }
    }

    /******************************************************
     * PARKING FLOOR
     ******************************************************/

    static class ParkingFloor {

        private final String floorId;

        private final Map<String, ParkingSpot> parkingSpots =
                new LinkedHashMap<>();

        public ParkingFloor(String floorId) {
            this.floorId = floorId;
        }

        public String getFloorId() {
            return floorId;
        }

        public void addSpot(ParkingSpot spot) {

            parkingSpots.put(
                    spot.getSpotId(),
                    spot
            );
        }

        public ParkingSpot findAvailableSpot(VehicleType type) {

            for (ParkingSpot spot : parkingSpots.values()) {

                if (spot.canPark(type)) {
                    return spot;
                }
            }

            return null;
        }

        public ParkingSpot getSpot(String spotId) {

            return parkingSpots.get(spotId);
        }

        public Collection<ParkingSpot> getAllSpots() {

            return parkingSpots.values();
        }

        public void displayAvailability() {

            System.out.println("\nFloor : " + floorId);

            for (ParkingSpot spot : parkingSpots.values()) {
                System.out.println(spot);
            }
        }
    }

    /******************************************************
     * PRICING STRATEGY
     ******************************************************/

    interface PriceStrategy {

        double calculatePrice(VehicleType vehicleType,
                              LocalDateTime entryTime,
                              LocalDateTime exitTime);
    }

    /******************************************************
     * HOURLY PRICING
     ******************************************************/

    static class HourlyPricing implements PriceStrategy {

        private static final Map<VehicleType, Double> PRICE_PER_HOUR =
                new HashMap<>();

        static {

            PRICE_PER_HOUR.put(VehicleType.BIKE, 20.0);
            PRICE_PER_HOUR.put(VehicleType.CAR, 50.0);
            PRICE_PER_HOUR.put(VehicleType.TRUCK, 100.0);
        }

        @Override
        public double calculatePrice(VehicleType vehicleType,
                                     LocalDateTime entryTime,
                                     LocalDateTime exitTime) {

            long hours = Duration
                    .between(entryTime, exitTime)
                    .toHours();

            if (hours == 0)
                hours = 1;

            return hours * PRICE_PER_HOUR.get(vehicleType);
        }
    }

    /******************************************************
     * MONTHLY PRICING
     ******************************************************/

    static class MonthlyPricing implements PriceStrategy {

        private static final Map<VehicleType, Double> MONTHLY_RATE =
                new HashMap<>();

        static {

            MONTHLY_RATE.put(VehicleType.BIKE, 1500.0);
            MONTHLY_RATE.put(VehicleType.CAR, 5000.0);
            MONTHLY_RATE.put(VehicleType.TRUCK, 9000.0);
        }

        @Override
        public double calculatePrice(VehicleType vehicleType,
                                     LocalDateTime entryTime,
                                     LocalDateTime exitTime) {

            return MONTHLY_RATE.get(vehicleType);
        }
    }

    /******************************************************
     * PRICE FACTORY
     ******************************************************/

    static class PriceFactory {

        private PriceFactory() {
        }

        public static PriceStrategy getPricingStrategy(boolean monthlyPass) {

            if (monthlyPass) {
                return new MonthlyPricing();
            }

            return new HourlyPricing();
        }
    }

    /******************************************************
     * GATE
     ******************************************************/

    static abstract class Gate {

        protected final String gateId;
        protected final GateType gateType;

        public Gate(String gateId,
                    GateType gateType) {

            this.gateId = gateId;
            this.gateType = gateType;
        }

        public String getGateId() {
            return gateId;
        }

        public GateType getGateType() {
            return gateType;
        }
    }

    /******************************************************
     * ENTRY GATE
     ******************************************************/

    static class EntryGate extends Gate {

        public EntryGate(String gateId) {
            super(gateId, GateType.ENTRY);
        }

        public Ticket parkVehicle(Vehicle vehicle) {

            System.out.println();
            System.out.println("Vehicle arrived at Entry Gate : " + gateId);

            return ParkingLot.getInstance()
                    .parkVehicle(vehicle);
        }
    }

    /******************************************************
     * EXIT GATE
     ******************************************************/

    static class ExitGate extends Gate {

        public ExitGate(String gateId) {
            super(gateId, GateType.EXIT);
        }

        public void unParkVehicle(String ticketId) {

            System.out.println();
            System.out.println("Vehicle arrived at Exit Gate : " + gateId);

            ParkingLot.getInstance()
                    .unParkVehicle(ticketId);
        }
    }

    /******************************************************
     * SINGLETON INSTANCE
     ******************************************************/

    private static ParkingLot instance;

    private final String parkingId;

    private final Map<String, ParkingFloor> floors;

    // ticketId -> Ticket
    private final Map<String, Ticket> activeTickets;

    // vehicleNumber -> ticketId
    private final Map<String, String> vehicleTicketMap;

    private PriceStrategy pricingStrategy;

    private ParkingLot() {

        parkingId = "PARKING-LOT-1";

        floors = new LinkedHashMap<>();

        activeTickets = new HashMap<>();

        vehicleTicketMap = new HashMap<>();

        pricingStrategy = PriceFactory.getPricingStrategy(false);
    }

    public static synchronized ParkingLot getInstance() {

        if (instance == null) {
            instance = new ParkingLot();
        }

        return instance;
    }

    /******************************************************
     * CONFIGURATION
     ******************************************************/

    public void addFloor(ParkingFloor floor) {
        floors.put(floor.getFloorId(), floor);
    }

    public void setPricingStrategy(PriceStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    /******************************************************
     * PARK VEHICLE
     ******************************************************/

    public Ticket parkVehicle(Vehicle vehicle) {

        if (vehicleTicketMap.containsKey(vehicle.getVehicleNumber())) {

            System.out.println(
                    "Vehicle already parked."
            );

            return null;
        }

        for (ParkingFloor floor : floors.values()) {

            ParkingSpot spot =
                    floor.findAvailableSpot(
                            vehicle.getVehicleType());

            if (spot != null) {

                spot.occupy();

                Ticket ticket =
                        new Ticket(
                                floor.getFloorId(),
                                spot.getSpotId(),
                                vehicle);

                activeTickets.put(
                        ticket.getTicketId(),
                        ticket);

                vehicleTicketMap.put(
                        vehicle.getVehicleNumber(),
                        ticket.getTicketId());

                System.out.println();

                System.out.println(
                        "Vehicle Parked Successfully");

                System.out.println(
                        "Floor : " + floor.getFloorId());

                System.out.println(
                        "Spot  : " + spot.getSpotId());

                System.out.println(ticket);

                return ticket;
            }
        }

        System.out.println();

        System.out.println(
                "Parking Full!!");

        return null;
    }

    /******************************************************
     * FIND FLOOR
     ******************************************************/

    private ParkingFloor findFloor(String floorId) {

        return floors.get(floorId);
    }

    /******************************************************
     * FIND TICKET
     ******************************************************/

    public Ticket getTicket(String ticketId) {

        return activeTickets.get(ticketId);
    }

    /******************************************************
     * UNPARK VEHICLE
     ******************************************************/

    public boolean unParkVehicle(String ticketId) {

        Ticket ticket = activeTickets.get(ticketId);

        if (ticket == null) {

            System.out.println("Invalid Ticket");

            return false;
        }

        ParkingFloor floor =
                findFloor(ticket.getFloorId());

        ParkingSpot spot =
                floor.getSpot(ticket.getSpotId());

        LocalDateTime exitTime =
                LocalDateTime.now();

        double amount =
                pricingStrategy.calculatePrice(
                        ticket.getVehicle().getVehicleType(),
                        ticket.getEntryTime(),
                        exitTime);

        System.out.println();

        System.out.println("========== EXIT ==========");

        System.out.println(
                "Vehicle : "
                        + ticket.getVehicle());

        System.out.println(
                "Entry Time : "
                        + ticket.getEntryTime());

        System.out.println(
                "Exit Time  : "
                        + exitTime);

        System.out.println(
                "Amount     : ₹"
                        + amount);

        ticket.markPaid();

        spot.vacate();

        activeTickets.remove(ticketId);

        vehicleTicketMap.remove(
                ticket.getVehicle()
                        .getVehicleNumber());

        System.out.println(
                "Payment Successful");

        System.out.println(
                "Spot Released");

        System.out.println("==========================");

        return true;
    }

    /******************************************************
     * DISPLAY STATUS
     ******************************************************/

    public void displayParkingStatus() {

        System.out.println();

        System.out.println(
                "========== PARKING STATUS ==========");

        for (ParkingFloor floor : floors.values()) {

            floor.displayAvailability();

            System.out.println();
        }
    }

    /******************************************************
     * DISPLAY ACTIVE TICKETS
     ******************************************************/

    public void displayActiveTickets() {

        System.out.println();

        System.out.println(
                "========== ACTIVE TICKETS ==========");

        if (activeTickets.isEmpty()) {

            System.out.println("No Active Tickets");

            return;
        }

        for (Ticket ticket : activeTickets.values()) {

            System.out.println(ticket);

            System.out.println();
        }
    }

    /******************************************************
     * MAIN
     ******************************************************/

    public static void main(String[] args) {

        ParkingLot parkingLot = ParkingLot.getInstance();

        /*
         * Create Floor 1
         */

        ParkingFloor floor1 = new ParkingFloor("FLOOR-1");

        floor1.addSpot(new ParkingSpot("B1", VehicleType.BIKE));
        floor1.addSpot(new ParkingSpot("B2", VehicleType.BIKE));

        floor1.addSpot(new ParkingSpot("C1", VehicleType.CAR));
        floor1.addSpot(new ParkingSpot("C2", VehicleType.CAR));
        floor1.addSpot(new ParkingSpot("C3", VehicleType.CAR));

        floor1.addSpot(new ParkingSpot("T1", VehicleType.TRUCK));

        /*
         * Create Floor 2
         */

        ParkingFloor floor2 = new ParkingFloor("FLOOR-2");

        floor2.addSpot(new ParkingSpot("B3", VehicleType.BIKE));
        floor2.addSpot(new ParkingSpot("B4", VehicleType.BIKE));

        floor2.addSpot(new ParkingSpot("C4", VehicleType.CAR));
        floor2.addSpot(new ParkingSpot("C5", VehicleType.CAR));

        floor2.addSpot(new ParkingSpot("T2", VehicleType.TRUCK));

        /*
         * Add Floors
         */

        parkingLot.addFloor(floor1);
        parkingLot.addFloor(floor2);

        /*
         * Gates
         */

        EntryGate entryGate = new EntryGate("ENTRY-1");
        ExitGate exitGate = new ExitGate("EXIT-1");

        /*
         * Vehicles
         */

        Vehicle car1 =
                new Vehicle("KA01AB1111",
                        VehicleType.CAR);

        Vehicle car2 =
                new Vehicle("KA01AB2222",
                        VehicleType.CAR);

        Vehicle bike1 =
                new Vehicle("KA02XY3333",
                        VehicleType.BIKE);

        Vehicle truck1 =
                new Vehicle("KA99TR9999",
                        VehicleType.TRUCK);

        /*
         * Park Vehicles
         */

        Ticket ticket1 =
                entryGate.parkVehicle(car1);

        Ticket ticket2 =
                entryGate.parkVehicle(car2);

        Ticket ticket3 =
                entryGate.parkVehicle(bike1);

        Ticket ticket4 =
                entryGate.parkVehicle(truck1);

        /*
         * Parking Status
         */

        parkingLot.displayParkingStatus();

        parkingLot.displayActiveTickets();

        /*
         * Wait for few seconds
         */

        try {

            Thread.sleep(3000);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        /*
         * Exit Vehicles
         */

        if (ticket1 != null) {
            exitGate.unParkVehicle(ticket1.getTicketId());
        }

        if (ticket3 != null) {
            exitGate.unParkVehicle(ticket3.getTicketId());
        }

        /*
         * Final Status
         */

        parkingLot.displayParkingStatus();

        parkingLot.displayActiveTickets();
    }
}


// Output:


Vehicle arrived at Entry Gate : ENTRY-1

Vehicle Parked Successfully
Floor : FLOOR-1
Spot  : C1

========== TICKET ==========
Ticket Id     : 1720aa36-fedf-4d36-bcb6-9f5276112a31
Vehicle       : CAR : KA01AB1111
Floor         : FLOOR-1
Spot          : C1
Entry Time    : 2026-07-23T09:35:41.009499716
Payment       : PENDING
============================

Vehicle arrived at Entry Gate : ENTRY-1

Vehicle Parked Successfully
Floor : FLOOR-1
Spot  : C2

========== TICKET ==========
Ticket Id     : c26c47aa-2f62-427e-8a5f-adbf1125b085
Vehicle       : CAR : KA01AB2222
Floor         : FLOOR-1
Spot          : C2
Entry Time    : 2026-07-23T09:35:41.109900126
Payment       : PENDING
============================

Vehicle arrived at Entry Gate : ENTRY-1

Vehicle Parked Successfully
Floor : FLOOR-1
Spot  : B1

========== TICKET ==========
Ticket Id     : 18a14fcb-59b5-4c68-a4a1-3ccd8182771b
Vehicle       : BIKE : KA02XY3333
Floor         : FLOOR-1
Spot          : B1
Entry Time    : 2026-07-23T09:35:41.111190096
Payment       : PENDING
============================

Vehicle arrived at Entry Gate : ENTRY-1

Vehicle Parked Successfully
Floor : FLOOR-1
Spot  : T1

========== TICKET ==========
Ticket Id     : 3d9dbc04-78fc-453a-ae93-e0af90ac7474
Vehicle       : TRUCK : KA99TR9999
Floor         : FLOOR-1
Spot          : T1
Entry Time    : 2026-07-23T09:35:41.111969116
Payment       : PENDING
============================

        ========== PARKING STATUS ==========

Floor : FLOOR-1
Spot{id='B1', type=BIKE, occupied=true}
Spot{id='B2', type=BIKE, occupied=false}
Spot{id='C1', type=CAR, occupied=true}
Spot{id='C2', type=CAR, occupied=true}
Spot{id='C3', type=CAR, occupied=false}
Spot{id='T1', type=TRUCK, occupied=true}


Floor : FLOOR-2
Spot{id='B3', type=BIKE, occupied=false}
Spot{id='B4', type=BIKE, occupied=false}
Spot{id='C4', type=CAR, occupied=false}
Spot{id='C5', type=CAR, occupied=false}
Spot{id='T2', type=TRUCK, occupied=false}


========== ACTIVE TICKETS ==========

        ========== TICKET ==========
Ticket Id     : c26c47aa-2f62-427e-8a5f-adbf1125b085
Vehicle       : CAR : KA01AB2222
Floor         : FLOOR-1
Spot          : C2
Entry Time    : 2026-07-23T09:35:41.109900126
Payment       : PENDING
============================


        ========== TICKET ==========
Ticket Id     : 18a14fcb-59b5-4c68-a4a1-3ccd8182771b
Vehicle       : BIKE : KA02XY3333
Floor         : FLOOR-1
Spot          : B1
Entry Time    : 2026-07-23T09:35:41.111190096
Payment       : PENDING
============================


        ========== TICKET ==========
Ticket Id     : 1720aa36-fedf-4d36-bcb6-9f5276112a31
Vehicle       : CAR : KA01AB1111
Floor         : FLOOR-1
Spot          : C1
Entry Time    : 2026-07-23T09:35:41.009499716
Payment       : PENDING
============================


        ========== TICKET ==========
Ticket Id     : 3d9dbc04-78fc-453a-ae93-e0af90ac7474
Vehicle       : TRUCK : KA99TR9999
Floor         : FLOOR-1
Spot          : T1
Entry Time    : 2026-07-23T09:35:41.111969116
Payment       : PENDING
============================


Vehicle arrived at Exit Gate : EXIT-1

        ========== EXIT ==========
Vehicle : CAR : KA01AB1111
Entry Time : 2026-07-23T09:35:41.009499716
Exit Time  : 2026-07-23T09:35:44.211192039
Amount     : ?50.0
Payment Successful
Spot Released
==========================

Vehicle arrived at Exit Gate : EXIT-1

        ========== EXIT ==========
Vehicle : BIKE : KA02XY3333
Entry Time : 2026-07-23T09:35:41.111190096
Exit Time  : 2026-07-23T09:35:44.214809590
Amount     : ?20.0
Payment Successful
Spot Released
==========================

        ========== PARKING STATUS ==========

Floor : FLOOR-1
Spot{id='B1', type=BIKE, occupied=false}
Spot{id='B2', type=BIKE, occupied=false}
Spot{id='C1', type=CAR, occupied=false}
Spot{id='C2', type=CAR, occupied=true}
Spot{id='C3', type=CAR, occupied=false}
Spot{id='T1', type=TRUCK, occupied=true}


Floor : FLOOR-2
Spot{id='B3', type=BIKE, occupied=false}
Spot{id='B4', type=BIKE, occupied=false}
Spot{id='C4', type=CAR, occupied=false}
Spot{id='C5', type=CAR, occupied=false}
Spot{id='T2', type=TRUCK, occupied=false}


========== ACTIVE TICKETS ==========

        ========== TICKET ==========
Ticket Id     : c26c47aa-2f62-427e-8a5f-adbf1125b085
Vehicle       : CAR : KA01AB2222
Floor         : FLOOR-1
Spot          : C2
Entry Time    : 2026-07-23T09:35:41.109900126
Payment       : PENDING
============================


        ========== TICKET ==========
Ticket Id     : 3d9dbc04-78fc-453a-ae93-e0af90ac7474
Vehicle       : TRUCK : KA99TR9999
Floor         : FLOOR-1
Spot          : T1
Entry Time    : 2026-07-23T09:35:41.111969116
Payment       : PENDING
============================


        === Code Execution Successful ===