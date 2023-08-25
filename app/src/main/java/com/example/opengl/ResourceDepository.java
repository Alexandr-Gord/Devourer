package com.example.opengl;

import com.example.opengl.devourer.DevourerNode;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResourceDepository {
    private final BlockingQueue<Resource> resourceQueue = new LinkedBlockingQueue<>();
    private final int dispatchPeriod;
    private Timer timer;

    public ResourceDepository(int dispatchPeriod) {
        this.dispatchPeriod = dispatchPeriod;
    }

    public Resource takeResource() {
        try {
            return resourceQueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void addResource(Resource resource) {
        resourceQueue.add(resource);
    }

    public int getSize() {
        return resourceQueue.size();
    }

    public int getDispatchPeriod() {
        return dispatchPeriod;
    }

    public void startResourceMining(DevourerNode startDevourerNode, Position startPosition, List<Resource> movingResources) {
        if (getSize() > 0) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    Resource resource = takeResource();
                    if (resource == null) {
                        timer.cancel();
                        return;
                    }
                    resource.startResource(startDevourerNode, startPosition);
                    movingResources.add(resource);
                }
            }, 0, dispatchPeriod);
        }
    }


}
