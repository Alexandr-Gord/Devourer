package com.example.opengl.devourer;

import java.util.ArrayList;
import java.util.List;

public class DevourerNode {
    public int x;
    public int y;
    public int dist;
    public List<DevourerNode> neighbors = new ArrayList<>();

    public DevourerNode(int x, int y, int dist) {
        this.x = x;
        this.y = y;
        this.dist = dist;
    }
}
