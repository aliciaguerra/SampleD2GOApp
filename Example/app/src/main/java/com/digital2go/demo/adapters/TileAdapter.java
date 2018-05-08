package com.digital2go.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.digital2go.sdk.D2GOSDK;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.digital2go.demo.R;
import com.digital2go.demo.models.Tile;

/**
 * Created by Ulises on 27/12/2017.
 */

public class TileAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;

    private List<Tile> tiles = null;

    public TileAdapter(Context context) {
        this.context = context;
        updateAdapter();
    }

    @Override
    public int getCount() {
        return tiles.size();
    }

    @Override
    public Object getItem(int i) {
        return tiles.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //If null, gets the inflater service
        if (inflater == null) inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Recreates the view with the custom layout
        if (convertView == null) convertView = inflater.inflate(R.layout.tile_row, null);

        ImageView thumb = convertView.findViewById(R.id.thumbnail);
        TextView text = convertView.findViewById(R.id.text);
        text.setText(tiles.get(position).getText());

        thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String link = tiles.get(position).getLink();
                    if (link != null && !TextUtils.isEmpty(link)){
                        if(!link.startsWith("http://")){
                            link = "http://"+link;
                        }
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        context.startActivity(browserIntent);
                    }else Toast.makeText(context, "Empty link", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(context, "Error opening Tile", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (tiles.get(position).getImg() != 0) Glide.with(context).load(tiles.get(position).getImg()).into(thumb);
        else Glide.with(context).load(tiles.get(position).getImg_uri()).into(thumb);
        return convertView;
    }

    public void updateAdapter(){
        JSONArray tilesArray = D2GOSDK.getTiles(context);
        if (tilesArray != null){
            tiles = new ArrayList<>();
            try {
                for (int i = 0; i < tilesArray.length(); i++) {
                    String link = tilesArray.getJSONObject(i).getString("link");
                    String image = tilesArray.getJSONObject(i).getString("image");
                    String title = tilesArray.getJSONObject(i).getString("title");

                    tiles.add(new Tile(link, title, 0, image));
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            tiles = Arrays.asList( //Custom tile list
                    new Tile("http://www.digital2go.com", "Home", R.drawable.home_tile, null),
                    new Tile("http://www.digital2go.com/solutions/", "Solutions", R.drawable.solutions_tile, null),
                    new Tile("http://www.digital2go.com/news/", "News", R.drawable.news_tile, null)
            );
        }
    }
}
