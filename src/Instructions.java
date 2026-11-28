public class Instructions {
    private final Operations operation;
    private final char symbol;
    private final String target;
    private final int sourceLine;

    public Instructions(Operations operation, char symbol, String target, int sourceLine) {
        this.operation = operation;
        this.symbol = symbol;
        this.target = target;
        this.sourceLine = sourceLine;
    }

    public Operations getOperation() {
        return operation;
    }

    public char getSymbol() {
        return symbol;
    }

    public String getTarget() {
        return target;
    }

    public int getSourceLine() {
        return sourceLine;
    }

    @Override 
    public String toString() {
        return switch (operation){
            case LEFT, RIGHT, HALT -> operation.name();
            case WRITE -> "WRITE " + symbol;
            case GOTO -> "GOTO " + target;
            case IF -> "IF " + symbol + " GOTO " + target;
        };
    }
}
