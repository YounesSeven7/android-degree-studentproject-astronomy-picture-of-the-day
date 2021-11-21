package com.barmej.apod;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.barmej.apod.databinding.ActivityMainBinding;
import com.barmej.apod.entity.AstronomyInfo;
import com.barmej.apod.fragment.DatePickerFragment;
import com.barmej.apod.fragment.FragmentAbout;
import com.barmej.apod.network.NetWork;
import com.barmej.apod.utils.JsonToAstronomyInfo;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DatePickerFragment.OnSelectDay {
    private ActivityMainBinding binding;
    public RequestQueue requestQueue;
    MenuItem menuDownloadHd;
    URL urlDownloadHd;
    AstronomyInfo astronomyInfo;


    ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {

                @Override
                public void onActivityResult(Boolean result) {
                    if (result == true){
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        // send info
                        if (astronomyInfo.getMediaType().equals(JsonToAstronomyInfo.IS_MEDIA_TYPE_Image)) {
                            intent.setType("image/jpeg");
                            Picasso.get().load(astronomyInfo.getUrl().toString()).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), placeHolderDrawable);
                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                                    Uri imageUri =  Uri.parse(path);
                                    intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });

                            //intent.putExtra("send_title", astronomyInfo.getTitle());
                        } else if (astronomyInfo.getMediaType().equals(JsonToAstronomyInfo.IS_MEDIA_TYPE_VIDEO)) {
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, Uri.parse(astronomyInfo.getUrl().toString()));
                        }
                        startActivity(Intent.createChooser(intent, "share via"));
                    } else {
                        Toast.makeText(getBaseContext(), "لم يتم قبول الصلاحية", Toast.LENGTH_SHORT).show();
                    }




                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       /*
        tvTitle = findViewById(R.id.tv_title);
        tvExplanation = findViewById(R.id.tv_explanation);
        webView = findViewById(R.id.wv_video_player);
        progressBar = findViewById(R.id.progressBar);*/

        //
        String toDay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        //Toast.makeText(this, toDay+"", Toast.LENGTH_SHORT).show();
        NetWork.setInfoWithDate(toDay, getBaseContext());
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestInfo();


    }

    public Uri getLocalBitmapUri(Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menuDownloadHd = menu.findItem(R.id.action_download_hd);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_pick_day:
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(), null);
                break;
            case R.id.action_download_hd:
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlDownloadHd.toString()));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                long reference = manager.enqueue(request);
                break;
            case R.id.action_share:
                // permission
                activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            case R.id.action_about:
                FragmentManager fm = getSupportFragmentManager();
                FragmentAbout fragmentAbout = new FragmentAbout();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.cl_container, fragmentAbout);
                ft.addToBackStack(null);
                ft.commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestInfo(){
        binding.progressBar.setVisibility(View.VISIBLE);
        URL url = NetWork.getUrl();
        if (url != null){
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                    url.toString(),
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonArray) {
                            astronomyInfo = JsonToAstronomyInfo.getAstronomyInfo(jsonArray);
                            binding.tvTitle.setText(astronomyInfo.getTitle());
                            binding.tvExplanation.setText(astronomyInfo.getExplanation());
                            if (astronomyInfo.getHdUrl() != null){


                                // to stop music if play
                                binding.wvVideoPlayer.onPause();
                                //
                                binding.ivPictureView.setVisibility(View.VISIBLE);
                                binding.wvVideoPlayer.setVisibility(View.GONE);
                                binding.progressBar.setVisibility(View.GONE);
                                menuDownloadHd.setVisible(true);
                                //
                                Glide.with(getBaseContext())
                                        .load(astronomyInfo.getUrl().toString())
                                        .into(binding.ivPictureView);
                                urlDownloadHd = astronomyInfo.getHdUrl();
                            } else {
                                binding.wvVideoPlayer.onResume();
                                binding.ivPictureView.setVisibility(View.GONE);
                                binding.progressBar.setVisibility(View.GONE);
                                binding.wvVideoPlayer.setVisibility(View.VISIBLE);
                                menuDownloadHd.setVisible(false);
                                binding.wvVideoPlayer.getSettings().setLoadsImagesAutomatically(true);
                                binding.wvVideoPlayer.getSettings().setJavaScriptEnabled(true);
                                binding.wvVideoPlayer.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                                binding.wvVideoPlayer.loadUrl(astronomyInfo.getUrl().toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            String message = null;
                            if (volleyError instanceof NetworkError) {
                                message = "Cannot connect to Internet...Please check your connection!";
                            } else if (volleyError instanceof ServerError) {
                                message = "The server could not be found. Please try again after some time!!";
                            } else if (volleyError instanceof AuthFailureError) {
                                message = "Cannot connect to Internet...Please check your connection!";
                            } else if (volleyError instanceof ParseError) {
                                message = "Parsing error! Please try again after some time!!";
                            } else if (volleyError instanceof NoConnectionError) {
                                message = "Cannot connect to Internet...Please check your connection!";
                            } else if (volleyError instanceof TimeoutError) {
                                message = "Connection TimeOut! Please check your internet connection.";
                            }
                            Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
            //jsonObjectRequest.setTag("TAG");
            requestQueue.add(jsonArrayRequest);
        }

    }

    @Override
    public void onSelectDay(String date) {
        //Toast.makeText(getBaseContext(), String.valueOf(date), Toast.LENGTH_SHORT).show();
        NetWork.setInfoWithDate(date, getBaseContext());
        Log.d("younes", date);
        requestInfo();

    }
}
