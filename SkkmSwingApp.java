import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Models.Truck;
import App.SimulationLogger;

public class SkkmSwingApp {

    private SimulationEngine engine = new SimulationEngine();
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea logArea;

    public static void main(String[] args) {
        // Uruchomienie w wątku interfejsu graficznego
        SwingUtilities.invokeLater(() -> {
            try {
                SkkmSwingApp window = new SkkmSwingApp();
                window.frame.setVisible(true);
                window.startSimulation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public SkkmSwingApp() {
        initialize();
    }

    private void initialize() {
        // 1. Setup silnika
        engine.setup();

        // 2. Główne okno
        frame = new JFrame();
        frame.setTitle("System SKKM - Symulacja");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        // 3. Tabela (Górna część)
        // Definiujemy kolumny
        String[] columns = {"ID Wozu", "Jednostka", "Stan", "Czas (s)"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        
        // Dodajemy tabelę do panelu z przewijaniem
        JScrollPane scrollPane = new JScrollPane(table);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // 4. Panel Logów (Dolna część)
        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BorderLayout());
        logPanel.setPreferredSize(new Dimension(800, 200));
        
        JLabel lblLogi = new JLabel("  Dziennik Zdarzeń:");
        logPanel.add(lblLogi, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logPanel.add(logScroll, BorderLayout.CENTER);

        frame.getContentPane().add(logPanel, BorderLayout.SOUTH);
    }

    private void startSimulation() {
        // 5. ZEGAR (Zamiast while(true))
        // To wykonuje się co 1000 ms (1 sekunda)
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // A. Krok symulacji
                engine.tick();

                // B. Odświeżenie tabeli
                updateTable();

                // C. Odświeżenie logów
                updateLogs();
            }
        });
        timer.start();
    }

    private void updateTable() {
        // Czyścimy stare dane
        tableModel.setRowCount(0);

        // Pobieramy nową listę wozów z silnika
        List<Truck> trucks = engine.getAllTrucksForTable();

        for (Truck t : trucks) {
            // Dodajemy wiersz do tabeli
            Object[] row = {
                t.getTruckId(),
                t.getTruckFireStation().getName(),
                t.getTruckState().toString(), // Tu zadziała Twoje toString() ze stanów
                t.getElapsedTime()
            };
            tableModel.addRow(row);
        }
    }

    private void updateLogs() {
        List<String> newLogs = SimulationLogger.consumeLogs();
        for (String log : newLogs) {
            logArea.append(log + "\n");
            // Autoscroll do dołu
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
}