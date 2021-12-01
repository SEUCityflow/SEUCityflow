import entity.engine.Engine;

public class Test {
    public static void main(String[] args) {
        Engine engine = new Engine("src/test/resources/config.json", 5);
        System.out.println("load finished");
        for (int i = 0; i < 3000; i++) {
            engine.nextStep();
        }
        engine.close();
    }
}
