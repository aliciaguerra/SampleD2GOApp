package com.digital2go.demo.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.digital2go.sdk.D2GOSDK;
import com.digital2go.sdk.exceptions.SDKException;
import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

import com.digital2go.demo.R;
import com.digital2go.demo.fragments.FragmentOffers;
import com.digital2go.demo.utils.Preferences;


/**
 * Created by Ulrick on 11/10/2016.
 */
public class CampaignListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private JSONArray campaignItems;
    private String url = null;

    public CampaignListAdapter(Context context) {
        this.context = context;
        campaignItems = Preferences.getInstance(context).getCampaigns();
    }

    public void addCampaign(String campaign){
        try {
            if (!containsCampaign(campaign)) campaignItems.put(new JSONObject(campaign));
            Preferences.getInstance(context).saveCampaigns(campaignItems);
            notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeCampaign(int position){
        campaignItems.remove(position);
        Preferences.getInstance(context).saveCampaigns(campaignItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return campaignItems.length();
    }

    @Override
    public Object getItem(int position) {
        Object o = null;
        try {
            o = campaignItems.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return o;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //If null, gets the inflater service
        if (inflater == null) inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Recreates the view with the custom layout
        if (convertView == null) convertView = inflater.inflate(R.layout.list_row, null);
        CircularImageView image = (CircularImageView) convertView.findViewById(R.id.thumbnail);
        TextView name = (TextView) convertView.findViewById(R.id.campaign_name);
        TextView description = (TextView) convertView.findViewById(R.id.campaign_description);


        //Gets the current campaign at #position value
        JSONObject campaign = null;
        try {
            campaign = (JSONObject) campaignItems.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

                        Glide.with(context).load(urlImage).into(image);
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
                        Glide.with(context).load(imageUrl).into(image);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } else Glide.with(context).load(R.drawable.no_image).fitCenter().into(image);



        try {
            name.setText(campaign.getJSONObject("campaign_content").getString("name"));
            description.setText(campaign.getJSONObject("campaign_content").getString("notification_message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject campaign = null;
                try {
                    campaign = campaignItems.getJSONObject(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final Dialog dialog = new Dialog(context);
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

                            Glide.with(context).load(urlImage).into(image);
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
                            Glide.with(context).load(imageUrl).into(image);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } else Glide.with(context).load(R.drawable.no_image).fitCenter().into(image);

                Button button = (Button) dialog.findViewById(R.id.save);
                button.setText("Delete");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentOffers.removeCampaign(position);
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
                                final JSONObject finalCampaign = campaign;
                                action.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
                                        context.startActivity(browserIntent);

                                        try {
                                            D2GOSDK.registerInteraction(finalCampaign, context);
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
                                        context.startActivity(intent);
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
        });
        notifyDataSetChanged();
        return convertView;
    }

    private boolean containsCampaign(String campaign){
        boolean result = false;
        try {
            JSONObject campaignObject = new JSONObject(campaign);
            for (int i = 0; i < campaignItems.length(); i++) {
                JSONObject item = campaignObject.getJSONObject(String.valueOf(i));
                if (item.getInt("id") == campaignObject.getInt("id")) result = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}