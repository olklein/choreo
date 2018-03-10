package com.olklein.choreo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.support.v4.content.FileProvider.getUriForFile;

/**
 * Created by olklein on 06/07/2017.
 *
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects
 *    for all of the code used other than as permitted herein. If you modify
 *    file(s) with this exception, you may extend this exception to your
 *    version of the file(s), but you are not obligated to do so. If you do not
 *    wish to do so, delete this exception statement from your version. If you
 *    delete this exception statement from all source files in the program,
 *    then also delete it in the license file.
 */

class BkgMediaContentBackup {
    public void createCopy(Context ctxt, Uri uri, File dst, String filename) {
        new GetResults(ctxt, uri, dst, filename).execute();
    }

    static class GetResults extends AsyncTask<String, Void, String> {
        final ContentResolver mContentResolver;
        final Uri mUri;
        final File mDst;
        final NotificationManager mNotifyManager;
        final Notification.Builder mBuilder ;


        GetResults(Context context,Uri uri, File dst, String filename){
            mUri = uri;
            mDst = dst;
            mContentResolver = context.getContentResolver();
            Resources mResources = context.getResources();
            Uri fileURI;


            final String mimeType = ListFragment.isMimeTypeValid(uri);
            String type = "*/*";
            if (mimeType!=null) {
                if (mimeType.startsWith("video")) type = "video/*";
                if (mimeType.startsWith("audio")) type = "audio/*";
                if (mimeType.startsWith("image")) type = "image/*";
                if (mimeType.startsWith("application")) type = "application/pdf";
            }


            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileURI = getUriForFile(context, "com.olklein.choreo.fileProvider", dst);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setDataAndType(fileURI, type);
            }else{
                fileURI= Uri.fromFile(dst);
                intent.setDataAndType(fileURI, type);
            }



            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION );

            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);


            mNotifyManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationUtils mNotification = new NotificationUtils(context);
            mNotification.createChannels();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder = new Notification.Builder(context,NotificationUtils.CHOREO_CHANNEL_ID);
            }else{
                mBuilder = new Notification.Builder(context);
            }
            int color = mResources.getColor(android.R.color.holo_blue_light);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setContentTitle(mResources.getString(R.string.app_name))
                        .setContentText(mResources.getString(R.string.backupcompleted,filename))
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .setColor(color);
            }else{
                mBuilder.setContentTitle(mResources.getString(R.string.app_name))
                        .setContentText(mResources.getString(R.string.backupcompleted,filename))
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true);

            }


        }

        @Override
        protected String doInBackground(String... params) {
            try {
                copy(mUri, mDst);
            } catch (IOException e) {
                Log.d("Choreo", "error during copy=");
                e.printStackTrace();
            }
            return "Done";
        }

        protected void onPostExecute(String result) {
            if (mNotifyManager != null){
                mNotifyManager.notify(1335, mBuilder.build());
                mNotifyManager.cancel(1334);
            }
        }
        private void copy(Uri uri, File dst) throws IOException {
            InputStream in = mContentResolver.openInputStream(uri);
            if (in != null) {
                try {
                    OutputStream out = new FileOutputStream(dst);
                    try {
                        // Transfer bytes from in to out
                        byte[] buf = new byte[1024];
                        int len;

                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    } finally {
                        out.close();
                    }
                } finally {
                    in.close();
                }
            }
        }
    }
}
