package entity.vehicle.vehicle;

import entity.flow.Route;

public class VehicleInfo {
    public double speed = 0;
    public double len = 0;
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
}
