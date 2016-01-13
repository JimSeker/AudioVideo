package edu.cs4730.VideoCap2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

//nothing to see here, check the MainFragment code.

public class MainActivity extends AppCompatActivity  {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment()).commit();
        }
    }

}