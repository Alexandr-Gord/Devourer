package com.example.opengl;

import androidx.annotation.NonNull;

import com.example.opengl.devourer.DevourerNode;
import com.example.opengl.tiles.MineralType;

public class Resource {
    public MineralType mineralType;
    public Position startPosition;
    public Position nextPosition;
    public DevourerNode startDevourerNode;
    public DevourerNode nextDevourerNode;
    public float distance; // 0...1
    public boolean isMoving;
    public boolean isDelivered;

    public Resource(MineralType mineralType) {
        this.mineralType = mineralType;
        this.startPosition = null;
        this.nextPosition = null;
        this.startDevourerNode = null;
        this.nextDevourerNode = null;
        this.distance = 0;
        this.isMoving = false;
        this.isDelivered = false;
    }

    public void startResource(@NonNull DevourerNode startDevourerNode, Position startPosition) {
        this.startPosition = startPosition;
        this.nextPosition = null;
        this.startDevourerNode = startDevourerNode;
        this.nextDevourerNode = calculateNextEntityNode(startDevourerNode);
        this.distance = 0;
        this.isMoving = true;
        this.isDelivered = false;
    }

    public void move(float delta) {
        if ((startDevourerNode == null) && ((distance + delta) <= 0.5f)) { // in deleted start tile
            // stopped in startPosition
            isMoving = false;
            return;
        }
        if (nextDevourerNode == null) {
            if ((distance + delta) > 0.5f) {
                isMoving = false;
                // stopped in nextPosition
                return;
            } else {
                // stopped in startPosition;
                isMoving = false;
                return;
            }
        }
        float distanceOverflow = delta + distance - 1f;
        if (distanceOverflow > 0) { // move to the next tile
            if (nextDevourerNode.dist == 0) {
                distance = 1f;
                isMoving = false;
                isDelivered = true;
                return;
            }
            startDevourerNode = nextDevourerNode;
            nextDevourerNode = calculateNextEntityNode(startDevourerNode);
            distance = distanceOverflow;
            startPosition = nextPosition;
            nextPosition = null;
        } else {
            distance += delta;
        }
    }

    public DevourerNode calculateNextEntityNode(@NonNull DevourerNode devourerNode) {
        int dist = devourerNode.dist - 1;
        DevourerNode result = null;
        for (DevourerNode neighbor : devourerNode.neighbors) {
            if (neighbor.dist == dist) {
                result = neighbor;
                break;
            }
        }
        return result;
    }
}
