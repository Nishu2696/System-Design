/*
 ===========================================================
                    SPLITWISE LLD
             Single File Java Implementation

 Design Patterns Used
 --------------------
 1. Singleton
 2. Strategy
 3. Factory

 Features
 --------
 ✓ Create Users
 ✓ Create Groups
 ✓ Add Members
 ✓ Add Expenses
 ✓ Equal Split
 ✓ Percentage Split
 ✓ Balance Sheet
 ✓ Debt Simplification (Min Cash Flow)
 ✓ Interactive CLI

 Remaining Parts
 ----------------
 Part-2 : Group & Balance Sheet
 Part-3 : GroupService (Singleton)
 Part-4 : Debt Simplifier
 Part-5 : Main Driver + Test Cases

 ===========================================================
*/

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Splitwise {
    // =========================================================
    // ENUMS
    // =========================================================

    enum SplitType {
        EQUAL,
        PERCENTAGE
    }

    // =========================================================
    // USER
    // =========================================================

    static class User {
        private final String id;
        private final String name;

        public User(String id, String name) {
            this.id = id;
            this.name = name;
        }

//        getter
        public String getId() {
            return id;
        }
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "User{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof User)) return false;
            User user = (User) o;
            if (!id.equals(user.id)) return false;
            return name.equals(user.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    // =========================================================
    // SPLIT
    // =========================================================

    static class Split {
        private User user;
        private double amount;
        private double percentage;

        public Split (User user) {
            this.user = user;
        }

        public Split(User user, double amount) {
            this.user = user;
            this.amount = amount;
        }

        public Split(User user, double amount, double percentage) {
            this.user = user;
            this.amount = amount;
            this.percentage = percentage;
        }

        public User getUser() {
            return user;
        }

        public double getAmount() {
            return amount;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }

        @Override
        public String toString() {
            return "Split{" + "amount=" + amount + ", percentage=" + percentage + '}';
        }
    }

    // =========================================================
    // EXPENSE
    // =========================================================

    static class Expense {
        private final String expenseId;
        private String expenseName;
        private double expenseAmount;
        private User paidBy;
        private SplitType splitType;
        private List<Split> splits;

        public Expense (String expenseName, double expenseAmount, User paidBy, SplitType splitType, List<Split> splits) {
            this.expenseId = UUID.randomUUID().toString();
            this.expenseName = expenseName;
            this.expenseAmount = expenseAmount;
            this.paidBy = paidBy;
            this.splitType = splitType;
            this.splits = splits;
        }

//        getter
        public String getExpenseId() {
            return expenseId;
        }
        public String getExpenseName() {
            return expenseName;
        }
        public double getExpenseAmount() {
            return expenseAmount;
        }
        public User getPaidBy() {
            return paidBy;
        }
        public SplitType getSplitType() {
            return splitType;
        }
        public List<Split> getSplits() {
            return splits;
        }
        @Override
        public String toString() {
            return "Expense{" + "expenseName='"  + expenseName + '\'' + ", expenseAmount=" + expenseAmount + ", paidBy=" + paidBy + ", splitType=" + splitType + '}';
        }
    }

    // =========================================================
    // BALANCE SHEET
    // =========================================================

    static class BalanceSheet {
        private double totalPaid;
        private double totalOwes;
        private double netBalance;

        /*
            Example

            Alice paid

            Bob -> 500
            Charlie -> 300

            balanceMap

            Bob -> 500
            Charlie -> 300
         */

        private final Map<User, Double> balanceMap;
        public BalanceSheet() {
            this.balanceMap = new ConcurrentHashMap<>();
        }

//        getter
        public double getTotalPaid() {
            return totalPaid;
        }
        public double getTotalOwes() {
            return totalOwes;
        }
        public double getNetBalance() {
            return netBalance;
        }

        public void addPaid(double amount) {
            totalPaid += amount;
        }
        public void addOwes(double amount) {
            totalOwes += amount;
        }
        public void addNetBalance(double amount) {
            netBalance += amount;
        }
        public Map<User, Double> getBalanceMap() {
            return balanceMap;
        }
        public void addBalance(User user, double amount) {
            balanceMap.put(user, balanceMap.getOrDefault(user, 0.0) + amount);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("===================================\n");
            sb.append("BalanceSheet{\n");
            sb.append("totalPaid=").append(totalPaid).append("\n");
            sb.append("totalOwes=").append(totalOwes).append("\n");
            sb.append("netBalance=").append(netBalance).append("\n");

            if (!balanceMap.isEmpty()) {
                sb.append("\nTransactions:\n");
                for (Map.Entry<User, Double> entry : balanceMap.entrySet()) {
                    sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                sb.append("\n==========================\n");
                return sb.toString();
            }
        }
    }

    // =========================================================
    // GROUP
    // =========================================================

    static class Group {
        private final String groupId;
        private final String groupName;

        private final List<User> members;
        private final List<Expense> expenses;

        /*
            One BalanceSheet per user.

            Alice  -> BalanceSheet
            Bob    -> BalanceSheet
            Charlie-> BalanceSheet
         */

        private final Map<User, BalanceSheet> balanceSheets;

        public Group (String groupName) {
            this.groupId = UUID.randomUUID().toString();
            this.groupName = groupName;

            this.members = new ArrayList<>();
            this.expenses = new ArrayList<>();
            this.balanceSheets = new ConcurrentHashMap<>();
        }

//        getter
        public String getGroupId() {
            return groupId;
        }
        public String getGroupName() {
            return groupName;
        }
        public List<User> getMembers() {
            return members;
        }
        public List<Expense> getExpenses() {
            return expenses;
        }
        public Map<User, BalanceSheet> getBalanceSheets() {
            return balanceSheets;
        }

        public void addMember(User member) {
            if (!members.contains(member)) {
                members.add(member);
                balanceSheets.put(member, new BalanceSheet());
            }
        }

        public void addExpense(Expense expense) {
            expenses.add(expense);
        }

        @Override
        public String toString() {
            return "Group{" + "groupName='" + groupName + '\'' + ", members=" + members +  ", expenses=" + expenses + ", balanceSheets=" + balanceSheets + '}';
        }
    }

    // =========================================================
    // STRATEGY
    // =========================================================

    interface SplitStrategy {

        void calculateSplits(Expense expense);
    }

    // =========================================================
    // EQUAL SPLIT STRATEGY
    // =========================================================

    static class EqualSplitStrategy implements SplitStrategy {
        @Override
        public void calculateSplits(Expense expense) {
            List<Split> splits = expense.getSplits();
            if (splits == null || splits.isEmpty()) {
                throw new IllegalArgumentException("splits is empty");
            }

            double totalAmount = expense.getExpenseAmount();
            double share = totalAmount / splits.size();

//            Round to 2 decimal places
            share = Math.round(share * 100.0) / 100.0;

            double assigned = 0;
            for (int i = 0; i < splits.size(); i++) {
                if (i == splits.size() - 1) {
//                    Last user gets remaining amount
                    splits.get(i).setAmount(Math.round((totalAmount - assigned) * 100.0) / 100.0);
                } else {
                    splits.get(i).setAmount(share);
                    assigned = assigned + share;
                }
            }
        }
    }

    // =========================================================
    // PERCENTAGE SPLIT STRATEGY
    // =========================================================


    static class PercentageSplitStrategy implements SplitStrategy {
        @Override
        public void calculateSplits(Expense expense) {
            List<Split> splits = expense.getSplits();
            if (splits == null || splits.isEmpty()) {
                throw new IllegalArgumentException("splits is empty");
            }

            double totalAmount = expense.getExpenseAmount();

            for (Split split : splits) {
                double amount = (totalAmount * split.getPercentage()) / 100.0;
                amount =  Math.round(amount * 100.0) / 100.0;
                split.setAmount(amount);
            }
        }
    }

    // =========================================================
    // VALIDATOR
    // =========================================================

    static class Validator {
        private SplitValidator () {}
        public static void validate(Expense expense) {
            if (expense == null) {
                throw new IllegalArgumentException("expense is null");
            }

            if (expense.getExpenseAmount() <= 0) {
                throw new IllegalArgumentException("expense amount is negative");
            }

            if (expense.getPaidBy() == null) {
                throw new IllegalArgumentException("paidBy is null");
            }

            if (expense.getSplitType() == null || expense.getSplitType().isEmpty()) {
                throw new IllegalArgumentException("splitType is null");
            }

            switch (expense.getSplitType()) {
                case EQUAL:
                    validateEqual(expense);
                    break;
                case PERCENTAGE:
                    validatePercentage(expense);
                    break;
                default:
                    throw new IllegalArgumentException("expense type is null");
            }
        }

        private static void validateEqual(Expense expense) {
            if (expense == null) {
                throw new IllegalArgumentException("expense is null");
            }

            if (expense.getSplits().isEmpty()) {
                throw new IllegalArgumentException("splits is empty");
            }
        }
        private static void validatePercentage(Expense expense) {
            if (expense == null) {
                throw new IllegalArgumentException("expense is null");
            }
            double totalPercentage = 0;
            for (Split split:  expense.getSplits()) {
                totalPercentage += split.getPercentage();
            }

            if (Math.abs(totalPercentage - 100.0) > 0.001) {
                throw new IllegalArgumentException("percentage is out of range");
            }
        }
    }

    // =========================================================
    // STRATEGY FACTORY
    // =========================================================

    static class SplitStrategyFactory {
        private static final Map<SplitType, SplitStrategy> splitStrategyMap = new HashMap<>();
        static {
            splitStrategyMap.put(SplitType.EQUAL, new EqualSplitStrategy());
            splitStrategyMap.put(SplitType.PERCENTAGE, new PercentageSplitStrategy());
        }

        public static SplitStrategy getSplitStrategy(SplitType splitType) {
            SplitStrategy splitStrategy = splitStrategyMap.get(splitType);
            if (splitStrategy == null) {
                throw new IllegalArgumentException("splitStrategy is null");
            }
            return splitStrategy;
        }
    }

    // =========================================================
    // SETTLEMENT
    // =========================================================

    static class Settlement {
        private final User from;
        private final User to;
        private final double amount;
        public Settlement(User from, User to, double amount) {
            this.from = from;
            this.to = to;
            this.amount = amount;
        }

        public User getFrom() {
            return from;
        }
        public User getTo() {
            return to;
        }
        public double getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return "Settlement{" + "from=" + from + ", to=" + to + ", amount=" + amount + '}';
        }
    }

    // =========================================================
    // BALANCE NODE
    // =========================================================

    static class BalanceNode {
        private final User user;
        private double amount;

        public BalanceNode(User user, double amount) {
            this.user = user;
            this.amount = amount;
        }

        public User getUser() {
            return user;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        @Override
        public String toString() {
            return "BalanceNode{" + "user=" + user + ", amount=" + amount + '}';
        }
    }

    // =========================================================
    // DEBT SIMPLIFIER
    // =========================================================

    static class DebtSimplifier {
        private DebtSimplifier () {}
        public static List<Settlement> simplify (Group group) {
            List<Settlement> settlements = new ArrayList<>();
            PriorityQueue<BalanceNode> creditors = new PriorityQueue<>(
                    (a, b) -> Double.compare(a.getAmount(), b.getAmount())
            );
            PriorityQueue<BalanceNode> debtors = new PriorityQueue<>(
                    (a, b) -> Double.compare(a.getAmount(), b.getAmount())
            );

            for (Map.Entry<User, BalanceSheet> entry: group.getBalanceSheets().entrySet()) {
                User user = entry.getKey();
                BalanceSheet balanceSheet = entry.getValue();
                double balance = Math.round(balanceSheet.getNetBalance() * 100.0) / 100.0;

                if (balance > 0.01) {
                    creditors.offer(new BalanceNode(user, balance));
                } else if (balance < -0.01) {
                    debtors.offer(new BalanceNode(user, Math.abs(balance)));
                }
            }

            while (!creditors.isEmpty() && !debtors.isEmpty()) {
                BalanceNode creditor = creditors.poll();
                BalanceNode debtor = debtors.poll();

                double amount = Math.min(creditor.getAmount(), debtor.getAmount());
                amount = round(amount);

                settlements.add(new Settlement(creditor.getUser(), debtor.getUser(), amount));
                creditor.setAmount(creditor.getAmount() - amount);
                debtor.setAmount(round(debtor.getAmount() - amount));

                if (creditor.getAmount() > 0.01) {
                    creditors.offer(creditor);
                }

                if (debtor.getAmount() > 0.01) {
                    debtors.offer(debtor);
                }
            }

            return settlements;
        }

        // =====================================================
        // PRINT
        // =====================================================

        public static void printSettlements(List<Settlement> settlements) {

            System.out.println("settlements:");
            if (settlements.isEmpty()) {
                System.out.println("no settlements");
                return;
            }

            for (Settlement settlement: settlements) {
                System.out.println("\t" + settlement);
            }

            System.out.println("====================================");
        }

        // =====================================================
        // ROUND OFF
        // =====================================================

        private static double round(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }

    // =========================================================
    // GROUP SERVICE (Singleton)
    // =========================================================

    static class GroupService {
        private static final GroupService groupService = new GroupService();
        private final Map<String, Group> groups = new ConcurrentHashMap<>();

        private GroupService() {}

        public static GroupService getInstance() {
            return groupService;
        }

        // =====================================================
        // CREATE GROUP
        // =====================================================

        public Group createGroup(String groupName) {
            Group group = groups.get(groupName);
            if (group == null) {
                group = new Group(groupName);
            }
            groups.put(groupName, group);
            return group;
        }

        // =====================================================
        // ADD MEMBER
        // =====================================================

        public void addMember(String group, User user) {
            if (group == null) {
                throw new IllegalArgumentException("group is null");
            }

            if (user == null) {
                throw new IllegalArgumentException("user is null");
            }

            group.addMember(user);
        }

        // =====================================================
        // ADD EXPENSE
        // =====================================================

        public void addExpense (Group group, Expense expense) {
            SplitValidator.validate(expense);
            SplitStrategy strategy = SplitStrategy.getStrategy(expense.getSplitType());

            strategy.calculateSplits(expense);
            group.addExpense(expense);
            updateBalanceSheet(group, expense);

            System.out.println();
            System.out.println("Expense Added: " + expense.getExpenseName());
        }

        // =====================================================
        // UPDATE BALANCE SHEET
        // =====================================================

        private void updateBalanceSheet(Group group, Expense expense) {
            User paidBy = expense.getPaidBy();
            BalanceSheet payerSheet = group.getBalanceSheets().get(paidBy);
            payerSheet.addPaid(expense.getExpenseAmount());

            for (Split split: expense.getSplits()) {
                User borrower = split.getUser();
                double amount = split.getAmount();

                if (borrower.equals(paidBy)) {
                    continue;
                }

                BalanceSheet borrowerSheet = group.getBalanceSheets().get(borrower);
                borrowerSheet.addOwes(amount);
                borrowerSheet.addNetBalance(-amount);

                payerSheet.addNetBalance(-amount);
                payerSheet.addBalance(borrower, amount);
            }
        }

        // =====================================================
        // PRINT GROUP
        // =====================================================

        public void printGroup (Group group) {
            System.out.println("group:");
            System.out.println(group.getGroupName());
            System.out.println("===============================");

            for (User user: group.getMembers()) {
                System.out.println(user.getName());
            }
        }

        // =====================================================
        // PRINT USER BALANCE
        // =====================================================

        public void printBalance(User user, Group group) {
            BalanceSheet balanceSheet = group.getBalanceSheets().get(user);

            System.out.println();
            System.out.println("===================================");
            System.out.println(user.getName());
            System.out.println("====================================");

            System.out.println("Paid: %.2f%n", balanceSheet.getTotalPaid());
            System.out.println("Owes: %.2f%n", balanceSheet.getTotalOwes());
            System.out.println("Net: %.2f%n", balanceSheet.getNetBalance());

            if (balanceSheet.getBalanceMap().isEmpty()) {
                System.out.println("No one owes this user");
            } else {
                System.out.println();
                System.out.println("Receivable from");

                for (Map.Entry<User, Double> e: balanceSheet.getBalanceMap().entrySet()) {
                    System.out.println(e.getKey().getName() + ": " + e.getValue());
                }
            }
        }

        // =====================================================
        // PRINT ALL BALANCES
        // =====================================================

        public void printAllBalances(Group group) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("All balances:");
            System.out.println("=========================================");

            for (User user: group.getMembers()) {
                printBalance(user, group);
            }
        }

        // =====================================================
        // GROUP SUMMARY
        // =====================================================

        public void printGroupSummary(Group group) {
            System.out.println();
            System.out.println("===========================================");
            System.out.println("Group: " + group.getGroupName());
            System.out.println("=========================================");

            for (User user: group.getMembers()) {
                double balance = group.getBalanceSheets().get(user).getNetBalance();

                if (balance > 0) {
                    System.out.println("%s should receive %.2f%n", user.getName(), balance);
                } else if (balance < 0) {
                    System.out.println("%s should receive %.2f%n", user.getName(), Math.abs(balance));
                } else {
                    System.out.println("%s is SETTLED%n", user.getName());
                }
            }
        }

        // =====================================================
        // SIMPLIFY
        // =====================================================

        public void simplify(Group group) {

            List<Settlement> settlements =
                    DebtSimplifier.simplify(group);

            DebtSimplifier.printSettlements(
                    settlements
            );
        }

        // =====================================================
        // GETTERS
        // =====================================================

        public Group getGroup(String groupId) {
            return groups.get(groupId);
        }

        public Collection<Group> getAllGroups() {
            return groups.values();
        }
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        GroupService service = GroupService.getInstance();

        // =====================================================
        // CREATE USERS
        // =====================================================

        User alice = new User("U1", "Alice");
        User bob = new User("U2", "Bob");
        User charlie = new User("U3", "Charlie");
        User david = new User("U4", "David");

        // =====================================================
        // CREATE GROUP
        // =====================================================

        Group group = service.createGroup("Goa Trip");

        service.addMember(group, alice);
        service.addMember(group, bob);
        service.addMember(group, charlie);
        service.addMember(group, david);

        service.printGroup(group);

        // =====================================================
        // EXPENSE 1
        // Dinner
        // Alice Paid
        // Equal Split
        // =====================================================

        List<Split> dinnerSplits = new ArrayList<>();

        dinnerSplits.add(new Split(alice));
        dinnerSplits.add(new Split(bob));
        dinnerSplits.add(new Split(charlie));
        dinnerSplits.add(new Split(david));

        Expense dinner = new Expense(
                "Dinner",
                1200,
                alice,
                SplitType.EQUAL,
                dinnerSplits
        );

        service.addExpense(group, dinner);

        // =====================================================
        // EXPENSE 2
        // Hotel
        // Bob Paid
        // 40 / 30 / 20 / 10
        // =====================================================

        List<Split> hotelSplits = new ArrayList<>();

        Split s1 = new Split(alice);
        s1.setPercentage(40);

        Split s2 = new Split(bob);
        s2.setPercentage(30);

        Split s3 = new Split(charlie);
        s3.setPercentage(20);

        Split s4 = new Split(david);
        s4.setPercentage(10);

        hotelSplits.add(s1);
        hotelSplits.add(s2);
        hotelSplits.add(s3);
        hotelSplits.add(s4);

        Expense hotel = new Expense(
                "Hotel",
                6000,
                bob,
                SplitType.PERCENTAGE,
                hotelSplits
        );

        service.addExpense(group, hotel);

        // =====================================================
        // EXPENSE 3
        // Taxi
        // Charlie Paid
        // Equal Split
        // =====================================================

        List<Split> taxiSplits = new ArrayList<>();

        taxiSplits.add(new Split(alice));
        taxiSplits.add(new Split(bob));
        taxiSplits.add(new Split(charlie));
        taxiSplits.add(new Split(david));

        Expense taxi = new Expense(
                "Taxi",
                800,
                charlie,
                SplitType.EQUAL,
                taxiSplits
        );

        service.addExpense(group, taxi);

        // =====================================================
        // EXPENSE 4
        // Shopping
        // David Paid
        // Equal Split
        // =====================================================

        List<Split> shoppingSplits = new ArrayList<>();

        shoppingSplits.add(new Split(alice));
        shoppingSplits.add(new Split(bob));
        shoppingSplits.add(new Split(charlie));
        shoppingSplits.add(new Split(david));

        Expense shopping = new Expense(
                "Shopping",
                2000,
                david,
                SplitType.EQUAL,
                shoppingSplits
        );

        service.addExpense(group, shopping);

        // =====================================================
        // PRINT DETAILS
        // =====================================================

        service.printAllBalances(group);

        service.printGroupSummary(group);

        // =====================================================
        // SIMPLIFY DEBT
        // =====================================================

        service.simplify(group);

        // =====================================================
        // ALL EXPENSES
        // =====================================================

        System.out.println();
        System.out.println("========================================");
        System.out.println("ALL EXPENSES");
        System.out.println("========================================");

        AtomicInteger counter = new AtomicInteger(1);

        group.getExpenses().forEach(expense -> {

            System.out.println();

            System.out.println(counter.getAndIncrement() + ". "
                    + expense.getExpenseName());

            System.out.println("Amount : "
                    + expense.getExpenseAmount());

            System.out.println("Paid By : "
                    + expense.getPaidBy().getName());

            System.out.println("Split Type : "
                    + expense.getSplitType());

            System.out.println("Participants:");

            for (Split split : expense.getSplits()) {

                System.out.printf(
                        "   %-10s -> %.2f%n",
                        split.getUser().getName(),
                        split.getAmount()
                );
            }
        });

        System.out.println();
        System.out.println("========================================");
        System.out.println("Splitwise Demo Completed Successfully");
        System.out.println("========================================");
    }
}
}