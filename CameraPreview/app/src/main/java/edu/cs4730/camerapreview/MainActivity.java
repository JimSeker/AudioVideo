package edu.cs4730.camerapreview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;

//Nothing to see here.  Go to MainFragment.

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener {

    //public static final int REQUEST_PERM_ACCESS = 1;
    String TAG = "MainActivity";
    MainFragment myFrag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
