import entity.Grid;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

/**
 * @author Zhou Guanliang
 * @since 2018/7/12
 */
public class Sudoku {
    public static final int LINE_SIZE = 9;
    public static final int LINE_BLOCK_SIZE = 3;
    public static final String INNER_COLUMN_BORDER_SYMBOL = "┆";
    public static final String OUTER_COLUMN_BORDER_SYMBOL = "║";
    public static final String INNER_ROW_BORDER_SYMBOL = "┈";
    public static final String OUTER_ROW_BORDER_SYMBOL = "═";

    public static int scanCount = 0;

    private List<List<Grid>> board;

    public void init(List<Triple<Integer, Integer, Integer>> presets) {
        this.board = newBoard();
        presets.forEach(preset -> {
            Grid grid = this.board.get(preset.getLeft()).get(preset.getMiddle());
            grid.setValue(preset.getRight());
            grid.setPreset(true);
        });
    }

    public String toGraph() {
        String graph = "";
        for (int i = 0; i < LINE_SIZE; i++) {
            if (i == 0 || i == 3 || i == 6) {
                graph += getLineSplit(OUTER_ROW_BORDER_SYMBOL) + "\n";
            } else {
                graph += getLineSplit(INNER_ROW_BORDER_SYMBOL) + "\n";
            }
            for (int j = 0; j < LINE_SIZE; j++) {
                if (j == 0 || j == 3 || j == 6) {
                    graph += OUTER_COLUMN_BORDER_SYMBOL;
                } else {
                    graph += INNER_COLUMN_BORDER_SYMBOL;
                }

                Grid grid = this.board.get(i).get(j);
                int number = grid.isPreset() ? grid.getValue() : grid.getDraft();
                graph += (grid.isPreset() ? "*" : " ") + (number > 0 ? number : " ") + " ";
            }
            graph += OUTER_COLUMN_BORDER_SYMBOL + "\n";
        }
        return graph + getLineSplit(OUTER_ROW_BORDER_SYMBOL);
    }

    public boolean isLegal() {
        //check rows
        for (int i = 0; i < LINE_SIZE; i++) {
            for (int j = 0; j < LINE_SIZE; j++) {
                for (int k = j + 1; k < LINE_SIZE; k++) {
                    int number1 = this.board.get(i).get(j).getNumber();
                    int number2 = this.board.get(i).get(k).getNumber();
                    if (number1 > 0 && number2 > 0) {
                        if (number1 == number2) {
                            return false;
                        }
                    }
                }
            }
        }

        //check columns
        for (int i = 0; i < LINE_SIZE; i++) {
            for (int j = 0; j < LINE_SIZE; j++) {
                for (int k = j + 1; k < LINE_SIZE; k++) {
                    int number1 = this.board.get(j).get(i).getNumber();
                    int number2 = this.board.get(k).get(i).getNumber();
                    if (number1 > 0 && number2 > 0) {
                        if (number1 == number2) {
                            return false;
                        }
                    }
                }
            }
        }

        //check blocks
        for (int i = 0; i < LINE_BLOCK_SIZE; i++) {
            for (int j = 0; j < LINE_BLOCK_SIZE; j++) {
                List<List<Grid>> block = getBlock(i, j);
                for (int m = 0; m < LINE_SIZE; m++) {
                    for (int n = m + 1; n < LINE_SIZE; n++) {
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

    public void shuffle(int presetCount) {
        while (true) {
            boolean error = false;

            this.board = newBoard();
            Set<Integer> presetIndexes = new HashSet<>();
            for (int i = 0; i < presetCount; i++) {
                int index = randomInteger(0, LINE_SIZE * LINE_SIZE - 1);
                while (!presetIndexes.add(index)) {
                    index = randomInteger(0, LINE_SIZE * LINE_SIZE - 1);
                }
            }

            for (int index : presetIndexes) {
                Pair<Integer, Integer> location = getLocation(index);
                if (findPossibles(location).size() == 0) {
                    error = true;
                    break;
                }

                Grid grid = this.board.get(location.getLeft()).get(location.getRight());
                grid.setPreset(true);
                grid.setValue(randomInteger(1, LINE_SIZE));
                while (!this.isLegal() || this.getAnswer() == null) {
                    grid.setValue(randomInteger(1, LINE_SIZE));
                }
            }
            if (!error) {
                break;
            }
        }
    }

    public List<List<Grid>> getAnswer() {
        List<List<Grid>> answer = newBoard();
        copy(this.board, answer);

        scanCount = 0;
        if (scan(answer)) {
            return answer;
        } else {
            return null;
        }
    }

    public boolean isSolved() {
        for (int i = 0; i < LINE_SIZE; i++) {
            for (int j = 0; j < LINE_SIZE; j++) {
                if (getGrid(i, j).getNumber() == 0) {
                    return false;
                }
            }
        }

        if (!isLegal()) {
            return false;
        }

        return true;
    }

    public boolean put(int x, int y, int value) {
        Grid grid = getGrid(x, y);

        if (grid.isPreset()) {
            System.out.println("This grid cannot be modified!");
            return false;
        }

        int backUp = grid.getDraft();
        grid.setDraft(value);

        if (!isLegal()) {
            System.out.println("Conflicted!");
            grid.setDraft(backUp);
            return false;
        }

        return true;
    }

    public boolean remove(int x, int y) {
        Grid grid = getGrid(x, y);

        if (grid.isPreset()) {
            System.out.println("This grid cannot be modified!");
            return false;
        }

        grid.setDraft(0);
        return true;
    }

    public void answer() {
        List<List<Grid>> answer = getAnswer();
        if (answer != null) {
            System.out.println(SudokuUtils.toGraph(answer));
            return;
        }

        System.out.println("Your solution is incorrect, here's another way:");
        List<List<Grid>> presetBoard = newBoard();
        copy(this.board, presetBoard);
        for (int i = 0; i < LINE_SIZE; i++) {
            for (int j = 0; j < LINE_SIZE; j++) {
                if (!getGrid(i, j).isPreset()) {
                    getGrid(i, j).setDraft(0);
                }
            }
        }
        System.out.println(SudokuUtils.toGraph(SudokuUtils.getAnswer(presetBoard)));
    }

    public Triple<Integer, Integer, Integer> getTip() {
        List<List<Grid>> answer = getAnswer();
        if (answer == null) {
            return null;
        }
        List<Pair<Integer, Integer>> emptyLocations = new ArrayList<>();
        for (int i = 0; i < LINE_SIZE; i++) {
            for (int j = 0; j < LINE_SIZE; j++) {
                if (getGrid(i, j).getNumber() == 0) {
                    emptyLocations.add(Pair.of(i, j));
                }
            }
        }
        if (emptyLocations.size() == 0) {
            //Impossible exception
            return null;
        }
        int randomIndex = randomInteger(0, emptyLocations.size() - 1);
        Pair<Integer, Integer> randomLocation = emptyLocations.get(randomIndex);
        int number = answer.get(randomLocation.getLeft()).get(randomLocation.getRight()).getNumber();
        return Triple.of(randomLocation.getLeft(), randomLocation.getRight(), number);
    }

    private boolean scan(List<List<Grid>> answer) {
        scanCount++;
        for (int code = 0; code < LINE_SIZE * LINE_SIZE; code++) {
            Pair<Integer, Integer> location = getLocation(code);
            if (SudokuUtils.getGrid(answer, location).getNumber() == 0) {
                Set<Integer> possibles = SudokuUtils.findPossibles(answer, location);
                if (possibles.size() == 0) {
                    return false;
                }

                boolean found = false;
                for (Integer possible : possibles) {
                    SudokuUtils.getGrid(answer, location).setDraft(possible);
                    if (scan(answer)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    SudokuUtils.getGrid(answer, location).setDraft(0);
                    return false;
                }
            }
        }
        return true;
    }

    private Set<Integer> findPossibles(Pair<Integer, Integer> location) {
        Set<Integer> possibles = new HashSet<>();
        Set<Integer> impossibles = new HashSet<>();

        //check same row
        for (int i = 0; i < LINE_SIZE; i++) {
            if (i != location.getRight()) {
                int number = getGrid(location.getLeft(), i).getNumber();
                if (number > 0) {
                    impossibles.add(number);
                }
            }
        }

        //check same column
        for (int i = 0; i < LINE_SIZE; i++) {
            if (i != location.getLeft()) {
                int number = getGrid(i, location.getRight()).getNumber();
                if (number > 0) {
                    impossibles.add(number);
                }
            }
        }

        //check same block
        List<List<Grid>> block = getBlockByAbsolute(location);
        for (int i = 0; i < LINE_BLOCK_SIZE; i++) {
            for (int j = 0; j < LINE_BLOCK_SIZE; j++) {
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

    private static void copy(List<List<Grid>> originalBoard, List<List<Grid>> newBoard) {
        for (int i = 0; i < LINE_SIZE; i++) {
            for (int j = 0; j < LINE_SIZE; j++) {
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

    private List<List<Grid>> getBlock(int blockX, int blockY) {
        List<List<Grid>> block = new ArrayList<>(LINE_BLOCK_SIZE);
        for (int m = 0; m < LINE_BLOCK_SIZE; m++) {
            List<Grid> row = new ArrayList<>(LINE_BLOCK_SIZE);
            for (int n = 0; n < LINE_BLOCK_SIZE; n++) {
                row.add(this.board.get(blockX * 3 + m).get(blockY * 3 + n));
            }
            block.add(row);
        }
        return block;
    }

    private List<List<Grid>> getBlockByAbsolute(int x, int y) {
        int blockX = x / 3;
        int blockY = y / 3;
        return getBlock(blockX, blockY);
    }

    private List<List<Grid>> getBlockByAbsolute(Pair<Integer, Integer> location) {
        int blockX = location.getLeft() / 3;
        int blockY = location.getRight() / 3;
        return getBlock(blockX, blockY);
    }

    private Grid getGrid(int x, int y) {
        return this.board.get(x).get(y);
    }

    private Grid getGrid(Pair<Integer, Integer> location) {
        return this.board.get(location.getLeft()).get(location.getRight());
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

    private static List<List<Grid>> newBoard() {
        List<List<Grid>> board = new ArrayList<>(LINE_SIZE);
        for (int i = 0; i < LINE_SIZE; i++) {
            List<Grid> row = new ArrayList<>(LINE_SIZE);
            for (int j = 0; j < LINE_SIZE; j++) {
                row.add(new Grid());
            }
            board.add(row);
        }
        return board;
    }

    private static int randomInteger(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
}
