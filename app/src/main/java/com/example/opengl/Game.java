package com.example.opengl;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.opengl.devourer.Devourer;
import com.example.opengl.devourer.DevourerNode;
import com.example.opengl.tiles.Mineral;
import com.example.opengl.tiles.TileMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    private static final Game instance = new Game();
    public GameActivity gameActivity;
    public GameView gameView;
    public TileMap tileMap;
    public int tileMapWidth = 35;
    public int tileMapHeight = 25;
    public Devourer devourer;
    public Mode mode = Mode.VIEW;
    public boolean isShowMessage = false;
    Timer mineralTimer = new Timer();

    public enum Mode {
        BUILD, VIEW, DELETE
    }

    private Game() {
    }

    public static Game getInstance() {
        return instance;
    }

    public void startUp() {
        Properties mapProperties = getMapProperties(R.raw.map_properties, gameActivity);
        tileMapWidth = Integer.parseInt(Objects.requireNonNull(mapProperties).getProperty("width"));
        tileMapHeight = Integer.parseInt(Objects.requireNonNull(mapProperties).getProperty("height"));

        tileMap = new TileMap(tileMapWidth, tileMapHeight, mapProperties);
        tileMap.loadTileMap(
                gameActivity.getResources().openRawResource(R.raw.basis_map),
                gameActivity.getResources().openRawResource(R.raw.mineral_map),
                gameActivity.getResources().openRawResource(R.raw.entity_map));
        devourer = new Devourer(tileMap, gameView.betweenTileCentersX, gameView.betweenTileCentersY);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                redraw();
                mineralTimer.schedule(new TimerTask() {
                    public void run() {
                        mineralTimerTick();
                    }
                }, 0, 33);
            }
        };
        handler.postDelayed(runnable, 1000);

    }

    private void mineralTimerTick() {
        if (devourer.moveResources()) {
            redraw();
        }
    }

    public void showMessage(String s) {
        TextView textView = gameActivity.findViewById(R.id.textView);
        if (isShowMessage) {
            textView.post(new Runnable() {
                public void run() {
                    textView.setText(s);
                }
            });
        } else {
            textView.post(new Runnable() {
                public void run() {
                    textView.setText("");
                }
            });
        }
    }

    public void showMessage2(String s) {
        TextView textView = gameActivity.findViewById(R.id.textView2);
        if (isShowMessage) {
            textView.post(new Runnable() {
                public void run() {
                    textView.setText(s);
                }
            });
        } else {
            textView.post(new Runnable() {
                public void run() {
                    textView.setText("");
                }
            });
        }
    }


    @Nullable
    public static Properties getMapProperties(int mapPropertiesId, @NonNull Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(mapPropertiesId);
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            return properties;
        } catch (IOException e) {
            return null;
        }
    }

    public void onButtonPress() {
        isShowMessage = !isShowMessage;
        if (!isShowMessage) {
            TextView textView = gameActivity.findViewById(R.id.textView);
            textView.setText("");
            textView = gameActivity.findViewById(R.id.textView2);
            textView.setText("");
        } else {
            TextView textView = gameActivity.findViewById(R.id.textView);
            textView.setText("Message on");
            textView = gameActivity.findViewById(R.id.textView2);
            textView.setText("Message on");
        }
    }

    public ShortBuffer createRenderDataTile() {
        List<Short> tilesData = tileMap.getTileMapData();
        ShortBuffer result = ShortBuffer.allocate(tilesData.size());
        for (short sh : tilesData) {
            result.put(sh);
        }
        return result;
    }

    public void tileClickHandler(int indX, int indY) {
        switch (mode) {
            case VIEW:
                selectTile(indX, indY);
                break;
            case BUILD:
                removeSelectTile();
                devourer.placeDevourer(indX, indY);
                redraw();
                break;
            case DELETE:
                removeSelectTile();
                devourer.deleteDevourer(indX, indY);
                redraw();
                break;

            default:
                break;
        }
    }

    public void redraw() {
        gameView.queueEvent(new Runnable() {
            // This method will be called on the rendering
            // thread:
            public void run() {
                gameView.openGLRenderer.prepareTileData(createRenderDataTile());
                gameView.openGLRenderer.prepareMovingData(devourer.createRenderDataMoving());
            }
        });
    }

    private void selectTile(int indX, int indY) {
        tileMap.setLastSelectedTileIndX(indX);
        tileMap.setLastSelectedTileIndY(indY);
        DevourerNode node = devourer.getDevourerNode(indX, indY);
        int dist = -1;
        if (node != null) {
            dist = node.dist;
        }
        showMessage("indX:" + indX + " indY:" + indY + " dist:" + dist);
        redraw();
    }

    private void removeSelectTile() {
        tileMap.setLastSelectedTileIndX(-1);
        tileMap.setLastSelectedTileIndY(-1);
    }
}

// Java Concurrency in Practice https://jcip.net/listings.html
// https://www.baeldung.com/java-synchronization-bad-practices

// https://evileg.com/ru/post/359/#header_%D0%9F%D1%80%D0%B8%D0%BC%D0%B5%D1%80_%D0%BA%D0%BE%D0%B4%D0%B0:
// https://www.techiedelight.com/lee-algorithm-shortest-path-in-a-maze/