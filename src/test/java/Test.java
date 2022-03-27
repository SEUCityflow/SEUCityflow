import entity.engine.Engine;

import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        try {
            Engine engine = new Engine("src/test/resources/test/config.json", 4);
//          engine.loadFromFile("src/test/resources/test/2000step.json");
            int N = 4000;
            for (int i = 0; i < N; i++) {
                engine.nextStep();
                if (i % 50 == 0) {
                    System.out.println(i + " steps");
                    System.out.println(engine.getVehicleCount());
                }
            }
//          System.out.println(engine.getFinishedVehicleCnt() + " " + engine.getAverageTravelTime());
//          engine.saveArchiveToFile(engine.snapshot(), "src/test/resources/test/2000step.json");
            engine.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
