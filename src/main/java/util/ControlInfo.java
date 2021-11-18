package util;

import entity.roadNet.roadNet.Lane;

public class ControlInfo {
    public double speed;
    public double changingSpeed;
    public Lane nextLane;
    public boolean waitingForChangingLane;
    public boolean collision;
}
