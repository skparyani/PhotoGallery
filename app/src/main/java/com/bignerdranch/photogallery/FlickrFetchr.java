package com.bignerdranch.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sateesh Kumar on 10/23/2016.
 */

public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "6db95286b3164ce2b73b6f8dbd88f627";

    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url =new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() +
                    ": with "+ urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(int pageNumber){

        List<GalleryItem> items = new ArrayList<>();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method","flickr.photos.getRecent")
                    .appendQueryParameter("api_key",API_KEY)
                    .appendQueryParameter("format","json")
                    .appendQueryParameter("nojsoncallback","1")
                    .appendQueryParameter("extras","url_s")
                    .appendQueryParameter("page",Integer.toString(pageNumber))
                    .build().toString();

            String jsonString = getUrlString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
            Log.i(TAG,"Received JSON: "+jsonString);
        } catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        } catch(IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }

        return items;
    }


    private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
            throws IOException, JSONException{
        Gson gson = new Gson();
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for(int i=0; i<photoJsonArray.length(); i++){
            GalleryItem item = gson.fromJson(photoJsonArray.getString(i), GalleryItem.class);

            if(item.getUrl() == null){
                continue;
            }
          items.add(item);
        }

    }
}
