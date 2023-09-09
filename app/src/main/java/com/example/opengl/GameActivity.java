package com.example.opengl;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.opengl.databinding.ActivityGameBinding;
import com.google.android.material.navigation.NavigationView;

public class GameActivity extends AppCompatActivity {
    public GameView3D gameView3D;
    private DrawerLayout drawer;
    private NavigationView navigation;

    private MenuItem selectedOrientation, selectedShowMessages;

    public void initialize() {
        Game.getInstance().gameActivity = this;
        gameView3D = (GameView3D) this.findViewById(R.id.myGLSurfaceView);
        Game.getInstance().gameView3D = this.gameView3D;
        Game.getInstance().startUp();
        gameView3D.testMode = true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGameBinding activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_game);
        //activityMainBinding.setViewModel(new UiViewModel(Game.getInstance()));
        activityMainBinding.setViewModel(Game.getInstance());
        activityMainBinding.executePendingBindings();
        //setContentView(R.layout.activity_game);
        initialize();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.END);
        Button buttonMenu = (Button) findViewById(R.id.buttonMenu);
        buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END);
                } else {
                    drawer.openDrawer(GravityCompat.END);
                }
            }
        });

        navigation = (NavigationView) findViewById(R.id.navigationViewGameMenu);
        selectedOrientation = (MenuItem) navigation.getMenu().findItem(R.id.game_menu_orientation_vertical);
        selectedOrientation.setChecked(true);
        selectedShowMessages = (MenuItem) navigation.getMenu().findItem(R.id.game_menu_hide_messages);
        selectedShowMessages.setChecked(true);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.game_menu_orientation_auto) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                } else if (itemId == R.id.game_menu_orientation_horizontal) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (itemId == R.id.game_menu_orientation_vertical){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else if (itemId == R.id.game_menu_show_messages) {
                    Game.getInstance().setShowMessage(true);
                } else if (itemId == R.id.game_menu_hide_messages) {
                    Game.getInstance().setShowMessage(false);
                } else if (itemId == R.id.game_menu_exit) {
                    exitMainMenu();
                }
                if (item.getGroupId() == R.id.group_orientation) {
                    if (selectedOrientation != null) {
                        selectedOrientation.setChecked(false);
                    }
                    selectedOrientation = item;
                    selectedOrientation.setChecked(true);
                } else if (item.getGroupId() == R.id.group_show_messages) {
                    if (selectedShowMessages != null) {
                        selectedShowMessages.setChecked(false);
                    }
                    selectedShowMessages = item;
                    selectedShowMessages.setChecked(true);
                }
                return true;
            }
        });
    }

    public void exitMainMenu() {
        finishAndRemoveTask();
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView3D.onResume();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}