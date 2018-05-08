package com.digital2go.demo.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.digital2go.sdk.D2GOSDK;
import com.digital2go.sdk.exceptions.SDKException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import com.digital2go.demo.R;
import com.digital2go.demo.fragments.FragmentOffers;
import com.digital2go.demo.utils.Preferences;


/**
 * Created by Ulises Rosas on 18/10/16.
 */
public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.MyViewHolder>{

    private Context mContext;
    private JSONArray campaignList;


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView title, count;
        public ImageView thumbnail, overflow;
        public int position;
        public View card;

        public MyViewHolder(View view) {
            super(view);
            card = view;
            view.setOnClickListener(this);

            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.count);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            thumbnail.setOnClickListener(this);
        }


        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            JSONObject campaign = null;
            try {
                campaign = campaignList.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final Dialog dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.detail_dialog);
            dialog.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.dialog_title);
            try {
                dialog.setTitle(campaign.getJSONObject("campaign_content").getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            TextView header = (TextView) dialog.findViewById(R.id.header);
            ImageView image = (ImageView) dialog.findViewById(R.id.image);
            TextView decription = (TextView) dialog.findViewById(R.id.decription);
            try {
                decription.setText(campaign.getJSONObject("campaign_content").getString("notification_message"));
                header.setText(campaign.getJSONObject("campaign_content").getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            header.bringToFront();
            Button action = (Button) dialog.findViewById(R.id.action);
            action.setVisibility(View.GONE);

            String urlImage;
            String cdn = null;
            try {
                cdn = campaign.getString("media_cdn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (cdn != null && !cdn.isEmpty()) {

                JSONObject campaignContent = null;
                try {
                    campaignContent = campaign.getJSONObject("campaign_content");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject media_image = null;
                try {
                    media_image = campaignContent.getJSONObject("media_image");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (media_image != null) {
                    try {
                        urlImage = campaign.getString("media_cdn");
                        urlImage += "/" + campaign.getJSONObject("campaign_content").getJSONObject("media_image").getString("path");
                        urlImage += campaign.getJSONObject("campaign_content").getJSONObject("media_image").getString("filename");

                        Glide.with(mContext).load(urlImage).into(image);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                JSONObject media_video = null;
                try{
                    media_video = campaignContent.getJSONObject("media_video");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (media_video != null) {

                    String imageUrl = null;
                    try {
                        imageUrl = media_video.getString("video_sthumb");
                        Glide.with(mContext).load(imageUrl).into(image);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } else Glide.with(mContext).load(R.drawable.no_image).fitCenter().into(image);


            Button button = (Button) dialog.findViewById(R.id.save);
            final JSONObject finalCampaign = campaign;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentOffers.addCampaign(finalCampaign.toString());
                    dialog.dismiss();
                }
            });

            try {
                if (campaign.getJSONObject("campaign_content") != null && campaign.getJSONObject("campaign_content").getJSONArray("campaign_content_buttons").length() > 0) {
                    final JSONArray contentButtons = campaign.getJSONObject("campaign_content").getJSONArray("campaign_content_buttons");
                    JSONObject actions = contentButtons.getJSONObject(0);
                    action.setVisibility(View.VISIBLE);
                    action.setText(actions.getString("label"));
                    switch (actions.getString("action")) {
                        // TODO: 31/10/2016 other actions?
                        case "OPEN_URL":
                            String url = actions.getString("data");
                            if (!url.startsWith("http://") && !url.startsWith("https://"))
                                url = "http://" + url;

                            final String finalUrl = url;
                            final JSONObject finalCampaign2 = campaign;
                            action.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
                                    mContext.startActivity(browserIntent);

                                    try {
                                        D2GOSDK.registerInteraction(finalCampaign2, mContext);
                                    } catch (SDKException e) {
                                        e.printStackTrace();
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                    }

                } else {
                    if (campaign.getJSONObject("campaign_content").getJSONObject("media_video") != null) {
                        action.setVisibility(View.VISIBLE);
                        action.setText("    Play    ");
                        final JSONObject finalCampaign1 = campaign;
                        action.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    String videoUrl = finalCampaign1.getJSONObject("campaign_content").getJSONObject("media_video").getString("encoded_file");
                                    Uri uri = Uri.parse(videoUrl);

                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(uri, "video/*");
                                    mContext.startActivity(intent);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else action.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            dialog.show();
        }
    }


    public CampaignAdapter(Context mContext) {
        this.mContext = mContext;
        campaignList = new JSONArray();
    }

    /**
     * Add a campaign to the list and then refresh UI
     * @param campaign Campaign to add
     */
    public void addCampaign(String campaign){
        try {
            if (!containsCampaign(campaign)) campaignList.put(new JSONObject(campaign));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void restartCampaigns(){
        campaignList = new JSONArray();

        notifyDataSetChanged();
    }

    public void setCampaignList(JSONArray campaignList){
        this.campaignList = campaignList;
    }

    private boolean containsCampaign(String campaign) throws JSONException {
        boolean result = false;
        JSONObject object = new JSONObject(campaign);

        for ( int i = 0; i < campaignList.length(); i++){
            if (campaignList.getJSONObject(i).getInt("id") == object.getInt("id")) {
                result = true;
                break;
            }
        }
        return  result;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        JSONObject campaign = null;
        try {
            campaign = campaignList.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            holder.title.setText(campaign.getJSONObject("campaign_content").getString("name"));
            holder.count.setText(campaign.getJSONObject("campaign_content").getString("notification_message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.position = position;

        String cdn = null;
        try {
             cdn = campaign.getString("media_cdn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (cdn != null && !cdn.isEmpty()) {

                JSONObject campaignContent = null;
                try {
                    campaignContent = campaign.getJSONObject("campaign_content");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject media_image = null;
                try {
                     media_image = campaignContent.getJSONObject("media_image");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (media_image != null) {
                    String urlImage = null;
                    try {
                        urlImage = campaign.getString("media_cdn");
                        urlImage += "/" + campaign.getJSONObject("campaign_content").getJSONObject("media_image").getString("path");
                        urlImage += campaign.getJSONObject("campaign_content").getJSONObject("media_image").getString("filename");

                        Glide.with(mContext).load(urlImage).into(holder.thumbnail);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                JSONObject media_video = null;
                try{
                    media_video = campaignContent.getJSONObject("media_video");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (media_video != null) {

                    String imageUrl = null;
                    try {
                        imageUrl = media_video.getString("video_sthumb");
                        Glide.with(mContext).load(imageUrl).into(holder.thumbnail);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

        } else Glide.with(mContext).load(R.drawable.no_image).fitCenter().into(holder.thumbnail);

    }


    @Override
    public int getItemCount() {
        return campaignList.length();
    }
}