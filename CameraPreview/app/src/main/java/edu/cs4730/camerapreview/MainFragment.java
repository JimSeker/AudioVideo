package edu.cs4730.camerapreview;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.Callable;


/**
 * A simple fragment to setup the permissions and then launch the demo code.
 */
public class MainFragment extends Fragment {
    String TAG = "MainFragment";
    Button btn1, btn2;
    TextView label;
    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        btn1 = (Button) myView.findViewById(R.id.btn_perm);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               CheckPerm();
            }
        });
        btn2 = (Button) myView.findViewById(R.id.btn_cam);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onFragmentInteraction(1);
            }
        });
        label = (TextView) myView.findViewById(R.id.logperm);

        CheckPerm();

        return myView;
    }

    public void CheckPerm() {
        if ((ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            //I'm on not explaining why, just asking for permission.
            Log.v(TAG, "asking for permissions");
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    MainActivity.REQUEST_PERM_ACCESS);

        } else {
            label.setText("Camera access: Granted\n External File access: Granted\n");
        }

    }

    public void setPerm(Boolean cam, Boolean file) {
        if (cam && file) {
            label.setText("Camera access: Granted\n External File access: Granted\n");
        } else if (cam) {
            label.setText("Camera access: Granted\n External File access: NOT\n");
        } else if (file) {
            label.setText("Camera access: NOT\n External File access: Granted\n");
        } else {
            label.setText("Camera access: NOT\n External File access: NOT\n");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(int which);
    }
}
