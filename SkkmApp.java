import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.List;

import Models.Truck;
import App.SimulationLogger;

public class SkkmApp extends Application {

    private SimulationEngine engine = new SimulationEngine();
    private TableView<Truck> table = new TableView<>();
    private TextArea logArea = new TextArea();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 1. Inicjalizacja symulacji
        engine.setup();

        // 2. Konfiguracja Kolumn Tabeli
        
        // Kolumna ID
        TableColumn<Truck, String> idCol = new TableColumn<>("ID Wozu");
        idCol.setCellValueFactory(new PropertyValueFactory<>("truckId"));

        // Kolumna Stacja (pobieramy nazwę ze stacji przypisanej do wozu)
        TableColumn<Truck, String> stationCol = new TableColumn<>("Jednostka");
        stationCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTruckFireStation().getName()));

        // Kolumna Stan (tu zadziała nasze toString() ze stanów)
        TableColumn<Truck, String> stateCol = new TableColumn<>("Stan");
        stateCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTruckState().toString()));

        // Kolumna Czas
        TableColumn<Truck, Integer> timeCol = new TableColumn<>("Czas (s)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("elapsedTime"));

        table.getColumns().addAll(idCol, stationCol, stateCol, timeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Rozciągnij kolumny

        // 3. Konfiguracja panelu logów
        logArea.setPrefHeight(150);
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");

        // 4. Układ okna (Tabela na środku, Logi na dole)
        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(logArea);

        // 5. PĘTLA CZASU (Zamiast while(true))
        // To wywołuje się co 1 sekundę
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            
            // A. Wykonaj krok logiczny symulacji
            engine.tick(); 

            // B. Odśwież tabelę (pobierz najnowsze dane wozów)
            // (Dla prostoty czyścimy i dodajemy wszystko od nowa - przy <100 wozach jest to błyskawiczne)
            table.getItems().clear();
            table.getItems().addAll(engine.getAllTrucksForTable());

            // C. Pobierz nowe logi i dopisz do pola tekstowego
            List<String> newLogs = SimulationLogger.consumeLogs();
            for(String log : newLogs) {
                logArea.appendText(log + "\n");
            }
        }));
        
        timeline.setCycleCount(Timeline.INDEFINITE); // Działaj w nieskończoność
        timeline.play(); // Start!

        // 6. Wyświetlenie okna
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setTitle("System Wspomagania Decyzji SKKM");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}