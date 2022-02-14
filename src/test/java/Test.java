import entity.engine.Engine;

public class Test {
    public static void main(String[] args) {
        Engine engine = new Engine("src/test/resources/test/config.json", 3);
//        engine.loadFromFile("src/test/resources/test/5000step.json");
        for (int i = 0; i < 5000; i++) {
            engine.nextStep();
            System.out.println(i + " step");
        }
        System.out.println(engine.getFinishedVehicleCnt() + " " + engine.getAverageTravelTime());
//        engine.saveArchiveToFile(engine.snapshot(), "src/test/resources/test/5000step.json");
        engine.close();
    }
}
