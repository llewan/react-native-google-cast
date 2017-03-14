package com.googlecast;

import android.net.Uri;
import android.content.Context;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.DataCastManager;

/**
 * Created by Charlie on 6/9/16.
 */
public class GoogleCastService {
    public static final String REACT_CLASS = "GoogleCastModule";

    public static MediaInfo getMediaInfo(String filmUrl, String filmTitle, String imageUrl) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, filmTitle);
        mediaMetadata.addImage(new WebImage(Uri.parse(imageUrl)));

        MediaInfo mediaInfo = new MediaInfo.Builder(filmUrl)
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata).build();
        return mediaInfo;
    }

    public static CastConfiguration getCastConfig(){
        CastConfiguration options = new CastConfiguration.Builder("D4DF8C2E")
                .build();
        return options;
    }
}
