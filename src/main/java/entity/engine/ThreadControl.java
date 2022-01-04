package entity.engine;

import entity.roadNet.roadNet.*;
import entity.vehicle.vehicle.Vehicle;
import javafx.util.Pair;
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

    public ThreadControl(Engine engine, Barrier startBarrier, Barrier endBarrier, Set<Vehicle> vehicles, List<Road> roads, List<Intersection> intersections, List<Drivable> drivables) {
        this.engine = engine;
        this.startBarrier = startBarrier;
        this.endBarrier = endBarrier;
        this.vehicles = vehicles;
        this.roads = roads;
        this.drivables = drivables;
        this.intersections = intersections;
    }

    private void threadPlanRoute() {
        startBarrier.Wait();
        for (Road road : roads) {
            for (Vehicle vehicle : road.getPlanRouteBuffer()) {
                vehicle.updateRoute();
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
                //每辆车与前车距离更新
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
        for (Intersection intersection : intersections) {
            for (Cross cross : intersection.getCrosses())
                cross.clearNotify();
        }
        for (Intersection intersection : intersections) {
            for (LaneLink laneLink : intersection.getLaneLinks()) {
                List<Cross> crosses = laneLink.getCrosses();
                ListIterator<Cross> crossIterator = crosses.listIterator();
                // first check the vehicle on the end lane
                Vehicle vehicle = laneLink.getEndLane().getLastVehicle();
                if (vehicle != null && vehicle.getPrevDrivable() == laneLink) {
                    double vehDistance = vehicle.getCurDis() - vehicle.getLen();//problem vehicle 距离此 endLane 起点的距离 C++ 里为 getDistance()函数名未找到
                    while (crossIterator.hasPrevious()) {
                        Cross cross_now = crossIterator.previous();
                        double crossDistance = laneLink.getLength() - cross_now.getDistanceByLane(laneLink);
                        // cross 距 laneLink 终点
                        if (crossDistance + vehDistance < cross_now.getLeaveDistance()) {                     //problem  vehicle 距 cross 的距离小于 leaveDistance
                            cross_now.notify(laneLink, vehicle, -(vehicle.getCurDis() + crossDistance));   //problem  信息填入此 cross
                        } else {
                            break;
                        }
                    }
                }
                // check each vehicle on laneLink
                for (Vehicle linkVehicle : laneLink.getVehicles()) {
                    double vehDistance = linkVehicle.getCurDis();//problem
                    while (crossIterator.hasPrevious()) {
                        Cross cross_now = crossIterator.previous();
                        double crossDistance = cross_now.getDistanceByLane(laneLink);
                        if (vehDistance > crossDistance) { // vehicle 已过 cross
                            if (vehDistance - crossDistance - linkVehicle.getLen() <= cross_now.getLeaveDistance()) {//problem
                                cross_now.notify(laneLink, linkVehicle, crossDistance - vehDistance);
                            } else {
                                break;
                            }
                        } else { // vehicle未过cross
                            cross_now.notify(laneLink, linkVehicle, crossDistance - vehDistance);
                        }
                    }
                }
                // check vehicle on the incoming lane（laneLink 上车已经检查完成但仍有 cross 未 notify）
                vehicle = laneLink.getStartLane().getFirstVehicle();
                if (vehicle != null && vehicle.getNextDrivable() == laneLink && laneLink.isAvailable()) {
                    double vehDistance = laneLink.getStartLane().getLength() - vehicle.getCurDis();
                    while (crossIterator.hasPrevious()) {
                        crossIterator.previous().notify(laneLink, vehicle, vehDistance);
                    }
                }
            }
        }

        endBarrier.Wait();
    }

    private void threadGetAction() {
        startBarrier.Wait();
        List<Pair<Vehicle, Double>> buffer = new LinkedList<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.isCurRunning()) {
                engine.vehicleControl(vehicle, buffer); //计算speed、dis等信息
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
                if (vehicle.getChangedDrivable() != null || vehicle.hasSetEnd()) { // 该车已移动到下一个 drivable 或 finishChange 或 abortChange
                    vehicleListIterator.remove();
                }
                if (vehicle.hasSetEnd()) { // 已跑完 route 或 vehicle.finishChange 或 shadow.abortChange，此时 vehicle 将被 delete
                    synchronized (engine) {
                        engine.getVehicleRemoveBuffer().add(vehicle);
                        if (!vehicle.getLaneChange().isFinished()) {
                            engine.getVehicleMap().remove(vehicle.getId());
                            engine.setFinishedVehicleCnt(engine.getFinishedVehicleCnt() + 1);
                            engine.setCumulativeTravelTime(engine.getCurrentTime() - vehicle.getEnterTime());
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

    private void threadUpdateAction() { // vehicle 信息更新
        startBarrier.Wait();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.isCurRunning()) {
                if (engine.getVehicleRemoveBuffer().contains(vehicle.getBufferBlocker())) {
                    vehicle.setBufferBlocker(null); //problem
                }
                vehicle.update();    // vehicle.buffer 信息移入 vehicle.controllerInfo
                vehicle.clearSignal(); //problem 清空信号
            }
        }
        endBarrier.Wait();
    }

    public void run() {
        while (!engine.getFinished()) {
            threadPlanRoute();
            if (engine.isLaneChange()) {
                threadInitSegments();
                threadPlanLaneChange();
//                threadUpdateLeaderAndGap();
            }
            threadNotifyCross();
            threadGetAction();
            threadUpdateLocation();
            threadUpdateAction();
            threadUpdateLeaderAndGap();
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
