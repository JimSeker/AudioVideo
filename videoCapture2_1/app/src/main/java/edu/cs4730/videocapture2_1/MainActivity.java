package edu.cs4730.videocapture2_1;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    static int MEDIA_TYPE_IMAGE = 1;
    static int MEDIA_TYPE_VIDEO = 2;
    static int MEDIA_TYPE_AUDIO = 3;
    final static String TAG = "MainActivity";
    private videoViewModel myViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
            .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        myViewModel = new ViewModelProvider(this).get(videoViewModel.class);
    }
}
