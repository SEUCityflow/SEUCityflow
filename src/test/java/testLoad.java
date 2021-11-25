import entity.engine.Engine;
import entity.flow.Flow;
import entity.roadNet.roadNet.Intersection;
import entity.roadNet.roadNet.RoadNet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class testLoad {
    public static void bufferedWriterMethod(String filepath, StringBuilder content) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath))) {
            bufferedWriter.write(String.valueOf(content));
        }
    }

    public static void main(String[] args) {
        try {
            Engine engine = new Engine("src/test/resources/config.json", 4);
            try {
                StringBuilder stringBuilder = new StringBuilder();
                for (Intersection intersection: engine.getRoadNet().getIntersections()) {
                    stringBuilder.append(intersection.getId()).append("\n");
                    stringBuilder.append(intersection.getRoads().size()).append("\n");
                }
                bufferedWriterMethod("src/test/resources/write.txt", stringBuilder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
