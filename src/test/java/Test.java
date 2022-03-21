import entity.engine.Engine;

public class Test {
    public static void main(String[] args) {
        Engine engine = new Engine("src/test/resources/test/config.json", 4);
//        engine.loadFromFile("src/test/resources/test/2000step.json");
        int N = 10000;
        for (int i = 0; i < N; i++) {
            if (i % 50 == 0) {
                System.out.println(i + " steps");
                System.out.println(engine.getVehicleCount());
            }
        }
//        System.out.println(engine.getFinishedVehicleCnt() + " " + engine.getAverageTravelTime());
//        engine.saveArchiveToFile(engine.snapshot(), "src/test/resources/test/2000step.json");
        engine.close();
    }
}
