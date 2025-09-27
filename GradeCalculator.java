import javax.swing.*;
import java.util.ArrayList;
import java.text.DecimalFormat;
import java.awt.*;
import java.util.Scanner;

public class GradeCalculator {
    private static ArrayList<Double> grades = new ArrayList<>();
    private static DecimalFormat df = new DecimalFormat("#.##");

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Running in headless mode (no GUI). Falling back to console UI.\n");
            runConsole();
            return;
        }

        // ---- GUI path ----
        showWelcomeMessage();

        int choice;
        do {
            choice = showMenu();
            processChoice(choice);
        } while (choice != 4);

        showGoodbyeMessage();
    }

    // ===================== GUI IMPLEMENTATION (JOptionPane) =====================

    private static void showWelcomeMessage() {
        JOptionPane.showMessageDialog(null,
            "Welcome to the Grade Calculator System!\n\n" +
            "This program will help you track your grades\n" +
            "and calculate your current average.",
            "Grade Calculator",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private static int showMenu() {
        String menu = """
                Grade Calculator Menu
                =====================

                1. Add a Grade
                2. View Current Average
                3. View Letter Grade
                4. Exit

                Please enter your choice (1-4):
                """;

        int choice = 0;
        boolean validChoice = false;

        while (!validChoice) {
            try {
                String input = JOptionPane.showInputDialog(null, menu,
                        "Grade Calculator Menu", JOptionPane.QUESTION_MESSAGE);

                if (input == null) {
                    choice = 4; // Treat cancel as exit
                    validChoice = true;
                } else {
                    choice = Integer.parseInt(input.trim());
                    if (choice >= 1 && choice <= 4) {
                        validChoice = true;
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Please enter a number between 1 and 4.",
                                "Invalid Choice", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Please enter a valid number.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
        return choice;
    }

    private static void processChoice(int choice) {
        switch (choice) {
            case 1 -> addGrade();
            case 2 -> viewAverage();
            case 3 -> viewLetterGrade();
            case 4 -> { /* exit */ }
            default -> JOptionPane.showMessageDialog(null,
                    "Invalid choice. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addGrade() {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    null,
                    "Enter a grade (0–100). Click Cancel to stop.",
                    "Add Grade",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) return; // cancel

            try {
                double grade = Double.parseDouble(input.trim());
                if (grade < 0 || grade > 100) {
                    JOptionPane.showMessageDialog(null,
                            "Grade must be between 0 and 100.",
                            "Invalid Grade",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                grades.add(grade);
                JOptionPane.showMessageDialog(null,
                        "Grade " + df.format(grade) + " added.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                int again = JOptionPane.showConfirmDialog(
                        null, "Add another grade?", "Continue?", JOptionPane.YES_NO_OPTION);
                if (again != JOptionPane.YES_OPTION) return;

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Please enter a valid number (e.g., 88 or 92.5).",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void viewAverage() {
        if (grades.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No grades entered yet.",
                    "Average",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double average = calculateAverage();
        JOptionPane.showMessageDialog(null,
                "Current Average: " + df.format(average) + "%",
                "Average",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static void viewLetterGrade() {
        if (grades.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No grades entered yet.",
                    "Letter Grade",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double average = calculateAverage();
        String letter = getLetterGrade(average);
        JOptionPane.showMessageDialog(null,
                "Average: " + df.format(average) + "%\nLetter Grade: " + letter,
                "Letter Grade",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showGoodbyeMessage() {
        String message = "Thank you for using Grade Calculator!\n\n";
        if (!grades.isEmpty()) {
            double average = calculateAverage();
            message += "Final Statistics:\n" +
                    "Total Grades: " + grades.size() + "\n" +
                    "Final Average: " + df.format(average) + "%\n" +
                    "Letter Grade: " + getLetterGrade(average);
        }

        JOptionPane.showMessageDialog(null, message,
                "Goodbye", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===================== CONSOLE FALLBACK (headless) =====================

    private static void runConsole() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Grade Calculator (Console Mode)");
        System.out.println("--------------------------------");

        int choice;
        do {
            System.out.println("""
                    
                    1) Add a Grade
                    2) View Current Average
                    3) View Letter Grade
                    4) Exit
                    """);
            System.out.print("Enter choice (1-4): ");

            String line = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number 1-4.");
                choice = 0;
            }

            switch (choice) {
                case 1 -> addGradeConsole(sc);
                case 2 -> viewAverageConsole();
                case 3 -> viewLetterGradeConsole();
                case 4 -> System.out.println("\nGoodbye!");
                default -> System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 4);
    }

    private static void addGradeConsole(Scanner sc) {
        while (true) {
            System.out.print("Enter a grade (0–100) or blank to stop: ");
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return;

            try {
                double g = Double.parseDouble(s);
                if (g < 0 || g > 100) {
                    System.out.println("Grade must be between 0 and 100.");
                    continue;
                }
                grades.add(g);
                System.out.println("Added: " + df.format(g));
                System.out.print("Add another? (y/N): ");
                String again = sc.nextLine().trim().toLowerCase();
                if (!again.equals("y")) return;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (e.g., 88 or 92.5).");
            }
        }
    }

    private static void viewAverageConsole() {
        if (grades.isEmpty()) {
            System.out.println("No grades entered yet.");
            return;
        }
        double avg = calculateAverage();
        System.out.println("Current Average: " + df.format(avg) + "%");
    }

    private static void viewLetterGradeConsole() {
        if (grades.isEmpty()) {
            System.out.println("No grades entered yet.");
            return;
        }
        double avg = calculateAverage();
        System.out.println("Average: " + df.format(avg) + "%  Letter: " + getLetterGrade(avg));
    }

    // ===================== SHARED HELPERS =====================

    private static double calculateAverage() {
        double sum = 0.0;
        for (double g : grades) sum += g;
        return sum / grades.size();
    }

    private static String getLetterGrade(double average) {
        if (average >= 90) return "A";
        if (average >= 80) return "B";
        if (average >= 70) return "C";
        if (average >= 60) return "D";
        return "F";
    }
}
