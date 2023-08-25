package com.example.opengl.tiles;

import net.jcip.annotations.*;

@ThreadSafe
public class Tile {
    @GuardedBy("this")
    private Basis basis;
    @GuardedBy("this")
    private Mineral mineral;
    @GuardedBy("this")
    private Entity entity;
    @GuardedBy("this")
    private boolean isFogged = true;
    @GuardedBy("this")
    private int fogTileId = 28;

    public Tile() {
    }

    public synchronized Basis getBasis() {
        return basis;
    }

    public synchronized void setBasis(Basis basis) {
        this.basis = basis;
    }

    public synchronized Mineral getMineral() {
        return mineral;
    }

    public synchronized void setMineral(Mineral mineral) {
        this.mineral = mineral;
    }

    public synchronized Entity getEntity() {
        return entity;
    }

    public synchronized void setEntity(Entity entity) {
        this.entity = entity;
    }

    public synchronized void setFogged(boolean fogged) {
        isFogged = fogged;
    }
    public synchronized boolean isFogged() {
        return isFogged;
    }

    public synchronized void setFogTileId(int fogTileId) {
        this.fogTileId = fogTileId;
    }

    public synchronized int getFogTileId() {
        return fogTileId;
    }
}
