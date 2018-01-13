package com.olklein.choreo;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


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
class ChoreographerConstants {
    public static String[] DANCE_LIST_NAME;
    public static String[] DANCE_LIST_FILENAME;

    static final private String TAG ="DANCE";
    public static final String TITLENAME 	 = "dance";
    public static final String FILE	 = "filename";

    public static int getMaxFilenameLength() {
        return MaxFilenameLength;
    }
    private static int MaxFilenameLength;

    public static int getIndex(String name){
        int index = 0;
        for (String filename : DANCE_LIST_FILENAME) {
            if (filename.equals(name)) {
                return index;
            }
            index++;
        }
        return -1;

    }

    public static String addNew(Context context, String newFilename) {
        File home = context.getExternalFilesDir(null);
        int addCounter = 1;
        String updatedFilename = newFilename;

        ArrayList<String> fileList = new ArrayList<>();
        int foundIndex = getIndex(newFilename);

        if (null != home) {
            if (home.exists()) {
                Log.d("Files", home.getAbsolutePath());
            }
            if (home.isDirectory()) {
                File[] listOfFiles = home.listFiles();

                for (File file : listOfFiles) {
                    //Log.d("Files", file.getName());
                    String filename = file.getName();
                    if (!filename.endsWith("onscreen")){
                        fileList.add(filename);
                    }
                }
            }
        }
        if (foundIndex == -1) {
            fileList.add(newFilename);
            Collections.sort(fileList, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareTo(s2);
                }
            });
            DANCE_LIST_FILENAME = new String[fileList.size()];
            DANCE_LIST_NAME = new String[fileList.size()];
            int index=0;
            for (String name : fileList) {
                DANCE_LIST_FILENAME[index] = name;
                DANCE_LIST_NAME[index++] = name;
            }
            return newFilename;
        }else{
            if (fileList.size() > 0) {
                Collections.sort(fileList, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return s1.compareTo(s2);
                    }
                });
                DANCE_LIST_FILENAME = new String[fileList.size()];
                DANCE_LIST_NAME = new String[fileList.size()];
                int index=0;
                for (String name : fileList) {
                    DANCE_LIST_FILENAME[index] = name;
                    DANCE_LIST_NAME[index++] = name;
                }
                boolean reDoIt;

                do {
                    foundIndex = getIndex(updatedFilename);
                    if (foundIndex == -1) {
                        fileList.add(updatedFilename);
                        Collections.sort(fileList, new Comparator<String>() {
                            @Override
                            public int compare(String s1, String s2) {
                                return s1.compareTo(s2);
                            }
                        });
                        DANCE_LIST_FILENAME = new String[fileList.size()];
                        DANCE_LIST_NAME = new String[fileList.size()];
                        index=0;
                        for (String name : fileList) {
                            DANCE_LIST_FILENAME[index] = name;
                            DANCE_LIST_NAME[index++] = name;
                        }
                        return updatedFilename;
                    }else{
                                updatedFilename = newFilename + " (" + addCounter + ")";
                                addCounter++;
                                reDoIt = true;
                    }
                } while (reDoIt);
                return updatedFilename;
            } else {
                DANCE_LIST_FILENAME = new String[1];
                DANCE_LIST_NAME = new String[1];

                DANCE_LIST_FILENAME[0] = newFilename;
                DANCE_LIST_NAME[0] = newFilename;
                return newFilename;
            }
        }
    }


    public static void init(Context context){
        File home = context.getExternalFilesDir(null);
        ArrayList<String> fileList= new ArrayList<>();

        if (null != home){
            MaxFilenameLength= 127-home.getAbsolutePath().length()-2-8;
            Log.d(TAG,"MaxFilenameLength ="+ String.valueOf(MaxFilenameLength));
            if (home.exists()){
                Log.d("Files", home.getAbsolutePath());
            }
            if (home.isDirectory()){
                File[] listOfFiles = home.listFiles();

                for (File file : listOfFiles) {
                    //Log.d("Files", file.getName());
                    String filename = file.getName();
                    if (!filename.endsWith("onscreen")){
                        fileList.add(filename);
                    }
                }
            }
        }
        if (fileList.size()>0) {
            Collections.sort(fileList, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareTo(s2);
                }
            });
            DANCE_LIST_FILENAME = new String[fileList.size()];
            DANCE_LIST_NAME =new String[fileList.size()];
            int index = 0;
            for (String name : fileList) {
                DANCE_LIST_FILENAME[index] = name;
                DANCE_LIST_NAME[index++] = name;
            }
            MainActivity.setCurrentItem(0);
        }else{
            DANCE_LIST_FILENAME = new String[0];
            DANCE_LIST_NAME = new String[0];
            MainActivity.setCurrentItem(-1);
        }
    }
}
