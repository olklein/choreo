package com.olklein.choreo;

/**
 * Created by olklein on 10/03/2018.
 */

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

public class NotificationUtils extends ContextWrapper {

    private NotificationManager mManager;
    public static final String CHOREO_CHANNEL_ID = "com.olklein.choreo";
    public static final String CHOREO_CHANNEL_NAME = "CHOERO CHANNEL";

    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }

    public void createChannels() {

        // create android channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel androidChannel = null;
            androidChannel = new NotificationChannel(CHOREO_CHANNEL_ID,
                    CHOREO_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            getManager().createNotificationChannel(androidChannel);

        }
    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }
}
