package entity.vehicle.vehicle;

import entity.roadNet.roadNet.Drivable;

import java.util.List;

class Buffer {
    boolean isDisSet;
    boolean isSpeedSet;
    boolean isDrivableSet;
    boolean isNotifiedVehicles;
    boolean isEndSet;
    boolean isEnterLaneLinkTimeSet;
    boolean isBlockerSet;
    boolean isCustomSpeedSet;

    double dis;
    double speed;
    Drivable drivable;
    List<Vehicle> notifiedVehicles;
    boolean end;
    int enterLaneLinkTime;
    Vehicle blocker;
    double customSpeed;
    double deltaDis;

    Buffer() {
    }

    Buffer(Buffer buffer) {
        isDisSet = buffer.isDisSet;
        isSpeedSet = buffer.isSpeedSet;
        isDrivableSet = buffer.isDrivableSet;
        isNotifiedVehicles = buffer.isNotifiedVehicles;
        isEndSet = buffer.isEndSet;
        isEnterLaneLinkTimeSet = buffer.isEnterLaneLinkTimeSet;
        isBlockerSet = buffer.isBlockerSet;
        isCustomSpeedSet = buffer.isCustomSpeedSet;

        dis = buffer.dis;
        speed = buffer.speed;
        drivable = buffer.drivable;
        notifiedVehicles = buffer.notifiedVehicles;
        end = buffer.end;
        enterLaneLinkTime = buffer.enterLaneLinkTime;
        blocker = buffer.blocker;
        customSpeed = buffer.customSpeed;
        deltaDis = buffer.deltaDis;
    }

    public boolean isDisSet() {
        return isDisSet;
    }

    public void setDisSet(boolean disSet) {
        isDisSet = disSet;
    }

    public boolean isSpeedSet() {
        return isSpeedSet;
    }

    public void setSpeedSet(boolean speedSet) {
        isSpeedSet = speedSet;
    }

    public boolean isDrivableSet() {
        return isDrivableSet;
    }

    public void setDrivableSet(boolean drivableSet) {
        isDrivableSet = drivableSet;
    }

    public boolean isNotifiedVehicles() {
        return isNotifiedVehicles;
    }

    public void setNotifiedVehicles(boolean notifiedVehicles) {
        isNotifiedVehicles = notifiedVehicles;
    }

    public boolean isEndSet() {
        return isEndSet;
    }

    public void setEndSet(boolean endSet) {
        isEndSet = endSet;
    }

    public boolean isEnterLaneLinkTimeSet() {
        return isEnterLaneLinkTimeSet;
    }

    public void setEnterLaneLinkTimeSet(boolean enterLaneLinkTimeSet) {
        isEnterLaneLinkTimeSet = enterLaneLinkTimeSet;
    }

    public boolean isBlockerSet() {
        return isBlockerSet;
    }

    public void setBlockerSet(boolean blockerSet) {
        isBlockerSet = blockerSet;
    }

    public boolean isCustomSpeedSet() {
        return isCustomSpeedSet;
    }

    public void setCustomSpeedSet(boolean customSpeedSet) {
        isCustomSpeedSet = customSpeedSet;
    }

    public double getDis() {
        return dis;
    }

    public void setDis(double dis) {
        this.dis = dis;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Drivable getDrivable() {
        return drivable;
    }

    public void setDrivable(Drivable drivable) {
        this.drivable = drivable;
    }

    public List<Vehicle> getNotifiedVehicles() {
        return notifiedVehicles;
    }

    public void setNotifiedVehicles(List<Vehicle> notifiedVehicles) {
        this.notifiedVehicles = notifiedVehicles;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public int getEnterLaneLinkTime() {
        return enterLaneLinkTime;
    }

    public void setEnterLaneLinkTime(int enterLaneLinkTime) {
        this.enterLaneLinkTime = enterLaneLinkTime;
    }

    public Vehicle getBlocker() {
        return blocker;
    }

    public void setBlocker(Vehicle blocker) {
        this.blocker = blocker;
    }

    public double getCustomSpeed() {
        return customSpeed;
    }

    public void setCustomSpeed(double customSpeed) {
        this.customSpeed = customSpeed;
    }

    public double getDeltaDis() {
        return deltaDis;
    }

    public void setDeltaDis(double deltaDis) {
        this.deltaDis = deltaDis;
    }
}
