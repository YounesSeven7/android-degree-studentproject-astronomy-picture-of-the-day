package com.barmej.apod.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.barmej.apod.R;

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


    private static NetWork newInstance;
    private RequestQueue requestQueue;
    private Context context;

    // this variable  get date if date selected is larger then date of to day
    private static String saveDate;

    public NetWork(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public  void setInfoWithDate(String date, Context context) {
        String toDay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        try {
            long dataMilliSeconds = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date).getTime();
            long toDayMolliSeconds = Calendar.getInstance().getTime().getTime();
            if (toDayMolliSeconds >= dataMilliSeconds){
                START_DATE = date;
                END_DATE = date;
                saveDate = date;
            } else{
                START_DATE = saveDate;
                END_DATE = saveDate;
                if (this.requestQueue != null)
                Toast.makeText(this.context, R.string.YOU_CANT_GET_INFO_OF_NEXT_DAYS, Toast.LENGTH_SHORT).show();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        
    }
    public  URL getUrl() {
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
    public static synchronized NetWork getInstance(Context context) {
        if (newInstance == null) {
            newInstance = new NetWork(context);
        }
        return newInstance;
    }
    public RequestQueue getRequestQueue() {
        if (requestQueue == null){
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


}
