import entity.Grid;

import java.util.List;

/**
 * @author Zhou Guanliang
 * @since 2018/7/12
 */
public class Test {
    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku();
//        sudoku.init(IOUtils.load("/Users/colin/Desktop/test2.txt"));
        sudoku.shuffle(50);
        System.out.println(sudoku.toGraph());
        System.out.println();
        List<List<Grid>> answer = sudoku.getAnswer();
        if (answer != null) {
            System.out.println(SudokuUtils.toGraph(answer));
            System.out.println("Scanned " + Sudoku.scanCount + " times");
        }
//        System.out.println(Sudoku.toGraph(Sudoku.generatePuzzle(50)));
    }

    public static void printBlock(List<List<Grid>> block) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int number = block.get(i).get(j).getNumber();
                String v = number > 0 ? number + "" : " ";
                System.out.print(v + " ");
            }
            System.out.println();
        }
    }
}
