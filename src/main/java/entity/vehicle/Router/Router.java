package entity.vehicle.Router;

import entity.flow.Route;
import entity.roadNet.roadNet.Drivable;
import entity.roadNet.roadNet.Lane;
import entity.roadNet.roadNet.LaneLink;
import entity.roadNet.roadNet.Road;
import entity.vehicle.vehicle.Vehicle;
import javafx.util.Pair;

import java.util.*;

enum RouterType {
    LENGTH,
    DURATION,
    DYNAMIC
}


public class Router {
    private Vehicle vehicle;  // route 对应的车辆
    private List<Road> route;  // 由 anchorPoints 生成的路径
    private List<Road> anchorPoints;  // Flow 提供的必须经过的 Road
    private Iterator<Road> iCurRoad;   // 当前所在的 road 的位置
    private Random rnd;  // 随机数
    private List<Drivable> planned;  // 未来要走的路径缓存，由 getNextDrivable() 提前计算得出
    private RouterType type;

    private int selectLaneIndex(Lane curLane, List<Lane> lanes) {
        assert(lanes.size() > 0);
        if(curLane == null){  // 当前未入 lane，随机选择备选 lane
            return rnd.nextInt() % lanes.size();
        }
        // 已在 lane 上则选择最相邻的 lane
        int laneDiff = Integer.MAX_VALUE;
        int selected = -1;
        for(int i = 0; i < lanes.size(); i++){
            int curLaneDiff = lanes.get(i).getLaneIndex() - curLane.getLaneIndex();
            if(Math.abs(curLaneDiff) < laneDiff){
                laneDiff = Math.abs(curLaneDiff);
                selected = i;
            }
        }
        return selected;
    }

    // 根据当前 lane 在 laneLink 备选区的 endline 内选下一个 lane
    private LaneLink selectLaneLink(Lane curLane, List<LaneLink> laneLinks) {
        if(laneLinks.size() == 0){
            return null;
        }
        List<Lane> lanes = new ArrayList<>();
        for(LaneLink laneLink : laneLinks){
            lanes.add(laneLink.getEndLane());
        }
        return laneLinks.get(selectLaneIndex(curLane, lanes));
    }

    // 根据当前 lane 在备选区选下一个 lane
    private Lane selectLane(Lane curLane, List<Lane> lanes) {
        if(lanes.size() == 0){
            return null;
        }
        return lanes.get(selectLaneIndex(curLane, lanes));
    }

    // 最短路, redo
    private boolean dijkstra(Road start, Road end, List<Road> buffer) {
        Map<Road, Double> dis = new HashMap<>();
        Map<Road, Road> from = new HashMap<>();
        Set<Road> visited = new HashSet<>();
        boolean success = false;
        PriorityQueue<Pair<Road, Double>> queue = new PriorityQueue<Pair<Road, Double>>((o1, o2) -> (int)(o1.getValue() - o2.getValue()));

        dis.put(start, 0.0);
        queue.add(new Pair<>(start, 0.0));
        while(!queue.isEmpty()){
            Road curRoad = queue.peek().getKey();
            if(curRoad.equals(end)){
                success = true;
                break;
            }
            queue.poll();
            if (visited.contains(curRoad))
                continue;
            visited.add(curRoad);
            double curDis = dis.get(curRoad);
            dis.put(curRoad, curDis);
            for (Road adjRoad : curRoad.getEndIntersection().getRoads()) {
                if (!curRoad.connectedToRoad(adjRoad))
                    continue;
                // 拿到迭代器
                Iterator<Map.Entry<Road, Double>> iter = dis.entrySet().iterator();
                while(iter.hasNext()){
                        Map.Entry<Road, Double> entry = iter.next();
                        Road key = entry.getKey();
                        if(key.equals(adjRoad));
                        break;
                }
                double newDis = 0;

                switch (type) {
                    case LENGTH:
                        newDis = curDis + adjRoad.getAverageLength();
                        break;
                    case DURATION: {
                        double avgDur;
                        avgDur = adjRoad.getAverageDuration();
                        if (avgDur < 0) {
                            avgDur = adjRoad.getAverageLength() / vehicle.getMaxSpeed();
                        }
                        newDis = curDis + avgDur;
                    } break;
                    default:
                        assert(false); // under construction
                        break;
                }

                Map.Entry<Road, Double> entry = iter.next();
                if (iter.hasNext() || newDis < entry.getValue()) {
                    from.put(adjRoad, curRoad);
                    dis.put(adjRoad, newDis);
                    queue.add(new Pair<>(adjRoad, newDis));
                }
            }

            List<Road> path = new ArrayList<>();
            path.add(end);

            Iterator<Map.Entry<Road, Road>> iter = from.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<Road, Road> entry = iter.next();
                Road key = entry.getKey();
                if(key.equals(end));
                break;
            }
            Map.Entry<Road, Road> entry = iter.next();
            while (iter.hasNext() && !entry.getValue().equals(start) ) {
                path.add(entry.getValue());
                Iterator<Map.Entry<Road, Road>> roaditer = from.entrySet().iterator();
                while(roaditer.hasNext()){
                    Map.Entry<Road, Road> fromEntry = iter.next();
                    Road value = fromEntry.getValue();
                    if(value.equals(start));
                    break;
                }
            }

            for(int i = path.size()-1; i>= 0; i--){
                buffer.add(path.get(i));
            }

            return success;
        }


        return false;
    }

    public Router(Router other) {
        this.vehicle = other.vehicle;
        this.route = other.route;
        this.anchorPoints = other.anchorPoints;
        this.rnd = other.rnd;
        this.iCurRoad = this.route.iterator();

    }

    public Router(Vehicle vehicle, Route route, Random rnd) {
        this.vehicle = vehicle;
        this.route = route.getRoute();
        this.rnd = rnd;
        assert (this.anchorPoints.size() > 0);
        this.route = route.getRoute();
        this.iCurRoad = this.route.iterator();

    }

    public Road getFirstRoad() {
        return this.anchorPoints.get(0);
    }

    // 进入 route[0] 应选的 drivable
    public Drivable getFirstDrivable() {
        List<Lane> lanes = route.get(0).getLanes();
        if(route.size() == 1){  // 仅 1 road
            return selectLane(null, lanes);  // 随机选 lane
        }else {
            List<Lane> candidateLanes = new ArrayList<>();
            for(Lane lane : lanes){
                // 选择拥有由 route[0] 驶向 route[1] 的 laneLink 的 lane
                if(lane.getLaneLinksToRoad(route.get(1)).size() > 0){
                    candidateLanes.add(lane);
                }
            }
            assert (candidateLanes.size() > 0);
            return selectLane(null, candidateLanes);  // candidateLanes 内选 lane
        }
    }

    // 下 i + 1 步将走的 drivable
    public Drivable getNextDrivable(int i) {
        if(i < planned.size()){  // 已计算
            return planned.get(i);
        }else{
            // 未事先计算
            // 在前一个 drivable 基础上计算 nextDrivable
            Drivable ret = getNextDrivable(planned.size() !=0 ? planned.get(planned.size()-1) : vehicle.getCurDrivable());
            planned.add(ret); // 填入 planned 备用
            return ret;
        }
    }

    // getNextDrivable 重载
    public Drivable getNextDrivable() {
        if(0 < planned.size()){  // 已计算
            return planned.get(0);
        }else{
            // 未事先计算
            // 在前一个 drivable 基础上计算 nextDrivable
            Drivable ret = getNextDrivable(planned.size() !=0 ? planned.get(planned.size()-1) : vehicle.getCurDrivable());
            planned.add(ret); // 填入 planned 备用
            return ret;
        }
    }

    // redo
    // 由当前 drivable 计算下一个 drivable（当前为 lane 则给出 laneLink，反之同理）
    public Drivable getNextDrivable(Drivable curDrivable) {
        if(curDrivable.isLaneLink()){ // 当前是 laneLink 直接得出
            return ((LaneLink)curDrivable).getEndLane();
        }else { // 当前是 lane
            Lane curLane = ((Lane)curDrivable);
            Iterator<Road> tmpCurRoadIter = iCurRoad;
            Road tmpCurRoad = iCurRoad.next();
            // 找到 curDrivable 对应的 CurRoad
            while( !(tmpCurRoad.equals(curLane.getBeLongRoad())) && tmpCurRoadIter.hasNext() ){
                tmpCurRoad = tmpCurRoadIter.next();
            }
            assert ( tmpCurRoadIter.hasNext() && (curLane.getBeLongRoad().equals(tmpCurRoad)));
            if(tmpCurRoad.equals(route.get(route.size()-1))){ // 已到 route 末尾
                return null;
            }else if (tmpCurRoad.equals(route.get(route.size()-2))){  // route 内倒数第二 road
                tmpCurRoadIter.next();
                List<LaneLink> laneLinks = curLane.getLaneLinksToRoad(tmpCurRoadIter.next());
                return selectLaneLink(curLane, laneLinks);  // 走向可选 laneLink 的 endLane 中距离 curlane 最近的 lane
            }else{   // 选取的 laneLink 需能确保到达 route 的再下一个 road
                tmpCurRoad = tmpCurRoadIter.next();
                tmpCurRoad = tmpCurRoadIter.next();
                // 由 route[i] 到 route[i+1] 的 laneLink
                List<LaneLink> laneLinks = curLane.getLaneLinksToRoad(tmpCurRoad);
                List<LaneLink> candidateLaneLinks = new ArrayList<>();
                for(LaneLink laneLink : laneLinks){
                    Lane nextLane = laneLink.getEndLane();
                    // 走此 laneLink 后能从 route[i+1] 到达 route[i+2]
                    if(nextLane.getLaneLinksToRoad(tmpCurRoad).size() > 0){
                        candidateLaneLinks.add(laneLink);

                    }
                }
                return selectLaneLink(curLane, candidateLaneLinks); // 变动最少的 laneLink
            }
        }
    }

    // 更新 iCurRoad 与 planned
    public void update() {
        Drivable curDrivable = vehicle.getCurDrivable();
        if(curDrivable.isLane()){
            while(iCurRoad.hasNext() && ((Lane)curDrivable).getBeLongRoad() != iCurRoad.next()){
                iCurRoad.next();
            }
            assert (iCurRoad.hasNext());
            for(int i = 0; i < planned.size(); i++){
                if(!planned.get(i).equals(curDrivable)){
                    planned.remove(i);
                }else{
                    planned.remove(i);
                    break;
                }
            }
        }

    }

    public boolean isLastRoad(Drivable drivable) {
        if(drivable.isLaneLink()){
            return false;
        }
        return ((Lane)drivable).getBeLongRoad().equals(route.get(route.size()-1));
    }

    public boolean onLastRoad() {
        return isLastRoad(vehicle.getCurDrivable());
    }

    // 没有这个方法? redo
    public boolean onValidRoad() {
        return false;
    }

    // 选取从 curLane 走向下一个 Road 时 laneIndex 差距最小的 lane
    public Lane getValidLane(Lane curLane) {
        if(isLastRoad(curLane)){
            return null;
        }
        Iterator<Road> nextRoad = iCurRoad;
        Road tmpCurRoad = iCurRoad.next();

        int min_diff = curLane.getBeLongRoad().getLanes().size();
        Lane chosen = null;
        for(Lane lane : curLane.getBeLongRoad().getLanes()){
            int curLaneDiff = lane.getLaneIndex() - curLane.getLaneIndex();
            if(lane.getLaneLinksToRoad(tmpCurRoad).size() > 0 && Math.abs(curLaneDiff) < min_diff){
                min_diff = Math.abs(curLaneDiff);
                chosen = lane;
            }
        }
        assert (chosen.getBeLongRoad().equals(curLane.getBeLongRoad()));
        return chosen;
    }

    // 更新 route 为经过 anchorpoint 各路的最短路
    public boolean updateShortestPath() {
        // Dijkstra
        planned.clear();
        route.clear();
        route.add(anchorPoints.get(0));
        for(int i = 0; i < anchorPoints.size(); i++){
            if(anchorPoints.get(i-1).equals(anchorPoints.get(i)))
                continue;
            if(!dijkstra(anchorPoints.get(i-1), anchorPoints.get(i), route))
                return false;
        }

        if(route.size() <= 1)
            return false;
        iCurRoad = this.route.iterator();
        return true;
    }

    public boolean changeRoute(List<Road> anchor) {
        if (vehicle.getCurDrivable().isLaneLink())
            return false;
        Road curRoad = iCurRoad.next();
        List<Road> backUp = new ArrayList<>(anchorPoints);
        List<Road> backUpRoute = new ArrayList<>(route);
        anchorPoints.clear();
        anchorPoints.add(curRoad);
        for(int i = 0; i < anchor.size(); i++){
            anchorPoints.add(anchor.get(i));
        }
        boolean result = updateShortestPath();
        if(result && onValidRoad()){
            return true;
        }else{
            anchorPoints = new ArrayList<>(backUp);
            route = new ArrayList<>(backUpRoute);
            planned.clear();
            iCurRoad = route.iterator();
            while(iCurRoad.hasNext() && !iCurRoad.next().equals(curRoad));
            return false;
        }
    }

    // set / get
    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<Road> getRoute() {
        return route;
    }


    private boolean onValidLane() {
        return !(getNextDrivable() == null && !onLastRoad());
    }

    public List<Road> getAnchorPoints() {
        return anchorPoints;
    }

    public void setAnchorPoints(List<Road> anchorPoints) {
        this.anchorPoints = anchorPoints;
    }

    public Iterator<Road> getiCurRoad() {
        return iCurRoad;
    }

    public void setiCurRoad(Iterator<Road> iCurRoad) {
        this.iCurRoad = iCurRoad;
    }

    public Random getRnd() {
        return rnd;
    }

    public void setRnd(Random rnd) {
        this.rnd = rnd;
    }

    public List<Drivable> getPlanned() {
        return planned;
    }

    public void setPlanned(List<Drivable> planned) {
        this.planned = planned;
    }

    public RouterType getType() {
        return type;
    }

    public void setType(RouterType type) {
        this.type = type;
    }

    // 获取未来将走的所有 Road
    public List<Road> getFollowingRoads(){
        List<Road> ret = new ArrayList<>();
        while(iCurRoad.hasNext()){
            ret.add(iCurRoad.next());
        }
        return ret;
    }
}
