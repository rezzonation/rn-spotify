package com.lufinkey.react.spotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionBroadcastReceiverSpotify extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("TRACKS_SPOTIFY")
        .putExtra("actionname_spotify", intent.getAction()));
    }
}
