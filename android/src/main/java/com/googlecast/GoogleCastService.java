package com.googlecast;

import android.net.Uri;
import android.content.Context;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.DataCastManager;

public class GoogleCastService {
    public static final String REACT_CLASS = "GoogleCastModule";

    public static CastConfiguration getCastConfig(){
        CastConfiguration options = new CastConfiguration.Builder("460E4EDE")
                .enableAutoReconnect()
                .enableNotification()
                .build();
        return options;
    }
}
