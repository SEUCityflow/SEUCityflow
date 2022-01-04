import entity.engine.Engine;

public class Test {
    public static void main(String[] args) {
        Engine engine = new Engine("src/test/resources/manhattan/config.json", 3);
        for (int i = 0; i < 1000; i++) {
            engine.nextStep();
            System.out.println(i + " step");
        }
        engine.close();
    }
}
