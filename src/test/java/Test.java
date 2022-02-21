import entity.engine.Engine;

public class Test {
    public static void main(String[] args) {
        Engine engine = new Engine("src/test/resources/test/config.json", 4);
//        engine.loadFromFile("src/test/resources/test/2000step.json");
//        for (int i = 0; i < 2000; i++) {
//            engine.nextStep();
//            System.out.println(i + " step");
//            System.out.println(engine.getVehicleCount());
//        }
        int cnt = 1;
        while (true) {
            System.out.println(cnt + " step");
            engine.nextStep();
            cnt++;
            System.out.println(engine.getVehicleCount());
            if (engine.getVehicleCount() == 0) {
                break;
            }
        }
//        System.out.println(engine.getFinishedVehicleCnt() + " " + engine.getAverageTravelTime());
//        engine.saveArchiveToFile(engine.snapshot(), "src/test/resources/test/2000step.json");
        engine.close();
    }
}
