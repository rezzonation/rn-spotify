package com.lufinkey.react.spotify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import static com.lufinkey.react.spotify.AuthActivity.spotifyModule;


public class ForeGroundPlayerServiceSpotify extends Service {
  public static final String CHANNEL_ID = "channel2";


  public static final String ACTION_PREVIUOS = "actionprevious_spotify";
  public static final String ACTION_PLAY = "actionplay_spotify";
  public static final String ACTION_NEXT = "actionnext_spotify";

  public static Notification notification;

  private static final String TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE";

  public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

  public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

  public static final String CHANNEL_NAME = "Rhythm";
  public static final String SUCCESSFUL_MESSAGE = "File uploaded successfully";
  public static final String FAILED_MESSAGE = "Failed to upload file";
  private NotificationManager mNotificationManager;
  private NotificationCompat.Builder mBuilder;
  private int courseId;
  private String title;
  private String description;
  private String videoLength;
  Bitmap placeholdericonBitmap;
  String trackName="",artistName="",trackImageUrl="";
  int playbutton=0;



  @Override
  public void onTaskRemoved(Intent rootIntent) {
    stopSelf();
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // String input = intent.getStringExtra("inputExtra");
    try {

      if(intent!=null) {
        if(intent.hasExtra("trackName"))
          trackName = intent.getStringExtra("trackName");
        if(intent.hasExtra("artistName"))
          artistName = intent.getStringExtra("artistName");
        if(intent.hasExtra("trackImageUrl"))
         trackImageUrl = intent.getStringExtra("trackImageUrl");
        if(intent.hasExtra("playbutton"))
        playbutton = intent.getIntExtra("playbutton", 0);

        //  createNotification(this, trackName,artistName,trackImageUrl, playbutton);
        if (trackImageUrl == null || trackImageUrl.trim().equalsIgnoreCase(""))
          trackImageUrl = null;


        placeholdericonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rezzonation_base_image);

        Picasso.with(this)
          .load(trackImageUrl)
          .into(new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
              createNotification(ForeGroundPlayerServiceSpotify.this, trackName, artistName, bitmap, playbutton);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
              createNotification(ForeGroundPlayerServiceSpotify.this, trackName, artistName, placeholdericonBitmap, playbutton);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
              createNotification(ForeGroundPlayerServiceSpotify.this, trackName, artistName, placeholdericonBitmap, playbutton);
            }
          });
      }
    }
    catch (Exception e){
      e.printStackTrace();
    }
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.e("ForgroundService==","OnDestroy");
    if(spotifyModule!=null)
      spotifyModule.clearNotification();

  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
  public void createNotification(Context context, String trackName, String artistName, Bitmap bitmapIcon, int playbutton) {

    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");
    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    PendingIntent pendingIntentPrevious;
    int drw_previous;

    Intent intentPrevious = new Intent(context, NotificationActionBroadcastReceiverSpotify.class)
      .setAction(ACTION_PREVIUOS);
    pendingIntentPrevious = PendingIntent.getBroadcast(context, 0,
      intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
    drw_previous = R.drawable.ic_skip_previous_black_24dp;


    Intent intentPlay = new Intent(context, NotificationActionBroadcastReceiverSpotify.class)
      .setAction(ACTION_PLAY);
    PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,
      intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent pendingIntentNext;
    int drw_next;

    Intent intentNext = new Intent(context, NotificationActionBroadcastReceiverSpotify.class)
      .setAction(ACTION_NEXT);
    pendingIntentNext = PendingIntent.getBroadcast(context, 0,
      intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
    drw_next = R.drawable.ic_skip_next_black_24dp;

    NotificationCompat.Action actionPrevious = new NotificationCompat.Action(drw_previous, "Previous", pendingIntentPrevious);
    NotificationCompat.Action actionPlay = new NotificationCompat.Action(playbutton, "Play", pendingIntentPlay);
    NotificationCompat.Action actionNext = new NotificationCompat.Action(drw_next, "Next", pendingIntentNext);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

      NotificationChannel chan = new NotificationChannel(CHANNEL_ID, "Rezzonation", NotificationManager.IMPORTANCE_DEFAULT);
      chan.setDescription("no sound");
      chan.setSound(null, null);
      chan.setLightColor(Color.BLUE);
      chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
      assert mNotificationManager != null;
      mNotificationManager.createNotificationChannel(chan);

      notification = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_music_note)
        .setContentTitle(trackName)
        .setContentText(artistName)
        .setLargeIcon(bitmapIcon)
       .setColor(getDominantColor(bitmapIcon))
       .setColorized(true)
        .setOnlyAlertOnce(true)//show notification for only first time
        .setShowWhen(false)
        .addAction(actionPrevious)
        .addAction(actionPlay)
        .addAction(actionNext)
        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
          .setShowActionsInCompactView(0, 1, 2))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .build();

      mNotificationManager.notify(1, notification);

    } else {

      notification = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_music_note)
        .setContentTitle(trackName)
        .setContentText(artistName)
        .setLargeIcon(bitmapIcon)
        .setColor(getDominantColor(bitmapIcon))
        .setColorized(true)
        .setOnlyAlertOnce(true)//show notification for only first time
        .setShowWhen(false)
        .addAction(actionPrevious)
        .addAction(actionPlay)
        .addAction(actionNext)
        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
          .setShowActionsInCompactView(0, 1, 2)
          .setMediaSession(mediaSessionCompat.getSessionToken()))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .build();

    }
    startForeground(1, notification);

  }

  public static int getDominantColor(Bitmap bitmap) {
    Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
    final int color = newBitmap.getPixel(0, 0);
    newBitmap.recycle();
    return color;
  }

}
