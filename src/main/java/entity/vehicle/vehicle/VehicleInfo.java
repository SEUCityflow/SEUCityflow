package entity.vehicle.vehicle;

import entity.flow.Route;

public class VehicleInfo {
    public double speed = 0;
    public double len = 5;
    public double width = 2;
    public double maxPosAcc = 4.5;
    public double maxNegAcc = 4.5;
    public double usualPosAcc = 2.5;
    public double usualNegAcc = 2.5;
    public double minGap = 2;
    public double maxSpeed = 16.66667;
    public double headwayTime = 1;
    public double yieldDistance = 5;
    public double turnSpeed = 8.3333;
    public Route route;

    public VehicleInfo() {
        route = new Route();
    }

    public VehicleInfo(VehicleInfo vehicleInfo) {
        this.speed = vehicleInfo.speed;
        this.len = vehicleInfo.len;
        this.width = vehicleInfo.width;
        this.maxPosAcc = vehicleInfo.maxPosAcc;
        this.maxNegAcc = vehicleInfo.maxNegAcc;
        this.usualPosAcc = vehicleInfo.usualPosAcc;
        this.usualNegAcc = vehicleInfo.usualNegAcc;
        this.minGap = vehicleInfo.minGap;
        this.maxSpeed = vehicleInfo.maxSpeed;
        this.headwayTime = vehicleInfo.headwayTime;
        this.yieldDistance = vehicleInfo.yieldDistance;
        this.turnSpeed = vehicleInfo.turnSpeed;
        this.route = new Route(vehicleInfo.route);
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getLen() {
        return len;
    }

    public void setLen(double len) {
        this.len = len;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getMaxPosAcc() {
        return maxPosAcc;
    }

    public void setMaxPosAcc(double maxPosAcc) {
        this.maxPosAcc = maxPosAcc;
    }

    public double getMaxNegAcc() {
        return maxNegAcc;
    }

    public void setMaxNegAcc(double maxNegAcc) {
        this.maxNegAcc = maxNegAcc;
    }

    public double getUsualPosAcc() {
        return usualPosAcc;
    }

    public void setUsualPosAcc(double usualPosAcc) {
        this.usualPosAcc = usualPosAcc;
    }

    public double getUsualNegAcc() {
        return usualNegAcc;
    }

    public void setUsualNegAcc(double usualNegAcc) {
        this.usualNegAcc = usualNegAcc;
    }

    public double getMinGap() {
        return minGap;
    }

    public void setMinGap(double minGap) {
        this.minGap = minGap;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getHeadwayTime() {
        return headwayTime;
    }

    public void setHeadwayTime(double headwayTime) {
        this.headwayTime = headwayTime;
    }

    public double getYieldDistance() {
        return yieldDistance;
    }

    public void setYieldDistance(double yieldDistance) {
        this.yieldDistance = yieldDistance;
    }

    public double getTurnSpeed() {
        return turnSpeed;
    }

    public void setTurnSpeed(double turnSpeed) {
        this.turnSpeed = turnSpeed;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }
}
