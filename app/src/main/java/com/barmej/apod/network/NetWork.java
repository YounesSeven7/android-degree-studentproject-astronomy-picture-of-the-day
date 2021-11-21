package com.barmej.apod.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NetWork {

    public static  final String BASE_URL = "https://api.nasa.gov/planetary/apod";

    public static final String API_KEY_PARAM = "api_key";
    public static final String START_DATE_PARAM = "start_date";
    public static final String END_DATE_PARAM = "end_date";


    public static final String API_KEY = "bOCL9d98JkBQQwS7qYugPqeM2ohQcy4cvVXZzlju";
    public static  String START_DATE = "2021-11-13";
    public static  String END_DATE = "2021-11-13";


    public static void setInfoWithDate(String date, Context context){
        String toDay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        //if (toDay.equals(date))
        try {
            long dataMilliSeconds = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date).getTime();
            long toDayMolliSeconds = Calendar.getInstance().getTime().getTime();
            Log.d("younes", toDayMolliSeconds + ">=" + dataMilliSeconds+ "            "+ Calendar.getInstance().getTime());
            if (toDayMolliSeconds >= dataMilliSeconds){
                START_DATE = date;
                END_DATE = date;
            } else{
                START_DATE = toDay;
                END_DATE = toDay;
                Toast.makeText(context, "لايمكنك الحصول معلومات الأيام القادمة ", Toast.LENGTH_SHORT).show();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        
    }

    public static URL getUrl(){
        Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon();
        Uri uri = uriBuilder
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(START_DATE_PARAM, START_DATE)
                .appendQueryParameter(END_DATE_PARAM, END_DATE)
                .build();
        try {
            URL url = new URL(uri.toString());
            Log.d("younes ", "Url :" + url);
            return  url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
