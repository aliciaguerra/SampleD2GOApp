package com.digital2go.demo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.digital2go.sdk.D2GOSDK;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Ulises on 27/12/2017.
 */

public class FragmentHome extends Fragment {
    private ListView listView;
    private static TileAdapter adapter;

    Timer timer;

    Thread timeThread;
    ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = (ListView) view.findViewById(R.id.tiles);
        adapter = new TileAdapter(getContext());
        listView.setAdapter(adapter);

        if(timer == null){
            timer = new Timer();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    adapter.updateAdapter();
                }
            }, 500, 1000);
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        timer.cancel();
        timer = null;
    }
}
