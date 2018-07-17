import org.apache.commons.lang3.tuple.Triple;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Guanliang
 * @since 2018/7/13
 */
public class IOUtils {
    public static List<Triple<Integer, Integer, Integer>> load(String filePath) {
        List<Triple<Integer, Integer, Integer>> presets = new ArrayList<>();
        String encoding = "utf-8";
        File file = new File(filePath);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            Long fileLength = file.length();
            byte[] fileContent = new byte[fileLength.intValue()];

            FileInputStream in = new FileInputStream(file);
            in.read(fileContent);
            in.close();

            String text = new String(fileContent, encoding);
            String[] lines = text.split("\n");
            for (int i = 0; i < Sudoku.LINE_SIZE; i++) {
                for (int j = 0; j < Sudoku.LINE_SIZE; j++) {
                    char ch = lines[i].toCharArray()[j];
                    if (ch != ' ') {
                        presets.add(Triple.of(i, j, ch - '0'));
                    }
                }
            }
            return presets;
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeStrToFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(filePath);
        writer.write(content);
        writer.close();
    }
}
