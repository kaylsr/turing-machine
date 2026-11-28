public class ProgramException extends Exception {
    private final int lineNumber;
    private final String reason;

    public ProgramException(int lineNumber, String reason){
        super("Line " + lineNumber + ": " + reason);
        this.lineNumber = lineNumber;
        this.reason = reason;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getReason() {
        return reason;
    }
}
