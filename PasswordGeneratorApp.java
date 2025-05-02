import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class PasswordGeneratorApp extends JFrame {
    private JTextArea outputArea;
    private JComboBox<String> wordlistCombo;
    private JComboBox<String> modeCombo;
    private JSpinner lengthSpinner;
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ@Ł!&#$/<>*ł?£&{}[]€;:-_÷^";
    private static final String JOHN_LIST_PATH = "~/usr/share/wordlists/john.lst";
    private static final String WIFILITE_LIST_PATH = "~/usr/share/wordlists/wifilite.txt";
    private static final String FILE_NAME = "passwords5.txt";

    public PasswordGeneratorApp() {
        setTitle("Password Generator");
        setMinimumSize(new Dimension(550, 400));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Gornji panel za inpute
        JPanel inputPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        wordlistCombo = new JComboBox<>(new String[]{"john.lst", "wifilite.txt", "both (1 and 2)"});
        modeCombo = new JComboBox<>(new String[]{"Manual", "Automatic", "Scan"});
        lengthSpinner = new JSpinner(new SpinnerNumberModel(8, 8, 15, 1));

        inputPanel.add(new JLabel("Select wordlist:"));
        inputPanel.add(wordlistCombo);
        inputPanel.add(new JLabel("Select generation mode:"));
        inputPanel.add(modeCombo);
        inputPanel.add(new JLabel("Password length (8-15):"));
        inputPanel.add(lengthSpinner);
        add(inputPanel, BorderLayout.NORTH);

        // Gumbi u sredini
        JButton generateBtn = new JButton("Generate Password");
        JButton exitBtn = new JButton("Exit");

        generateBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        exitBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        Dimension btnSize = new Dimension(150, 25);
        generateBtn.setPreferredSize(btnSize);
        exitBtn.setPreferredSize(btnSize);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(generateBtn);
        buttonPanel.add(exitBtn);

        // Output zona koja se skalira
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Panel za gumbe i izlaz
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.CENTER);

        // Gumb funkcije
        generateBtn.addActionListener(e -> generatePasswordAction());
        exitBtn.addActionListener(e -> System.exit(0));
    }

    private static Map<String, Integer> readPasswords() {
        Map<String, Integer> passwords = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    passwords.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
        } catch (IOException e) {
            // ako ne postoji datoteka, ignoriraj
        }
        return passwords;
    }

    private static void writePasswords(Map<String, Integer> passwords) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, Integer> entry : passwords.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> readWordlist(String path) {
        Set<String> blacklist = new HashSet<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(path.replace("~", System.getProperty("user.home"))));
            blacklist.addAll(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blacklist;
    }

    private static String generatePassword(int length) {
        StringBuilder password = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(rand.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    private void generatePasswordAction() {
        Map<String, Integer> passwords = readPasswords();
        Set<String> blacklist = new HashSet<>();

        int choice = wordlistCombo.getSelectedIndex();
        if (choice == 0) blacklist = readWordlist(JOHN_LIST_PATH);
        else if (choice == 1) blacklist = readWordlist(WIFILITE_LIST_PATH);
        else {
            blacklist.addAll(readWordlist(JOHN_LIST_PATH));
            blacklist.addAll(readWordlist(WIFILITE_LIST_PATH));
        }

        int passlen = (int) lengthSpinner.getValue();
        String password = "";
        boolean unique = false;
        String mode = (String) modeCombo.getSelectedItem();

        if ("Manual".equals(mode) || "Automatic".equals(mode)) {
            do {
                password = generatePassword(passlen);
            } while (blacklist.contains(password) || passwords.containsKey(password));
        } else if ("Scan".equals(mode)) {
            while (!unique) {
                password = generatePassword(new Random().nextInt(8) + 8); // 8-15
                if (!passwords.containsKey(password) && !blacklist.contains(password)) {
                    unique = true;
                }
            }
        }

        passwords.put(password, passwords.getOrDefault(password, 0) + 1);
        outputArea.setText("Generated Password: " + password + "\nThis password has been generated " + passwords.get(password) + " times.");
        writePasswords(passwords);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PasswordGeneratorApp().setVisible(true));
    }
}
