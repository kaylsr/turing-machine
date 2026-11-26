import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Program{
    private final List<Instructions> instructions;
    private final Map<String, Integer> labels;

    public program(List<Instructions> instructions, Map<String, Integer> labels) {
        this.instructions = Collections.unmodifiableList(instructions);
        this.labels = Collections.unmodifiableMap(labels);
    }

    public List<Instructions> getInstructions() {
        return instructions;
    }

    public Map<String, Integer> getLabels() {
        return labels;
    }
}
