import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradeCalculator extends JFrame {

    // ---------- Data ----------
    private final List<Double> grades = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("#0.##");

    // ---------- Header widgets ----------
    private JLabel headerAvg;
    private JLabel headerLetter;
    private JLabel headerGpa;

    // ---------- Cards ----------
    private CardLayout cardLayout;
    private JPanel cardHost;

    // ---------- UI on cards ----------
    private JTextArea scoresArea;
    private JTable table;

    private Runnable averageUpdater;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GradeCalculator().setVisible(true));
    }

    public GradeCalculator() {
        applyNimbus();
        setDarkDefaults();

        setTitle("Grade Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));

        setContentPane(buildRoot());
        refreshHeader();
    }

    // ---------------- Look & Feel ----------------

    private static void applyNimbus() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
    }

    private void setDarkDefaults() {
        UIManager.put("control", new Color(0x1E2B2D));             // window bg
        UIManager.put("nimbusLightBackground", new Color(0x2B3B3F)); // cards/table bg
        UIManager.put("Label.foreground", Color.WHITE);             // all labels white
        UIManager.put("text", Color.WHITE);
    }

    // ---------------- Root layout ----------------

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0,0,new Color(0x0F766E), w,h,new Color(0x19A7A1)));
                g2.fillRect(0,0,w,h);
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("Grade Calculator ✅");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setIconTextGap(12);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        headerAvg = chip("Avg: 0");
        headerLetter = chip("—");
        headerGpa = chip("GPA: 0.00");

        JButton addBtn = primary("Add", e -> showCard("add"));
        JButton listBtn = primary("Grades", e -> showCard("list"));
        JButton avgBtn = primary("Average", e -> showCard("avg"));
        JButton exitBtn = primary("Exit", e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        right.add(headerAvg); right.add(headerLetter); right.add(headerGpa);
        right.add(addBtn); right.add(listBtn); right.add(avgBtn); right.add(exitBtn);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        cardLayout = new CardLayout();
        cardHost = new JPanel(cardLayout);
        cardHost.setBorder(new EmptyBorder(16, 16, 16, 16));

        cardHost.add(buildAddCard(), "add");
        cardHost.add(buildListCard(), "list");
        cardHost.add(buildAverageCard(), "avg");

        showCard("add");

        JPanel bodyWrap = new JPanel(new BorderLayout());
        bodyWrap.add(cardHost, BorderLayout.CENTER);
        return bodyWrap;
    }

    // ---------------- Cards ----------------

    private JComponent buildAddCard() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        page.add(instructionCard(
            "Instructions",
            new String[]{
                "• Enter one or more grades (0–100).",
                "• Separate by spaces, commas, or new lines.",
                "• Press Ctrl/⌘ + Enter or click “Add grades”."
            }
        ));
        page.add(Box.createVerticalStrut(12));

        JPanel wrap = card("Add grades");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        scoresArea = new JTextArea(4, 24);
        scoresArea.setLineWrap(true);
        scoresArea.setWrapStyleWord(true);
        scoresArea.setBackground(new Color(0x324346));
        scoresArea.setForeground(Color.WHITE);
        scoresArea.setCaretColor(Color.WHITE);
        scoresArea.setSelectedTextColor(Color.WHITE);
        scoresArea.setSelectionColor(new Color(0x19A7A1));

        JScrollPane areaScroll = new JScrollPane(scoresArea);
        areaScroll.setBorder(BorderFactory.createEmptyBorder());

        JButton add = primary("Add grades", e -> addGradesFromArea());
        JButton clear = ghost("Clear", e -> scoresArea.setText(""));

        gc.insets = new Insets(8, 16, 0, 16);
        gc.gridx=0; gc.gridy=0; form.add(areaScroll, gc);
        
        gc.gridy=1; gc.weightx=0; gc.fill = GridBagConstraints.NONE; 
        gc.insets = new Insets(8, 0, 0, 0); 
        form.add(add, gc);
        
        gc.gridy=2; form.add(clear, gc);

        // Ctrl/Cmd + Enter submits
        InputMap im = scoresArea.getInputMap();
        ActionMap am = scoresArea.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "submit");
        am.put("submit", new AbstractAction() { public void actionPerformed(ActionEvent e) { addGradesFromArea(); } });

        wrap.add(form, BorderLayout.CENTER);
        page.add(wrap);
        return page;
    }

    private JPanel instructionCard(String title, String[] lines) {
        JPanel wrap = new JPanel(new BorderLayout());
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 25));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel h = new JLabel(title);
        h.setFont(h.getFont().deriveFont(Font.BOLD, 16f));
        h.setForeground(Color.WHITE);
        card.add(h, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new GridLayout(lines.length, 1, 0, 6));
        for (String line : lines) {
            JLabel l = new JLabel(line);
            l.setForeground(new Color(0xE0FDFC));
            l.setFont(l.getFont().deriveFont(13f));
            list.add(l);
        }
        card.add(list, BorderLayout.CENTER);
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JComponent buildListCard() {
        JPanel wrap = card("Your grades");
        String[] cols = {"#", "Score"};
        table = new JTable(new AbstractTableModel() {
            @Override public int getRowCount() { return grades.size(); }
            @Override public int getColumnCount() { return 2; }
            @Override public String getColumnName(int c) { return cols[c]; }
            @Override public boolean isCellEditable(int r,int c){ return false; }
            @Override public Object getValueAt(int r, int c) {
                return c==0 ? r+1 : df.format(grades.get(r));
            }
        });

        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setForeground(Color.WHITE);
        table.setBackground(new Color(0x2B3B3F));
        table.setGridColor(new Color(0x3A4A4D));
        table.setSelectionBackground(new Color(0x0F766E));
        table.setSelectionForeground(Color.WHITE);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(0x2B3B3F) : new Color(0x324346));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        JTableHeader th = table.getTableHeader();
        th.setBackground(new Color(0x0F766E));
        th.setForeground(Color.WHITE);
        th.setFont(th.getFont().deriveFont(Font.BOLD));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        wrap.add(sp, BorderLayout.CENTER);

        // ---------- Toolkit row ----------
        JPanel toolkit = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        toolkit.setOpaque(false);

        JButton editBtn = primary("Edit Grade", e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String newVal = JOptionPane.showInputDialog(this,
                        "Enter new grade (0–100):", grades.get(row));
                if (newVal != null) {
                    try {
                        double updated = Double.parseDouble(newVal);
                        if (updated < 0 || updated > 100) {
                            toast("Grade must be 0–100");
                            return;
                        }
                        grades.set(row, updated);
                        ((AbstractTableModel) table.getModel()).fireTableDataChanged();
                        refreshHeader();
                    } catch (NumberFormatException ex) {
                        toast("Invalid number");
                    }
                }
            } else {
                toast("Select a grade first");
            }
        });

        JButton removeBtn = primary("Remove Grade", e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                grades.remove(row);
                ((AbstractTableModel) table.getModel()).fireTableDataChanged();
                refreshHeader();
            } else {
                toast("Select a grade first");
            }
        });

        JButton resetBtn = primary("Reset", e -> {
            grades.clear();
            ((AbstractTableModel) table.getModel()).fireTableDataChanged();
            refreshHeader();
            toast("All grades cleared");
        });

        toolkit.add(editBtn);
        toolkit.add(removeBtn);
        toolkit.add(resetBtn);

        wrap.add(toolkit, BorderLayout.SOUTH);
        return wrap;
    }

    private JComponent buildAverageCard() {
        JPanel wrap = card("Average");
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new GridLayout(3,1,8,8));

        JLabel avg = big("0");
        JLabel letter = big("—");
        JLabel gpa = big("0.00");

        avg.setForeground(Color.WHITE);
        letter.setForeground(Color.WHITE);
        gpa.setForeground(Color.WHITE);

        box.add(tile("Average", avg));
        box.add(tile("Letter", letter));
        box.add(tile("GPA", gpa));

        wrap.add(box, BorderLayout.CENTER);
        averageUpdater = () -> {
            double m = mean();
            avg.setText(df.format(m));
            letter.setText(letterOf(m));
            gpa.setText(df.format(toGpa(m)));
        };
        return wrap;
    }

    // ---------------- Actions ----------------

    private void addGradesFromArea() {
        String text = scoresArea.getText();
        if (text == null || text.trim().isEmpty()) { toast("Enter one or more grades (0–100)"); return; }

        Pattern p = Pattern.compile("(-?\\d+(?:[\\.,]\\d+)?)");
        Matcher m = p.matcher(text);

        int added = 0, skipped = 0, scanned = 0;
        while (m.find()) {
            scanned++;
            String token = m.group(1).replace(',', '.');
            try {
                double v = Double.parseDouble(token);
                if (v < 0 || v > 100) skipped++;
                else { grades.add(v); added++; }
            } catch (NumberFormatException ex) {
                skipped++;
            }
        }

        ((AbstractTableModel) table.getModel()).fireTableDataChanged();
        scoresArea.setText("");
        refreshHeader();
        if (added > 0) toast("Added " + added + (skipped>0? (" • Skipped " + skipped) : ""));
        else toast(scanned == 0 ? "No numbers found" : "No valid grades");
    }

    private void showCard(String name) { cardLayout.show(cardHost, name); }

    // ---------------- Helpers ----------------

    private double mean() {
        if (grades.isEmpty()) return 0;
        double sum = 0; for (double g : grades) sum += g; return sum / grades.size();
    }

    private String letterOf(double avg) {
        if (avg >= 97) return "A+"; if (avg >= 93) return "A"; if (avg >= 90) return "A-";
        if (avg >= 87) return "B+"; if (avg >= 83) return "B"; if (avg >= 80) return "B-";
        if (avg >= 77) return "C+"; if (avg >= 73) return "C"; if (avg >= 70) return "C-";
        if (avg >= 67) return "D+"; if (avg >= 63) return "D"; if (avg >= 60) return "D-";
        return "F";
    }

    private double toGpa(double avg) {
        if (avg < 60) {
            return 0.0;
        }
        return Math.min(4.0, (avg - 60) / 10.0);
    }

    private JPanel card(String title) {
        JPanel wrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int pad = 6, arc = 24;
                Shape r = new RoundRectangle2D.Float(pad, pad, getWidth()-pad*2, getHeight()-pad*2, arc, arc);
                g2.setColor(new Color(0,0,0, 70));
                g2.fill(r);
                g2.dispose();
            }
        };
        wrap.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("nimbusLightBackground"));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel h = new JLabel(title);
        h.setFont(h.getFont().deriveFont(Font.BOLD, 18f));
        h.setForeground(Color.WHITE);
        h.setBorder(new EmptyBorder(0, 0, 16, 0));
        card.add(h, BorderLayout.NORTH);

        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JLabel chip(String text) {
        JLabel chip = new JLabel(text, SwingConstants.CENTER);
        chip.setOpaque(true);
        chip.setBackground(new Color(255,255,255,32));
        chip.setForeground(Color.WHITE);
        chip.setBorder(new EmptyBorder(8,12,8,12));
        return chip;
    }

    private JButton primary(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.addActionListener(al);
        b.setFocusPainted(false);
        b.setBackground(new Color(0x19A7A1));
        b.setForeground(Color.WHITE);
        
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x19A7A1), 2, true),
            new EmptyBorder(8, 14, 8, 14)
        ));
        
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(0x12807C));
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(0x19A7A1));
            }
        });
        return b;
    }

    private JButton ghost(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.addActionListener(al);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0,0,0,60));
        
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255,255,255,100), 1, true),
                new EmptyBorder(8,14,8,14)));
        
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(0,0,0,100));
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(0,0,0,60));
            }
        });
        return b;
    }

    private Icon makeBrandIcon(int size, Color a, Color b) {
        int s = size;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, a, s, s, b));
        g.fillRoundRect(0, 0, s, s, s/3, s/3);
        g.dispose();
        return new ImageIcon(img);
    }

    private JLabel big(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 24f));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JPanel tile(String title, JComponent center) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(UIManager.getColor("nimbusLightBackground"));
        p.setBorder(new EmptyBorder(16,16,16,16));
        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 14f));
        p.add(t, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private void toast(String msg) {
        JWindow w = new JWindow(this);
        JLabel l = new JLabel(msg);
        l.setBorder(new EmptyBorder(10,14,10,14));
        l.setForeground(Color.WHITE);
        l.setOpaque(true);
        l.setBackground(new Color(0,0,0, 180));
        w.add(l);
        w.pack();
        Point p = getLocationOnScreen();
        w.setLocation(p.x + getWidth() - w.getWidth() - 24, p.y + getHeight() - w.getHeight() - 48);
        Timer t = new Timer(1400, e -> w.dispose());
        t.setRepeats(false);
        t.start();
        w.setVisible(true);
    }

    private void refreshHeader() {
        double m = mean();
        headerAvg.setText("Avg: " + df.format(m));
        headerLetter.setText(letterOf(m));
        headerGpa.setText("GPA: " + df.format(toGpa(m)));
        if (averageUpdater != null) averageUpdater.run();
    }
}
