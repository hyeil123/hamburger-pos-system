import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

interface Printable {
    void printInfo();
}

interface Payable {
    int getTotalPrice();
}

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

    public abstract String getCategory();

    @Override
    public void printInfo() {
        System.out.printf("%-5d %-15s %-15s %,8d원\n",
                id, name, getCategory(), price);
    }
}

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
class SingleBurger extends Burger {

    public SingleBurger(int id, String name, int price, String pattyType) {
        super(id, name, price, pattyType);
    }

    @Override
    public String getCategory() {
        return "Single Burger";
    }
}
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

    public void printSetInfo() {
        System.out.println("감자튀김 포함: " + hasFries);
        System.out.println("음료 포함: " + hasDrink);
    }
}
class Side extends MenuItem {
    public Side(int id, String name, int price) {
        super(id, name, price);
    }

    @Override
    public String getCategory() {
        return "Side";
    }
}

class Drink extends MenuItem {
    public Drink(int id, String name, int price) {
        super(id, name, price);
    }

    @Override
    public String getCategory() {
        return "Drink";
    }
}

class OrderItem {
    private MenuItem menuItem;
    private int quantity;

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }

    public int getSubtotal() {
        return menuItem.getPrice() * quantity;
    }

    public void printLine() {
        System.out.printf("%-15s x %-3d %,8d원\n",
                menuItem.getName(), quantity, getSubtotal());
    }
}

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

    public void addItem(MenuItem item, int quantity) {
        items.add(new OrderItem(item, quantity));
    }

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

        System.out.println("\n========== 영수증 ==========");
        System.out.println("주문번호: " + orderId);
        System.out.println("테이블 번호: " + tableNumber);
        System.out.println("주문시간: " + orderTime.format(formatter));
        System.out.println("--------------------------");

        for (OrderItem item : items) {
            item.printLine();
        }

        System.out.println("--------------------------");
        System.out.printf("총 금액: %,d원\n", getTotalPrice());
        System.out.println("결제 상태: " + (paid ? "결제 완료" : "미결제"));
        System.out.println("==========================\n");
    }

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
                tableNumber, occupied ? "사용중" : "비어있음");
    }
}

public class HamburgerPOS {
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
                int choice = inputInt("선택: ");

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
                        System.out.println("데이터 저장 완료. 프로그램을 종료합니다.");
                        return;
                    default:
                        System.out.println("잘못된 메뉴 번호입니다.");
                }
            } catch (Exception e) {
                System.out.println("오류가 발생했습니다: " + e.getMessage());
            }
        }
    }
    private static void printSalesByDate() {
        System.out.print("조회할 날짜 입력 (예: 2026-05-24): ");
        String targetDate = sc.nextLine();

        int totalSales = 0;
        int paidCount = 0;

        File file = new File("sales.csv");

        if (!file.exists()) {
            System.out.println("매출 파일이 존재하지 않습니다.");
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

            System.out.println("\n===== 날짜별 매출 조회 =====");
            System.out.println("조회 날짜: " + targetDate);
            System.out.println("결제 완료 주문 수: " + paidCount);
            System.out.printf("총 매출: %,d원\n", totalSales);

        } catch (Exception e) {
            System.out.println("매출 조회 중 오류 발생");
        }
    }
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

    private static void printStartAnimation() {

        try {

            System.out.println();
            System.out.println();
            Thread.sleep(500);

            slowPrint(" ██╗  ██╗ █████╗ ███╗   ███╗██████╗ ██╗   ██╗██████╗ ", 3);
            slowPrint(" ██║  ██║██╔══██╗████╗ ████║██╔══██╗██║   ██║██╔══██╗", 3);
            slowPrint(" ███████║███████║██╔████╔██║██████╔╝██║   ██║██████╔╝", 3);
            slowPrint(" ██╔══██║██╔══██║██║╚██╔╝██║██╔══██╗██║   ██║██╔══██╗", 3);
            slowPrint(" ██║  ██║██║  ██║██║ ╚═╝ ██║██████╔╝╚██████╔╝██║  ██║", 3);
            slowPrint(" ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝     ╚═╝╚═════╝  ╚═════╝ ╚═╝  ╚═╝", 3);

            System.out.println();
            Thread.sleep(300);

            slowPrint("██████╗  ██████╗ ███████╗", 5);
            slowPrint("██╔══██╗██╔═══██╗██╔════╝", 5);
            slowPrint("██████╔╝██║   ██║███████╗", 5);
            slowPrint("██╔═══╝ ██║   ██║╚════██║", 5);
            slowPrint("██║     ╚██████╔╝███████║", 5);
            slowPrint("╚═╝      ╚═════╝ ╚══════╝", 5);

            System.out.println();
            Thread.sleep(500);

            slowPrint("🍔 HAMBURGER STORE POS SYSTEM 🍟", 40);

            System.out.println();

            slowPrint("[ 시스템 부팅 중", 40);
            Thread.sleep(300);

            slowPrint(".", 300);
            slowPrint(".", 300);
            slowPrint(".", 300);

            Thread.sleep(500);

            slowPrint("[ 메뉴 데이터 로딩 완료 ]", 30);
            Thread.sleep(300);

            slowPrint("[ 주문 시스템 연결 완료 ]", 30);
            Thread.sleep(300);

            slowPrint("[ 테이블 시스템 연결 완료 ]", 30);
            Thread.sleep(300);

            slowPrint("[ 매출 시스템 연결 완료 ]", 30);

            System.out.println();
            Thread.sleep(700);

            slowPrint("🚀 프로그램을 시작합니다...", 50);

            Thread.sleep(1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initializeData() {
        menuList.add(new SingleBurger(1, "불고기버거", 4500, "Beef"));
        menuList.add(new SetBurger(2, "불고기버거 세트", 7500, "Beef", true, true));

        menuList.add(new SingleBurger(3, "치즈버거", 5000, "Beef"));
        menuList.add(new SetBurger(4, "치즈버거 세트", 8000, "Beef", true, true));

        menuList.add(new Side(5, "감자튀김", 2500));
        menuList.add(new Side(6, "치킨너겟", 3500));

        menuList.add(new Drink(7, "콜라", 2000));
        menuList.add(new Drink(8, "사이다", 2000));
        for (int i = 1; i <= 5; i++) {
            tableList.add(new RestaurantTable(i));
        }
    }

    private static void printMainMenu() {
        System.out.println("\n===== 햄버거 가게 POS =====");
        System.out.println("1. 메뉴 보기");
        System.out.println("2. 주문하기");
        System.out.println("3. 테이블 상태 보기");
        System.out.println("4. 영수증 출력");
        System.out.println("5. 결제하기");
        System.out.println("6. 오늘 매출 조회");
        System.out.println("7. 날짜별 매출 조회");
        System.out.println("8. 테이블 정리 완료");
        System.out.println("0. 종료");
    }

    private static void viewMenu() {
        System.out.println("\n========== 메뉴판 ==========");
        System.out.printf("%-5s %-15s %-15s %-8s\n", "ID", "이름", "종류", "가격");
        System.out.println("---------------------------------------------");

        for (MenuItem item : menuList) {
            item.printInfo();
        }
    }

    private static void placeOrder() {
        viewMenu();

        int tableNumber = inputInt("테이블 번호 입력(1~5): ");
        RestaurantTable table = findTable(tableNumber);

        if (table == null) {
            System.out.println("존재하지 않는 테이블입니다.");
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
                System.out.println("테이블은 사용중인데 주문 정보가 없습니다.");
                return;
            }

            if (order.isPaid()) {
                System.out.println("이미 결제된 테이블입니다.");
                System.out.println("테이블 정리 완료 후 새 주문을 해주세요.");
                return;
            }

            System.out.println("기존 주문에 추가 주문을 진행합니다.");
        } else {
            order = new Order(nextOrderId++, tableNumber);
        }

        while (true) {
            int menuId = inputInt("메뉴 ID 입력(0 입력 시 주문 종료): ");

            if (menuId == 0) {
                break;
            }

            MenuItem item = findMenuItem(menuId);

            if (item == null) {
                System.out.println("존재하지 않는 메뉴입니다.");
                continue;
            }

            int quantity = inputInt("수량 입력: ");

            if (quantity <= 0) {
                System.out.println("수량은 1개 이상이어야 합니다.");
                continue;
            }

            order.addItem(item, quantity);
            System.out.println(item.getName() + " " + quantity + "개 추가 완료");
        }

        if (order.getTotalPrice() == 0) {
            System.out.println("주문한 메뉴가 없어 주문을 취소합니다.");
            return;
        }

        orderMap.put(order.getOrderId(), order);
        table.setOccupied(true);
        saveOrdersToFile();

        System.out.println("주문 완료! 주문번호: " + order.getOrderId());
        order.printInfo();
    }

    private static void viewTables() {
        System.out.println("\n===== 테이블 상태 =====");
        System.out.printf("%-10s %-10s\n", "테이블", "상태");
        System.out.println("----------------------");

        for (RestaurantTable table : tableList) {
            table.printInfo();
        }
    }

    private static void printReceipt() {
        int orderId = inputInt("영수증 출력할 주문번호 입력: ");
        Order order = orderMap.get(orderId);

        if (order == null) {
            System.out.println("해당 주문번호가 없습니다.");
            return;
        }

        order.printInfo();
    }

    private static void payOrder() {
        int orderId = inputInt("결제할 주문번호 입력: ");
        Order order = orderMap.get(orderId);

        if (order == null) {
            System.out.println("해당 주문번호가 없습니다.");
            return;
        }

        if (order.isPaid()) {
            System.out.println("이미 결제된 주문입니다.");
            System.out.println("테이블 정리 완료를 진행해주세요.");
            return;
        }

        order.printInfo();

        System.out.println("결제 유형 선택");
        System.out.println("1. 일반 결제");
        System.out.println("2. 분할 결제 - 메뉴별 계산");
        System.out.println("3. 분할 결제 - N분의 1");
        int splitType = inputInt("선택: ");

        boolean success = false;

        if (splitType == 1) {
            success = processPayment(order.getTotalPrice(), "일반 결제", order);
        } else if (splitType == 2) {
            success = payByMenuItems(order);
        } else if (splitType == 3) {
            success = payByDutchPay(order);
        } else {
            System.out.println("잘못된 결제 유형입니다.");
            return;
        }

        if (!success) {
            System.out.println("결제가 완료되지 않았습니다.");
            return;
        }

        order.pay();
        savePaidOrderToSalesFile(order);
        saveOrdersToFile();

        System.out.println("전체 결제가 완료되었습니다.");
        System.out.println("테이블 정리 완료 전까지는 테이블이 사용중 상태로 유지됩니다.");
    }
    private static boolean payByMenuItems(Order order) {
        System.out.println("\n===== 메뉴별 분할 결제 =====");

        ArrayList<OrderItem> items = order.getItems();

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);

            System.out.printf("%d. %s x %d = %,d원\n",
                    i + 1,
                    item.getMenuItem().getName(),
                    item.getQuantity(),
                    item.getSubtotal());
        }

        System.out.println("--------------------------");

        for (OrderItem item : items) {
            int amount = item.getSubtotal();
            String description = item.getMenuItem().getName() + " 메뉴별 결제";

            System.out.printf("\n[%s] 결제 금액: %,d원\n",
                    item.getMenuItem().getName(), amount);

            boolean result = processPayment(amount, description, order);

            if (!result) {
                return false;
            }
        }

        return true;
    }

    private static boolean payByDutchPay(Order order) {
        System.out.println("\n===== N분의 1 분할 결제 =====");

        int people = inputInt("몇 명이 나눠서 결제하나요?: ");

        if (people <= 0) {
            System.out.println("인원 수는 1명 이상이어야 합니다.");
            return false;
        }

        int total = order.getTotalPrice();
        int share = total / people;
        int remainder = total % people;

        System.out.printf("총 결제 금액: %,d원\n", total);
        System.out.printf("1인 기본 결제 금액: %,d원\n", share);

        if (remainder > 0) {
            System.out.printf("나머지 %,d원은 마지막 사람이 함께 결제합니다.\n", remainder);
        }

        for (int i = 1; i <= people; i++) {
            int amount = share;

            if (i == people) {
                amount += remainder;
            }

            System.out.printf("\n%d번째 손님 결제 금액: %,d원\n", i, amount);

            boolean result = processPayment(amount, "N분의 1 결제 - " + i + "번째 손님", order);

            if (!result) {
                return false;
            }
        }

        return true;
    }
    private static boolean processPayment(int amount, String description, Order order) {
        System.out.println("\n결제 방식 선택");
        System.out.println("1. 카드");
        System.out.println("2. 현금");
        int payType = inputInt("선택: ");

        if (payType == 1) {
            processCardPayment(amount, description, order);
            return true;
        } else if (payType == 2) {
            return processCashPayment(amount, description, order);
        } else {
            System.out.println("잘못된 결제 방식입니다.");
            return false;
        }
    }

    private static void processCardPayment(int amount, String description, Order order) {
        System.out.printf("카드 결제 금액: %,d원\n", amount);

        String installmentInfo = "일시불";

        if (amount >= 50000) {
            System.out.println("5만원 이상 카드 결제입니다.");
            System.out.println("할부로 결제하시겠습니까?");
            System.out.println("1. 예");
            System.out.println("2. 아니오");
            int choice = inputInt("선택: ");

            if (choice == 1) {
                System.out.println("할부 개월 수 선택");
                System.out.println("1. 2개월");
                System.out.println("2. 3개월");
                System.out.println("3. 6개월");
                System.out.println("4. 12개월");

                int monthChoice = inputInt("선택: ");
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
                        System.out.println("잘못 선택하여 일시불로 처리합니다.");
                        months = 1;
                }

                if (months > 1) {
                    installmentInfo = months + "개월 할부";
                }
            }
        }

        System.out.println("카드 결제가 완료되었습니다.");
        System.out.println("결제 방식: " + installmentInfo);

        savePaymentLog(order, description, "카드", amount, installmentInfo, "");
    }

    private static boolean processCashPayment(int amount, String description, Order order) {
        System.out.printf("현금 결제 금액: %,d원\n", amount);

        int received = inputInt("손님에게 받은 금액 입력: ");

        if (received < amount) {
            System.out.println("받은 금액이 결제 금액보다 부족합니다.");
            return false;
        }

        int change = received - amount;

        System.out.printf("받은 금액: %,d원\n", received);
        System.out.printf("거스름돈: %,d원\n", change);

        String cashReceiptPhone = "";

        System.out.println("현금영수증을 발행하시겠습니까?");
        System.out.println("1. 예");
        System.out.println("2. 아니오");
        int receiptChoice = inputInt("선택: ");

        if (receiptChoice == 1) {
            System.out.print("전화번호 입력: ");
            cashReceiptPhone = sc.nextLine();
            System.out.println("현금영수증 발행 완료: " + cashReceiptPhone);
        } else {
            System.out.println("현금영수증 미발행");
        }

        System.out.println("현금 결제가 완료되었습니다.");

        savePaymentLog(order, description, "현금", amount, "받은금액 " + received + "원 / 거스름돈 " + change + "원", cashReceiptPhone);

        return true;
    }
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
            System.out.println("결제 기록 저장 중 오류 발생");
        }
    }

    private static void clearTable() {
        int tableNumber = inputInt("정리 완료할 테이블 번호 입력: ");

        RestaurantTable table = findTable(tableNumber);

        if (table == null) {
            System.out.println("존재하지 않는 테이블입니다.");
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
            System.out.println("해당 테이블에 주문이 없습니다.");
            return;
        }

        if (!targetOrder.isPaid()) {
            System.out.println("아직 결제가 완료되지 않은 테이블입니다.");
            System.out.println("먼저 결제를 완료해주세요.");
            return;
        }

        orderMap.remove(targetOrder.getOrderId());
        table.setOccupied(false);
        saveOrdersToFile();

        System.out.println("테이블 정리 완료!");
        System.out.println("테이블 " + tableNumber + "번은 이제 빈자리입니다.");
    }

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
                System.out.println("매출 파일을 불러오지 못했습니다.");
            }
        }

        System.out.println("\n===== 일일 매출 보고서 =====");
        System.out.println("날짜: " + today);
        System.out.println("결제 완료 주문 수: " + paidCount);
        System.out.printf("총 매출: %,d원\n", totalSales);
    }

    private static void savePaidOrderToSalesFile(Order order) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = now.format(dateFormatter);

        try (PrintWriter writer = new PrintWriter(new FileWriter("sales.csv", true))) {
            writer.println(today + "," + order.getOrderId() + "," + order.getTableNumber() + "," + order.getTotalPrice());
        } catch (IOException e) {
            System.out.println("매출 저장 중 오류 발생");
        }
    }

    private static MenuItem findMenuItem(int id) {
        for (MenuItem item : menuList) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    private static RestaurantTable findTable(int tableNumber) {
        for (RestaurantTable table : tableList) {
            if (table.getTableNumber() == tableNumber) {
                return table;
            }
        }
        return null;
    }

    private static int inputInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력해주세요.");
            }
        }
    }

    private static void saveOrdersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("orders.csv"))) {
            writer.println("orderId,tableNumber,paid,items");

            for (Order order : orderMap.values()) {
                writer.println(order.toFileString());
            }
        } catch (IOException e) {
            System.out.println("주문 저장 중 오류 발생");
        }
    }

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
            System.out.println("기존 주문 파일을 불러오지 못했습니다.");
        }
    }
}