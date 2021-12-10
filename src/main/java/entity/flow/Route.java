package entity.flow;

import entity.roadNet.roadNet.Road;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private List<Road> route;

    public Route() {
        route = new ArrayList<>();
    }

    public Route(Route other) { // 深拷贝
        route = new ArrayList<>(other.route);
    }

    public Route(List<Road> route) {
        this.route = route;
    }

    public void setRoute(List<Road> route) {
        this.route = route;
    }

    public List<Road> getRoute() {
        return route;
    }
}
