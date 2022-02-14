import entity.engine.Engine;

import java.util.ArrayList;
import java.util.List;

public class MultiProcessTest implements  Runnable{
    public static void main(String[] args) {
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Thread thread = new Thread(new MultiProcessTest());
            list.add(thread);
            thread.start();
        }
    }

    @Override
    public void run() {
        Engine engine = new Engine("src/test/resources/test/config.json", 3);
        for (int i = 0; i < 1000; i++) {
            engine.nextStep();
            System.out.println(this + " " + i + " step");
        }
        System.out.println(engine.getFinishedVehicleCnt() + " " + engine.getAverageTravelTime() + "!!!!!!!!!!!!!!!!!!!!!!");
        engine.close();
    }
}
