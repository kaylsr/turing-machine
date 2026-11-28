import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final int DEFAULT_STEP_LIMIT = 10_000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String filePath;
        String inputTape;
        int stepLimit = DEFAULT_STEP_LIMIT;

        if (args.length == 0) {
            System.out.print("Program File: ");
            filePath = scanner.nextLine().trim();

            System.out.print("Input Tape: ");
            inputTape = scanner.nextLine();

            System.out.print("Step Limit [default " + DEFAULT_STEP_LIMIT + "]: ");
            String limitInput = scanner.nextLine().trim();
            if (!limitInput.isEmpty()) {
                try {
                    stepLimit = Integer.parseInt(limitInput);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid step limit '" + limitInput + "': must be a positive integer.");
                    return;
                }
            }
        } else if (args.length == 2 || args.length == 3) {
            filePath = args[0];
            inputTape = args[1];
            if (args.length == 3) {
                try {
                    stepLimit = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid step limit '" + args[2] + "': must be a positive integer.");
                    return;
                }
            }
        } else {
            System.err.println("Usage: java Main <program_file> <input_tape> [step_limit]");
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            System.err.println("Failed to read program file '" + filePath + "': " + e.getMessage());
            return;
        }

        Program program;
        try {
            program = Parser.parse(lines);
        } catch (ProgramException e) {
            System.err.println("Parse error - " + e.getMessage());
            return;
        }

        Machine machine;
        try {
            machine = new Machine(program, inputTape, stepLimit);
        } catch (IllegalArgumentException e) {
            System.err.println("Machine Setup Error: " + e.getMessage());
            return;
        }

        printConfiguration(machine);

        while (true) {
            System.out.print("\nCommand (step, run, reset, quit): ");
            if (!scanner.hasNextLine()) {
                break;
            }

            String command = scanner.nextLine().trim().toLowerCase();
            switch (command) {
                case "step" -> {
                    if (machine.getStatus() != Status.READY) {
                        System.out.println("Status is " + formatStatus(machine.getStatus())
                                + ". Use 'reset' before stepping again.");
                    } else {
                        machine.step();
                        printConfiguration(machine);
                    }
                }

                case "run" -> {
                    if (machine.getStatus() != Status.READY) {
                        System.out.println("Status is " + formatStatus(machine.getStatus())
                                + ". Use 'reset' before running again.");
                    } else {
                        machine.run();
                        printConfiguration(machine);
                    }
                }

                case "reset" -> {
                    machine.reset();
                    printConfiguration(machine);
                }

                case "quit" -> {
                    System.out.println("Huzzah End.");
                    return;
                }

                case "" -> {
                    // Do nothing if the user just presses Enter.
                }

                default -> System.out.println("Unknown command '" + command
                        + "'. Valid Commands: step, run, reset, quit.");
            }
        }
    }

    public static void printConfiguration(Machine machine) {
        int head = machine.getHead();
        int cellCount = Math.max(11, machine.getTape().displayEndIndex(head) + 1);

        int[] widths = new int[cellCount];
        int innerWidth = 0;
        for (int i = 0; i < cellCount; i++) {
            widths[i] = String.valueOf(i).length();
            innerWidth += widths[i] + (i < cellCount - 1 ? 1 : 0);
        }

        String divider = "        " + "=".repeat(Math.max(innerWidth, 24));
        StringBuilder indexRow = new StringBuilder("Index   ");
        StringBuilder tapeRow = new StringBuilder("Tape    ");
        StringBuilder headRow = new StringBuilder("        ");

        for (int i = 0; i < cellCount; i++) {
            int w = widths[i];
            char val = machine.getTape().read(i);

            indexRow.append(i);
            tapeRow.append(val).append(" ".repeat(w - 1));
            headRow.append(i == head ? "^" : " ").append(" ".repeat(w - 1));

            if (i < cellCount - 1) {
                indexRow.append(" ");
                tapeRow.append(" ");
                headRow.append(" ");
            }
        }

        System.out.println();
        System.out.println(divider);
        System.out.println(indexRow);
        System.out.println(divider);
        System.out.println(tapeRow);
        System.out.println(divider);
        System.out.println(headRow);
        System.out.println();

        System.out.println("Head Index: " + head);
        System.out.println("Instruction: " + formatNextInstruction(machine));
        System.out.println("Steps: " + machine.getSteps());
        System.out.println("Status: " + formatStatus(machine.getStatus()));

        if (machine.getStatus() == Status.ERROR && !machine.getStatusMessage().isEmpty()) {
            System.out.println("Reason: " + machine.getStatusMessage());
        }
    }

    private static String formatNextInstruction(Machine machine) {
        int pc = machine.getPc();
        List<Instructions> list = machine.getProgram().getInstructions();

        if (pc < 0 || pc >= list.size()) {
            return "none (fell past the end)";
        }

        String prefix = "";
        for (Map.Entry<String, Integer> entry : machine.getProgram().getLabels().entrySet()) {
            if (entry.getValue() == pc) {
                prefix = entry.getKey() + ": ";
                break;
            }
        }

        return prefix + list.get(pc);
    }

    private static String formatStatus(Status status) {
        return switch (status) {
            case READY -> "Ready";
            case HALTED -> "HALTED";
            case ERROR -> "ERROR";
            case LIMIT_REACHED -> "LIMIT REACHED";
        };
    }
}