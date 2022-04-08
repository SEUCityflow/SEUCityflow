import com.alibaba.fastjson.JSON;
import entity.archive.Archive;
import entity.engine.Engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class engine { // 供 python 调用
    private final Engine eng;

    public engine(String configFile, int threadNum) throws IOException {
        eng = new Engine(configFile, threadNum);
    }

    public int get_finished_vehicle_count() {
        return eng.getFinishedVehicleCnt();
    }

    public void next_step() {
        eng.nextStep();
    }

    public int get_vehicle_count() {
        return eng.getVehicleCount();
    }

    public String get_vehicles() {
        return JSON.toJSONString(eng.getVehicles(false));
    }

    public String get_vehicles(boolean includeWaiting) {
        return JSON.toJSONString(eng.getVehicles(includeWaiting));
    }

    public String get_lane_waiting_vehicle_count() {
        return JSON.toJSONString(eng.getLaneWaitingVehicleCount());
    }

    public String get_lane_vehicle_count() {
        return JSON.toJSONString(eng.getLaneVehicleCount());
    }

    public String get_lane_vehicles() {
        return JSON.toJSONString(eng.getLaneVehicles());
    }

    public String get_vehicle_speed() {
        return JSON.toJSONString(eng.getVehicleSpeed());
    }

    public String get_vehicle_info(String id) {
        return JSON.toJSONString(eng.getVehicleInfo(id));
    }

    public String get_vehicle_distance() {
        return JSON.toJSONString(eng.getVehicleDistance());
    }

    public String get_leader(String vehicleId) {
        return eng.getLeader(vehicleId);
    }

    public float get_current_time() {
        return (float) eng.getCurrentTime();
    }

    public float get_average_travel_time() {
        return (float) eng.getAverageTravelTime();
    }

    public void set_tl_phase(String id, int phaseIndex) {
        eng.setTrafficLightPhase(id, phaseIndex);
    }

    public void set_vehicle_speed(String id, double speed) throws Exception {
        eng.setVehicleSpeed(id, speed);
    }

    public void set_replay_file(String logFile) throws IOException {
        eng.setReplayLogFile(logFile);
    }

    public void set_random_seed(int seed) {
        eng.setRandomSeed(seed);
    }

    public void set_save_replay(boolean open) {
        eng.setSaveReplay(open);
    }

    public void push_vehicle(Map<String, Double> info, String roads) {
        eng.pushVehicle(info, JSON.parseArray(roads, String.class));
    }

    public boolean set_vehicle_route(String vehicleId, String anchorId) {
        return eng.setRoute(vehicleId, JSON.parseArray(anchorId, String.class));
    }

    public void reset() {
        eng.reset(false);
    }

    public void reset(boolean resetRnd) {
        eng.reset(resetRnd);
    }

    public void load(Archive archive) {
        eng.load(archive);
    }

    public Archive snapshot() {
        return eng.snapshot();
    }

    public void load_from_file(String fileName) throws FileNotFoundException {
        eng.loadFromFile(fileName);
    }

    public void save_to_file(String fileName) throws IOException {
        eng.saveToFile(fileName);
    }

    public void close() {
        eng.close();
    }

    public Engine getEng() {
        return eng;
    }
}
