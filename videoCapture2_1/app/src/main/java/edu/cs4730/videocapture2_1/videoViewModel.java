package edu.cs4730.videocapture2_1;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class videoViewModel extends ViewModel {

    private List<String> mylist;
    private MutableLiveData<List<String>> Files;

    public videoViewModel() {
        mylist = new ArrayList<String>();
        Files = new MutableLiveData<>(mylist);

    }

    public void add(String item) {
        mylist.add(item);
        Files.setValue(mylist);
    }

    public LiveData<List<String>> getfiles() {
        return Files;
    }
}