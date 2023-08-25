package com.example.opengl.devourer;

import androidx.annotation.NonNull;

import com.example.opengl.Position;
import com.example.opengl.PositionInd;
import com.example.opengl.tiles.Entity;
import com.example.opengl.tiles.TileMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class DevourerStructure {
    Map<Integer, DevourerNode> map = new HashMap<>();
    public DevourerNode main;

    private static final int[] NEIGHBOR_DX = {0, 1, 1, 0, -1, -1};
    private static final int[] NEIGHBOR_EVEN_X_DY = {-1, -1, 0, 1, 0, -1};
    private static final int[] NEIGHBOR_ODD_X_DY = {-1, 0, 1, 1, 1, 0};
    private static final int INDEX_WIDTH = 10000; // for square map max 46340

    public DevourerStructure() {
    }

    public int getNumberOfAllNodes() {
        return map.size();
    }

    private int getIndex(int x, int y) {
        return y * INDEX_WIDTH + x;
    }

    public DevourerNode getDevourerNode(int x, int y) {
        return map.get(getIndex(x, y));
    }

    public int getDevourerNodeDist(int x, int y) {
        int result = 0;
        DevourerNode devourerNode = map.get(getIndex(x, y));
        if (devourerNode != null) result = devourerNode.dist;
        return result;
    }

    public void createStructure(@NonNull TileMap tileMap) {
        this.main = new DevourerNode(tileMap.getMainEntityTileIndX(), tileMap.getMainEntityTileIndY(), 0);
        map.clear();
        map.put(getIndex(main.x, main.y), main);

        int x, y, index;
        DevourerNode neighbor;

        Queue<DevourerNode> q = new ArrayDeque<>();
        q.add(main);

        while (!q.isEmpty()) {
            DevourerNode node = q.poll();
            int newDist = node.dist + 1;

            for (int k = 0; k < 6; k++) {
                x = node.x + NEIGHBOR_DX[k];
                if (node.x % 2 == 0) {
                    y = node.y + NEIGHBOR_EVEN_X_DY[k];
                } else {
                    y = node.y + NEIGHBOR_ODD_X_DY[k];
                }
                if (isValid(tileMap, x, y)) {
                    index = getIndex(x, y);
                    neighbor = map.get(index);
                    if (neighbor == null) {
                        DevourerNode newNeighbor = new DevourerNode(x, y, newDist);
                        node.neighbors.add(newNeighbor);
                        q.add(newNeighbor);
                        map.put(index, newNeighbor);
                    } else {
                        /*
                        if (neighbor.dist > newDist) { // ?????
                            neighbor.dist = newDist;
                        }
                         */
                        node.neighbors.add(neighbor);
                    }
                }
            }
        }
    }

    private static boolean isValid(TileMap tileMap, int x, int y) {
        return (tileMap.isTileExist(x, y)) && (tileMap.isEntity(x, y));
    }

    public ArrayList<Position> getPathToMainEntity(int x, int y, int tileMapHeight, float betweenTileCentersX, float betweenTileCentersY) {
        ArrayList<Position> path = new ArrayList<>();
        DevourerNode node = getDevourerNode(x, y);
        if (node == null) return path;
        int dist = node.dist;

        Position startPosition = new Position();
        startPosition.x = x * betweenTileCentersX;
        if (x % 2 == 0) {
            startPosition.y = (tileMapHeight - y) * betweenTileCentersY;
        } else {
            startPosition.y = (tileMapHeight - y - 0.5f) * betweenTileCentersY;
        }
        path.add(startPosition);
        dist--;

        while (dist > -1) {
            for (DevourerNode neighbor : node.neighbors) {
                if (neighbor.dist == dist) {
                    node = neighbor;
                    Position position = new Position();
                    position.x = node.x * betweenTileCentersX;
                    if (node.x % 2 == 0) {
                        position.y = (tileMapHeight - node.y) * betweenTileCentersY;
                    } else {
                        position.y = (tileMapHeight - node.y - 0.5f) * betweenTileCentersY;
                    }
                    path.add(position);
                    break;
                }
            }
            dist--;
        }
        return path;
    }

    public PositionInd getNextPositionInd(int x, int y) {
        DevourerNode node = getDevourerNode(x, y);
        if (node == null) return null;
        int dist = node.dist;
        PositionInd position = new PositionInd();
        if (node.dist == 0) {
            position.x = x;
            position.y = y;
        } else if (node.dist > 0) {
            dist--;
            for (DevourerNode neighbor : node.neighbors) {
                if (neighbor.dist == dist) {
                    position.x = neighbor.x;
                    position.y = neighbor.y;
                    break;
                }
            }
        } else {
            position = null;
        }
        return position;
    }


    public void addEntityNode(int x, int y, TileMap tileMap) {
        ArrayList<DevourerNode> near = new ArrayList<>();
        int indX, indY;
        int maxDist = 0;
        int minDist = Integer.MAX_VALUE;
        for (int k = 0; k < 6; k++) {
            indX = x + NEIGHBOR_DX[k];
            if (x % 2 == 0) {
                indY = y + NEIGHBOR_EVEN_X_DY[k];
            } else {
                indY = y + NEIGHBOR_ODD_X_DY[k];
            }

            DevourerNode node = getDevourerNode(indX, indY);
            Entity entity = tileMap.getTile(indX, indY).getEntity();
            if ((entity != null) && (node == null)) {
                createStructure(tileMap);
                return;
            }
            if (node != null) {
                near.add(node);
                if (node.dist > maxDist) {
                    maxDist = node.dist;
                }
                if (node.dist < minDist) {
                    minDist = node.dist;
                }
            }
        }
        if (near.size() > 0) {
            if ((maxDist - minDist) < 2) {
                int newDist = maxDist == minDist ? minDist + 1 : maxDist;
                DevourerNode newNode = new DevourerNode(x, y, newDist);
                map.put(getIndex(x, y), newNode);
                for (DevourerNode neighbor : near) {
                    newNode.neighbors.add(neighbor);
                    neighbor.neighbors.add(newNode);
                }
            } else {
                createStructure(tileMap);
            }
        }
    }

    public boolean removeDevourerNode(int x, int y) {
        //TODO implement
        return false;
    }

}
