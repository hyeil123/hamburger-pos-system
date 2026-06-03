import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/*
 * ============================================================
 * Hamburger Store POS System
 * 햄버거 가게 POS 시스템
 * ============================================================
 * This program is a console-based POS system for a hamburger store.
 * 이 프로그램은 햄버거 가게를 위한 콘솔 기반 POS 시스템입니다.
 *
 * Main features:
 * 주요 기능:
 * - Menu management / 메뉴 관리
 * - Table-based ordering / 테이블별 주문 관리
 * - Receipt printing / 영수증 출력
 * - Card and cash payment / 카드 및 현금 결제
 * - Split payment / 분할 결제
 * - Daily and date-based sales report / 오늘 매출 및 날짜별 매출 조회
 * - CSV file save/load / CSV 파일 저장 및 불러오기
 * ============================================================
 */

// Interface for printable objects.
// 출력 가능한 객체를 위한 인터페이스입니다.
interface Printable {
    void printInfo();
}

// Interface for payable objects.
// 결제 금액 계산이 가능한 객체를 위한 인터페이스입니다.
interface Payable {
    int getTotalPrice();
}

/*
 * ============================================================
 * MenuItem Class
 * MenuItem 클래스
 * ------------------------------------------------------------
 * Abstract parent class for all menu items.
 * 모든 메뉴 항목의 공통 부모 추상 클래스입니다.
 * ============================================================
 */
abstract class MenuItem implements Printable {
    protected int id;
    protected String name;
    protected int price;

    public MenuItem(int id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }

    // Returns the category name of the menu item.
    // 메뉴 항목의 카테고리 이름을 반환합니다.
    public abstract String getCategory();

    @Override
    public void printInfo() {
        System.out.printf("%-5d %-20s %-15s %,8d KRW\n",
                id, name, getCategory(), price);
    }
}

/*
 * ============================================================
 * Burger Class
 * Burger 클래스
 * ------------------------------------------------------------
 * First-level child class of MenuItem.
 * MenuItem을 상속받는 1단계 자식 클래스입니다.
 * ============================================================
 */
class Burger extends MenuItem {
    protected String pattyType;

    public Burger(int id, String name, int price, String pattyType) {
        super(id, name, price);
        this.pattyType = pattyType;
    }

    @Override
    public String getCategory() {
        return "Burger";
    }
}

/*
 * ============================================================
 * SingleBurger Class
 * SingleBurger 클래스
 * ------------------------------------------------------------
 * Second-level child class for single burger menu items.
 * 단품 버거 메뉴를 표현하는 2단계 자식 클래스입니다.
 * ============================================================
 */
class SingleBurger extends Burger {
    public SingleBurger(int id, String name, int price, String pattyType) {
        super(id, name, price, pattyType);
    }

    @Override
    public String getCategory() {
        return "Single Burger";
    }
}

/*
 * ============================================================
 * SetBurger Class
 * SetBurger 클래스
 * ------------------------------------------------------------
 * Second-level child class for set burger menu items.
 * 세트 버거 메뉴를 표현하는 2단계 자식 클래스입니다.
 * ============================================================
 */
class SetBurger extends Burger {
    private boolean hasFries;
    private boolean hasDrink;

    public SetBurger(int id, String name, int price,
                     String pattyType,
                     boolean hasFries,
                     boolean hasDrink) {
        super(id, name, price, pattyType);
        this.hasFries = hasFries;
        this.hasDrink = hasDrink;
    }

    @Override
    public String getCategory() {
        return "Set Burger";
    }

    // Prints set menu details.
    // 세트 메뉴 구성 정보를 출력합니다.
    public void printSetInfo() {
        System.out.println("Includes fries: " + hasFries);
        System.out.println("Includes drink: " + hasDrink);
    }
}

/*
 * ============================================================
 * Side Class
 * Side 클래스
 * ------------------------------------------------------------
 * Represents side menu items.
 * 사이드 메뉴를 표현하는 클래스입니다.
 * ============================================================
 */
class Side extends MenuItem {
    public Side(int id, String name, int price) {
        super(id, name, price);
    }

    @Override
    public String getCategory() {
        return "Side";
    }
}

/*
 * ============================================================
 * Drink Class
 * Drink 클래스
 * ------------------------------------------------------------
 * Represents drink menu items.
 * 음료 메뉴를 표현하는 클래스입니다.
 * ============================================================
 */
class Drink extends MenuItem {
    public Drink(int id, String name, int price) {
        super(id, name, price);
    }

    @Override
    public String getCategory() {
        return "Drink";
    }
}

/*
 * ============================================================
 * OrderItem Class
 * OrderItem 클래스
 * ------------------------------------------------------------
 * Stores one selected menu item and its quantity.
 * 선택된 메뉴 하나와 수량을 저장하는 클래스입니다.
 * ============================================================
 */
class OrderItem {
    private MenuItem menuItem;
    private int quantity;

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }

    // Calculates subtotal price for this order item.
    // 해당 주문 항목의 소계 금액을 계산합니다.
    public int getSubtotal() {
        return menuItem.getPrice() * quantity;
    }

    // Prints one receipt line.
    // 영수증 한 줄을 출력합니다.
    public void printLine() {
        System.out.printf("%-20s x %-3d %,8d KRW\n",
                menuItem.getName(), quantity, getSubtotal());
    }
}

/*
 * ============================================================
 * Order Class
 * Order 클래스
 * ------------------------------------------------------------
 * Manages one full order for a table.
 * 한 테이블의 전체 주문 정보를 관리하는 클래스입니다.
 * ============================================================
 */
class Order implements Payable, Printable {
    private int orderId;
    private int tableNumber;
    private ArrayList<OrderItem> items;
    private boolean paid;
    private LocalDateTime orderTime;

    public Order(int orderId, int tableNumber) {
        this.orderId = orderId;
        this.tableNumber = tableNumber;
        this.items = new ArrayList<>();
        this.paid = false;
        this.orderTime = LocalDateTime.now();
    }

    public int getOrderId() { return orderId; }
    public int getTableNumber() { return tableNumber; }
    public boolean isPaid() { return paid; }
    public ArrayList<OrderItem> getItems() { return items; }

    // Adds a menu item to this order.
    // 주문에 메뉴 항목을 추가합니다.
    public void addItem(MenuItem item, int quantity) {
        items.add(new OrderItem(item, quantity));
    }

    // Marks this order as paid.
    // 주문을 결제 완료 상태로 변경합니다.
    public void pay() {
        paid = true;
    }

    @Override
    public int getTotalPrice() {
        int total = 0;

        for (OrderItem item : items) {
            total += item.getSubtotal();
        }

        return total;
    }

    @Override
    public void printInfo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        System.out.println("\n========== RECEIPT ==========");
        System.out.println("Order ID: " + orderId);
        System.out.println("Table Number: " + tableNumber);
        System.out.println("Order Time: " + orderTime.format(formatter));
        System.out.println("-----------------------------");

        for (OrderItem item : items) {
            item.printLine();
        }

        System.out.println("-----------------------------");
        System.out.printf("Total Price: %,d KRW\n", getTotalPrice());
        System.out.println("Payment Status: " + (paid ? "Paid" : "Unpaid"));
        System.out.println("=============================\n");
    }

    // Converts order data into CSV format.
    // 주문 데이터를 CSV 저장 형식으로 변환합니다.
    public String toFileString() {
        StringBuilder sb = new StringBuilder();

        sb.append(orderId).append(",");
        sb.append(tableNumber).append(",");
        sb.append(paid).append(",");

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);

            sb.append(item.getMenuItem().getId())
                    .append(":")
                    .append(item.getQuantity());

            if (i < items.size() - 1) {
                sb.append("|");
            }
        }

        return sb.toString();
    }
}

/*
 * ============================================================
 * RestaurantTable Class
 * RestaurantTable 클래스
 * ------------------------------------------------------------
 * Manages table number and table status.
 * 테이블 번호와 사용 상태를 관리하는 클래스입니다.
 * ============================================================
 */
class RestaurantTable implements Printable {
    private int tableNumber;
    private boolean occupied;

    public RestaurantTable(int tableNumber) {
        this.tableNumber = tableNumber;
        this.occupied = false;
    }

    public int getTableNumber() { return tableNumber; }
    public boolean isOccupied() { return occupied; }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    @Override
    public void printInfo() {
        System.out.printf("%-10d %-10s\n",
                tableNumber, occupied ? "Occupied" : "Available");
    }
}

/*
 * ============================================================
 * Main POS Program
 * 메인 POS 프로그램
 * ============================================================
 */
public class Main {
    private static Scanner sc = new Scanner(System.in);

    private static ArrayList<MenuItem> menuList = new ArrayList<>();
    private static HashMap<Integer, Order> orderMap = new HashMap<>();
    private static ArrayList<RestaurantTable> tableList = new ArrayList<>();

    private static int nextOrderId = 1;

    public static void main(String[] args) {
        printStartAnimation();
        initializeData();
        loadOrdersFromFile();

        while (true) {
            try {
                printMainMenu();
                int choice = inputInt("Select: ");

                switch (choice) {
                    case 1:
                        viewMenu();
                        break;
                    case 2:
                        placeOrder();
                        break;
                    case 3:
                        viewTables();
                        break;
                    case 4:
                        printReceipt();
                        break;
                    case 5:
                        payOrder();
                        break;
                    case 6:
                        printDailySalesReport();
                        break;
                    case 7:
                        printSalesByDate();
                        break;
                    case 8:
                        clearTable();
                        break;
                    case 0:
                        saveOrdersToFile();
                        System.out.println("Data saved successfully. Program terminated.");
                        return;
                    default:
                        System.out.println("Invalid menu number.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    /*
     * ============================================================
     * Sales Search by Date
     * 날짜별 매출 조회 기능
     * ============================================================
     */
    private static void printSalesByDate() {
        System.out.print("Enter date to search (example: 2026-05-24): ");
        String targetDate = sc.nextLine();

        int totalSales = 0;
        int paidCount = 0;

        File file = new File("sales.csv");

        if (!file.exists()) {
            System.out.println("Sales file does not exist.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length < 4) {
                    continue;
                }

                String date = data[0];
                int price = Integer.parseInt(data[3]);

                if (date.equals(targetDate)) {
                    totalSales += price;
                    paidCount++;
                }
            }

            System.out.println("\n===== SALES REPORT BY DATE =====");
            System.out.println("Search Date: " + targetDate);
            System.out.println("Paid Order Count: " + paidCount);
            System.out.printf("Total Sales: %,d KRW\n", totalSales);

        } catch (Exception e) {
            System.out.println("An error occurred while searching sales data.");
        }
    }

    /*
     * ============================================================
     * Slow Print Animation
     * 천천히 출력하는 애니메이션 기능
     * ============================================================
     */
    private static void slowPrint(String text, int delay) {
        for (char c : text.toCharArray()) {
            System.out.print(c);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println();
    }

    /*
     * ============================================================
     * Start Screen Animation
     * 시작 화면 애니메이션 기능
     * ============================================================
     */
    private static void printStartAnimation() {
        try {
            System.out.println();
            System.out.println();
            Thread.sleep(500);

            System.out.println();
            slowPrint("██╗  ██╗ █████╗ ███╗   ███╗██████╗ ██╗   ██╗██████╗  ██████╗ ███████╗██████╗ ", 2);
            slowPrint("██║  ██║██╔══██╗████╗ ████║██╔══██╗██║   ██║██╔══██╗██╔════╝ ██╔════╝██╔══██╗", 2);
            slowPrint("███████║███████║██╔████╔██║██████╔╝██║   ██║██████╔╝██║  ███╗█████╗  ██████╔╝", 2);
            slowPrint("██╔══██║██╔══██║██║╚██╔╝██║██╔══██╗██║   ██║██╔══██╗██║   ██║██╔══╝  ██╔══██╗", 2);
            slowPrint("██║  ██║██║  ██║██║ ╚═╝ ██║██████╔╝╚██████╔╝██║  ██║╚██████╔╝███████╗██║  ██║", 2);
            slowPrint("╚═╝  ╚═╝╚═╝  ╚═╝╚═╝     ╚═╝╚═════╝  ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝", 2);

            System.out.println();
            slowPrint("██████╗  ██████╗ ███████╗", 4);
            slowPrint("██╔══██╗██╔═══██╗██╔════╝", 4);
            slowPrint("██████╔╝██║   ██║███████╗", 4);
            slowPrint("██╔═══╝ ██║   ██║╚════██║", 4);
            slowPrint("██║     ╚██████╔╝███████║", 4);
            slowPrint("╚═╝      ╚═════╝ ╚══════╝", 4);

            System.out.println();
            slowPrint("🍔 HAMBURGER STORE POS SYSTEM 🍟", 30);
            System.out.println();
            Thread.sleep(500);

            slowPrint("🍔 HAMBURGER STORE POS SYSTEM 🍟", 40);

            System.out.println();

            slowPrint("[ System booting", 40);
            Thread.sleep(300);

            slowPrint(".", 300);
            slowPrint(".", 300);
            slowPrint(".", 300);

            Thread.sleep(500);

            slowPrint("[ Menu data loaded ]", 30);
            Thread.sleep(300);

            slowPrint("[ Order system connected ]", 30);
            Thread.sleep(300);

            slowPrint("[ Table system connected ]", 30);
            Thread.sleep(300);

            slowPrint("[ Sales system connected ]", 30);

            System.out.println();
            Thread.sleep(700);

            slowPrint("🚀 Starting program...", 50);

            Thread.sleep(1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * ============================================================
     * Initialize Basic Data
     * 기본 데이터 초기화 기능
     * ============================================================
     */
    private static void initializeData() {
        menuList.add(new SingleBurger(1, "Bulgogi Burger", 4500, "Beef"));
        menuList.add(new SetBurger(2, "Bulgogi Burger Set", 7500, "Beef", true, true));

        menuList.add(new SingleBurger(3, "Cheese Burger", 5000, "Beef"));
        menuList.add(new SetBurger(4, "Cheese Burger Set", 8000, "Beef", true, true));

        menuList.add(new Side(5, "French Fries", 2500));
        menuList.add(new Side(6, "Chicken Nuggets", 3500));

        menuList.add(new Drink(7, "Cola", 2000));
        menuList.add(new Drink(8, "Sprite", 2000));

        for (int i = 1; i <= 5; i++) {
            tableList.add(new RestaurantTable(i));
        }
    }

    /*
     * ============================================================
     * Main Menu Display
     * 메인 메뉴 출력 기능
     * ============================================================
     */
    private static void printMainMenu() {
        System.out.println("\n===== HAMBURGER STORE POS =====");
        System.out.println("1. View Menu");
        System.out.println("2. Place Order");
        System.out.println("3. View Table Status");
        System.out.println("4. Print Receipt");
        System.out.println("5. Payment");
        System.out.println("6. Today's Sales Report");
        System.out.println("7. Search Sales by Date");
        System.out.println("8. Clear Table");
        System.out.println("0. Exit");
    }

    /*
     * ============================================================
     * Menu Display
     * 메뉴판 출력 기능
     * ============================================================
     */
    private static void viewMenu() {
        System.out.println("\n========== MENU BOARD ==========");
        System.out.printf("%-5s %-20s %-15s %-8s\n", "ID", "Name", "Category", "Price");
        System.out.println("-----------------------------------------------------");

        for (MenuItem item : menuList) {
            item.printInfo();
        }
    }

    /*
     * ============================================================
     * Order Placement
     * 주문 생성 및 추가 주문 기능
     * ============================================================
     */
    private static void placeOrder() {
        viewMenu();

        int tableNumber = inputInt("Enter table number (1~5): ");
        RestaurantTable table = findTable(tableNumber);

        if (table == null) {
            System.out.println("This table does not exist.");
            return;
        }

        Order order = null;

        if (table.isOccupied()) {
            for (Order o : orderMap.values()) {
                if (o.getTableNumber() == tableNumber) {
                    order = o;
                    break;
                }
            }

            if (order == null) {
                System.out.println("The table is occupied, but order data was not found.");
                return;
            }

            if (order.isPaid()) {
                System.out.println("This table has already been paid.");
                System.out.println("Please clear the table before creating a new order.");
                return;
            }

            System.out.println("Adding items to the existing order.");
        } else {
            order = new Order(nextOrderId++, tableNumber);
        }

        while (true) {
            int menuId = inputInt("Enter menu ID (0 to finish order): ");

            if (menuId == 0) {
                break;
            }

            MenuItem item = findMenuItem(menuId);

            if (item == null) {
                System.out.println("This menu item does not exist.");
                continue;
            }

            int quantity = inputInt("Enter quantity: ");

            if (quantity <= 0) {
                System.out.println("Quantity must be at least 1.");
                continue;
            }

            order.addItem(item, quantity);
            System.out.println(item.getName() + " x " + quantity + " added successfully.");
        }

        if (order.getTotalPrice() == 0) {
            System.out.println("No menu item was selected. Order canceled.");
            return;
        }

        orderMap.put(order.getOrderId(), order);
        table.setOccupied(true);
        saveOrdersToFile();

        System.out.println("Order completed! Order ID: " + order.getOrderId());
        order.printInfo();
    }

    /*
     * ============================================================
     * Table Status Display
     * 테이블 상태 출력 기능
     * ============================================================
     */
    private static void viewTables() {
        System.out.println("\n===== TABLE STATUS =====");
        System.out.printf("%-10s %-10s\n", "Table", "Status");
        System.out.println("----------------------");

        for (RestaurantTable table : tableList) {
            table.printInfo();
        }
    }

    /*
     * ============================================================
     * Receipt Printing
     * 영수증 출력 기능
     * ============================================================
     */
    private static void printReceipt() {
        int orderId = inputInt("Enter order ID to print receipt: ");
        Order order = orderMap.get(orderId);

        if (order == null) {
            System.out.println("Order ID not found.");
            return;
        }

        order.printInfo();
    }

    /*
     * ============================================================
     * Payment Main Flow
     * 결제 메인 흐름
     * ============================================================
     */
    private static void payOrder() {
        int orderId = inputInt("Enter order ID for payment: ");
        Order order = orderMap.get(orderId);

        if (order == null) {
            System.out.println("Order ID not found.");
            return;
        }

        if (order.isPaid()) {
            System.out.println("This order has already been paid.");
            System.out.println("Please clear the table.");
            return;
        }

        order.printInfo();

        System.out.println("Select payment type");
        System.out.println("1. Full Payment");
        System.out.println("2. Split Payment - By Menu Item");
        System.out.println("3. Split Payment - N-way Split");
        int splitType = inputInt("Select: ");

        boolean success = false;

        if (splitType == 1) {
            success = processPayment(order.getTotalPrice(), "Full Payment", order);
        } else if (splitType == 2) {
            success = payByMenuItems(order);
        } else if (splitType == 3) {
            success = payByDutchPay(order);
        } else {
            System.out.println("Invalid payment type.");
            return;
        }

        if (!success) {
            System.out.println("Payment was not completed.");
            return;
        }

        order.pay();
        savePaidOrderToSalesFile(order);
        saveOrdersToFile();

        System.out.println("Full payment completed successfully.");
        System.out.println("The table will remain occupied until it is cleared.");
    }

    /*
     * ============================================================
     * Split Payment by Menu Item
     * 메뉴별 분할 결제 기능
     * ============================================================
     */
    private static boolean payByMenuItems(Order order) {
        System.out.println("\n===== SPLIT PAYMENT BY MENU ITEM =====");

        ArrayList<OrderItem> items = order.getItems();

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);

            System.out.printf("%d. %s x %d = %,d KRW\n",
                    i + 1,
                    item.getMenuItem().getName(),
                    item.getQuantity(),
                    item.getSubtotal());
        }

        System.out.println("-------------------------------------");

        for (OrderItem item : items) {
            int amount = item.getSubtotal();
            String description = item.getMenuItem().getName() + " - Menu Item Split Payment";

            System.out.printf("\n[%s] Payment Amount: %,d KRW\n",
                    item.getMenuItem().getName(), amount);

            boolean result = processPayment(amount, description, order);

            if (!result) {
                return false;
            }
        }

        return true;
    }

    /*
     * ============================================================
     * N-way Split Payment
     * N분의 1 분할 결제 기능
     * ============================================================
     */
    private static boolean payByDutchPay(Order order) {
        System.out.println("\n===== N-WAY SPLIT PAYMENT =====");

        int people = inputInt("Enter number of people: ");

        if (people <= 0) {
            System.out.println("Number of people must be at least 1.");
            return false;
        }

        int total = order.getTotalPrice();
        int share = total / people;
        int remainder = total % people;

        System.out.printf("Total Payment Amount: %,d KRW\n", total);
        System.out.printf("Base Payment per Person: %,d KRW\n", share);

        if (remainder > 0) {
            System.out.printf("The remaining %,d KRW will be paid by the last customer.\n", remainder);
        }

        for (int i = 1; i <= people; i++) {
            int amount = share;

            if (i == people) {
                amount += remainder;
            }

            System.out.printf("\nCustomer %d Payment Amount: %,d KRW\n", i, amount);

            boolean result = processPayment(amount, "N-way Split Payment - Customer " + i, order);

            if (!result) {
                return false;
            }
        }

        return true;
    }

    /*
     * ============================================================
     * Payment Method Selection
     * 결제 방식 선택 기능
     * ============================================================
     */
    private static boolean processPayment(int amount, String description, Order order) {
        System.out.println("\nSelect payment method");
        System.out.println("1. Card");
        System.out.println("2. Cash");
        int payType = inputInt("Select: ");

        if (payType == 1) {
            processCardPayment(amount, description, order);
            return true;
        } else if (payType == 2) {
            return processCashPayment(amount, description, order);
        } else {
            System.out.println("Invalid payment method.");
            return false;
        }
    }

    /*
     * ============================================================
     * Card Payment
     * 카드 결제 기능
     * ============================================================
     */
    private static void processCardPayment(int amount, String description, Order order) {
        System.out.printf("Card Payment Amount: %,d KRW\n", amount);

        String installmentInfo = "Lump Sum";

        if (amount >= 50000) {
            System.out.println("This card payment is 50,000 KRW or more.");
            System.out.println("Would you like to use installment payment?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            int choice = inputInt("Select: ");

            if (choice == 1) {
                System.out.println("Select installment months");
                System.out.println("1. 2 months");
                System.out.println("2. 3 months");
                System.out.println("3. 6 months");
                System.out.println("4. 12 months");

                int monthChoice = inputInt("Select: ");
                int months;

                switch (monthChoice) {
                    case 1:
                        months = 2;
                        break;
                    case 2:
                        months = 3;
                        break;
                    case 3:
                        months = 6;
                        break;
                    case 4:
                        months = 12;
                        break;
                    default:
                        System.out.println("Invalid selection. Payment will be processed as lump sum.");
                        months = 1;
                }

                if (months > 1) {
                    installmentInfo = months + " months installment";
                }
            }
        }

        System.out.println("Card payment completed.");
        System.out.println("Payment detail: " + installmentInfo);

        savePaymentLog(order, description, "Card", amount, installmentInfo, "");
    }

    /*
     * ============================================================
     * Cash Payment
     * 현금 결제 기능
     * ============================================================
     */
    private static boolean processCashPayment(int amount, String description, Order order) {
        System.out.printf("Cash Payment Amount: %,d KRW\n", amount);

        int received = inputInt("Enter received amount from customer: ");

        if (received < amount) {
            System.out.println("Received amount is less than payment amount.");
            return false;
        }

        int change = received - amount;

        System.out.printf("Received Amount: %,d KRW\n", received);
        System.out.printf("Change: %,d KRW\n", change);

        String cashReceiptPhone = "";

        System.out.println("Would you like to issue a cash receipt?");
        System.out.println("1. Yes");
        System.out.println("2. No");
        int receiptChoice = inputInt("Select: ");

        if (receiptChoice == 1) {
            System.out.print("Enter phone number: ");
            cashReceiptPhone = sc.nextLine();
            System.out.println("Cash receipt issued: " + cashReceiptPhone);
        } else {
            System.out.println("Cash receipt not issued.");
        }

        System.out.println("Cash payment completed.");

        savePaymentLog(
                order,
                description,
                "Cash",
                amount,
                "Received " + received + " KRW / Change " + change + " KRW",
                cashReceiptPhone
        );

        return true;
    }

    /*
     * ============================================================
     * Payment Log Save
     * 결제 상세 기록 저장 기능
     * ============================================================
     */
    private static void savePaymentLog(Order order, String description, String paymentType,
                                       int amount, String detail, String cashReceiptPhone) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (PrintWriter writer = new PrintWriter(new FileWriter("payment_log.csv", true))) {
            writer.println(
                    now.format(formatter) + "," +
                            order.getOrderId() + "," +
                            order.getTableNumber() + "," +
                            description + "," +
                            paymentType + "," +
                            amount + "," +
                            detail + "," +
                            cashReceiptPhone
            );
        } catch (IOException e) {
            System.out.println("An error occurred while saving payment log.");
        }
    }

    /*
     * ============================================================
     * Clear Table
     * 테이블 정리 기능
     * ============================================================
     */
    private static void clearTable() {
        int tableNumber = inputInt("Enter table number to clear: ");

        RestaurantTable table = findTable(tableNumber);

        if (table == null) {
            System.out.println("This table does not exist.");
            return;
        }

        Order targetOrder = null;

        for (Order order : orderMap.values()) {
            if (order.getTableNumber() == tableNumber) {
                targetOrder = order;
                break;
            }
        }

        if (targetOrder == null) {
            System.out.println("There is no order for this table.");
            return;
        }

        if (!targetOrder.isPaid()) {
            System.out.println("This table has not been paid yet.");
            System.out.println("Please complete payment first.");
            return;
        }

        orderMap.remove(targetOrder.getOrderId());
        table.setOccupied(false);
        saveOrdersToFile();

        System.out.println("Table cleared successfully!");
        System.out.println("Table " + tableNumber + " is now available.");
    }

    /*
     * ============================================================
     * Today's Sales Report
     * 오늘 매출 보고서 출력 기능
     * ============================================================
     */
    private static void printDailySalesReport() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = now.format(dateFormatter);

        int totalSales = 0;
        int paidCount = 0;

        File file = new File("sales.csv");

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");

                    if (data.length < 4) {
                        continue;
                    }

                    String date = data[0];
                    int price = Integer.parseInt(data[3]);

                    if (date.equals(today)) {
                        totalSales += price;
                        paidCount++;
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not load sales file.");
            }
        }

        System.out.println("\n===== TODAY'S SALES REPORT =====");
        System.out.println("Date: " + today);
        System.out.println("Paid Order Count: " + paidCount);
        System.out.printf("Total Sales: %,d KRW\n", totalSales);
    }

    /*
     * ============================================================
     * Save Paid Order to Sales File
     * 결제 완료 주문을 매출 파일에 저장하는 기능
     * ============================================================
     */
    private static void savePaidOrderToSalesFile(Order order) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = now.format(dateFormatter);

        try (PrintWriter writer = new PrintWriter(new FileWriter("sales.csv", true))) {
            writer.println(today + "," + order.getOrderId() + "," + order.getTableNumber() + "," + order.getTotalPrice());
        } catch (IOException e) {
            System.out.println("An error occurred while saving sales data.");
        }
    }

    /*
     * ============================================================
     * Find Menu Item
     * 메뉴 항목 검색 기능
     * ============================================================
     */
    private static MenuItem findMenuItem(int id) {
        for (MenuItem item : menuList) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    /*
     * ============================================================
     * Find Table
     * 테이블 검색 기능
     * ============================================================
     */
    private static RestaurantTable findTable(int tableNumber) {
        for (RestaurantTable table : tableList) {
            if (table.getTableNumber() == tableNumber) {
                return table;
            }
        }
        return null;
    }

    /*
     * ============================================================
     * Safe Integer Input
     * 안전한 정수 입력 처리 기능
     * ============================================================
     */
    private static int inputInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter numbers only.");
            }
        }
    }

    /*
     * ============================================================
     * Save Current Orders to File
     * 현재 주문 정보를 파일에 저장하는 기능
     * ============================================================
     */
    private static void saveOrdersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("orders.csv"))) {
            writer.println("orderId,tableNumber,paid,items");

            for (Order order : orderMap.values()) {
                writer.println(order.toFileString());
            }
        } catch (IOException e) {
            System.out.println("An error occurred while saving orders.");
        }
    }

    /*
     * ============================================================
     * Load Orders from File
     * 저장된 주문 정보를 파일에서 불러오는 기능
     * ============================================================
     */
    private static void loadOrdersFromFile() {
        File file = new File("orders.csv");

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 4);

                if (data.length < 4) {
                    continue;
                }

                int orderId = Integer.parseInt(data[0]);
                int tableNumber = Integer.parseInt(data[1]);
                boolean paid = Boolean.parseBoolean(data[2]);

                Order order = new Order(orderId, tableNumber);

                if (!data[3].isEmpty()) {
                    String[] itemData = data[3].split("\\|");

                    for (String itemText : itemData) {
                        String[] itemInfo = itemText.split(":");

                        int menuId = Integer.parseInt(itemInfo[0]);
                        int quantity = Integer.parseInt(itemInfo[1]);

                        MenuItem menuItem = findMenuItem(menuId);

                        if (menuItem != null) {
                            order.addItem(menuItem, quantity);
                        }
                    }
                }

                if (paid) {
                    order.pay();
                }

                orderMap.put(orderId, order);

                RestaurantTable table = findTable(tableNumber);
                if (table != null) {
                    table.setOccupied(true);
                }

                if (orderId >= nextOrderId) {
                    nextOrderId = orderId + 1;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load existing order file.");
        }
    }
}