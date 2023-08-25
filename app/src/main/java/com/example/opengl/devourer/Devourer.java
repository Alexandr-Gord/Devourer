package com.example.opengl.devourer;

import com.example.opengl.Game;
import com.example.opengl.Position;
import com.example.opengl.Resource;
import com.example.opengl.ResourceDepository;
import com.example.opengl.tiles.Mineral;
import com.example.opengl.tiles.TileMap;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Devourer {
    private final TileMap tileMap;
    private final DevourerStructure devourerStructure;
    private final List<Resource> movingResources = Collections.synchronizedList(new ArrayList<>());
    //private final List<ResourceDepository> resourceDepositoryList = Collections.synchronizedList(new ArrayList<>());
    private final float betweenTileCentersX;
    private final float betweenTileCentersY;

    public Devourer(TileMap tileMap, float betweenTileCentersX, float betweenTileCentersY) {
        this.tileMap = tileMap;
        this.betweenTileCentersX = betweenTileCentersX;
        this.betweenTileCentersY = betweenTileCentersY;
        this.devourerStructure = new DevourerStructure();
        devourerStructure.createStructure(tileMap);
        //initResourceDepositoryList();
    }

    /*
    private void initResourceDepositoryList() {
        resourceDepositoryList.clear();
        Mineral mineral;
        for (int y = 0; y < tileMap.getTileMapHeight(); y++) {
            for (int x = 0; x < tileMap.getTileMapWidth(); x++) {
                mineral = tileMap.getTile(x, y).getMineral();
                if (mineral != null) {
                    resourceDepositoryList.add(mineral.resourceDepository);
                }
            }
        }
    }
     */

    public DevourerNode getDevourerNode(int x, int y) {
        return devourerStructure.getDevourerNode(x, y);
    }

    public boolean moveResources() {
        if (movingResources.size() > 0) {
            for (Resource resource : movingResources) {
                resource.move(0.1f);
            }
            return true;
        } else {
            return false;
        }
    }

    public void placeDevourer(int indX, int indY) {
        if (tileMap.getTile(indX, indY).getMineral() != null) { // is mineral
            startEatingMineral(indX, indY);
        } else {
            tileMap.placeDevourerTile(indX, indY);
            devourerStructure.addEntityNode(indX, indY, tileMap);
            Game.getInstance().showMessage("indX:" + indX + " indY:" + indY + " tile:" + tileMap.getTile(indX, indY).getEntity());
        }
    }

    private void startEatingMineral(int indX, int indY) {
        tileMap.placeDevourerTile(indX, indY);
        devourerStructure.addEntityNode(indX, indY, tileMap);
        DevourerNode devourerNode = devourerStructure.getDevourerNode(indX, indY);
        if (devourerNode != null) {
            tileMap.getTile(indX, indY).getMineral().resourceDepository.startResourceMining(devourerNode, calculateDevourerNodePosition(devourerNode), movingResources);
            /*
            Resource resource = tileMap.getTile(indX, indY).getMineral().resourceDepository.takeResource();
            resource.startResource(devourerNode, calculateDevourerNodePosition(devourerNode));
            movingResources.add(resource);
             */
        }
    }

    public void deleteDevourer(int indX, int indY) {
        tileMap.deleteDevourerTile(indX, indY);
        Game.getInstance().showMessage("indX:" + indX + " indY:" + indY + " tile:" + tileMap.getTile(indX, indY).getBasis());
    }

    public FloatBuffer createRenderDataMoving() {
        synchronized (movingResources) {
            List<Float> resourcesData = new ArrayList<>();
            for (Resource resource : movingResources) {
                Position position = calculateResourcePosition(resource);
                resourcesData.add(position.x);
                resourcesData.add(position.y);
                resourcesData.add(0f); // tile number
            }
            if (resourcesData.size() == 0) return null;
            FloatBuffer result = FloatBuffer.allocate(resourcesData.size());
            for (float fl : resourcesData) {
                result.put(fl);
            }
            return result;
        }
    }

    private Position calculateDevourerNodePosition(DevourerNode devourerNode) {
        Position result = new Position();
        result.x = devourerNode.x * betweenTileCentersX;
        if (devourerNode.x % 2 == 0) {
            result.y = devourerNode.y * betweenTileCentersY;
        } else {
            result.y = (devourerNode.y + 0.5f) * betweenTileCentersY;
        }
        return result;
    }

    private Position calculateResourcePosition(Resource resource) {
        Position result = new Position();
        if (resource.nextPosition == null) {
            resource.nextPosition = calculateDevourerNodePosition(resource.nextDevourerNode);
        }
        result.x = resource.startPosition.x + (resource.nextPosition.x - resource.startPosition.x) * resource.distance;
        result.y = resource.startPosition.y + (resource.nextPosition.y - resource.startPosition.y) * resource.distance;
        return result;
    }

}
