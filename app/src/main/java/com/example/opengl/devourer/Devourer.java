package com.example.opengl.devourer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.library.baseAdapters.BR;

import com.example.opengl.Game;
import com.example.opengl.Position;
import com.example.opengl.PositionInd;
import com.example.opengl.Resource;
import com.example.opengl.ResourcesStorage;
import com.example.opengl.tiles.MineralType;
import com.example.opengl.tiles.TileMap;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Devourer {
    private final TileMap tileMap;
    private final DevourerStructure devourerStructure;
    private final List<Resource> movingResources = Collections.synchronizedList(new ArrayList<>());
    private final ResourcesStorage resourcesStorage = new ResourcesStorage();
    private final Position mainDevourerPos;
    private final float betweenTileCentersX;
    private final float betweenTileCentersY;

    public Devourer(TileMap tileMap, float betweenTileCentersX, float betweenTileCentersY) {
        this.tileMap = tileMap;
        this.betweenTileCentersX = betweenTileCentersX;
        this.betweenTileCentersY = betweenTileCentersY;
        this.devourerStructure = new DevourerStructure();
        devourerStructure.createStructure(tileMap);
        PositionInd mainDevourerPosInd = new PositionInd(tileMap.getMainDevourerTileIndX(), tileMap.getMainDevourerTileIndY());
        mainDevourerPos = calculateDevourerNodePosition(mainDevourerPosInd);
    }

    public DevourerNode getDevourerNode(int x, int y) {
        return devourerStructure.getDevourerNode(x, y);
    }

    public boolean moveResources() {
        if (movingResources.size() > 0) {
            synchronized (movingResources) {
                // Must be in synchronized block
                Iterator<Resource> iterator = movingResources.iterator();
                while (iterator.hasNext()) {
                    Resource resource = iterator.next();
                    if (resource.isMoving) {
                        resource.move(0.1f);
                    }
                    if (resource.isDelivered) {
                        resourcesStorage.addMineral(resource.mineralType, 1);
                        Game.getInstance().showMineralsCounts();
                        iterator.remove();
                    }
                }
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
            devourerStructure.addDevourerNode(indX, indY, tileMap);
            Game.getInstance().setMessage1("indX:" + indX + " indY:" + indY + " tile:" + tileMap.getTile(indX, indY).getEntity());
        }
    }

    private void startEatingMineral(int indX, int indY) {
        tileMap.placeDevourerTile(indX, indY);
        devourerStructure.addDevourerNode(indX, indY, tileMap);
        DevourerNode devourerNode = devourerStructure.getDevourerNode(indX, indY);
        if (devourerNode != null) {
            tileMap.getTile(indX, indY).getMineral().resourceDepository.startResourceMining(indX, indY, devourerStructure, calculateDevourerNodePosition(new PositionInd(indX, indY)), movingResources);
        }
    }

    public void deleteDevourer(int indX, int indY) {
        tileMap.deleteDevourerTile(indX, indY);
        devourerStructure.removeDevourerNode(indX, indY, tileMap);
        Game.getInstance().setMessage1("indX:" + indX + " indY:" + indY + " tile:" + tileMap.getTile(indX, indY).getBasis());
    }

    public FloatBuffer createRenderDataMoving() {
        synchronized (movingResources) {
            List<Float> resourcesData = new ArrayList<>();
            for (Resource resource : movingResources) {
                Position position = calculateResourcePosition(resource);
                if (position != null) {
                    resourcesData.add(position.x);
                    resourcesData.add(position.y);
                    resourcesData.add(0f); // tile number
                    resourcesData.add(0f); // texture number
                }
            }

            resourcesData.add(mainDevourerPos.x);
            resourcesData.add(mainDevourerPos.y);
            resourcesData.add(0f); // tile number
            resourcesData.add(1f); // texture number main devourer

            //if (resourcesData.size() == 0) return null;
            FloatBuffer result = FloatBuffer.allocate(resourcesData.size());
            for (float fl : resourcesData) {
                result.put(fl);
            }
            return result;
        }
    }

    @NonNull
    private Position calculateDevourerNodePosition(@NonNull PositionInd positionInd) {
        Position result = new Position();
        result.x = positionInd.x * betweenTileCentersX;
        if (positionInd.x % 2 == 0) {
            result.y = positionInd.y * betweenTileCentersY;
        } else {
            result.y = (positionInd.y + 0.5f) * betweenTileCentersY;
        }
        return result;
    }

    @Nullable
    private Position calculateResourcePosition(@NonNull Resource resource) { // interpolation
        Position result = new Position();
        if (resource.nextPosition == null) {
            if (resource.nextPositionInd != null) {
                resource.nextPosition = calculateDevourerNodePosition(resource.nextPositionInd);
            } else {
                return null;
            }
        }
        result.x = resource.startPosition.x + (resource.nextPosition.x - resource.startPosition.x) * resource.distance;
        result.y = resource.startPosition.y + (resource.nextPosition.y - resource.startPosition.y) * resource.distance;
        return result;
    }

    public int getMineralCount(MineralType mineralType) {
        return resourcesStorage.getMineralCount(mineralType);
    }
}
