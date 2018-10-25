package edu.cs4730.qrDemo;

import android.arch.lifecycle.ViewModel;

/**
 * Created by Seker on 2/13/2018.
 */

public class LoggerViewModel extends ViewModel {

    StringBuilder LogData = new StringBuilder();

    String GetData() { return LogData.toString();}

    void AddData(String item) {
        LogData.append(item);
        LogData.append("\n");
    }
}
