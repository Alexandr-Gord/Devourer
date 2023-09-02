package com.example.opengl.tiles;

import androidx.annotation.NonNull;

import com.example.opengl.Resource;
import com.example.opengl.ResourceDepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class TileMap {
    private final List<Tile> tileList;
    private final int tileMapWidth;
    private final int tileMapHeight;
    private final Properties mapProperties;
    private int lastSelectedTileIndX = -1;
    private int lastSelectedTileIndY = -1;
    private int mainEntityTileIndX = -1;
    private int mainEntityTileIndY = -1;
    private static final String DELIMITER_MAP = " ";
    private static final String DELIMITER_PROPERTY = ",";
    private static final int[] FOG_TILE_CODE = new int[]{
            0, 0, 0, 1, 0, 0, 2, 7, 0, 0, 0, 1,
            3, 3, 8, 13, 0, 0, 0, 1, 0, 0, 2, 7,
            4, 4, 4, 25, 9, 9, 14, 23, 0, 6, 0, 12,
            0, 6, 2, 18, 0, 6, 0, 12, 3, 27, 8, 22,
            5, 11, 5, 17, 5, 11, 26, 21, 10, 16, 10, 20,
            15, 19, 24, 28};
    private static final int FULL_FOG_TILE_CODE = 28;

    public TileMap(int tileMapWidth, int tileMapHeight, Properties mapProperties) {
        this.tileMapWidth = tileMapWidth;
        this.tileMapHeight = tileMapHeight;
        this.mapProperties = mapProperties;
        int arraySize = tileMapWidth * tileMapHeight;
        ArrayList<Tile> initList = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            initList.add(new Tile());
        }
        tileList = Collections.synchronizedList(initList);
    }

    public Tile getTile(int x, int y) {
        return isTileExist(x, y) ? tileList.get(getTileIndex(x, y)) : null;
    }

    public void setTile(Tile tile, int x, int y) {
        tileList.set(getTileIndex(x, y), tile);
    }

    private int getTileIndex(int x, int y) {
        return y * tileMapWidth + x;
    }

    public int getLastSelectedTileIndX() {
        return lastSelectedTileIndX;
    }

    public void setLastSelectedTileIndX(int lastSelectedTileIndX) {
        this.lastSelectedTileIndX = lastSelectedTileIndX;
    }

    public int getLastSelectedTileIndY() {
        return lastSelectedTileIndY;
    }

    public void setLastSelectedTileIndY(int lastSelectedTileIndY) {
        this.lastSelectedTileIndY = lastSelectedTileIndY;
    }

    public int getMainEntityTileIndX() {
        return mainEntityTileIndX;
    }

    public int getMainEntityTileIndY() {
        return mainEntityTileIndY;
    }

    private void findMainEntityTilePos() {
        mainEntityTileIndX = 7; // del
        mainEntityTileIndY = 16; // del
        //TODO implement
    }

    public int getTileMapWidth() {
        return tileMapWidth;
    }

    public int getTileMapHeight() {
        return tileMapHeight;
    }

    public void loadTileMap(InputStream basisIn, InputStream mineralIn, InputStream entityIn) {
        try {
            readBasis(basisIn);
            readMinerals(mineralIn);
            readEntity(entityIn);
        } catch (IOException ignored) {
            // todo log exception
        }
        fixSprites();
        findMainEntityTilePos();
        generateFogMap();
    }

    private void readBasis(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        for (int y = tileMapHeight - 1; y > -1; y--) {
            String line = reader.readLine();
            String[] lineArray = line.split(DELIMITER_MAP);
            for (int x = 0; x < tileMapWidth; x++) {
                byte code = Byte.parseByte(lineArray[x]);
                getTile(x, y).setBasis(chooseBasis(code));
            }
        }
        reader.close();
    }

    private Basis chooseBasis(byte code) {
        Basis basis = null;
        switch (code) {
            case 0:
                break;
            case 1:
                basis = new Dirt();
                basis.id = 0;
                break;
        }
        return basis;
    }

    private void readMinerals(InputStream inputStream) {
        synchronized (tileList) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String prefix = "mineral_";
                for (int y = tileMapHeight - 1; y > -1; y--) {
                    String line = reader.readLine();
                    String[] lineArray = line.split(DELIMITER_MAP);
                    for (int x = 0; x < tileMapWidth; x++) {
                        String code = lineArray[x];
                        String[] property = mapProperties.getProperty(prefix + code, "").split(DELIMITER_PROPERTY);
                        if (property.length > 2) {
                            int id = Integer.parseInt(property[0]);
                            int count = Integer.parseInt(property[1]);
                            int period = Integer.parseInt(property[2]);
                            Mineral mineral = chooseMineral(id);
                            if ((mineral != null) && (count > 0)) {
                                ResourceDepository resourceDepository = new ResourceDepository(period);
                                for (int i = 0; i < count; i++) {
                                    Resource resource = new Resource(mineral.type);
                                    resourceDepository.addResource(resource);
                                }
                                mineral.resourceDepository = resourceDepository;
                                getTile(x, y).setMineral(mineral);
                            }
                        }
                    }
                }
                reader.close();
            } catch (IOException e) {

            }
        }
    }

    private Mineral chooseMineral(int id) {
        Mineral mineral = null;
        switch (id) {
            case 0:
                mineral = new Mineral();
                mineral.id = id;
                mineral.type = MineralType.GOLD;
                break;
            case 1:
                mineral = new Mineral();
                mineral.id = id;
                mineral.type = MineralType.CRYSTAL;
                break;
            case 2:
                mineral = new Mineral();
                mineral.id = id;
                mineral.type = MineralType.DIAMOND;
                break;
            default:
                break;
        }
        return mineral;
    }

    private void readEntity(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        for (int y = tileMapHeight - 1; y > -1; y--) {
            String line = reader.readLine();
            String[] lineArray = line.split(DELIMITER_MAP);
            for (int x = 0; x < tileMapWidth; x++) {
                byte code = Byte.parseByte(lineArray[x]);
                getTile(x, y).setEntity(chooseEntity(code));
            }
        }
        reader.close();
    }

    private Entity chooseEntity(byte code) {
        Entity entity = null;
        switch (code) {
            case 0:
                break;
            case 2:
                entity = new EntityTransport();
                break;
        }
        return entity;
    }

    private void fixSprites() {
        Tile tile;
        for (int y = 0; y < tileMapHeight; y++) {
            for (int x = 0; x < tileMapWidth; x++) {
                tile = getTile(x, y);
                if (tile.getEntity() != null) {
                    tile.getEntity().id = calcEntitySpriteCode(x, y);
                }
            }
        }
    }

    public boolean isTileExist(int indX, int indY) {
        return (indX < tileMapWidth) && (indX > -1) && (indY < tileMapHeight) && (indY > -1);
    }

    private boolean isMineralEqual(int indX, int indY, MineralType type) {
        Mineral mineral = getTile(indX, indY).getMineral();
        if (mineral == null) {
            return false;
        } else {
            return mineral.type == type;
        }
    }

    private int calcMineralSpriteCode(int indX, int indY, MineralType type) {
        int code = 0;
        if (indX % 2 == 0) {
            if (isTileExist(indX, indY - 1) && isMineralEqual(indX, indY - 1, type)) {
                code = code + 1;
            }
            if (isTileExist(indX + 1, indY - 1) && isMineralEqual(indX + 1, indY - 1, type)) {
                code = code + 2;
            }
            if (isTileExist(indX + 1, indY) && isMineralEqual(indX + 1, indY, type)) {
                code = code + 4;
            }
            if (isTileExist(indX, indY + 1) && isMineralEqual(indX, indY + 1, type)) {
                code = code + 8;
            }
            if (isTileExist(indX - 1, indY) && isMineralEqual(indX - 1, indY, type)) {
                code = code + 16;
            }
            if (isTileExist(indX - 1, indY - 1) && isMineralEqual(indX - 1, indY - 1, type)) {
                code = code + 32;
            }
        } else {
            if (isTileExist(indX, indY - 1) && isMineralEqual(indX, indY - 1, type)) {
                code = code + 1;
            }
            if (isTileExist(indX + 1, indY) && isMineralEqual(indX + 1, indY, type)) {
                code = code + 2;
            }
            if (isTileExist(indX + 1, indY + 1) && isMineralEqual(indX + 1, indY + 1, type)) {
                code = code + 4;
            }
            if (isTileExist(indX, indY + 1) && isMineralEqual(indX, indY + 1, type)) {
                code = code + 8;
            }
            if (isTileExist(indX - 1, indY + 1) && isMineralEqual(indX - 1, indY + 1, type)) {
                code = code + 16;
            }
            if (isTileExist(indX - 1, indY) && isMineralEqual(indX - 1, indY, type)) {
                code = code + 32;
            }
        }
        return code;
    }

    public boolean isEntity(int indX, int indY) {
        return getTile(indX, indY).getEntity() != null;
    }

    private int calcEntitySpriteCode(int indX, int indY) {
        int code = 0;
        if (indX % 2 == 0) {
            if (isTileExist(indX, indY - 1) && isEntity(indX, indY - 1)) {
                code = code + 1;
            }
            if (isTileExist(indX + 1, indY - 1) && isEntity(indX + 1, indY - 1)) {
                code = code + 2;
            }
            if (isTileExist(indX + 1, indY) && isEntity(indX + 1, indY)) {
                code = code + 4;
            }
            if (isTileExist(indX, indY + 1) && isEntity(indX, indY + 1)) {
                code = code + 8;
            }
            if (isTileExist(indX - 1, indY) && isEntity(indX - 1, indY)) {
                code = code + 16;
            }
            if (isTileExist(indX - 1, indY - 1) && isEntity(indX - 1, indY - 1)) {
                code = code + 32;
            }
        } else {
            if (isTileExist(indX, indY - 1) && isEntity(indX, indY - 1)) {
                code = code + 1;
            }
            if (isTileExist(indX + 1, indY) && isEntity(indX + 1, indY)) {
                code = code + 2;
            }
            if (isTileExist(indX + 1, indY + 1) && isEntity(indX + 1, indY + 1)) {
                code = code + 4;
            }
            if (isTileExist(indX, indY + 1) && isEntity(indX, indY + 1)) {
                code = code + 8;
            }
            if (isTileExist(indX - 1, indY + 1) && isEntity(indX - 1, indY + 1)) {
                code = code + 16;
            }
            if (isTileExist(indX - 1, indY) && isEntity(indX - 1, indY)) {
                code = code + 32;
            }
        }
        return code;
    }

    public List<Short> getTileMapData() {
        List<Short> result = new ArrayList<>();
        synchronized (tileList) {
            Tile tile;
            for (int y = 0; y < tileMapHeight; y++) {
                for (int x = 0; x < tileMapWidth; x++) {
                    tile = getTile(x, y);
                    //if ((tile.getBasis() != null) && (tile.getFogTileId() != FULL_FOG_TILE_CODE)) {
                    if (tile.getBasis() != null) {
                        result.add((short) x);
                        result.add((short) y);
                        result.add((short) 0); // tile number
                        result.add((short) 0); // texture number
                    }
                }
            }

            for (int y = 0; y < tileMapHeight; y++) {
                for (int x = 0; x < tileMapWidth; x++) {
                    tile = getTile(x, y);
                    if (tile.getMineral() != null) {
                        result.add((short) x);
                        result.add((short) y);
                        result.add((short) tile.getMineral().id); // tile number
                        result.add((short) 1); // texture number
                    }
                }
            }

            for (int y = 0; y < tileMapHeight; y++) {
                for (int x = 0; x < tileMapWidth; x++) {
                    tile = getTile(x, y);
                    //if ((tile.getEntity() != null) && (tile.getFogTileId() != FULL_FOG_TILE_CODE)) {
                    if (tile.getEntity() != null) {
                        result.add((short) x);
                        result.add((short) y);
                        result.add((short) tile.getEntity().id); // tile number
                        result.add((short) 2); // texture number
                    }
                }
            }

            for (int y = 0; y < tileMapHeight; y++) {
                for (int x = 0; x < tileMapWidth; x++) {
                    tile = getTile(x, y);
                    if (tile.isFogged()) {
                        result.add((short) x);
                        result.add((short) y);
                        result.add((short) tile.getFogTileId()); // tile number
                        result.add((short) 3); // texture number
                    }
                }
            }

            if ((lastSelectedTileIndX > -1) && (lastSelectedTileIndY > -1)) {
                result.add((short) lastSelectedTileIndX);
                result.add((short) lastSelectedTileIndY);
                result.add((short) 1); // tile number
                result.add((short) 0); // texture number
            }
        }
        return result;
    }

    private void fixEntitySpriteID(@NonNull Tile tile, int indX, int indY) {
        tile.getEntity().id = calcEntitySpriteCode(indX, indY);
    }

    private void fixNearTile(int indX, int indY) {
        int[][] nearInd = getNearIndexes(indX, indY);
        for (int i = 0; i < 6; i++) {
            int nearIndX = nearInd[i][0];
            int nearIndY = nearInd[i][1];
            if ((nearIndX < tileMapWidth) && (nearIndY < tileMapHeight) && (nearIndX > -1) && (nearIndY > -1)) {
                Tile tile = getTile(nearIndX, nearIndY);
                if (tile.getEntity() != null) {
                    fixEntitySpriteID(tile, nearIndX, nearIndY);
                }
            }
        }
        if (isEntity(indX, indY)) {
            fixEntitySpriteID(getTile(indX, indY), indX, indY);
        }
    }

    private boolean isNearEntityExist(int indX, int indY) {
        int[][] nearInd = getNearIndexes(indX, indY);
        boolean isExist = false;
        for (int i = 0; i < 6; i++) {
            int nearIndX = nearInd[i][0];
            int nearIndY = nearInd[i][1];
            if ((nearIndX < tileMapWidth) && (nearIndY < tileMapHeight) && (nearIndX > -1) && (nearIndY > -1)) {
                if (getTile(nearIndX, nearIndY).getEntity() != null) {
                    isExist = true;
                }
            }
        }
        return isExist;
    }

    @NonNull
    private int[][] getNearIndexes(int indX, int indY) {
        int[][] nearInd = new int[6][2];
        if (indX % 2 == 0) {
            nearInd[0][0] = indX;
            nearInd[0][1] = indY - 1;
            nearInd[1][0] = indX + 1;
            nearInd[1][1] = indY - 1;
            nearInd[2][0] = indX + 1;
            nearInd[2][1] = indY;
            nearInd[3][0] = indX;
            nearInd[3][1] = indY + 1;
            nearInd[4][0] = indX - 1;
            nearInd[4][1] = indY;
            nearInd[5][0] = indX - 1;
            nearInd[5][1] = indY - 1;
        } else {
            nearInd[0][0] = indX;
            nearInd[0][1] = indY - 1;
            nearInd[1][0] = indX + 1;
            nearInd[1][1] = indY;
            nearInd[2][0] = indX + 1;
            nearInd[2][1] = indY + 1;
            nearInd[3][0] = indX;
            nearInd[3][1] = indY + 1;
            nearInd[4][0] = indX - 1;
            nearInd[4][1] = indY + 1;
            nearInd[5][0] = indX - 1;
            nearInd[5][1] = indY;
        }
        return nearInd;
    }

    public void placeDevourerTile(int mapIndX, int mapIndY) {
        if (isNearEntityExist(mapIndX, mapIndY)) {
            synchronized (tileList) {
                getTile(mapIndX, mapIndY).setEntity(new EntityTransport());
                fixNearTile(mapIndX, mapIndY);
                generateFogMap(); // todo generate not full map, only part
            }
        }
    }

    public void deleteDevourerTile(int mapIndX, int mapIndY) {
        if (isEntity(mapIndX, mapIndY)) {
            synchronized (tileList) {
                getTile(mapIndX, mapIndY).setEntity(null);
                fixNearTile(mapIndX, mapIndY);
                generateFogMap(); // todo generate not full map, only part
            }
        }
    }

    private void setFogAllMap() {
        for (int y = 0; y < tileMapHeight; y++) {
            for (int x = 0; x < tileMapWidth; x++) {
                getTile(x, y).setFogged(true);
            }
        }
    }

    private void generateFogMap() {
        setFogAllMap(); // todo do or not in settings
        Tile tile;
        for (int y = 0; y < tileMapHeight; y++) {
            for (int x = 0; x < tileMapWidth; x++) {
                tile = getTile(x, y);
                if (tile.getEntity() != null) {
                    tile.setFogged(false);
                    //resetFogAroundEntity(x, y); // todo fog radius in setting
                }
            }
        }
        for (int y = 0; y < tileMapHeight; y++) {
            for (int x = 0; x < tileMapWidth; x++) {
                tile = getTile(x, y);
                if (tile.isFogged()) {
                    tile.setFogTileId(calcFogSpriteCode(x, y));
                }
            }
        }
    }

    private void resetFogAroundEntity(int indX, int indY) {
        int[][] nearInd = getNearIndexes(indX, indY);
        int nearIndX, nearIndY;
        for (int i = 0; i < 6; i++) {
            nearIndX = nearInd[i][0];
            nearIndY = nearInd[i][1];
            if ((nearIndX < tileMapWidth) && (nearIndY < tileMapHeight) && (nearIndX > -1) && (nearIndY > -1)) {
                getTile(nearIndX, nearIndY).setFogged(false);
            }
        }
    }

    private boolean isFogged(int indX, int indY) {
        if ((indX < tileMapWidth) && (indX > -1) && (indY < tileMapHeight) && (indY > -1)) {
            return getTile(indX, indY).isFogged();
        } else {
            return true;
        }
    }

    private int calcFogSpriteCode(int indX, int indY) {
        int code = 0;
        if (indX % 2 == 0) {
            if (isFogged(indX, indY - 1)) {
                code += 1;
            }
            if (isFogged(indX + 1, indY - 1)) {
                code += 2;
            }
            if (isFogged(indX + 1, indY)) {
                code += 4;
            }
            if (isFogged(indX, indY + 1)) {
                code += 8;
            }
            if (isFogged(indX - 1, indY)) {
                code += 16;
            }
            if (isFogged(indX - 1, indY - 1)) {
                code += 32;
            }
        } else {
            if (isFogged(indX, indY - 1)) {
                code += 1;
            }
            if (isFogged(indX + 1, indY)) {
                code += 2;
            }
            if (isFogged(indX + 1, indY + 1)) {
                code += 4;
            }
            if (isFogged(indX, indY + 1)) {
                code += 8;
            }
            if (isFogged(indX - 1, indY + 1)) {
                code += 16;
            }
            if (isFogged(indX - 1, indY)) {
                code += 32;
            }
        }
        return FOG_TILE_CODE[code];
    }

}

