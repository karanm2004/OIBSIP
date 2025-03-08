import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

class Account {
    public double balance;
    public String accountNumber;
    public int pin;
    public String name;
    private static final String USER_FILE = "users.csv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public Account(String accountNumber, int pin, String name, double balance) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.name = name;
        this.balance = balance;
        saveUserDetails();
    }
    
    public void saveUserDetails() {
        List<String> users = new ArrayList<>();
        boolean exists = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] details = line.split(",");
                if (details[0].equals(accountNumber)) {
                    exists = true;
                    line = accountNumber + "," + pin + "," + name + "," + balance;
                }
                users.add(line);
            }
        } catch (IOException e) {}
        
        if (!exists) {
            users.add(accountNumber + "," + pin + "," + name + "," + balance);
        }
        
        try (PrintWriter out = new PrintWriter(new FileWriter(USER_FILE))) {
            for (String user : users) {
                out.println(user);
            }
        } catch (IOException e) {
            System.out.println("Error saving user details.");
        }
    }
    
    public void logTransaction(String transaction) {
        String filename = accountNumber + ".txt";
        try (PrintWriter out = new PrintWriter(new FileWriter(filename, true))) {
            String timestamp = LocalDateTime.now().format(FORMATTER);
            out.println(transaction + " on " + timestamp);
        } catch (IOException e) {
            System.out.println("Error saving transaction history.");
        }
    }
}

class ATM {
    private static final String USER_FILE = "users.csv";
    private Map<String, Account> accounts = new HashMap<>();
    
    public ATM() {
        loadUsers();
    }
    
    private void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length == 4) {
                    accounts.put(details[0], new Account(details[0], Integer.parseInt(details[1]), details[2], Double.parseDouble(details[3])));
                }
            }
        } catch (IOException e) {
            System.out.println("User data not found. Creating a new database.");
        }
    }
    
    public void createAccount(String name, int pin, double balance) {
        String accountNumber = String.valueOf((long) (Math.random() * 9000000000L) + 1000000000L);
        System.out.println("Your account number is: " + accountNumber);
        Account acc = new Account(accountNumber, pin, name, balance);
        acc.logTransaction("Initial deposit: " + balance);
        accounts.put(accountNumber, acc);
    }
    
    public Account login(String accountNumber, int pin) {
        if (accounts.containsKey(accountNumber) && accounts.get(accountNumber).pin == pin) {
            System.out.println("Login successful!");
            return accounts.get(accountNumber);
        }
        System.out.println("Invalid credentials");
        return null;
    }
    
    // New method for transferring money between accounts
    public boolean transferMoney(Account sender, String receiverAccountNumber, double amount) {
        // Check if receiver account exists
        if (!accounts.containsKey(receiverAccountNumber)) {
            System.out.println("Receiver account does not exist.");
            return false;
        }
        
        // Check if transfer amount is valid
        if (amount <= 0) {
            System.out.println("Transfer amount must be positive.");
            return false;
        }
        
        // Check if sender has sufficient balance
        if (sender.balance < amount) {
            System.out.println("Insufficient balance for transfer.");
            return false;
        }
        
        // Cannot transfer to your own account
        if (sender.accountNumber.equals(receiverAccountNumber)) {
            System.out.println("Cannot transfer to your own account.");
            return false;
        }
        
        // Get receiver account
        Account receiver = accounts.get(receiverAccountNumber);
        
        // Perform the transfer
        sender.balance -= amount;
        receiver.balance += amount;
        
        // Log the transaction for both sender and receiver
        sender.logTransaction("Transferred " + amount + " to account " + receiverAccountNumber);
        receiver.logTransaction("Received " + amount + " from account " + sender.accountNumber);
        
        // Save the updated account information
        sender.saveUserDetails();
        receiver.saveUserDetails();
        
        System.out.println("Transfer successful!");
        return true;
    }
    
    // Helper method to get account name (for displaying in transfer confirmation)
    public String getAccountName(String accountNumber) {
        if (accounts.containsKey(accountNumber)) {
            return accounts.get(accountNumber).name;
        }
        return null;
    }
}

public class BankAccountPro {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        ATM atm = new ATM();
        boolean running = true;
        
        while (running) {
            System.out.println("Welcome to THE Bank.\n1. Create a new account\n2. Login\n3. Exit");
            int choice = s.nextInt();
            switch (choice) {
                case 1:
                    System.out.print("Enter your name: ");
                    s.nextLine();
                    String name = s.nextLine();
                    System.out.print("Enter a PIN: ");
                    int pin = s.nextInt();
                    System.out.print("Enter initial deposit amount: ");
                    double balance = s.nextDouble();
                    atm.createAccount(name, pin, balance);
                    break;
                case 2:
                    System.out.print("Enter account number: ");
                    String accNum = s.next();
                    System.out.print("Enter PIN: ");
                    int userPin = s.nextInt();
                    Account acc = atm.login(accNum, userPin);
                    if (acc != null) {
                        boolean loggedIn = true;
                        while (loggedIn) {
                            // Added transfer option to the menu
                            System.out.println("1. Deposit\n2. Withdraw\n3. Check Balance\n4. Transaction History\n5. Transfer Money\n6. Logout");
                            int action = s.nextInt();
                            switch (action) {
                                case 1:
                                    System.out.print("Enter deposit amount: ");
                                    double depositAmount = s.nextDouble();
                                    acc.balance += depositAmount;
                                    acc.logTransaction("Deposited " + depositAmount);
                                    acc.saveUserDetails();
                                    break;
                                case 2:
                                    System.out.print("Enter withdrawal amount: ");
                                    double withdrawAmount = s.nextDouble();
                                    if (withdrawAmount > 0 && withdrawAmount <= acc.balance) {
                                        acc.balance -= withdrawAmount;
                                        acc.logTransaction("Withdrawn " + withdrawAmount);
                                        acc.saveUserDetails();
                                    } else {
                                        System.out.println("Insufficient balance or invalid amount.");
                                    }
                                    break;
                                case 3:
                                    System.out.println("Current balance: " + acc.balance);
                                    break;
                                case 4:
                                    try (BufferedReader br = new BufferedReader(new FileReader(acc.accountNumber + ".txt"))) {
                                        String line;
                                        while ((line = br.readLine()) != null) {
                                            System.out.println(line);
                                        }
                                    } catch (IOException e) {
                                        System.out.println("No transaction history available.");
                                    }
                                    break;
                                case 5:
                                    // New case for money transfer
                                    System.out.print("Enter receiver's account number: ");
                                    String receiverAccNum = s.next();
                                    
                                    // Get receiver name to confirm transfer
                                    String receiverName = atm.getAccountName(receiverAccNum);
                                    if (receiverName == null) {
                                        System.out.println("Receiver account not found.");
                                        break;
                                    }
                                    
                                    System.out.println("Transferring to: " + receiverName);
                                    System.out.print("Enter transfer amount: ");
                                    double transferAmount = s.nextDouble();
                                    
                                    // Confirm transfer
                                    System.out.print("Confirm transfer of " + transferAmount + " to " + receiverName + " (Y/N): ");
                                    String confirm = s.next();
                                    if (confirm.equalsIgnoreCase("Y")) {
                                        atm.transferMoney(acc, receiverAccNum, transferAmount);
                                    } else {
                                        System.out.println("Transfer cancelled.");
                                    }
                                    break;
                                case 6:
                                    loggedIn = false;
                                    System.out.println("Logged out.");
                                    break;
                                default:
                                    System.out.println("Invalid choice.");
                            }
                        }
                    }
                    break;
                case 3:
                    running = false;
                    System.out.println("Thank you for using THE Bank.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
        s.close();
    }
}