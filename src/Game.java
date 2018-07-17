import org.apache.commons.lang3.tuple.Triple;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Zhou Guanliang
 * @since 2018/7/13
 */
public class Game {
    private static final Set<String> COMMAND_PUT_LIST = new HashSet<>(Arrays.asList("put", "p"));
    private static final Set<String> COMMAND_DRAFT_LIST = new HashSet<>(Arrays.asList("draft", "d"));
    private static final Set<String> COMMAND_REMOVE_LIST = new HashSet<>(Arrays.asList("remove", "r"));
    private static final Set<String> COMMAND_DISPLAY_LIST = new HashSet<>(Arrays.asList("show", "s"));
    private static final Set<String> COMMAND_ANSWER_LIST = new HashSet<>(Arrays.asList("answer", "ans"));
    private static final Set<String> COMMAND_TIP_LIST = new HashSet<>(Arrays.asList("tip", "t"));
    private static final String COMMAND_HELP = "help";
    private static final String COMMAND_EXIT = "exit";

    private static Scanner scanner = new Scanner(System.in);
    private static String input;

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        while (true) {
            System.out.println("########################################");
            System.out.println("New game!");

            //Generate one puzzle
            System.out.println("Set number of preset grids:(1~80)");
            int number;
            while (true) {
                System.out.print(">");
                input = scanner.nextLine();
                if (COMMAND_EXIT.equals(input)) {
                    System.out.println("Bye~");
                    return;
                }
                try {
                    number = Integer.parseInt(input);
                    if (number >= 1 && number <= 80) {
                        break;
                    }
                    System.out.println("Only 1~80 supported!");
                } catch (NumberFormatException e) {
                    System.out.println("Only integer supported!");
                }
            }

            Sudoku sudoku = new Sudoku();
            sudoku.shuffle(number);
            solve(sudoku);
        }
    }

    private static void solve(Sudoku sudoku) {
        System.out.println(sudoku.toGraph());
        while (true) {
            if (sudoku.isSolved()) {
                System.out.println("Solved!");
                return;
            }

            System.out.print(">");
            input = scanner.nextLine();
            switch (input) {
                case COMMAND_EXIT:
                    System.out.println("Exited from this puzzle!");
                    return;
                case COMMAND_HELP:
                    System.out.println("Supported commands:");
                    System.out.println("[put/p x y value]: Put a number into a grid");
//                    System.out.println("[try/t x y value1 value2 ...]: Add numbers into the draft of a grid");
                    System.out.println("[del/d x y]: Remove the number of a grid");
                    System.out.println("[tip/t]: Get one tip for the next move");
                    System.out.println("[answer/ans]: Give up the puzzle and get the correct answer");
                    System.out.println("[help]: Show supported commands");
                    System.out.println("[exit]: Exit the game");
                    break;
                default:
                    if (COMMAND_DISPLAY_LIST.contains(input)) {
                        System.out.println(sudoku.toGraph());
                        break;
                    }

                    String[] parts = input.split(" ");
                    if (COMMAND_PUT_LIST.contains(parts[0])) {
                        if (parts.length != 4) {
                            System.out.println("Illegal format!");
                            break;
                        }
                        try {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            int value = Integer.parseInt(parts[3]);
                            boolean success = sudoku.put(x - 1, y - 1, value);
                            if (success) {
                                System.out.println(sudoku.toGraph());
                            }
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Illegal format!");
                            break;
                        }
                    } else if (COMMAND_REMOVE_LIST.contains(parts[0])) {
                        if (parts.length != 3) {
                            System.out.println("Illegal format!");
                            break;
                        }
                        try {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            boolean success = sudoku.remove(x - 1, y - 1);
                            if (success) {
                                System.out.println(sudoku.toGraph());
                            }
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Illegal format!");
                            break;
                        }
                    } else if (COMMAND_TIP_LIST.contains(parts[0])) {
                        if (parts.length != 1) {
                            System.out.println("Illegal format!");
                            break;
                        }
                        Triple<Integer, Integer, Integer> tip = sudoku.getTip();
                        if (tip == null) {
                            System.out.println("Your solution is incorrect, no tips can be provided!");
                        } else {
                            System.out.println("[" + (tip.getLeft() + 1) + ", " + (tip.getMiddle() + 1) + "] -> " + tip.getRight());
                        }
                        break;
                    } else if (COMMAND_ANSWER_LIST.contains(parts[0])) {
                        if (parts.length != 1) {
                            System.out.println("Illegal format!");
                            break;
                        }
                        sudoku.answer();
                        return;
                    } else {
                        System.out.println("Illegal command!");
                    }
            }
        }
    }
}
