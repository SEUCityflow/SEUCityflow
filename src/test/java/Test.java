import entity.engine.Engine;

public class Test {
    public static void main(String[] args) {
        Engine engine = new Engine("src/test/resources/manhattan/config.json", 5);
//        engine.loadFromFile("");
        System.out.println("load finished");
        for (int i = 0; i < 750; i++) {
            engine.nextStep();
            System.out.println(i + " steps finished");
//            engine.saveToFile("");
//            engine.loadFromFile("");
//            engine.load(engine.snapshot());
        }
//        engine.saveToFile("");
        engine.close();
    }
}
