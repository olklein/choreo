package com.olklein.choreo;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by olklein on 15/07/2016.
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
