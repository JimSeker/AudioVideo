package edu.cs4730.videocapture2_1;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

public class PlayFragment extends Fragment {

    private videoViewModel myViewModel;
    ArrayAdapter<String> adapter;
    Spinner mySpinner;
    VideoView vv;
    Context myContext;
    List<String> mfiles;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myViewModel = new ViewModelProvider(getActivity()).get(videoViewModel.class);

        final View myView = inflater.inflate(R.layout.fragment_play, container, false);

        vv = myView.findViewById(R.id.videoView);
        vv.setMediaController(new MediaController(getActivity()));
        myContext = getContext();
        mySpinner = myView.findViewById(R.id.spinner);
        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Uri videoUri = Uri.parse( mfiles.get(position));
                vv.setVideoURI(videoUri);
                vv.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                vv.stopPlayback();
            }
        });

        myViewModel.getfiles().observe(getActivity(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> files) {
                mfiles = files;
                adapter = new ArrayAdapter<String>(myContext, android.R.layout.simple_spinner_item, files);
                //set the dropdown layout
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mySpinner.setAdapter(adapter);
            }

        });
        return myView;
    }
}