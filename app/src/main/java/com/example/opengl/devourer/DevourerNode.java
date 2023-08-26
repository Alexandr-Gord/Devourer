package com.example.opengl.devourer;

import java.util.ArrayList;
import java.util.List;

public class DevourerNode {
    public int x;
    public int y;
    public int dist;
    private final List<DevourerNode> neighbors = new ArrayList<>(6); //TODO synchronizedList ???

    public DevourerNode(int x, int y, int dist) {
        this.x = x;
        this.y = y;
        this.dist = dist;
    }

    public void addNeighbor(DevourerNode devourerNode) {
        neighbors.add(devourerNode);
    }

    public void removeNeighbor(DevourerNode devourerNode) {
        neighbors.remove(devourerNode);
    }

    public List<DevourerNode> getNeighbors() {
        return neighbors;
    }
}
