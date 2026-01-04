package App;

import java.util.ArrayList;
import java.util.List;

public class SimulationLogger {
    
    private static final List<String> logBuffer = new ArrayList<>();

    public static void addLog(String message) {
        logBuffer.add(message);
    }

    public static List<String> consumeLogs() {
        List<String> logsCopy = new ArrayList<>(logBuffer);
        logBuffer.clear();
        return logsCopy;
    }
}
