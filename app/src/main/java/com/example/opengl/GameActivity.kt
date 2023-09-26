package com.example.opengl

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.example.opengl.databinding.ActivityGameBinding
import com.google.android.material.navigation.NavigationView

class GameActivity : AppCompatActivity() {
    var gameView3D: GameView3D? = null
    private var drawer: DrawerLayout? = null
    private var navigation: NavigationView? = null
    private var selectedOrientation: MenuItem? = null
    private var selectedShowMessages: MenuItem? = null
    private fun initialize() {
        Game.instance.gameActivity = this
        gameView3D = findViewById<View>(R.id.myGLSurfaceView) as GameView3D
        Game.instance.gameView3D = gameView3D
        Game.instance.startUp()
        gameView3D!!.testMode = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding =
            DataBindingUtil.setContentView<ActivityGameBinding>(this, R.layout.activity_game)
        //activityMainBinding.setViewModel(new UiViewModel(Game.getInstance()));
        activityMainBinding.viewModel = Game.instance
        activityMainBinding.executePendingBindings()
        //setContentView(R.layout.activity_game);
        initialize()
        drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer!!.closeDrawer(GravityCompat.END)
        val buttonMenu = findViewById<View>(R.id.buttonMenu) as Button
        buttonMenu.setOnClickListener {
            if (drawer!!.isDrawerOpen(GravityCompat.END)) {
                drawer!!.closeDrawer(GravityCompat.END)
            } else {
                drawer!!.openDrawer(GravityCompat.END)
            }
        }
        navigation = findViewById<View>(R.id.navigationViewGameMenu) as NavigationView
        selectedOrientation =
            navigation!!.menu.findItem(R.id.game_menu_orientation_vertical) as MenuItem
        selectedOrientation!!.isChecked = true
        selectedShowMessages = navigation!!.menu.findItem(R.id.game_menu_hide_messages) as MenuItem
        selectedShowMessages!!.isChecked = true
        navigation!!.setNavigationItemSelectedListener { item ->
            val itemId = item.itemId
            if (itemId == R.id.game_menu_orientation_auto) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } else if (itemId == R.id.game_menu_orientation_horizontal) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else if (itemId == R.id.game_menu_orientation_vertical) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else if (itemId == R.id.game_menu_show_messages) {
                Game.instance.setShowMessage(true)
            } else if (itemId == R.id.game_menu_hide_messages) {
                Game.instance.setShowMessage(false)
            } else if (itemId == R.id.game_menu_exit) {
                exitMainMenu()
            }
            if (item.groupId == R.id.group_orientation) {
                if (selectedOrientation != null) {
                    selectedOrientation!!.isChecked = false
                }
                selectedOrientation = item
                selectedOrientation!!.isChecked = true
            } else if (item.groupId == R.id.group_show_messages) {
                if (selectedShowMessages != null) {
                    selectedShowMessages!!.isChecked = false
                }
                selectedShowMessages = item
                selectedShowMessages!!.isChecked = true
            }
            true
        }
    }

    fun exitMainMenu() {
        finishAndRemoveTask()
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        gameView3D!!.onResume()
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }
}