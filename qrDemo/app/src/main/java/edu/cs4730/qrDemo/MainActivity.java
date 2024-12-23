package edu.cs4730.qrDemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Most of the code is in the MainFragment.  The activity has the
 * onActivityResult() method here, to get the information on the return from scanning a QR code.
 * <p>
 * https://github.com/srowen/zxing-bsplus which is archived, so barcode scanner+ may disappear at some point too.
 * <p>
 * Note the android zxing is in maintenance mode only.  all the qr codes can be handled via the
 * mlkit/vision libraries now.  So while this code still works (March 8, 2024 in api 34), it will likely fail
 * sometime soon.
 */

public class MainActivity extends AppCompatActivity {
    MainFragment myMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        if (myMainFragment == null) {
            myMainFragment = new MainFragment();
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, myMainFragment).commit();
        }
    }


    //wait for result from startActivityForResult calls.
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        //code to handle the intentintegrator, then

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            // handle scan result
            String contents = scanResult.getContents();
            if (contents != null) {
                myMainFragment.logthis("[II] Scan result is " + scanResult.toString());
            } else {
                myMainFragment.logthis("[II] Scan failed or canceled");
            }

        } else if (requestCode == 0) {
            //normal intent return codes.
            if (resultCode == Activity.RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                // Handle successful scan
                myMainFragment.logthis("[I] scan Result is " + contents);
                myMainFragment.logthis("[I] scan Format is " + format);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Handle cancel
                myMainFragment.logthis("[I] scan cancel");
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
}
