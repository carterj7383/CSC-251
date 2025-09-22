import javax.swing.*;
import java.util.ArrayList;
import java.text.DecimalFormat;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GradeCalculator {
    private static ArrayList<Double> grades = new ArrayList<>();
    private static DecimalFormat df = new DecimalFormat("#.##");
    private static Icon brandIcon;

    public static void main(String[] args) {
        setupUi();           // ← apply theme & fonts
        brandIcon = makeBrandIcon(56, new Color(0x19A7A1), new Color(0x0F766E));

        showWelcomeMessage();

        int choice;
        do {
            choice = showMenu();
            processChoice(choice);
        } while (choice != 4);

        showGoodbyeMessage();
    }

    // -------------------- THEME --------------------

    /** Apply Nimbus LAF + professional dark/slate UI and modern fonts */
    private static void setupUi() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        // Palette
        Color bg      = new Color(0x0B1220);  // deep slate
        Color panelBg = new Color(0x101827);
        Color fg      = new Color(0xE5E7EB);  // light gray text
        Color accent  = new Color(0x19A7A1);  // teal
        Color btnBg   = new Color(0x162235);

        // Typography
        Font base  = new Font("Segoe UI", Font.PLAIN, 16);
        Font bold  = base.deriveFont(Font.BOLD, 16f);
        Font title = base.deriveFont(Font.BOLD, 18f);

        // Apply to OptionPane + children
        UIManager.put("OptionPane.background", bg);
        UIManager.put("Panel.background", panelBg);
        UIManager.put("OptionPane.messageForeground", fg);
        UIManager.put("OptionPane.foreground", fg);
        UIManager.put("OptionPane.messageFont", base);
        UIManager.put("OptionPane.font", base);

        // Buttons
        UIManager.put("Button.background", btnBg);
        UIManager.put("Button.foreground", fg);
        UIManager.put("Button.font", bold);
        UIManager.put("Button.focus", accent);

        // Text fields used by showInputDialog
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", Color.BLACK);
        UIManager.put("TextField.font", base);

        // Titles
        UIManager.put("Label.font", base);
        UIManager.put("TitledBorder.font", title);

        // Nimbus accents
        UIManager.put("nimbusBase", panelBg);
        UIManager.put("nimbusBlueGrey", panelBg);
        UIManager.put("control", panelBg);
        UIManager.put("text", fg);
        UIManager.put("nimbusFocus", accent);
        UIManager.put("nimbusSelectionBackground", accent);
    }

    /** Small in-memory gradient icon so we don’t rely on external image files */
    private static Icon makeBrandIcon(int size, Color c1, Color c2) {
        int s = Math.max(32, size);
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // radial gradient circle
        GradientPaint gp = new GradientPaint(0, 0, c1, s, s, c2);
        g.setPaint(gp);
        g.fillOval(0, 0, s, s);

        // subtle inner ring
        g.setStroke(new BasicStroke(Math.max(2f, s / 28f)));
        g.setColor(new Color(255, 255, 255, 40));
        g.drawOval(s/12, s/12, s - s/6, s - s/6);

        // checkmark motif
        g.setStroke(new BasicStroke(Math.max(3f, s / 18f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, 180));
        int x1 = s/4, y1 = s/2;
        int x2 = s/2, y2 = s - s/3;
        int x3 = s - s/6, y3 = s/3;
        g.drawPolyline(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);

        g.dispose();
        return new ImageIcon(img);
    }

    // -------------------- FLOW --------------------

    /** Shows welcome message to user */
    private static void showWelcomeMessage() {
        String msg = "<html><div style='width:320px'>"
            + "<div style='font-size:18px;font-weight:700;margin-bottom:6px'>Grade Calculator</div>"
            + "<div style='opacity:.9'>Track grades and see your running average & letter grade."
            + " Add as many grades as you like.</div></div></html>";

        JOptionPane.showMessageDialog(
            null,
            msg,
            "Welcome",
            JOptionPane.INFORMATION_MESSAGE,
            brandIcon
        );
    }

    /** Displays main menu and gets user choice */
    private static int showMenu() {
        String menu = "<html><div style='width:340px'>"
            + "<div style='font-size:16px;font-weight:700;margin-bottom:8px'>Menu</div>"
            + "<ol style='margin:0 0 8px 18px'>"
            + "<li>Add a Grade</li>"
            + "<li>View Current Average</li>"
            + "<li>View Letter Grade</li>"
            + "<li>Exit</li>"
            + "</ol>"
            + "<div style='opacity:.85'>Enter a number (1–4):</div>"
            + "</div></html>";

        int choice = 0;
        boolean validChoice = false;

        while (!validChoice) {
            try {
                String input = (String)JOptionPane.showInputDialog(
                    null,
                    menu,
                    "Grade Calculator",
                    JOptionPane.QUESTION_MESSAGE,
                    brandIcon,
                    null,
                    ""
                );

                if (input == null) {
                    choice = 4; // Treat cancel as exit
                    validChoice = true;
                } else {
                    choice = Integer.parseInt(input.trim());
                    if (choice >= 1 && choice <= 4) {
                        validChoice = true;
                    } else {
                        JOptionPane.showMessageDialog(
                            null,
                            "Please enter a number between 1 and 4.",
                            "Invalid Choice",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Please enter a valid number.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
        return choice;
    }

    /** Processes user's menu choice */
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

    // -------------------- FEATURES --------------------

    /** Adds a new grade to the collection */
    private static void addGrade() {
        while (true) {
            String input = (String)JOptionPane.showInputDialog(
                null,
                "Enter a grade (0–100). Click Cancel to stop.",
                "Add Grade",
                JOptionPane.QUESTION_MESSAGE,
                brandIcon,
                null,
                ""
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

    /** Calculates and displays current average */
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

    /** Determines and displays letter grade based on average */
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

    /** Shows goodbye message */
    private static void showGoodbyeMessage() {
        String message = "Thank you for using Grade Calculator!\n\n";
        if (!grades.isEmpty()) {
            double average = calculateAverage();
            message += "Final Statistics:\n"
                    + "Total Grades: " + grades.size() + "\n"
                    + "Final Average: " + df.format(average) + "%\n"
                    + "Letter Grade: " + getLetterGrade(average);
        }

        JOptionPane.showMessageDialog(null, message, "Goodbye",
            JOptionPane.INFORMATION_MESSAGE);
    }

    // -------------------- HELPERS --------------------

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
