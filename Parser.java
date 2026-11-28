import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser{
    public static Program parse(List<String> lines) throws ProgramException{
        if (lines == null){
            throw new ProgramException(1, "Program lines cannot be null.");
        }

        List<Instructions> instructions = new ArrayList<>();
        Map<String, Integer> labels = new HashMap<>();

        for(int i = 0; i < lines.size(); i++){
            int lineNumber = i + 1;
            String trimmed = lines.get(i).trim();

            if (trimmed.isEmpty()){
                continue;
            }

            String[] tokens = trimmed.split("\\s+");
            int idx = 0;

            if(tokens[idx].endsWith(":")){
                String labelName = tokens[idx].substring(0, tokens[idx].length() - 1);
                if(!isValidLabel(labelName)){
                    throw new ProgramException(lineNumber, "Invalid label '" + labelName + "'.");
                }
                if(labels.containsKey(labelName)){
                    throw new ProgramException(lineNumber, "Duplicate label '" + labelName + "'.");
                }
                labels.put(labelName, instructions.size());
                idx++;
            }

            if (idx >= tokens.length){
                throw new ProgramException(lineNumber, "Label without an instruction.");
            }

            Operations operations = parseOperation(tokens[idx++], lineNumber);
            int remaining = tokens.length - idx;
            char symbol = '\0';
            String target = null;

            switch(operations){
                case LEFT, RIGHT, HALT -> {
                    if(remaining != 0){
                        throw new ProgramException(lineNumber, operations + " expects 0 arguments, got " + remaining + ".");
                    }
                }

                case WRITE -> {
                    if(remaining != 1){
                        throw new ProgramException(lineNumber, "WRITE expects 1 symbol argument, got " + remaining + ".");
                    }
                    String symToken = tokens[idx];
                    if(symToken.length() != 1){
                        throw new ProgramException(lineNumber, "Symbol '" + symToken + "' must be a single character.");
                    }
                    symbol = symToken.charAt(0);
                }

                case GOTO -> {
                    if(remaining != 1){
                        throw new ProgramException(lineNumber, "GOTO expects 1 label argument, got '" + remaining + ".");
                    }
                    target = tokens[idx];
                    if(!isValidLabel(target)){
                        throw new ProgramException(lineNumber, "Invalid target label '" + target + "'.");
                    }
                }

                case IF -> {
                    if(remaining != 3){
                        throw new ProgramException(lineNumber, "IF expects 3 tokens (<symbol> GOTO <label>), got '" + remaining + "'.");
                    }
                    String symToken = tokens[idx];
                    if(symToken.length() != 1){
                        throw new ProgramException(lineNumber, "Symbol '" + symToken + "' must be a single character.");
                    }
                    symbol = symToken.charAt(0);

                    if(!tokens[idx + 1].equals("GOTO")){
                        throw new ProgramException(lineNumber, "Expected 'GOTO' in IF instruction, got '" + tokens[idx + 1] + "'.");
                    }
                    target = tokens[idx + 2];
                    if(!isValidLabel(target)){
                        throw new ProgramException(lineNumber, "Invalid target label '" + target + "'.");
                    }
                }
            }
            instructions.add(new Instructions(operations, symbol, target, lineNumber));
        }
        if(instructions.isEmpty()){
            throw new ProgramException(1, "Program is empty.");
        }

        for (Instructions inst : instructions){
            if (inst.getOperation() == Operations.IF || inst.getOperation() == Operations.GOTO){
                if (!labels.containsKey(inst.getTarget())){
                    throw new ProgramException(inst.getSourceLine(), "Undefined jump target '" + inst.getTarget() + "'.");
                }
            }
        }
        return new Program(instructions, labels);
    }

    private static Operations parseOperation(String token, int lineNumber) throws ProgramException{
        return switch(token){
            case "LEFT" -> Operations.LEFT;
            case "RIGHT" -> Operations.RIGHT;
            case "WRITE" -> Operations.WRITE;
            case "IF" -> Operations.IF;
            case "GOTO" -> Operations.GOTO;
            case "HALT" -> Operations.HALT;
            default -> throw new ProgramException(lineNumber, "Unknown command '" + token + "'.");
        };
    }

    public static boolean isValidLabel(String label){
        return label != null && label.matches("[A-Za-z][A-Za-z0-9_]*");
    }
}