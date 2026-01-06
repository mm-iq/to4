import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import Models.Truck;
import Visualization.MapPanel;
import App.SimulationLogger;

public class SkkmSwingApp {

    private SimulationEngine engine = new SimulationEngine();
    
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private MapPanel mapPanel; // Nasz nowy panel mapy

    public static void main(String[] args) {
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
        engine.setup();

        frame = new JFrame();
        frame.setTitle("System SKKM - Wizualizacja Mapy");
        frame.setBounds(50, 50, 1200, 700); // Powiększyłem okno
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        // 1. SPLIT PANE (Podział okna na lewo i prawo)
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.7); // Mapa zajmuje 70% szerokości
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        // 2. LEWA STRONA - MAPA
        // Przekazujemy listy z silnika do mapy, żeby miała co rysować
        // Uwaga: Musimy dodać gettery w silniku (patrz krok 3)
        mapPanel = new MapPanel(engine.getFireStations(), engine.getActiveIncidents());
        splitPane.setLeftComponent(mapPanel);

        // 3. PRAWA STRONA - TABELA I LOGI
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        
        // Tabela
        String[] columns = {"ID", "JRG", "Stan", "Czas"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(300, 400));
        rightPanel.add(tableScroll, BorderLayout.CENTER);

        // Logi
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(300, 200));
        rightPanel.add(logScroll, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
    }

    private void startSimulation() {
        // 100ms dla płynniejszej animacji
        Timer timer = new Timer(100, new ActionListener() {
            private int counter = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                counter++;

                // logi czytamy często, żeby nic nie "uciekało"
                updateLogs();

                // Silnik/tabela rzadziej (co 1 sek = 10 * 100ms)
                if (counter % 10 == 0) {
                    engine.tick();
                    updateTable();
                }

                mapPanel.repaint();
            }
        });
        timer.start();
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        List<Truck> trucks = engine.getAllTrucksFromAllStationsAsOneList(); 
        for (Truck t : trucks) {
            Object[] row = {
                t.getTruckId(),
                t.getTruckFireStation().getName(),
                t.getTruckState().toString(), 
                t.getElapsedTime()
            };
            tableModel.addRow(row);
        }
    }

    private void updateLogs() {
        List<String> newLogs = SimulationLogger.consumeLogs();
        for (String log : newLogs) {
            logArea.append(log + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
}