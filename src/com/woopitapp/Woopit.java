package com.woopitapp;

import android.app.Application;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class Woopit extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Create global configuration and initialize ImageLoader with this configuration
        
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .build();
        
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .defaultDisplayImageOptions(options)
        .build();
        
        com.nostra13.universalimageloader.utils.L.disableLogging();
        
        ImageLoader.getInstance().init(config);
    }
}