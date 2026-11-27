import java.util.HashMap;
import java.util.Map;

public class Tape{
    public static final char BLANK = '_';
    private final Map<Integer, Character> cells = new HashMap<>();

    public char read(int position) {
        return cells.getOrDefault(position, BLANK);
    }

    public void write(int position, char symbol) {
        if (symbol == BLANK) {
            cells.remove(position);
        } else {
            cells.put(position, symbol);
        }
    }

    public int highestNonBlank(){
        int max = 0;
        for (int position : cells.keySet()) {
            if (position > max) {
                max = position;
            }
        }
        return max;
    }

    public int displayEndIndex(int head){
        return Math.max(highestNonBlank(), head);
    }
}