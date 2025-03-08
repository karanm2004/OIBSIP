/*
 Online Exam System
 This program is a console-based exam management system that allows:
 - Students to register, take timed exams, and view their results
 - Administrators to manage users, create/edit questions, and track performance
 
 The system features secure login, profile management, automatic grading,
 and persistent storage of user data and exam questions. Students can take
 multiple-choice exams with a 30-second time limit, while administrators
 have full control over the question database and user accounts.
 
 To use: Run the program and follow the menu prompts.
 Default admin login: username "admin", password "admin123"
 */
import java.util.*;
import java.io.*;

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    String username;
    String password;
    String name;
    int score;
    ArrayList<String> examHistory;

    User(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.score = 0; // Initialize score to 0
        this.examHistory = new ArrayList<>();
    }
    
    public void addExamResult(String result) {
        examHistory.add(result);
    }
}

class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    
    String questionText;
    String[] options;
    char correctAnswer;
    
    public Question(String questionText, String[] options, char correctAnswer) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }
}

public class OnlineExamSystem {
    private static HashMap<String, User> users = new HashMap<>();
    private static ArrayList<Question> questions = new ArrayList<>();
    private static User currentUser = null;
    private static Scanner scanner = new Scanner(System.in);
    private static boolean examInProgress = false;
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) {
        loadUsers(); // Load users from file
        loadQuestions(); // Load questions from file
        
        if (users.isEmpty()) {
            initializeUsers();
        }
        
        if (questions.isEmpty()) {
            initializeQuestions();
        }
        
        while (true) {
            System.out.println("\n===== ONLINE EXAM SYSTEM =====");
            System.out.println("1. Student Login");
            System.out.println("2. Admin Login");
            System.out.println("3. Create New Account");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
                continue;
            }

            switch (choice) {
                case 1:
                    studentLogin();
                    break;
                case 2:
                    adminLogin();
                    break;
                case 3:
                    createNewUser();
                    break;
                case 4:
                    saveUsers(); // Save users to file before exiting
                    saveQuestions(); // Save questions to file before exiting
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void initializeUsers() {
        // Predefined admin user
        users.put(ADMIN_USERNAME, new User(ADMIN_USERNAME, ADMIN_PASSWORD, "Admin"));
    }
    
    private static void initializeQuestions() {
        questions.add(new Question(
            "What is the capital of France?",
            new String[]{"A. Paris", "B. London", "C. Berlin", "D. Madrid"},
            'A'
        ));
        
        questions.add(new Question(
            "Which planet is known as the Red Planet?",
            new String[]{"A. Earth", "B. Mars", "C. Jupiter", "D. Saturn"},
            'B'
        ));
        
        questions.add(new Question(
            "What is the largest mammal?",
            new String[]{"A. Elephant", "B. Blue Whale", "C. Giraffe", "D. Shark"},
            'B'
        ));
    }

    private static void studentLogin() {
        System.out.println("\n===== STUDENT LOGIN =====");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        // Check if it's the admin account
        if (username.equals(ADMIN_USERNAME)) {
            System.out.println("This login is for students only. Please use Admin Login option.");
            return;
        }
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (users.containsKey(username) && users.get(username).password.equals(password)) {
            currentUser = users.get(username);
            System.out.println("Login successful! Welcome, " + currentUser.name + ".");
            userMenu();
        } else {
            System.out.println("Invalid username or password.");
        }
    }
    
    private static void adminLogin() {
        System.out.println("\n===== ADMIN LOGIN =====");
        System.out.print("Enter admin username: ");
        String username = scanner.nextLine();
        System.out.print("Enter admin password: ");
        String password = scanner.nextLine();

        if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
            currentUser = users.get(ADMIN_USERNAME);
            System.out.println("Admin login successful! Welcome, Administrator.");
            adminMenu();
        } else {
            System.out.println("Invalid admin credentials.");
        }
    }

    private static void createNewUser() {
        System.out.println("\n===== CREATE NEW ACCOUNT =====");
        System.out.print("Enter a new username: ");
        String username = scanner.nextLine();

        if (users.containsKey(username)) {
            System.out.println("Username already exists. Please choose a different username.");
            return;
        }
        
        if (username.equals(ADMIN_USERNAME)) {
            System.out.println("This username is reserved. Please choose a different username.");
            return;
        }

        System.out.print("Enter a new password: ");
        String password = scanner.nextLine();
        System.out.print("Confirm password: ");
        String confirmPassword = scanner.nextLine();

        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Please try again.");
            return;
        }

        System.out.print("Enter your full name: ");
        String name = scanner.nextLine();

        users.put(username, new User(username, password, name));
        System.out.println("User created successfully! You can now login.");
    }

    private static void adminMenu() {
        while (true) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. View All Users");
            System.out.println("2. Delete User");
            System.out.println("3. Add New Question");
            System.out.println("4. View All Questions");
            System.out.println("5. Remove Question");
            System.out.println("6. Reset User Score");
            System.out.println("7. Change Admin Password");
            System.out.println("8. Logout");
            System.out.print("Enter your choice: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
                continue;
            }

            switch (choice) {
                case 1:
                    viewAllUsers();
                    break;
                case 2:
                    deleteUser();
                    break;
                case 3:
                    addNewQuestion();
                    break;
                case 4:
                    viewAllQuestions();
                    break;
                case 5:
                    removeQuestion();
                    break;
                case 6:
                    resetUserScore();
                    break;
                case 7:
                    changeAdminPassword();
                    break;
                case 8:
                    logout();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewAllUsers() {
        System.out.println("\n===== ALL USERS =====");
        if (users.size() <= 1) {
            System.out.println("No regular users found.");
            return;
        }
        
        for (User user : users.values()) {
            if (!user.username.equals(ADMIN_USERNAME)) {
                System.out.println("Username: " + user.username + ", Name: " + user.name + ", Score: " + user.score);
            }
        }
    }

    private static void deleteUser() {
        System.out.print("\nEnter the username to delete: ");
        String username = scanner.nextLine();

        if (users.containsKey(username)) {
            if (username.equals(ADMIN_USERNAME)) {
                System.out.println("Cannot delete admin user.");
            } else {
                users.remove(username);
                System.out.println("User deleted successfully.");
            }
        } else {
            System.out.println("User not found.");
        }
    }
    
    private static void addNewQuestion() {
        System.out.println("\n===== ADD NEW QUESTION =====");
        System.out.print("Enter question text: ");
        String questionText = scanner.nextLine();
        
        String[] options = new String[4];
        System.out.println("Enter 4 options:");
        for (int i = 0; i < 4; i++) {
            System.out.print("Option " + (char)('A' + i) + ": ");
            options[i] = (char)('A' + i) + ". " + scanner.nextLine();
        }
        
        char correctAnswer;
        while (true) {
            System.out.print("Enter correct answer (A/B/C/D): ");
            String answerInput = scanner.nextLine().toUpperCase();
            if (answerInput.length() > 0 && 
                (answerInput.charAt(0) == 'A' || answerInput.charAt(0) == 'B' || 
                 answerInput.charAt(0) == 'C' || answerInput.charAt(0) == 'D')) {
                correctAnswer = answerInput.charAt(0);
                break;
            } else {
                System.out.println("Invalid answer. Please enter A, B, C, or D.");
            }
        }
        
        questions.add(new Question(questionText, options, correctAnswer));
        System.out.println("Question added successfully.");
    }
    
    private static void viewAllQuestions() {
        System.out.println("\n===== ALL QUESTIONS =====");
        if (questions.isEmpty()) {
            System.out.println("No questions found.");
            return;
        }
        
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println((i+1) + ". " + q.questionText);
            for (String option : q.options) {
                System.out.println("   " + option);
            }
            System.out.println("   Correct Answer: " + q.correctAnswer);
            System.out.println();
        }
    }
    
    private static void removeQuestion() {
        viewAllQuestions();
        
        if (questions.isEmpty()) {
            return;
        }
        
        System.out.print("Enter question number to remove: ");
        try {
            int questionNum = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (questionNum > 0 && questionNum <= questions.size()) {
                questions.remove(questionNum - 1);
                System.out.println("Question removed successfully.");
            } else {
                System.out.println("Invalid question number.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine(); // Clear invalid input
        }
    }
    
    private static void resetUserScore() {
        System.out.print("\nEnter the username to reset score: ");
        String username = scanner.nextLine();

        if (users.containsKey(username) && !username.equals(ADMIN_USERNAME)) {
            User user = users.get(username);
            user.score = 0;
            user.examHistory.clear();
            System.out.println("Score reset successfully for " + username);
        } else {
            System.out.println("User not found or cannot reset admin score.");
        }
    }
    
    private static void changeAdminPassword() {
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();
        
        if (!currentPassword.equals(currentUser.password)) {
            System.out.println("Incorrect current password.");
            return;
        }
        
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Please try again.");
            return;
        }

        currentUser.password = newPassword;
        users.put(ADMIN_USERNAME, currentUser);
        System.out.println("Admin password changed successfully.");
    }

    private static void userMenu() {
        while (true) {
            System.out.println("\n===== USER MENU =====");
            System.out.println("1. Update Profile");
            System.out.println("2. Start Exam");
            System.out.println("3. View Exam Results");
            System.out.println("4. View Exam History");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
                continue;
            }

            switch (choice) {
                case 1:
                    updateProfile();
                    break;
                case 2:
                    startExam();
                    break;
                case 3:
                    viewExamResults();
                    break;
                case 4:
                    viewExamHistory();
                    break;
                case 5:
                    logout();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void updateProfile() {
        System.out.println("\n===== UPDATE PROFILE =====");
        System.out.print("Enter new name (leave blank to keep current): ");
        String newName = scanner.nextLine();
        if (!newName.isEmpty()) {
            currentUser.name = newName;
        }
        
        System.out.print("Do you want to change password? (Y/N): ");
        String changePassword = scanner.nextLine();
        
        if (changePassword.equalsIgnoreCase("Y")) {
            System.out.print("Enter current password: ");
            String currentPassword = scanner.nextLine();
            
            if (!currentPassword.equals(currentUser.password)) {
                System.out.println("Incorrect current password.");
                return;
            }
            
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine();
            System.out.print("Confirm new password: ");
            String confirmPassword = scanner.nextLine();

            if (!newPassword.equals(confirmPassword)) {
                System.out.println("Passwords do not match. Please try again.");
                return;
            }

            currentUser.password = newPassword;
        }
        
        users.put(currentUser.username, currentUser);
        System.out.println("Profile updated successfully.");
    }

    private static void startExam() {
        if (examInProgress) {
            System.out.println("Exam is already in progress.");
            return;
        }
        
        if (questions.isEmpty()) {
            System.out.println("No questions available for the exam.");
            return;
        }

        examInProgress = true;
        int previousScore = currentUser.score;
        currentUser.score = 0; // Reset score for this exam
        
        System.out.println("\n===== EXAM STARTED =====");
        System.out.println("You have 100 seconds to answer " + questions.size() + " questions.");

        Thread timerThread = new Thread(() -> {
            try {
                Thread.sleep(100000); // 100 seconds
                autoSubmit();
            } catch (InterruptedException e) {
                System.out.println("Exam submitted.");
            }
        });
        timerThread.start();

        for (int i = 0; i < questions.size(); i++) {
            if (!examInProgress) {
                break; // Exit if the exam is auto-submitted
            }
            
            Question q = questions.get(i);
            System.out.println("\nQuestion " + (i+1) + ": " + q.questionText);
            for (String option : q.options) {
                System.out.println(option);
            }
            
            System.out.print("Your answer (A/B/C/D): ");
            String userInput = scanner.nextLine().toUpperCase();
            if (userInput.length() > 0) {
                char userAnswer = userInput.charAt(0);
                if (userAnswer == q.correctAnswer) {
                    currentUser.score++;
                }
            } else {
                System.out.println("No answer provided. Skipping to the next question.");
            }
        }

        if (examInProgress) {
            timerThread.interrupt();
            examInProgress = false;
            finishExam(previousScore);
        }
    }
    
    private static void finishExam(int previousScore) {
        String result = "Exam Date: " + java.time.LocalDate.now() + 
                       ", Score: " + currentUser.score + "/" + questions.size() + 
                       " (" + (currentUser.score * 100 / questions.size()) + "%)";
        
        currentUser.addExamResult(result);
        
        System.out.println("\n===== EXAM FINISHED =====");
        System.out.println("Your score: " + currentUser.score + "/" + questions.size());
        System.out.println("Percentage: " + (currentUser.score * 100 / questions.size()) + "%");
        
        // Set the final score (cumulative)
        currentUser.score += previousScore;
        
        users.put(currentUser.username, currentUser);
    }

    private static void viewExamResults() {
        System.out.println("\n===== EXAM RESULTS =====");
        System.out.println("Your current total score: " + currentUser.score);
    }
    
    private static void viewExamHistory() {
        System.out.println("\n===== EXAM HISTORY =====");
        if (currentUser.examHistory.isEmpty()) {
            System.out.println("No exam history found.");
            return;
        }
        
        for (int i = 0; i < currentUser.examHistory.size(); i++) {
            System.out.println((i+1) + ". " + currentUser.examHistory.get(i));
        }
    }

    private static void autoSubmit() {
        if (examInProgress) {
            System.out.println("\nTime's up! Auto-submitting your exam...");
            examInProgress = false;
            int previousScore = currentUser.score;
            finishExam(previousScore);
        }
    }

    private static void logout() {
        currentUser = null;
        System.out.println("Logged out successfully.");
    }

    private static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            oos.writeObject(users);
            System.out.println("Users saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private static void loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.dat"))) {
            users = (HashMap<String, User>) ois.readObject();
            System.out.println("Users loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No saved users found. Starting with default users.");
            users = new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading users: " + e.getMessage());
            users = new HashMap<>();
        }
    }
    
    private static void saveQuestions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("questions.dat"))) {
            oos.writeObject(questions);
            System.out.println("Questions saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving questions: " + e.getMessage());
        }
    }
    
    private static void loadQuestions() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("questions.dat"))) {
            questions = (ArrayList<Question>) ois.readObject();
            System.out.println("Questions loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No saved questions found. Starting with default questions.");
            questions = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading questions: " + e.getMessage());
            questions = new ArrayList<>();
        }
    }
}