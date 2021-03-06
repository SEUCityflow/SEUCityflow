package entity.engine;

import entity.roadNet.roadNet.*;
import entity.vehicle.router.Router;
import entity.vehicle.vehicle.Vehicle;
import util.Pair;
import util.Barrier;

import java.util.*;

class ThreadControl implements Runnable {
    private final Engine engine;
    private Barrier startBarrier;
    private Barrier endBarrier;
    private final Set<Vehicle> vehicles;
    private final List<Road> roads;
    private final List<Intersection> intersections;
    private final List<Drivable> drivables;
    private final List<Vehicle> dynamicVehiclesEnterLane;
    private final List<LaneLink> activateLaneLinks;

    public ThreadControl(Engine engine, Barrier startBarrier, Barrier endBarrier, Set<Vehicle> vehicles, List<Road> roads, List<Intersection> intersections, List<Drivable> drivables) {
        this.engine = engine;
        this.startBarrier = startBarrier;
        this.endBarrier = endBarrier;
        this.vehicles = vehicles;
        this.roads = roads;
        this.drivables = drivables;
        this.intersections = intersections;
        dynamicVehiclesEnterLane = new ArrayList<>();
        activateLaneLinks = new ArrayList<>();
    }

    private void threadPlanRoute() {
        startBarrier.Wait();
        for (Road road : roads) {
            for (Vehicle vehicle : road.getPlanRouteBuffer()) {
                vehicle.calculateRoute();
            }
        }
        endBarrier.Wait();
    }

    private void threadInitSegments() {
        startBarrier.Wait();
        for (Road road : roads) {
            for (Lane lane : road.getLanes()) {
                lane.initSegments();
            }
        }
        endBarrier.Wait();
    }

    private void threadPlanLaneChange() {
        startBarrier.Wait();
        List<Vehicle> buffer = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.isCurRunning() && vehicle.isReal()) {
                vehicle.makeLaneChangeSignal(engine.getInterval());
                if (vehicle.planLaneChange()) {
                    buffer.add(vehicle);
                }
            }
        }
        synchronized (engine) {
            engine.getLaneChangeNotifyBuffer().addAll(buffer);
        }
        endBarrier.Wait();
    }

    private void threadUpdateLeaderAndGap() {
        startBarrier.Wait();
        for (Drivable drivable : drivables) {
            Vehicle leader = null;
            for (Vehicle vehicle : drivable.getVehicles()) {
                //??????????????????????????????
                vehicle.updateLeaderAndGap(leader);
                leader = vehicle;
            }
            if (drivable.isLane()) {
                ((Lane) drivable).updateHistory();
            }
        }
        endBarrier.Wait();
    }

    private void threadNotifyCross() {
        startBarrier.Wait();
        for (LaneLink laneLink : activateLaneLinks) {
            for (Cross cross : laneLink.getCrosses()) {
                cross.clearNotify();
            }
        }
        activateLaneLinks.clear();
        for (Intersection intersection : intersections) {
            for (LaneLink laneLink : intersection.getLaneLinks()) {
                boolean isEdit = false;
                List<Cross> crosses = laneLink.getCrosses();
                ListIterator<Cross> crossIterator = crosses.listIterator(crosses.size());
                // first check the vehicle on the end lane
                Vehicle vehicle = laneLink.getEndLane().getLastVehicle();
                if (vehicle != null && vehicle.getPrevDrivable() == laneLink) {
                    double vehDistance = vehicle.getCurDis() - vehicle.getLen();
                    while (crossIterator.hasPrevious()) {
                        Cross cross_now = crossIterator.previous();
                        double crossDistance = laneLink.getLength() - cross_now.getDistanceByLane(laneLink);
                        // cross ??? laneLink ??????
                        if (crossDistance + vehDistance < cross_now.getLeaveDistance()) {
                            isEdit = true;
                            cross_now.notify(laneLink, vehicle, -(vehicle.getCurDis() + crossDistance));
                        } else {
                            crossIterator.next();
                            break;
                        }
                    }
                }
                if (laneLink.getVehicles().size() == 0 && !laneLink.isAvailable()) {
                    if (isEdit) {
                        activateLaneLinks.add(laneLink);
                    }
                    continue;
                }
                // check each vehicle on laneLink
                for (Vehicle linkVehicle : laneLink.getVehicles()) {
                    double vehDistance = linkVehicle.getCurDis();
                    while (crossIterator.hasPrevious()) {
                        Cross cross_now = crossIterator.previous();
                        double crossDistance = cross_now.getDistanceByLane(laneLink);
                        if (vehDistance > crossDistance) { // vehicle ?????? cross
                            if (vehDistance - crossDistance - linkVehicle.getLen() <= cross_now.getLeaveDistance()) {
                                isEdit = true;
                                cross_now.notify(laneLink, linkVehicle, crossDistance - vehDistance);
                            } else {
                                crossIterator.next();
                                break;
                            }
                        } else { // vehicle??????cross
                            isEdit = true;
                            cross_now.notify(laneLink, linkVehicle, crossDistance - vehDistance);
                        }
                    }
                }
                // check vehicle on the incoming lane???laneLink ????????????????????????????????? cross ??? notify???
                vehicle = laneLink.getStartLane().getFirstVehicle();
                if (vehicle != null && vehicle.getNextDrivable() == laneLink && laneLink.isAvailable()) {
                    double vehDistance = laneLink.getStartLane().getLength() - vehicle.getCurDis();
                    while (crossIterator.hasPrevious()) {
                        Cross cross_now = crossIterator.previous();
                        double crossDistance = cross_now.getDistanceByLane(laneLink);
                        isEdit = true;
                        cross_now.notify(laneLink, vehicle, crossDistance + vehDistance);
                    }
                }
                if (isEdit) {
                    activateLaneLinks.add(laneLink);
                }
            }
        }
        endBarrier.Wait();
    }

    private void threadGetAction() {
        startBarrier.Wait();
        List<Pair<Vehicle, Double>> buffer = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.isCurRunning() && vehicle.getGroupLeader() == null) {
                engine.vehicleControl(vehicle, buffer, dynamicVehiclesEnterLane); //?????? speed???dis?????????
            }
        }
        synchronized (engine) {
            engine.getPushBuffer().addAll(buffer);
        }
        endBarrier.Wait();
    }

    private void threadUpdateLocation() {
        startBarrier.Wait();
        for (Drivable drivable : drivables) {
            ListIterator<Vehicle> vehicleListIterator = drivable.getVehicles().listIterator();
            while (vehicleListIterator.hasNext()) {
                Vehicle vehicle = vehicleListIterator.next();
                if (vehicle.hasSetDrivable() || vehicle.hasSetEnd()) { // ??????????????????????????? drivable ??? finishChange ??? abortChange
                    vehicleListIterator.remove();
                }
                if (vehicle.hasSetEnd()) { // ????????? route ??? vehicle.finishChange ??? shadow.abortChange????????? vehicle ?????? delete
                    synchronized (engine) {
                        engine.getVehicleRemoveBuffer().add(vehicle);
                        if (!vehicle.getLaneChange().isFinished() && vehicle.isReal()) {
                            engine.getVehicleMap().remove(vehicle.getId());
                            engine.setFinishedVehicleCnt(engine.getFinishedVehicleCnt() + 1);
                            engine.addCumulativeTravelTime(engine.getCurrentTime() - vehicle.getEnterTime());
                        }
                        Pair<Vehicle, Integer> pair = engine.getVehiclePool().get(vehicle.getPriority());
                        engine.getThreadVehiclePool().get(pair.getValue()).remove(vehicle);
                        engine.getVehiclePool().remove(vehicle.getPriority());
                        engine.setActiveVehicleCount(engine.getActiveVehicleCount() - 1);
                    }
                }
            }
        }
        endBarrier.Wait();
    }

    private void threadUpdateAction() { // vehicle ????????????
        startBarrier.Wait();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.isCurRunning()) {
                if (engine.getVehicleRemoveBuffer().contains(vehicle.getBufferBlocker())) {
                    vehicle.setBufferBlocker(null); //problem
                }
                vehicle.update();    // vehicle.buffer ???????????? vehicle.controllerInfo
                vehicle.clearSignal(); //problem ????????????
            }
        }
        endBarrier.Wait();
    }

    private void threadUpdateShorterRoute() {
        startBarrier.Wait();
        for (Vehicle vehicle : dynamicVehiclesEnterLane) {
            Router router = vehicle.getCurRouter();
            List<Pair<Road, Integer>> route = router.getRoute();
            List<Road> anchorPoints = router.getAnchorPoints();
            while (((Lane) vehicle.getCurDrivable()).getBelongRoad() != route.get(0).getKey()) {
                route.remove(0);
            }
            if (!router.routeTooShort()) {
                Pair<Road, Integer> routeToBeCheck = route.get(2);
                Road roadToBeCheck = routeToBeCheck.getKey();
                int pos = routeToBeCheck.getValue();
                if (!roadToBeCheck.isAnchorPoint(anchorPoints.get(pos)) && roadToBeCheck.isCongestion()) {
                    Pair<Road, Integer> start = route.get(1);
                    route.subList(2, route.size()).clear();
                    if (!router.dijkstra(start.getKey(), anchorPoints.get(pos), route, pos)) {
                        vehicle.setRouteValid(false);
                        vehicle.getFlow().setValid(false);
                        System.err.println("Invalid route '" + vehicle.getFlow().getId() + ", the car will end earlier than schedule.");
                        System.err.println();
                    }
                    router.getPlanned().clear();
                    router.setNowAnchorPoint(new Pair<>(anchorPoints.get(pos), pos));
                }
            }
            while (router.routeTooShort() && !router.isEnd()) {
                Pair<Road, Integer> start = router.getNowAnchorPoint();
                if (!router.dijkstra(start.getKey(), router.getAnchorPoints().get(start.getValue() + 1), route, start.getValue() + 1)) {
                    vehicle.setRouteValid(false);
                    vehicle.getFlow().setValid(false);
                    System.err.println("Invalid route '" + vehicle.getFlow().getId() + ", the car will end earlier than schedule.");
                    break;
                }
                router.getPlanned().clear();
                router.setNowAnchorPoint(new Pair<>(router.getAnchorPoints().get(start.getValue() + 1), start.getValue() + 1));
            }
            router.setiCurRoad(route.listIterator());
        }
        dynamicVehiclesEnterLane.clear();
        endBarrier.Wait();
    }

    public void run() {
        while (!engine.getFinished()) {
            threadPlanRoute(); // O(n * ElogV), n = vehicle.size() in planRouteBuffer
            threadInitSegments(); // O(n), n = vehicles.size()
            if (engine.isLaneChange()) {
                threadPlanLaneChange(); // O(n), n = vehicles.size()
            }
            threadNotifyCross(); // O(n), n = vehicles.size();
            threadGetAction(); // O(n), n = vehicles.size();
            threadUpdateLocation(); // O(n), n = vehicles.size();
            threadUpdateAction(); // O(n), n = vehicles.size();
            threadUpdateLeaderAndGap(); // O(n), n = vehicles.size();
            threadUpdateShorterRoute(); // O(n), n = vehiclesNeedToBeUpdate.size()
        }
    }

    public Barrier getStartBarrier() {
        return startBarrier;
    }

    public void setStartBarrier(Barrier startBarrier) {
        this.startBarrier = startBarrier;
    }

    public Barrier getEndBarrier() {
        return endBarrier;
    }

    public void setEndBarrier(Barrier endBarrier) {
        this.endBarrier = endBarrier;
    }
}
