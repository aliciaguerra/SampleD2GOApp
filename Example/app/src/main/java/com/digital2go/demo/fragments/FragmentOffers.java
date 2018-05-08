package com.digital2go.demo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.digital2go.demo.R;
import com.digital2go.demo.adapters.CampaignListAdapter;

/**
 * Created by Ulrick on 10/10/2016.
 */
public class FragmentOffers extends Fragment {
    private ListView listView;
    private static TextView message;
    private static CampaignListAdapter adapter;
    public FragmentOffers() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offers, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        message = (TextView) view.findViewById(R.id.message);

        listView = (ListView) view.findViewById(R.id.list);
        adapter = new CampaignListAdapter(getContext());
        listView.setAdapter(adapter);

        if (adapter.getCount() == 0) message.setText(R.string.no_offers);
        else message.setVisibility(View.GONE);
    }

    public static void addCampaign(String campaign){
        message.setVisibility(View.INVISIBLE);
        adapter.addCampaign(campaign);
    }

    public static void removeCampaign(int position){
        adapter.removeCampaign(position);
        if (adapter.getCount() == 0) {
            message.setText("No Offers Saved");
            message.setVisibility(View.VISIBLE);
        }
    }
}
