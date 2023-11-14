package edu.cs4730.videocapture2;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.MediaController;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import edu.cs4730.videocapture2.databinding.FragmentPlayBinding;

/**
 * This fragment uses the viewmodel to determine which videos exist and play them.
 */
public class PlayFragment extends Fragment {

    private videoViewModel myViewModel;
    ArrayAdapter<String> adapter;
    FragmentPlayBinding binding;
    List<String> mfiles;
    Context myContext;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myViewModel = new ViewModelProvider(requireActivity()).get(videoViewModel.class);
        binding = FragmentPlayBinding.inflate(inflater, container, false);

        //needed for adapter, since observer will be called when requirecontext may not work.
        myContext = requireContext();

        binding.videoView.setMediaController(new MediaController(requireActivity()));

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Uri videoUri = Uri.parse(mfiles.get(position));
                binding.videoView.setVideoURI(videoUri);
                binding.videoView.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.videoView.stopPlayback();
            }
        });

        myViewModel.getfiles().observe(requireActivity(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> files) {
                mfiles = files;
                adapter = new ArrayAdapter<String>(myContext, android.R.layout.simple_spinner_item, files);
                //set the dropdown layout
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinner.setAdapter(adapter);
            }

        });
        return binding.getRoot();
    }
}