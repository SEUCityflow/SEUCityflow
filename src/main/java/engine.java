import entity.archive.Archive;
import entity.engine.Engine;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class engine { // 供 python 调用
    private final Engine eng;

    public engine(String configFile, int threadNum) {
        eng = new Engine(configFile, threadNum);
    }

    public void next_step() {
        eng.nextStep();
    }

    public int get_vehicle_count() {
        return eng.getVehicleCount();
    }

    public List<String> get_vehicles() {
        return eng.getVehicles(false);
    }

    public List<String> get_vehicles(boolean includeWaiting) {
        return eng.getVehicles(includeWaiting);
    }

    public Map<String, Integer> get_lane_waiting_vehicle_count() {
        return eng.getLaneWaitingVehicleCount();
    }

    public Map<String, List<String>> get_lane_vehicles() {
        return eng.getLaneVehicles();
    }

    public Map<String, Double> get_vehicle_speed() {
        return eng.getVehicleSpeed();
    }

    public Map<String, String> get_vehicle_info(String id) {
        return eng.getVehicleInfo(id);
    }

    public Map<String, Double> get_vehicle_distance() {
        return eng.getVehicleDistance();
    }

    public String get_leader(String vehicleId) {
        return eng.getLeader(vehicleId);
    }

    public double get_current_time() {
        return eng.getCurrentTime();
    }

    public double getAverageTravelTime() {
        return eng.getAverageTravelTime();
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

    public void push_vehicle(Map<String, Double> info, List<String> roads) {
        eng.pushVehicle(info, roads);
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

    public void loadFromFile(String fileName) {
        eng.loadFromFile(fileName);
    }

    public boolean set_vehicle_route(String vehicleId, List<String> anchorId) {
        return eng.setRoute(vehicleId, anchorId);
    }

    public void close() {
        eng.close();
    }
}
