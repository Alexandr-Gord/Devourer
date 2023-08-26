package com.example.opengl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.opengl.devourer.DevourerNode;
import com.example.opengl.devourer.DevourerStructure;
import com.example.opengl.tiles.MineralType;

public class Resource {
    public DevourerStructure devourerStructure;
    public MineralType mineralType;
    public Position startPosition;
    public Position nextPosition;
    public PositionInd startPositionInd;
    public PositionInd nextPositionInd;
    public float distance; // 0...1
    public boolean isMoving;
    public boolean isDelivered;

    public Resource(MineralType mineralType) {
        this.mineralType = mineralType;
        this.distance = 0;
        this.isMoving = false;
        this.isDelivered = false;
    }

    public void startResource(@NonNull PositionInd startPositionInd, Position startPosition, DevourerStructure devourerStructure) {
        this.devourerStructure = devourerStructure;
        this.startPosition = startPosition;
        this.nextPosition = null;
        this.startPositionInd = startPositionInd;
        this.nextPositionInd = calculateNextPositionInd(startPositionInd);
        this.distance = 0;
        this.isMoving = true;
        this.isDelivered = false;
    }

    public void move(float delta) {
        DevourerNode startDevourerNode = startPositionInd != null ? devourerStructure.getDevourerNode(startPositionInd.x, startPositionInd.y) : null;
        DevourerNode nextDevourerNode = nextPositionInd != null ? devourerStructure.getDevourerNode(nextPositionInd.x, nextPositionInd.y) : null;

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
            startPositionInd = nextPositionInd;
            nextPositionInd = calculateNextPositionInd(startPositionInd);
            distance = distanceOverflow;
            startPosition = nextPosition;
            nextPosition = null;
        } else {
            distance += delta;
        }
    }

    @Nullable
    public PositionInd calculateNextPositionInd(@NonNull PositionInd positionInd) {
        PositionInd result = null;
        DevourerNode devourerNode = devourerStructure.getDevourerNode(positionInd.x, positionInd.y);
        if (devourerNode != null) {
            int dist = devourerNode.dist - 1;
            for (DevourerNode neighbor : devourerNode.getNeighbors()) {
                if (neighbor.dist == dist) {
                    result = new PositionInd();
                    result.x = neighbor.x;
                    result.y = neighbor.y;
                    break;
                }
            }
        }
        return result;
    }

}
