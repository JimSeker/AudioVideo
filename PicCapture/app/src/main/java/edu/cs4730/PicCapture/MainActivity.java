package edu.cs4730.PicCapture;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//Nothing to see here, see the MainFragment for all the camera code.

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