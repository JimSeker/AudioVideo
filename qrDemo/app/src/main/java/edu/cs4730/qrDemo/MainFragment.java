package edu.cs4730.qrDemo;

import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This is a demo of how to the the QR code from ZXing code
 * with the integrator and without.  All of this uses intents to
 * "talk" to read/create a QR code.
 * 
 * the return for the scan intent is in the mainActivity code, which then calls 
 * back into the fragment code to display the information via logthis() method.
 * 
 * NOTE. There is no easy way to tell the simulator to use a picture
 * so you need to scan via a actual device!
 */

public class MainFragment extends Fragment {


	TextView logger;
	Button scani, encodei, encodeii,scanii;
	EditText edti, edtii;

	LoggerViewModel mViewModel;

	final String TAG = "MainFragment";

	public MainFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.fragment_main, container, false);

		//deals the screen rotating and keeps the data for the logger.
        //use activity, so it survives the rotation, instead of fragment here.
		mViewModel = ViewModelProviders.of(getActivity()).get(LoggerViewModel.class);

		// logger, to display text/info.
		logger = myView.findViewById(R.id.logger);
		logger.setText(mViewModel.GetData());

		//using only intents
		edti =  myView.findViewById(R.id.edti);
		scani = myView.findViewById(R.id.scani);
		encodei =  myView.findViewById(R.id.encodei);
		Button.OnClickListener mScan = new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				//intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");

				startActivityForResult(intent, 0);
			}
		};
		scani.setOnClickListener(mScan);
		Button.OnClickListener mEncode = new Button.OnClickListener() {
			public void onClick(View v) {
				encodeBarcode("TEXT_TYPE", edti.getText().toString());
			}
		};
		encodei.setOnClickListener(mEncode);


		//using the intentintegrator
		scanii = (Button) myView.findViewById(R.id.scanii);
		encodeii = (Button) myView.findViewById(R.id.encodeii);

		Button.OnClickListener scanQRCode = new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegrator integrator = new IntentIntegrator(getActivity());
				integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
			}
		};
		scanii.setOnClickListener(scanQRCode);
		Button.OnClickListener mEncodeii = new Button.OnClickListener() {
			public void onClick(View v) {
				encodeBarcode2("TEXT_TYPE", edti.getText().toString());
			}
		};
		encodeii.setOnClickListener(mEncodeii);

		return myView;
	}

	//using the intents to call the XYing code.
	private void encodeBarcode(String type, String data) {
		//for other encoding types, see http://code.google.com/p/zxing/source/browse/trunk/androidtest/src/com/google/zxing/client/androidtest/ZXingTestActivity.java
		Intent intent = new Intent("com.google.zxing.client.android.ENCODE");
		intent.putExtra("ENCODE_TYPE", type);
		intent.putExtra("ENCODE_DATA", data);
		startActivity(intent);
	}

	//encode the data, but using the IntentIntegrator
	private void encodeBarcode2(String type, String data) {
		IntentIntegrator integrator = new IntentIntegrator(getActivity());
		integrator.shareText(data);
	}

	/*
	 * simple method to add the log TextView.
	 */
	public void logthis (String item) {
		if (item.compareTo("") != 0) {
		    mViewModel.AddData(item);
			logger.setText(mViewModel.GetData());
			Log.d(TAG, item);
		}
	}

}
