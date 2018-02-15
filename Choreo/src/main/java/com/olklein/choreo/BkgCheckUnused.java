package com.olklein.choreo;

import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

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

class BkgCheckUnused {
    public void setup(Context ctxt) {
        new Clean(ctxt).execute();
    }

    static class Clean extends AsyncTask<String, Void, String> {
        final Context ctxt;

        Clean(Context context) {
            ctxt = context;
        }

        private void checkAndRemoveUnused(Context context) {
            ArrayList<File> movieFileList = getFileList(context, Environment.DIRECTORY_MOVIES);
            ArrayList<File> fileList = getFileList(context, null);
            boolean used;
            for (File movie : movieFileList) {
                String movieFileName = movie.getAbsolutePath();
                used=false;
                for (File file : fileList) {
                    used = false;
                    if(isFileContains(file,movieFileName)){
                        used =true;
                        Log.d("Choreo","This file will be removed: "+movieFileName);
                        break;
                    }
                }
                if (!used) {
                    movie.delete();
                }
            }
        }


        @Override
        protected String doInBackground(String... params) {
            checkAndRemoveUnused(ctxt);
            return "Done";
        }

        protected void onPostExecute(String result) {
            Resources resources = ctxt.getResources();
            NotificationManager mNotifyManager;
            NotificationCompat.Builder mBuilder;
            mNotifyManager = (NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

            mBuilder = new NotificationCompat.Builder(ctxt);
            int color = resources.getColor(android.R.color.holo_blue_light);
            mBuilder.setContentTitle(resources.getString(R.string.app_name))
                    .setContentText(resources.getString(R.string.cleanupDone))
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setAutoCancel(true)
                    .setColor(color);

            if(mNotifyManager!=null) mNotifyManager.notify(1237, mBuilder.build());
        }

    }

    static private boolean isFileContains(File file, String movieFileName)  {
        InputStream inputStream = null;
        try {
            String filePath =file.getPath();
            inputStream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return true;
        }
        final Scanner reader = new Scanner(inputStream, "ISO-8859-1");


        while (reader.hasNextLine()) {
            String line = reader.nextLine();

            if(line.contains(movieFileName)) {
                return true;
            }
        }
        return false;
    }


    static private ArrayList<File> getFileList(Context context, String type){
        File home = context.getExternalFilesDir(type);

        ArrayList<File> fileList= new ArrayList<>();

        if (null != home){
            if (home.exists()){
                Log.d("Files", home.getAbsolutePath());
            }
            if (home.isDirectory()){
                File[] listOfFiles = home.listFiles();

                for (File file : listOfFiles) {
                    if (!file.isDirectory()){
                        fileList.add(file);
                    }
                }
            }
        }

        return fileList;
    }
}
