package entity.vehicle.router;

import entity.flow.Route;
import entity.roadNet.roadNet.*;
import entity.roadNet.trafficLight.TrafficLight;
import entity.vehicle.vehicle.Vehicle;
import util.Pair;

import java.util.*;

public class Router {
    private Vehicle vehicle;  // route 对应的车辆
    private List<Pair<Road, Integer>> route;  // 由 anchorPoints 生成的路径
    private List<Road> anchorPoints;  // Flow 提供的必须经过的 Road
    private ListIterator<Pair<Road, Integer>> iCurRoad;   // 当前所在的 road 的位置
    private Random rnd;  // 随机数
    private List<Drivable> planned;  // 未来要走的路径缓存，由 getNextDrivable() 提前计算得出
    private RouterType type = RouterType.LENGTH;
    private Pair<Road, Integer> nowAnchorPoint = new Pair<>(null, null);

    // archive
    public Router(Vehicle vehicle) {
        this.vehicle = vehicle;
        route = new ArrayList<>();
        anchorPoints = new ArrayList<>();
        planned = new ArrayList<>();
    }

    // shadow
    public Router(Router other) {
        vehicle = other.vehicle;
        route = new ArrayList<>(other.route);
        anchorPoints = new ArrayList<>(other.anchorPoints);
        rnd = other.rnd;
        iCurRoad = this.route.listIterator();
        planned = new LinkedList<>();
        type = other.type;
        nowAnchorPoint = new Pair<>(other.nowAnchorPoint.getKey(), other.nowAnchorPoint.getValue());
    }

    // flow
    public Router(Vehicle vehicle, Route route, Random rnd, RouterType routerType) {
        this.vehicle = vehicle;
        this.route = new ArrayList<>();
        this.anchorPoints = route.getRoute();
        this.rnd = rnd;
        this.type = routerType;
        assert (this.anchorPoints.size() > 0);
        this.iCurRoad = this.route.listIterator();
        planned = new LinkedList<>();
    }

    private int selectLaneIndex(Lane curLane, List<Lane> lanes) {
        assert (lanes.size() > 0);
        if (curLane == null) {  // 当前未入 lane，随机选择备选 lane
            return rnd.nextInt(lanes.size()) % lanes.size();
        }
        // 已在 lane 上则选择最相邻的 lane
        int laneDiff = Integer.MAX_VALUE;
        int selected = -1;
        for (int i = 0; i < lanes.size(); i++) {
            int curLaneDiff = lanes.get(i).getLaneIndex() - curLane.getLaneIndex();
            if (Math.abs(curLaneDiff) < laneDiff) {
                laneDiff = Math.abs(curLaneDiff);
                selected = i;
            }
        }
        return selected;
    }

    // 根据当前 lane 在 laneLink 备选区的 endLane 内选下一个 lane
    private LaneLink selectLaneLink(Lane curLane, List<LaneLink> laneLinks) {
        if (laneLinks.size() == 0) {
            return null;
        }
        List<Lane> lanes = new ArrayList<>();
        for (LaneLink laneLink : laneLinks) {
            lanes.add(laneLink.getEndLane());
        }
        return laneLinks.get(selectLaneIndex(curLane, lanes));
    }

    // 根据当前 lane 在备选区选下一个 lane
    private Lane selectLane(Lane curLane, List<Lane> lanes) {
        if (lanes.size() == 0) {
            return null;
        }
        return lanes.get(selectLaneIndex(curLane, lanes));
    }

    // 最短路
    public boolean dijkstra(Road start, Road end, List<Pair<Road, Integer>> buffer, int num) {
        if (start == end) {
            return true;
        }
        Map<Road, Double> dis = new HashMap<>();
        Map<Road, Road> from = new HashMap<>();
        Set<Road> visited = new HashSet<>();
        boolean success = false;
        PriorityQueue<Pair<Road, Double>> queue = new PriorityQueue<>((o1, o2) -> (int) (o1.getValue() - o2.getValue()));
        dis.put(start, 0.0);
        queue.add(new Pair<>(start, 0.0));
        while (!queue.isEmpty()) {
            Pair<Road, Double> top = queue.peek();
            Road curRoad = top.getKey();
            if (curRoad == end) {
                success = true;
                break;
            }
            queue.remove();
            if (visited.contains(curRoad)) {
                continue;
            }
            visited.add(curRoad);
            double curDis = top.getValue();
            Intersection intersection = curRoad.getEndIntersection();
            for (Road adjRoad : intersection.getRoads()) {
                RoadLink roadLink = (curRoad.connectedToRoad(adjRoad));
                if (roadLink == null) {
                    continue;
                }
                Double nowDis = dis.get(adjRoad);
                double newDis = 0;
                switch (type) {
                    case LENGTH: {
                        newDis = curDis + adjRoad.getAverageLength();
                        break;
                    }
                    case DYNAMIC:
                    case DURATION: {
                        double avgDur = adjRoad.getAverageDuration();
                        if (avgDur < 0) {
                            avgDur = adjRoad.getAverageLength() / vehicle.getMaxSpeed();
                        }
                        newDis = curDis + avgDur;
                        TrafficLight trafficLight = intersection.getTrafficLight();
                        switch (roadLink.getType()) {
                            case turn_left:
                                newDis += trafficLight.getExpectedWaitingTime(0);
                                break;
                            case turn_right:
                                newDis += trafficLight.getExpectedWaitingTime(1);
                                break;
                            case go_straight:
                                newDis += trafficLight.getExpectedWaitingTime(2);
                                break;
                        }
                        break;
                    }
                    default: {
                        assert (false); // under construction
                        break;
                    }
                }
                if (nowDis == null || newDis < nowDis) {
                    from.put(adjRoad, curRoad);
                    dis.put(adjRoad, newDis);
                    queue.add(new Pair<>(adjRoad, newDis));
                }
            }
        }
        List<Pair<Road, Integer>> path = new ArrayList<>();
        path.add(new Pair<>(end, num));
        Road p = from.get(end);
        while (p != start) {
            path.add(new Pair<>(p, num));
            p = from.get(p);
        }
        Collections.reverse(path);
        buffer.addAll(path);
        return success;
    }

    public Road getFirstRoad() {
        return this.anchorPoints.get(0);
    }

    // 进入 route[0] 应选的 drivable
    public Drivable getFirstDrivable() {
        List<Lane> lanes = route.get(0).getKey().getLanes();
        if (route.size() == 1) {  // 仅 1 road
            return selectLane(null, lanes);  // 随机选 lane
        } else {
            List<Lane> candidateLanes = new ArrayList<>();
            for (Lane lane : lanes) {
                // 选择拥有由 route[0] 驶向 route[1] 的 laneLink 的 lane
                if (lane.getLaneLinksToRoad(route.get(1).getKey()).size() > 0) {
                    candidateLanes.add(lane);
                }
            }
            assert (candidateLanes.size() > 0);
            return selectLane(null, candidateLanes);  // candidateLanes 内选 lane
        }
    }

    // 下 i + 1 步将走的 drivable
    public Drivable getNextDrivable(int i) {
        if (i < planned.size()) {  // 已计算
            return planned.get(i);
        } else {
            // 未事先计算
            // 在前一个 drivable 基础上计算 nextDrivable
            Drivable ret = getNextDrivable(planned.size() != 0 ? planned.get(planned.size() - 1) : vehicle.getCurDrivable());
            planned.add(ret); // 填入 planned 备用
            return ret;
        }
    }

    // getNextDrivable 重载
    public Drivable getNextDrivable() {
        return getNextDrivable(0);
    }

    // 由当前 drivable 计算下一个 drivable（当前为 lane 则给出 laneLink，反之同理）
    public Drivable getNextDrivable(Drivable curDrivable) {
        if (curDrivable.isLaneLink()) { // 当前是 laneLink 直接得出
            return ((LaneLink) curDrivable).getEndLane();
        } else { // 当前是 lane
            Lane curLane = ((Lane) curDrivable);
            ListIterator<Pair<Road, Integer>> tmpCurRoadIter = route.listIterator(iCurRoad.nextIndex());
            Road tmpCurRoad = tmpCurRoadIter.next().getKey();
            // 找到 curDrivable 对应的 CurRoad
            while (tmpCurRoad != curLane.getBeLongRoad() && tmpCurRoadIter.hasNext()) {
                tmpCurRoad = tmpCurRoadIter.next().getKey();
            }
            assert (tmpCurRoadIter.hasNext() && (curLane.getBeLongRoad().equals(tmpCurRoad)));
            if (tmpCurRoadIter.nextIndex() == route.size()) { // 已到 route 末尾
                return null;
            } else if (tmpCurRoadIter.nextIndex() == route.size() - 1) {  // route 内倒数第二 road
                List<LaneLink> laneLinks = curLane.getLaneLinksToRoad(tmpCurRoadIter.next().getKey());
                return selectLaneLink(curLane, laneLinks);  // 走向可选 laneLink 的 endLane 中距离 curlane 最近的 lane
            } else {   // 选取的 laneLink 需能确保到达 route 的再下一个 road
                // 由 route[i] 到 route[i+1] 的 laneLink
                List<LaneLink> laneLinks = curLane.getLaneLinksToRoad(tmpCurRoadIter.next().getKey());
                Road nextTwoRoad = tmpCurRoadIter.next().getKey();
                List<LaneLink> candidateLaneLinks = new ArrayList<>();
                for (LaneLink laneLink : laneLinks) {
                    Lane nextLane = laneLink.getEndLane();
                    // 走此 laneLink 后能从 route[i+1] 到达 route[i+2]
                    if (nextLane.getLaneLinksToRoad(nextTwoRoad).size() > 0) {
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
        if (curDrivable.isLane()) {
            while (iCurRoad.hasNext() && ((Lane) curDrivable).getBeLongRoad() != iCurRoad.next().getKey()) {

            }
            iCurRoad.previous();
            assert (iCurRoad.hasNext());
        }
        Iterator<Drivable> drivableIterator = planned.iterator();
        while (drivableIterator.hasNext()) {
            Drivable drivable = drivableIterator.next();
            drivableIterator.remove();
            if (drivable == curDrivable) {
                break;
            }
        }
    }

    public boolean isLastRoad(Drivable drivable) {
        if (drivable.isLaneLink()) {
            return false;
        }
        return ((Lane) drivable).getBeLongRoad() == route.get(route.size() - 1).getKey();
    }

    public boolean onLastRoad() {
        return isLastRoad(vehicle.getCurDrivable());
    }

    // 选取从 curLane 走向下一个 Road 时 laneIndex 差距最小的 lane
    public Lane getValidLane(Lane curLane) {
        if (isLastRoad(curLane)) {
            return null;
        }
        ListIterator<Pair<Road, Integer>> nextRoad = route.listIterator(iCurRoad.nextIndex());
        nextRoad.next();
        Road tmpCurRoad = nextRoad.next().getKey();

        int min_diff = curLane.getBeLongRoad().getLanes().size();
        Lane chosen = null;
        for (Lane lane : curLane.getBeLongRoad().getLanes()) {
            int curLaneDiff = lane.getLaneIndex() - curLane.getLaneIndex();
            if (lane.getLaneLinksToRoad(tmpCurRoad).size() > 0 && Math.abs(curLaneDiff) < min_diff) {
                min_diff = Math.abs(curLaneDiff);
                chosen = lane;
            }
        }
        return chosen;
    }

    // 更新 route 为经过 anchorpoint 各路的最短路
    public boolean updateShortestPath() {
        route = new ArrayList<>();
        route.add(new Pair<>(anchorPoints.get(0), 0));
        nowAnchorPoint = new Pair<>(anchorPoints.get(0), 0);
        while ((type != RouterType.DYNAMIC || routeTooShort()) && !isEnd()) {
            Pair<Road, Integer> start = getNowAnchorPoint();
            if (!dijkstra(start.getKey(), getAnchorPoints().get(start.getValue() + 1), route, start.getValue() + 1)) {
                return false;
            }
            setNowAnchorPoint(new Pair<>(getAnchorPoints().get(start.getValue() + 1), start.getValue() + 1));
        }
        iCurRoad = this.route.listIterator();
        planned.clear();
        return route.size() > 1;
    }

    public boolean changeRoute(List<Road> anchor) {
        if (vehicle.getCurDrivable().isLaneLink()) {
            return false;
        }
        int pos = iCurRoad.nextIndex();
        Pair<Road, Integer> curRoad = iCurRoad.next();
        List<Road> backUp = new ArrayList<>(anchorPoints);
        List<Pair<Road, Integer>> backUpRoute = new ArrayList<>(route);
        anchorPoints.clear();
        anchorPoints.add(curRoad.getKey());
        anchorPoints.addAll(anchor);
        boolean result = updateShortestPath();
        if (result && onValidLane()) {
            iCurRoad = route.listIterator();
            return true;
        } else {
            anchorPoints = backUp;
            route = backUpRoute;
            planned.clear();
            iCurRoad = route.listIterator(pos);
            return false;
        }
    }

    public boolean routeTooShort() {
        if (route.size() < 3) {
            return true;
        }
        double sum = 0;
        for (Pair<Road, Integer> road : route) {
            sum += road.getKey().getAverageLength();
        }
        return sum < vehicle.getMaxSpeed() * 3;
    }

    public boolean isEnd() {
        return nowAnchorPoint.getValue() == anchorPoints.size() - 1;
    }

    // set / get
    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<Pair<Road, Integer>> getRoute() {
        return route;
    }

    public boolean onValidLane() {
        return !(getNextDrivable() == null && !onLastRoad());
    }

    public List<Road> getAnchorPoints() {
        return anchorPoints;
    }

    public void setAnchorPoints(List<Road> anchorPoints) {
        this.anchorPoints = anchorPoints;
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

    public Pair<Road, Integer> getNowAnchorPoint() {
        return nowAnchorPoint;
    }

    public void setNowAnchorPoint(Pair<Road, Integer> nowAnchorPoint) {
        this.nowAnchorPoint = nowAnchorPoint;
    }

    public ListIterator<Pair<Road, Integer>> getiCurRoad() {
        return iCurRoad;
    }

    public void setiCurRoad(ListIterator<Pair<Road, Integer>> iCurRoad) {
        this.iCurRoad = iCurRoad;
    }

    public void setRoute(List<Pair<Road, Integer>> route) {
        this.route = route;
    }

    // 获取未来将走的所有 Road
    public List<Road> getFollowingRoads() {
        List<Road> ret = new ArrayList<>();
        ListIterator<Pair<Road, Integer>> newIter = route.listIterator(iCurRoad.nextIndex());
        while (newIter.hasNext()) {
            ret.add(newIter.next().getKey());
        }
        return ret;
    }
}
