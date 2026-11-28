public class Machine {
    private final Program program;
    private final String originalInput;
    private final int stepLimit;

    private Tape tape;
    private int head;
    private int pc;
    private int steps;
    private Status status;
    private String statusMessage;

    public Machine(Program program, String originalInput, int stepLimit){
        if (program == null){
            throw new IllegalArgumentException("Program cannot be null.");
        }
        if (originalInput == null){
            throw new IllegalArgumentException("Input cannot be null.");
        }
        if (stepLimit <= 0){
            throw new IllegalArgumentException("Step limit must be positive.");
        }
        for (int i = 0; i < originalInput.length(); i++){
            if (Character.isWhitespace(originalInput.charAt(i))){
                throw new IllegalArgumentException("Input tape contains whitespace.");
            }
        }
        this.program = program;
        this.originalInput = originalInput;
        this.stepLimit = stepLimit;
        reset();
    }

    public void reset(){
        this.tape = new Tape();
        for (int i = 0; i < originalInput.length(); i++){
            tape.write(i, originalInput.charAt(i));
        }
        this.head = 0;
        this.pc = 0;
        this.steps = 0;
        this.status = Status.READY;
        this.statusMessage = "";
    }

    public boolean step(){
        if (status != Status.READY){
            return false;
        }

        if (pc < 0 || pc >= program.getInstructions().size()){
            status = Status.ERROR;
            statusMessage = "Program counter out of bounds before execution: pc = " + pc;
            return false;   
        }

        Instructions instruction = program.getInstructions().get(pc);
        steps++;

        switch (instruction.getOperation()){
            case LEFT -> {
                if (head > 0){
                    head--;
                }
                pc++;
            }
            case RIGHT -> {
                head++;
                pc++;
            }
            case WRITE -> {
                tape.write(head, instruction.getSymbol());
                pc++;
            }
            case IF -> {
                if (tape.read(head) == instruction.getSymbol()){
                    Integer targetPc = program.getLabels().get(instruction.getTarget());
                    if (targetPc == null){
                        status = Status.ERROR;
                        statusMessage = "Undefined jump target " + instruction.getTarget() + " at line " + instruction.getSourceLine() + ".";
                    } else {
                        pc = targetPc;
                    }
                } else {
                    pc++;
                }
            }
            case GOTO -> {
                Integer targetPc = program.getLabels().get(instruction.getTarget());
                if (targetPc == null){
                    status = Status.ERROR;
                    statusMessage = "Undefined jump target " + instruction.getTarget() + " at line " + instruction.getSourceLine() + ".";
                } else {
                    pc = targetPc;
                }
            }
            case HALT -> status = Status.HALTED;
        }

        if (status == Status.READY && pc >= program.getInstructions().size()){
            status = Status.ERROR;
            statusMessage = "Fell past the final instruction without HALT.";
        }

        if (status == Status.READY && steps >= stepLimit){
            status = Status.LIMIT_REACHED;
            statusMessage = "Step limit reached.";
        }

        return status == Status.READY;
    }

    public void run(){
        while (status == Status.READY){
            step();
        }
    }

    public Program getProgram() {
        return program;
    }
    
    public String getOriginalInput() {
        return originalInput;
    }

    public Tape getTape() {
        return tape;
    }

    public int getHead() {
        return head;
    }

    public int getPc() {
        return pc;
    }

    public int getSteps() {
        return steps;
    }

    public int getStepLimit() {
        return stepLimit;
    }

    public Status getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
