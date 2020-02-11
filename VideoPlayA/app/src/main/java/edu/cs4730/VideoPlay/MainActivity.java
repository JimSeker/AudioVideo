package edu.cs4730.VideoPlay;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

//nothing to see here, see the MainFragment code.

public class MainActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment()).commit();
        }
    }
}