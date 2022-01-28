import entity.engine.Engine;

import java.util.ArrayList;
import java.util.List;

class test implements Runnable {
    @Override
    public void run() {
        Engine engine = new Engine("src/test/resources/test/config.json", 3);
//        for (int i = 0; i < 1000; i++) {
//            engine.nextStep();
//            System.out.println(this + " " + i + " step");
//        }
//        System.out.println(engine.getFinishedVehicleCnt() + " " + engine.getAverageTravelTime() + "!!!!!!!!!!!!!!!!!!!!!!");
        engine.close();
    }
}

public class MultiProcessTest {
    public static void main(String[] args) throws InterruptedException {
        List<Thread> list = new ArrayList<>();
        System.out.println("start");
        for (int i = 0; i < 1; i++) {
            Thread thread = new Thread(new test());
            list.add(thread);
            thread.start();

        }
        System.out.println("before join!!!");
        for (Thread thread : list) {
            thread.join();
        }
        System.out.println("end join");
    }
}
