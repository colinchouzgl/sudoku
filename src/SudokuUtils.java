import entity.Grid;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

/**
 * @author Zhou Guanliang
 * @since 2018/7/13
 */
public class SudokuUtils {
    public static int scanCount = 0;

    public static List<List<Grid>> init(List<Triple<Integer, Integer, Integer>> presets) {
        List<List<Grid>> puzzle = newBoard();
        presets.forEach(preset -> {
            Grid grid = puzzle.get(preset.getLeft()).get(preset.getMiddle());
            grid.setValue(preset.getRight());
            grid.setPreset(true);
        });
        return puzzle;
    }

    public static String toGraph(List<List<Grid>> board) {
        String graph = "";
        for (int i = 0; i < Sudoku.LINE_SIZE; i++) {
            if (i == 0 || i == 3 || i == 6) {
                graph += getLineSplit(Sudoku.OUTER_ROW_BORDER_SYMBOL) + "\n";
            } else {
                graph += getLineSplit(Sudoku.INNER_ROW_BORDER_SYMBOL) + "\n";
            }
            for (int j = 0; j < Sudoku.LINE_SIZE; j++) {
                if (j == 0 || j == 3 || j == 6) {
                    graph += Sudoku.OUTER_COLUMN_BORDER_SYMBOL;
                } else {
                    graph += Sudoku.INNER_COLUMN_BORDER_SYMBOL;
                }

                Grid grid = board.get(i).get(j);
                int number = grid.isPreset() ? grid.getValue() : grid.getDraft();
                graph += (grid.isPreset() ? "*" : " ") + (number > 0 ? number : " ") + " ";
            }
            graph += Sudoku.OUTER_COLUMN_BORDER_SYMBOL + "\n";
        }
        return graph + getLineSplit(Sudoku.OUTER_ROW_BORDER_SYMBOL);
    }

    public static boolean isLegal(List<List<Grid>> board) {
        //check rows
        for (int i = 0; i < Sudoku.LINE_SIZE; i++) {
            for (int j = 0; j < Sudoku.LINE_SIZE; j++) {
                for (int k = j + 1; k < Sudoku.LINE_SIZE; k++) {
                    int number1 = board.get(i).get(j).getNumber();
                    int number2 = board.get(i).get(k).getNumber();
                    if (number1 > 0 && number2 > 0) {
                        if (number1 == number2) {
                            return false;
                        }
                    }
                }
            }
        }

        //check columns
        for (int i = 0; i < Sudoku.LINE_SIZE; i++) {
            for (int j = 0; j < Sudoku.LINE_SIZE; j++) {
                for (int k = j + 1; k < Sudoku.LINE_SIZE; k++) {
                    int number1 = board.get(j).get(i).getNumber();
                    int number2 = board.get(k).get(i).getNumber();
                    if (number1 > 0 && number2 > 0) {
                        if (number1 == number2) {
                            return false;
                        }
                    }
                }
            }
        }

        //check blocks
        for (int i = 0; i < Sudoku.LINE_BLOCK_SIZE; i++) {
            for (int j = 0; j < Sudoku.LINE_BLOCK_SIZE; j++) {
                List<List<Grid>> block = getBlock(board, i, j);
                for (int m = 0; m < Sudoku.LINE_SIZE; m++) {
                    for (int n = m + 1; n < Sudoku.LINE_SIZE; n++) {
                        Pair<Integer, Integer> location1 = getBlockLocation(m);
                        Pair<Integer, Integer> location2 = getBlockLocation(n);
                        int number1 = block.get(location1.getLeft()).get(location1.getRight()).getNumber();
                        int number2 = block.get(location2.getLeft()).get(location2.getRight()).getNumber();
                        if (number1 > 0 && number2 > 0) {
                            if (number1 == number2) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public static List<List<Grid>> generatePuzzle(int presetCount) {
        List<List<Grid>> board;
        while (true) {
            boolean error = false;
            board = newBoard();
            Set<Integer> presetIndexes = new HashSet<>();
            for (int i = 0; i < presetCount; i++) {
                int index = randomInteger(Sudoku.LINE_SIZE * Sudoku.LINE_SIZE - 1, 0);
                while (!presetIndexes.add(index)) {
                    index = randomInteger(Sudoku.LINE_SIZE * Sudoku.LINE_SIZE - 1, 0);
                }
            }

            for (int index : presetIndexes) {
                Pair<Integer, Integer> location = getLocation(index);
                if (findPossibles(board, location).size() == 0) {
                    error = true;
                    break;
                }

                Grid grid = board.get(location.getLeft()).get(location.getRight());
                grid.setPreset(true);
                grid.setValue(randomInteger(Sudoku.LINE_SIZE, 1));
                while (!isLegal(board) || getAnswer(board) == null) {
                    grid.setValue(randomInteger(Sudoku.LINE_SIZE, 1));
                }
            }
            if (!error) {
                break;
            }
        }

        return board;
    }

    public static List<List<Grid>> getAnswer(List<List<Grid>> puzzle) {
        List<List<Grid>> answer = newBoard();

        copy(puzzle, answer);

        scanCount = 0;
        if (scan(answer)) {
            return answer;
        } else {
            return null;
        }
    }

    private static boolean scan(List<List<Grid>> board) {
        scanCount++;
        for (int code = 0; code < Sudoku.LINE_SIZE * Sudoku.LINE_SIZE; code++) {
            Pair<Integer, Integer> location = getLocation(code);
            if (getGrid(board, location).getNumber() == 0) {
                Set<Integer> possibles = findPossibles(board, location);
                if (possibles.size() == 0) {
                    return false;
                }

                boolean found = false;
                for (Integer possible : possibles) {
                    getGrid(board, location).setDraft(possible);
                    if (scan(board)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    getGrid(board, location).setDraft(0);
                    return false;
                }
            }
        }
        return true;
    }

    public static Set<Integer> findPossibles(List<List<Grid>> board, Pair<Integer, Integer> location) {
        Set<Integer> possibles = new HashSet<>();
        Set<Integer> impossibles = new HashSet<>();

        //check same row
        for (int i = 0; i < Sudoku.LINE_SIZE; i++) {
            if (i != location.getRight()) {
                int number = getGrid(board, location.getLeft(), i).getNumber();
                if (number > 0) {
                    impossibles.add(number);
                }
            }
        }

        //check same column
        for (int i = 0; i < Sudoku.LINE_SIZE; i++) {
            if (i != location.getLeft()) {
                int number = getGrid(board, i, location.getRight()).getNumber();
                if (number > 0) {
                    impossibles.add(number);
                }
            }
        }

        //check same block
        List<List<Grid>> block = getBlockByAbsolute(board, location);
        for (int i = 0; i < Sudoku.LINE_BLOCK_SIZE; i++) {
            for (int j = 0; j < Sudoku.LINE_BLOCK_SIZE; j++) {
                if (getCodeInBlock(location) != getCodeInBlockByRelative(i, j)) {
                    int number = block.get(i).get(j).getNumber();
                    impossibles.add(number);
                }
            }
        }

        for (int i = 1; i <= 9; i++) {
            if (!impossibles.contains(i)) {
                possibles.add(i);
            }
        }
        return possibles;
    }

    private static List<List<Grid>> newBoard() {
        List<List<Grid>> board = new ArrayList<>(Sudoku.LINE_SIZE);
        for (int i = 0; i < Sudoku.LINE_SIZE; i++) {
            List<Grid> row = new ArrayList<>(Sudoku.LINE_SIZE);
            for (int j = 0; j < Sudoku.LINE_SIZE; j++) {
                row.add(new Grid());
            }
            board.add(row);
        }
        return board;
    }

    private static void copy(List<List<Grid>> originalBoard, List<List<Grid>> newBoard) {
        for (int i = 0; i < Sudoku.LINE_SIZE; i++) {
            for (int j = 0; j < Sudoku.LINE_SIZE; j++) {
                Grid originalGrid = originalBoard.get(i).get(j);
                newBoard.get(i).set(j, new Grid(originalGrid.getValue(), 0, originalGrid.isPreset()));
            }
        }
    }

    private static String getLineSplit(String symbol) {
        String split = "";
        for (int n = 0; n < 37; n++) {
            split += symbol;
        }
        return split;
    }

    private static List<List<Grid>> getBlock(List<List<Grid>> board, int blockX, int blockY) {
        List<List<Grid>> block = new ArrayList<>(Sudoku.LINE_BLOCK_SIZE);
        for (int m = 0; m < Sudoku.LINE_BLOCK_SIZE; m++) {
            List<Grid> row = new ArrayList<>(Sudoku.LINE_BLOCK_SIZE);
            for (int n = 0; n < Sudoku.LINE_BLOCK_SIZE; n++) {
                row.add(board.get(blockX * 3 + m).get(blockY * 3 + n));
            }
            block.add(row);
        }
        return block;
    }

    private static List<List<Grid>> getBlockByAbsolute(List<List<Grid>> board, int x, int y) {
        int blockX = x / 3;
        int blockY = y / 3;
        return getBlock(board, blockX, blockY);
    }

    private static List<List<Grid>> getBlockByAbsolute(List<List<Grid>> board, Pair<Integer, Integer> location) {
        int blockX = location.getLeft() / 3;
        int blockY = location.getRight() / 3;
        return getBlock(board, blockX, blockY);
    }

    private static Pair<Integer, Integer> getBlockLocation(int code) {
        int x = code / 3;
        int y = code % 3;
        return Pair.of(x, y);
    }

    private static Pair<Integer, Integer> getLocation(int code) {
        int x = code / 9;
        int y = code % 9;
        return Pair.of(x, y);
    }

    private static int getCode(int x, int y) {
        return x * 9 + y;
    }

    private static int getCode(Pair<Integer, Integer> location) {
        return getCode(location.getLeft(), location.getRight());
    }

    private static int getCodeInBlock(int x, int y) {
        int xInBlock = x % 3;
        int yInBlock = y % 3;
        return getCodeInBlockByRelative(xInBlock, yInBlock);
    }

    private static int getCodeInBlock(Pair<Integer, Integer> location) {
        return getCodeInBlock(location.getLeft(), location.getRight());
    }

    private static int getCodeInBlockByRelative(int xInBlock, int yInBlock) {
        return xInBlock * 3 + yInBlock;
    }

    private static Grid getGrid(List<List<Grid>> board, int x, int y) {
        return board.get(x).get(y);
    }

    public static Grid getGrid(List<List<Grid>> board, Pair<Integer, Integer> location) {
        return board.get(location.getLeft()).get(location.getRight());
    }

    private static int randomInteger(int max, int min) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
}
