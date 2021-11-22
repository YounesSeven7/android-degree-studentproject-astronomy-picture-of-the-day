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
    NetWork netWork;
    FragmentAbout fragmentAbout;

    ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {

                @Override
                public void onActivityResult(Boolean result) {
                    shareImage(result);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // this code get date of to day
        String toDay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        netWork = NetWork.getInstance(this);
        netWork.setInfoWithDate(toDay, getBaseContext());
        requestQueue = netWork.getRequestQueue();
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
                fragmentAbout = new FragmentAbout();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.cl_container, fragmentAbout);
                ft.addToBackStack(null);
                ft.commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void shareImage(Boolean result){
        if (result){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            // send if image
            if (astronomyInfo.getMediaType().equals(JsonToAstronomyInfo.IS_MEDIA_TYPE_Image)) {
                intent.setType("image/jpeg");
                Picasso.get().load(astronomyInfo.getUrl().toString()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                        Uri imageUri =  Uri.parse(path);
                        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {}

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {}
                });
                // send if video
            } else if (astronomyInfo.getMediaType().equals(JsonToAstronomyInfo.IS_MEDIA_TYPE_VIDEO)) {
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, astronomyInfo.getUrl().toString());
            }
            startActivity(Intent.createChooser(intent, "share_info"));
        } else {
            Toast.makeText(getBaseContext(), R.string.DONT_ACCEPT_PERMISSION, Toast.LENGTH_SHORT).show();
        }

    }
    private void requestInfo(){
        binding.progressBar.setVisibility(View.VISIBLE);
        URL url = netWork.getUrl();
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
                            if (fragmentAbout != null){
                                fragmentAbout.getView().setVisibility(View.GONE);
                            }
                            if (astronomyInfo.getHdUrl() != null){


                                // to stop music if play
                                binding.wvVideoPlayer.onPause();
                                //
                                binding.ivPictureView.setVisibility(View.VISIBLE);
                                binding.wvVideoPlayer.setVisibility(View.GONE);
                                binding.progressBar.setVisibility(View.GONE);
                                binding.buttonFullScreen.setVisibility(View.GONE);
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
                                binding.buttonFullScreen.setVisibility(View.VISIBLE);
                                binding.wvVideoPlayer.setVisibility(View.VISIBLE);

                                menuDownloadHd.setVisible(false);
                                // web view settings
                                binding.wvVideoPlayer.getSettings().setLoadsImagesAutomatically(true);
                                binding.wvVideoPlayer.getSettings().setJavaScriptEnabled(true);
                                binding.wvVideoPlayer.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                                binding.wvVideoPlayer.loadUrl(astronomyInfo.getUrl().toString());
                                // full screen
                                binding.buttonFullScreen.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String textButton = (String) binding.buttonFullScreen.getText();
                                        if (textButton.equals("full screen")){
                                            hideSystemUI();
                                        } else if (textButton.equals("unable full screen")) {
                                            showSystemUI();
                                        }

                                    }
                                });


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
                            Toast.makeText(getBaseContext(), String.valueOf(message), Toast.LENGTH_SHORT).show();
                        }
                    });
            netWork.addToRequestQueue(jsonArrayRequest);
        }

    }

    @Override
    public void onSelectDay(String date) {
        netWork.setInfoWithDate(date, getBaseContext());
        Log.d("younes", date);
        requestInfo();

    }
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        binding.buttonFullScreen.setText("unable full screen");
    }
    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        binding.buttonFullScreen.setText("full screen");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        netWork.getRequestQueue().stop();
    }
}
