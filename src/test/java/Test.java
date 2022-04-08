import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        try {
            engine eng = new engine("src/test/resources/manhattan/config.json", 4);
//            eng.load_from_file("src/test/resources/manhattan/5000step.json");
            int N = 5000;
            long startTime = System.currentTimeMillis(); //获取开始时间
            for (int i = 0; i < N; i++) {
                eng.next_step();
                if (i % 100 == 0) {
                    System.out.println(i + " steps");
                    System.out.println(eng.get_vehicle_count());
                }
            }
            long endTime = System.currentTimeMillis(); //获取结束时间
            System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间
            System.out.println(eng.get_finished_vehicle_count() + " " + eng.get_average_travel_time());
//            eng.save_to_file("src/test/resources/manhattan/5000step.json");
            eng.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
