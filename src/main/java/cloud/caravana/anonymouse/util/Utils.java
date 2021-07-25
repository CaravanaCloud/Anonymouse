package cloud.caravana.anonymouse.util;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class Utils {
    public static boolean isConfig(Path file) {
        return endsWith(file.toFile(),".yaml");
    }

    public static boolean endsWith(File file, String ext){
        return file.getName().endsWith(ext);
    }

    public static void writeFile(String stringToWrite, String outputFile) {
        try (var writer = new FileWriter(outputFile)){
            writer.append(stringToWrite);
            writer.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
