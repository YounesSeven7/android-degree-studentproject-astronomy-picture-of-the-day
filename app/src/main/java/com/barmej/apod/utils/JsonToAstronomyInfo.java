package com.barmej.apod.utils;

import android.util.Log;

import com.barmej.apod.entity.AstronomyInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class JsonToAstronomyInfo {

    public static final String DATE = "date";
    public static final String EXPLANATION = "explanation";
    public static final String HD_URL = "hdurl";
    public static final String MEDIA_TYPE = "media_type";
    public static final String TITLE = "title";
    public static final String URL = "url";

    public static final String IS_MEDIA_TYPE_Image = "image";
    public static final String IS_MEDIA_TYPE_VIDEO = "video";



    public static  AstronomyInfo getAstronomyInfo(JSONArray jsonArray){
        AstronomyInfo astronomyInfo = new AstronomyInfo();

        try {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            // astronomy info
            astronomyInfo.setDate(jsonObject.getString(DATE));
            astronomyInfo.setExplanation(jsonObject.getString(EXPLANATION));
            astronomyInfo.setMediaType(jsonObject.getString(MEDIA_TYPE));
            astronomyInfo.setTitle(jsonObject.getString(TITLE));
            try {
                astronomyInfo.setUrl(new URL(jsonObject.getString(URL)));
                //
                if (astronomyInfo.getMediaType().equals(IS_MEDIA_TYPE_Image)){
                    astronomyInfo.setHdUrl(new URL(jsonObject.getString(HD_URL)));
                }
                //
                if (astronomyInfo.getMediaType().equals(IS_MEDIA_TYPE_VIDEO)){
                    astronomyInfo.setHdUrl(null);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return astronomyInfo;
    }
}
