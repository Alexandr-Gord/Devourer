package com.example.opengl;

import com.example.opengl.tiles.MineralType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResourcesStorage {
    private final Map<MineralType, Integer> resourcesStorage = Collections.synchronizedMap(new HashMap<>());

    public ResourcesStorage() {
        for (MineralType mineralType : MineralType.values()) {
            resourcesStorage.put(mineralType, 0);
        }
    }

    public int getMineralCount(MineralType mineralType) {
        Integer count = resourcesStorage.get(mineralType);
        return count == null? 0 : count;
    }

    public void addMineral(MineralType mineralType, int count) {
        Integer currentCount = resourcesStorage.get(mineralType);
        int newCount = currentCount == null? count : count + currentCount;
        resourcesStorage.put(mineralType, newCount);
    }

}

// MVVM https://www.geeksforgeeks.org/mvvm-model-view-viewmodel-architecture-pattern-in-android/
// or https://www.digitalocean.com/community/tutorials/android-mvvm-design-pattern