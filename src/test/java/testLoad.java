import entity.engine.Engine;
import entity.flow.Flow;
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
//            RoadNet roadNet = new RoadNet();
//            roadNet.loadFromJson("src/test/resources/roadnet.json");
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append(roadNet.toString()).append("\n");
//            for (int i = 0; i < roadNet.getRoads().size(); i++) {
//                stringBuilder.append(roadNet.getRoads().get(i).getId()).append("\n");
//            }
//            for (int i = 0; i < roadNet.getLanes().size(); i++) {
//                stringBuilder.append(roadNet.getLanes().get(i).getId()).append("\n");
//            }
//            for (int i = 0; i < roadNet.getLaneLinks().size(); i++) {
//                stringBuilder.append(roadNet.getLaneLinks().get(i).getId()).append("\n");
//            }
//            bufferedWriterMethod("src/test/resources/write.txt", stringBuilder);
            Engine engine = new Engine();
            try {
                engine.loadConfig("src/test/resources/config.json");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(engine.getRoadNet().toString()).append("\n");
                stringBuilder.append(engine.getFlows().size());
                bufferedWriterMethod("src/test/resources/write.txt", stringBuilder);
            } catch (Exception e) {
                e.printStackTrace();
            }
//
//            System.out.println(engine.getFlows().size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
