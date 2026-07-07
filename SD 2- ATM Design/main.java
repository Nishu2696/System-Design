import java.util.*;

class ATMSystem {
    //=========================
    // ENUMS
    //=========================

    enum ATMState {
        IDLE,
        CARD_INSERTED,
        CARD_REMOVED,
        OUT_OF_SERVICE
    }

    enum TransactionType {
        WITHDRAW,
        BALANCE_ENQUIRY
    }

    //=========================
    // ACCOUNT related info
    //=========================

    static class Account {
        private String acc_no;
        private double balance;

        public Account(String acc_no, double balance) {
            this.acc_no = acc_no;
            this.balance = balance;
        }

//        getter
        public String getAcc_no() {
            return acc_no;
        }

        public double getBalance() {
            return balance;
        }

        public boolean debit(double amount) {
            if (balance < amount) {
                return false;
            }
            balance -= amount;
            return true;
        }
    }

    //=========================
    // CARD releated info
    //=========================

    static class Card {
        private String acc_no;
        private String card_no;
        private String pin;

        public Card(String acc_no, String pin, String card_no) {
            this.acc_no = acc_no;
            this.pin = pin;
            this.card_no = card_no;
        }

//        getters
        public String getAcc_no() {
            return acc_no;
        }

        public String getCard_no() {
            return card_no;
        }

        public boolean isValid() {
            return true;
        }

        public boolean verifyPin(String enteredPin) {
            return pin.equals(enteredPin);
        }
    }

    //=========================
    // BANK SERVER (Singleton)
    // Singleton classes will have private constructor, because its instance cant be created from outside
    //=========================

    static class BankServer {
        private static volatile BankServer instance;
        private Map<String, Account> accounts = new HashMap<>();
        private Map<String, Card> cards = new HashMap<>();

        private BankServer() {
            Account account = new Account("ACC1001", 5000);
            Card card = new Card("ACC1001", "1234", "CARD1001");

            accounts.put(account.getAcc_no(), account);
            cards.put(card.getCard_no(), card);
        }
    // Double Checked Locking
        public static BankServer getInstance() {
            if (instance == null) {
                synchronized (BankServer.class) {
                    if (instance == null) {
                        instance = new BankServer();
                    }
                }
            }
            return instance;
        }

        public Card getCard(String card_no) {
            return cards.get(card_no);
        }

        public boolean verifyPin(Card card, String pin) {
            return card.verifyPin(pin);
        }

        public double getBalance(String acc_no) {
            return accounts.get(acc_no).getBalance();
        }

        public boolean withdrawMoney (String acc_no, double amount, Card card) {
            Account account = accounts.get(acc_no);
            return account.debit(amount);
        }
    }

    //=========================
    // TRANSACTION
    //=========================

    static abstract class Transaction {
        protected String acc_no;
        protected double amount;
        protected TransactionType type;
        protected Date date;

        public Transaction (String acc_no, double amount, TransactionType type) {
            this.acc_no = acc_no;
            this.amount = amount;
            this.type = type;
            this.date = new Date();
        }

        abstract void execute();
    }

    //=========================
    // BALANCE ENQUIRY
    //=========================

    static class BalanceEnquiry extends Transaction {
        public BalanceEnquiry(String acc_no) {
            super(acc_no, 0, TransactionType.BALANCE_ENQUIRY);
        }

        @Override
        void execute() {
            double balance = BankServer.getInstance().getBalance(acc_no);
            System.out.println("Balance is " + balance);
        }
    }

    //=========================
    // BALANCE ENQUIRY
    //=========================

    static class Withdraw extends Transaction {
        private Card card;
        public Withdraw(String acc_no, double amount, Card card) {
            super(acc_no, amount, TransactionType.WITHDRAW);
            this.card = card;
        }

        @Override
        void execute() {
            boolean success = BankServer.getInstance().withdrawMoney(acc_no, amount, card);
            if (success) {
                System.out.println("Withdraw successful");
            } else {
                System.out.println("Withdraw failed");
            }

        }
    }

    //=========================
    // ATM (Singleton)
    //=========================

    static class ATM {
        private static ATM instance;
        private ATMState state =  ATMState.IDLE;
        private Card insertedCard;

        private ATM() {}

        public static ATM getInstance() {
            if (instance == null) {
                synchronized (ATM.class) {
                    if (instance == null) {
                        instance = new ATM();
                    }
                }
            }
            return instance;
        }

        public void insertCard(Card card) {
            if (card == null || !card.isValid()) {
                System.out.println("Invalid card");
                return;
            }
            insertedCard = card;
            state = ATMState.CARD_INSERTED;
            System.out.println("Card inserted");
        }

        public boolean authenticate(String pin) {
            if (state != ATMState.CARD_INSERTED) {
                return false;
            }

            boolean verified = BankServer.getInstance().verifyPin(insertedCard, pin);
            if (verified) {
                System.out.println("Authentication successful");
            } else {
                System.out.println("Authentication failed");
            }

            return verified;
        }

        public Card getInsertedCard() {
            return insertedCard;
        }

        public void ejectCard() {
            if (state != ATMState.CARD_INSERTED) {
                return;
            }
            insertedCard = null;
            state = ATMState.IDLE;
            System.out.println("Ejecting card");
        }
    }

    //=========================
    // MAIN
    //=========================

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        BankServer bank = BankServer.getInstance();
        ATM atm = ATM.getInstance();

        System.out.println("Welcome to the ATM system");
        System.out.print("Enter Card Number: ");
        String cardNo = scanner.next();
        Card card = bank.getCard(cardNo);

        if (card == null) {
            System.out.println("Invalid card");
            return;
        }

        atm.insertCard(card);
        System.out.println("Enter pin: ");
        String pin = scanner.next();

        if (!atm.authenticate(pin)) {
            System.out.println("Authentication failed");
            return;
        }

        while (true) {
            System.out.println("\n1. Balance Enquiry");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    Transaction balanceEnquiry = new BalanceEnquiry(card.getAcc_no());
                    balanceEnquiry.execute();
                    break;
                case 2:
                    System.out.println("Enter Amount to withdraw");
                    double amount = scanner.nextDouble();

                    Transaction withdraw =  new Withdraw(card.getAcc_no(), amount, card);
                    withdraw.execute();
                    break;
                case 3:
                    atm.ejectCard();
                    scanner.close();
                    System.out.println("Card removed");
                    return;
                 default:
                     System.out.println("Invalid choice");
            }
        }
    }
}


// Output of the above Code
Welcome to the ATM system
Enter Card Number: CARD1001
Card inserted
Enter pin:
        1234
Authentication successful

1. Balance Enquiry
2. Withdraw Money
3. Exit
Enter your choice: 1
Balance is 5000.0

        1. Balance Enquiry
2. Withdraw Money
3. Exit
Enter your choice: 2
Enter Amount to withdraw
1000
Withdraw successful

1. Balance Enquiry
2. Withdraw Money
3. Exit
Enter your choice: 1
Balance is 4000.0

        1. Balance Enquiry
2. Withdraw Money
3. Exit
Enter your choice: 3
Ejecting card
Card removed
