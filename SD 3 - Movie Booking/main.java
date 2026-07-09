import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MovieBookingSystem {

    // =========================================================
    // ENUMS
    // =========================================================

    enum BookingStatus {
        SUCCESSFUl,
        FAILURE
    }

    enum PaymentStatus {
        SUCCESS,
        FAILURE,
        PROCESSING,
        PENDING,
    }

    enum SeatStatus {
        AVALIABLE,
        RESERVED,
        BOOKED
    }

    // =========================================================
    // MOVIE
    // =========================================================

    static class Movie {
        private String movieId;
        private String movieTitle;
        private String movieDuration;

        public Movie(String movieId, String movieTitle, String movieDuration) {
            this.movieId = movieId;
            this.movieTitle = movieTitle;
            this.movieDuration = movieDuration;
        }

//        getter
        public String getMovieId() {
            return movieId;
        }

        public String getMovieTitle() {
            return movieTitle;
        }

        public String getMovieDuration() {
            return movieDuration;
        }

        @Override
        public String toString() {
            return "Movie(" + "movieId=" + movieId +  ", movieTitle=" + movieTitle + ", movieDuration=" + movieDuration + ")";
        }
    }

    // =========================================================
    // THEATRE
    // =========================================================

    static class Theatre {
        private String theatreId;
        private String theatreName;
        private Map<String, Screen> theatreScreens = new HashMap<>();
        public Theatre(String theatreId, String theatreName) {
            this.theatreId = theatreId;
            this.theatreName = theatreName;
        }

//        getter
        public String getTheatreId() {
            return theatreId;
        }

        public String getTheatreName() {
            return theatreName;
        }

        public void addScreen(Screen screen) {
            theatreScreens.put(screen.getScreenId(), screen);
        }

        public Screen getScreen(String screenId) {
            return theatreScreens.get(screenId);
        }
    }

    // =========================================================
    // SCREEN
    // =========================================================

    static class Screen {
        private String screenId;
        private Map<String, Seat> screenSeats = new HashMap<>();

        public Screen(String screenId) {
            this.screenId = screenId;
        }

//        getter
        public String getScreenId() {
            return screenId;
        }

        public void addSeat(Seat seat) {
            screenSeats.put(seat.getSeatId(), seat);
        }

        public Seat getSeat(String seatId) {
            return screenSeats.get(seatId);
        }

        public Collection<Seat> getAllSeats() {
            return screenSeats.values();
        }
    }

    // =========================================================
    // SEAT
    // =========================================================

    static class Seat {
        private String seatId;
        private double seatPrice;
        private SeatStatus seatStatus;

        public Seat(String seatId, double seatPrice) {
            this.seatId = seatId;
            this.seatPrice = seatPrice;
            this.seatStatus = SeatStatus.AVALIABLE;
        }

//        getter
        public String getSeatId() {
            return seatId;
        }
        public double getSeatPrice() {
            return seatPrice;
        }
        public SeatStatus getSeatStatus() {
            return seatStatus;
        }

        public void reserveSeat() {
            seatStatus = SeatStatus.RESERVED;
        }

        public void bookSeat() {
            seatStatus = SeatStatus.BOOKED;
        }

        public void releaseSeat() {
            seatStatus = SeatStatus.AVALIABLE;
        }

        @Override
        public String toString() {
            return "Seat(" + "seatId=" + seatId + ", seatPrice=" + seatPrice  + ", seatStatus=" + seatStatus + ")";
        }
    }

    // =========================================================
    // SHOW DETAILS
    // =========================================================

    static class ShowDetails {
        private String id;
        private Movie movie;
        private long startTIme;
        private long endTIme;
        private Theatre theatre;
        private Screen screen;

        public ShowDetails (String id, Movie movie, long startTIme, long endTIme, Theatre theatre, Screen screen) {
            this.id = id;
            this.movie = movie;
            this.startTIme = startTIme;
            this.endTIme = endTIme;
            this.theatre = theatre;
            this.screen = screen;
        }

//        getter
        public String getId() {
            return id;
        }
        public Movie getMovie() {
            return movie;
        }
        public long getStartTIme() {
            return startTIme;
        }
        public long getEndTIme() {
            return endTIme;
        }
        public Theatre getTheatre() {
            return theatre;
        }
        public Screen getScreen() {
            return screen;
        }

        @Override
        public String toString() {
            return "SHowDetails{"
                    + "id= " + id
                    + ", movie = " + movie.getMovieTitle()
                    + ", theatre = " + theatre.getTheatreName()
                    + ", screen = " + screen.getScreenId() +
                    '}';
        }
    }

    // =========================================================
    // BOOKING
    // =========================================================

    static class Booking {
        private String bookingId;
        private String userId;
        private String showId;

        private List<Seat> seats;
        private BookingStatus bookingStatus;
        private PaymentStatus paymentStatus;

        private double amount;

//        constructor
        public Booking (String bookingId, String userId, String showId, List<Seat> seats, double amount) {
            this.bookingId = bookingId;
            this.userId = userId;
            this.showId = showId;
            this.seats = seats;
            this.amount = amount;

            this.bookingStatus = BookingStatus.FAILURE;
            this.paymentStatus = PaymentStatus.PROCESSING;
        }

//        getter
        public String getBookingId() {
            return bookingId;
        }
        public String getUserId() {
            return userId;
        }
        public String getShowId() {
            return showId;
        }
        public List<Seat> getSeats() {
            return seats;
        }
        public BookingStatus getBookingStatus() {
            return bookingStatus;
        }
        public PaymentStatus getPaymentStatus() {
            return paymentStatus;
        }
        public double getAmount() {
            return amount;
        }

//        setters

        public void setBookingStatus(BookingStatus bookingStatus) {
            this.bookingStatus = bookingStatus;
        }

        public void setPaymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        @Override
        public String toString() {
            return "Booking{"
                    + "bookingId = " +  bookingId
                    + ", userId = " +  userId
                    + ", showId = " +  showId
                    + ", seats = " +  seats
                    + ", bookingStatus = " +  bookingStatus
                    + ", paymentStatus = " +  paymentStatus +
                    "}";
        }
    }

    // =========================================================
    // LOCK PROVIDER
    // =========================================================

    static class LockProvider {
        private static class LockInfo {
            String userId;
            long expiryTime;

            LockInfo(String userId, long expiryTime) {
                this.userId = userId;
                this.expiryTime = expiryTime;
            }
        }

        private final Map<String, LockInfo> locks = new ConcurrentHashMap<>();
        public synchronized boolean tryLock(String key, String userId, long ttlMillis) {
            long currentTime = System.currentTimeMillis();
            LockInfo existingLock = locks.get(key);

            if (existingLock == null || currentTime > existingLock.expiryTime) {
                locks.put(
                        key, new LockInfo(userId, ttlMillis)
                );
                return true;
            }

            return false;
        }
        public synchronized void unlock(String key) {
            locks.remove(key);
        }
        public synchronized boolean isExpired(String key) {
            LockInfo lock = locks.get(key);

            if (lock == null) return true;

            long currentTime = System.currentTimeMillis();

            return currentTime > lock.expiryTime;
        }
    }

    // =========================================================
    // BOOKING SERVICE
    // =========================================================

    static class BookingService {
        private LockProvider lockProvider;
        private Map<String, Booking> bookingDatabase = new HashMap<>();

        public BookingService(LockProvider lockProvider) {
            this.lockProvider = lockProvider;
        }

        public Booking createBooking (String userId, ShowDetails showDetails, List<String> seatIds) {
            List<Seat> bookedSeats = new ArrayList<>();
            double totalAmount = 0;

//           Lock every seat

            for (String seatid: seatIds) {
                Seat seat = showDetails.getScreen().getSeat(seatid);
                if (seat == null) {
                    System.out.println("Seat " + seatid + " not found");
                    releaseSeats(bookedSeats);
                    return null;
                }
                if (seat.getSeatStatus() != SeatStatus.AVALIABLE) {
                    System.out.println("Seat " + seat.getSeatId() + " not available");
                    releaseSeats(bookedSeats);
                    return null;
                }

                boolean locked = lockProvider.tryLock(seatid, userId, 30000);

                if (!locked) {
                    System.out.println("Seat " + seat.getSeatId() + " is already locked by another user");
                    releaseSeats(bookedSeats);
                    return null;
                }

                seat.reserveSeat();
                bookedSeats.add(seat);
                totalAmount = totalAmount + seat.getSeatPrice();
            }

            Booking booking = new  Booking(
                    UUID.randomUUID().toString(),
                    userId,
                    showDetails.getId(),
                    bookedSeats,
                    totalAmount
            );

            bookingDatabase.put(booking.getBookingId(), booking);

            System.out.println("---------------------------------");
            System.out.println("Booking Created");
            System.out.println("----------------------------------");
            System.out.println("Booking Id: " +  booking.getBookingId());
            System.out.println("Booking User: " +  booking.getUserId());
            System.out.println("Booking Show Id: " +  booking.getShowId());
            System.out.println("Booking amount: " + totalAmount);
            return booking;
        }

        public boolean confirmBooking (Booking booking, PaymentStatus paymentStatus) {
            booking.setPaymentStatus(paymentStatus);

            if (paymentStatus == PaymentStatus.SUCCESS) {
                for (Seat seat: booking.getSeats()) {
                    seat.bookSeat();
                    lockProvider.unlock(seat.getSeatId());
                }

                booking.setBookingStatus(BookingStatus.SUCCESSFUl);

                System.out.println("-----------------------------------");
                System.out.println("Booking Confirmed");
                System.out.println("---------------------------------");

                return true;
            }

            for  (Seat seat: booking.getSeats()) {
                seat.releaseSeat();
                lockProvider.unlock(seat.getSeatId());
            }

            booking.setBookingStatus(BookingStatus.FAILURE);

            System.out.println("--------------------------------------");
            System.out.println(PaymentStatus.FAILURE);
            System.out.println("Seats Released");
            System.out.println("------------------------------------");

            return false;
        }

        private void releaseSeats(List<Seat> seats) {
            for (Seat seat: seats) {
                seat.releaseSeat();
                lockProvider.unlock(seat.getSeatId());
            }
        }

        public void printBooking (String bookingId) {
            Booking booking = bookingDatabase.get(bookingId);

            if (booking == null) {
                System.out.println("Booking not found");
                return;
            }

            System.out.println(booking.getBookingId());
            System.out.println("Seats: ");

            for (Seat seat: booking.getSeats()) {
                System.out.println(seat.getSeatId() + " -> " + seat.getSeatStatus());
            }
        }

        public void printSeats(ShowDetails showDetails) {
            System.out.println("Current seat status");

            for (Seat seat: showDetails.getScreen().getAllSeats()) {
                System.out.println(seat.getSeatId() + " -> " + seat.getSeatStatus());
            }
        }
    }


    public static void main(String[] args) {
        // ----------------------------
        // Create Movie
        // ----------------------------

        Movie movie = new Movie(
                "M101", "Interstellar", "2h 49m"
        );

        // ----------------------------
        // Create Theatre
        // ----------------------------

        Theatre theatre = new Theatre(
                "T101", "PVR Cinemas"
        );

        // ----------------------------
        // Create Screen
        // ----------------------------

        Screen screen = new Screen("SCREEN - 1");

        // ----------------------------
        // Add Seats
        // ----------------------------

        for (int i = 1; i <= 10; i++) {
            screen.addSeat(new Seat(
                    "A" + i, 250
            ));
        }

        theatre.addScreen(screen);

        // ----------------------------
        // Create Show
        // ----------------------------

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (3 * 60 * 60 * 1000);

        ShowDetails showDetails = new ShowDetails(
                "SHOW-101",
                movie,
                startTime,
                endTime,
                theatre,
                screen
        );

        // ----------------------------
        // Create Services
        // ----------------------------

        LockProvider lockProvider = new LockProvider();
        BookingService bookingService = new BookingService(lockProvider);

        // ----------------------------
        // Display Movie Details
        // ----------------------------

        System.out.println("\n==================MOVIE=============");
        System.out.println(movie);

        System.out.println("\n===================THEATRE=============");
        System.out.println(theatre);

        System.out.println("\n====================SHOW================");
        System.out.println(showDetails);

        // ----------------------------
        // Display Available Seats
        // ----------------------------

        bookingService.printSeats(showDetails);

        // ----------------------------
        // User Selects Seats
        // ----------------------------

        List<String> selectedSeats = Arrays.asList("A1", "A2", "A3", "A4", "A5");

        System.out.println("\n User Selected Seats : " + selectedSeats);

        // ----------------------------
        // Create Booking
        // ----------------------------

        Booking booking = bookingService.createBooking(
                "USER-101",
                showDetails,
                selectedSeats
        );

        if (booking == null) {
            System.out.println("Booking failed");
            return;
        }

        // ----------------------------
        // Simulate Payment
        // ----------------------------

        PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

        bookingService.confirmBooking(booking, paymentStatus);

        // ----------------------------
        // Print Booking Details
        // ----------------------------

        System.out.println("\n======================BOOKING DETAILS=====================");
        bookingService.printBooking(booking.getBookingId());

        // ----------------------------
        // Display Seat Status
        // ----------------------------

        System.out.println("\n========================FINAL SEAT STATUS===================");
        bookingService.printSeats(showDetails);

        // ----------------------------
        // Try Booking an Already Booked Seat
        // ----------------------------

        System.out.println("\n=========================SECOND BOOKING=======================");

        Booking secondBooking = bookingService.createBooking("USER-202", showDetails, Arrays.asList("A1", "A4"));

        if (secondBooking == null) {
            System.out.println("Booking failed, because A1 & A4 is already booked");
        }

        // ----------------------------
        // Book Remaining Seats
        // ----------------------------

        System.out.println("\n========================== THIRD BOOKING========================");

        Booking thirdBooking =  bookingService.createBooking("USER-303", showDetails, Arrays.asList("A6", "A7"));

        if (thirdBooking == null) {
            System.out.println("Booking failed, because A6 is already booked");
        }

        bookingService.confirmBooking(thirdBooking, paymentStatus);
        bookingService.printBooking(thirdBooking.getBookingId());

        // ----------------------------
        // Final Seat Status
        // ----------------------------

        System.out.println("\n=================ALL SEATS==================");
        bookingService.printSeats(showDetails);
        System.out.println("\n Movie Booking System End");
    }
}

// OUTPUT:


==================MOVIE=============
Movie(movieId=M101, movieTitle=Interstellar, movieDuration=2h 49m)

===================THEATRE=============
MovieBookingSystem$Theatre@47c81abf

====================SHOW================
SHowDetails{id= SHOW-101, movie = Interstellar, theatre = PVR Cinemas, screen = SCREEN - 1}
Current seat status
A1 -> AVALIABLE
A10 -> AVALIABLE
A2 -> AVALIABLE
A3 -> AVALIABLE
A4 -> AVALIABLE
A5 -> AVALIABLE
A6 -> AVALIABLE
A7 -> AVALIABLE
A8 -> AVALIABLE
A9 -> AVALIABLE

User Selected Seats : [A1, A2, A3, A4, A5]
---------------------------------
Booking Created
----------------------------------
Booking Id: 613e8413-a5b4-4922-a663-421f8081ea08
Booking User: USER-101
Booking Show Id: SHOW-101
Booking amount: 1250.0
        -----------------------------------
Booking Confirmed
---------------------------------

======================BOOKING DETAILS=====================
613e8413-a5b4-4922-a663-421f8081ea08
Seats:
A1 -> BOOKED
A2 -> BOOKED
A3 -> BOOKED
A4 -> BOOKED
A5 -> BOOKED

========================FINAL SEAT STATUS===================
Current seat status
A1 -> BOOKED
A10 -> AVALIABLE
A2 -> BOOKED
A3 -> BOOKED
A4 -> BOOKED
A5 -> BOOKED
A6 -> AVALIABLE
A7 -> AVALIABLE
A8 -> AVALIABLE
A9 -> AVALIABLE

=========================SECOND BOOKING=======================
Seat A1 not available
Booking failed, because A1 & A4 is already booked

========================== THIRD BOOKING========================
---------------------------------
Booking Created
----------------------------------
Booking Id: afa101dc-04f7-4ff2-a7d1-45a3bbd566e1
Booking User: USER-303
Booking Show Id: SHOW-101
Booking amount: 500.0
-----------------------------------
Booking Confirmed
---------------------------------
afa101dc-04f7-4ff2-a7d1-45a3bbd566e1
Seats:
A6 -> BOOKED
A7 -> BOOKED

=================ALL SEATS==================
Current seat status
A1 -> BOOKED
A10 -> AVALIABLE
A2 -> BOOKED
A3 -> BOOKED
A4 -> BOOKED
A5 -> BOOKED
A6 -> BOOKED
A7 -> BOOKED
A8 -> AVALIABLE
A9 -> AVALIABLE

Movie Booking System End

=== Code Execution Successful ===