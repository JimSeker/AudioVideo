package edu.cs4730.camerapreview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener {

    public static final int REQUEST_PERM_ACCESS = 1;
    String TAG = "MainActivity";
    MainFragment myFrag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            myFrag = new MainFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, myFrag).commit();
        }
    }

    @Override
    public void onFragmentInteraction(int which) {
       if (which == 1) {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Cam2Fragment()).addToBackStack(null).commit();
        }
    }
}
