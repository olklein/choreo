/*
  Created by olklein on 06/07/2017.


     This program is free software: you can redistribute it and/or  modify
     it under the terms of the GNU Affero General Public License, version 3,
     as published by the Free Software Foundation.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.

     You should have received a copy of the GNU Affero General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.

     As a special exception, the copyright holders give permission to link the
     code of portions of this program with the OpenSSL library under certain
     conditions as described in each individual source file and distribute
     linked combinations including the program with the OpenSSL library. You
     must comply with the GNU Affero General Public License in all respects
     for all of the code used other than as permitted herein. If you modify
     file(s) with this exception, you may extend this exception to your
     version of the file(s), but you are not obligated to do so. If you do not
     wish to do so, delete this exception statement from your version. If you
     delete this exception statement from all source files in the program,
     then also delete it in the license file.
 */


package com.olklein.choreo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private static final int MYWRITEREQUEST = 223;
    private static int currentItem = 0;
    private static CharSequence mTitle;
    private static ActionBar mActionBar;
    private static FragmentManager mFragmentManager;
    private ActionBarDrawerToggle mDrawerToggle;
    private static Fragment mFragment;
    public static void setCurrentItem(int itemPosition) {
        currentItem = itemPosition;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);
        mActionBar = getSupportActionBar();
        mFragmentManager = getSupportFragmentManager();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MYWRITEREQUEST);
        } else {
            mTitle = getTitle();
            ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
            File home = getExternalFilesDir(null);

            if (home!=null) ChoreographerConstants.init(home.getPath());
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((ListFragment) mFragment).onNewFileClick();
                }
            });
            DrawerLayout mDrawerLayout =(DrawerLayout) findViewById(R.id.drawer_layout);
            DanceCustomAdapter danceListAdapter =
                    new DanceCustomAdapter(this, R.layout.dance_custom_list,
                            ChoreographerConstants.DANCE_LIST_FILENAME,mDrawerLayout);
            mDrawerList.setAdapter(danceListAdapter);
            // Set the list's click listener
            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
            ListFragment fragment = new ListFragment();
            fragment.setDrawer(mDrawerLayout);


            selectItem(fragment,currentItem,mDrawerLayout,mDrawerList);
            if (currentItem>=0) {
                setTitle(ChoreographerConstants.DANCE_LIST_FILENAME[currentItem]);
            }
            Syllabus.init(this);

            if (mActionBar!= null) {
                mActionBar.setDisplayShowHomeEnabled(true);
                mActionBar.setDisplayUseLogoEnabled(true);
                mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(),R.color.app_color)));

                mDrawerToggle = new ActionBarDrawerToggle(
                        this,                  /* host Activity */
                        (DrawerLayout) findViewById(R.id.drawer_layout),         /* DrawerLayout object */
                        R.string.drawer_open,  /* "open drawer" description */
                        R.string.drawer_close  /* "close drawer" description */
                ) {
                    /** Called when a drawer has settled in a completely closed state. */
                    public void onDrawerClosed(View view) {
                        super.onDrawerClosed(view);
                        invalidateOptionsMenu();
                    }

                    /** Called when a drawer has settled in a completely open state. */
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                        invalidateOptionsMenu();
                    }
                };


                mDrawerLayout.addDrawerListener(mDrawerToggle);
                if (ChoreographerConstants.DANCE_LIST_FILENAME!=null && ChoreographerConstants.DANCE_LIST_FILENAME.length==0){
                    mActionBar.setDisplayHomeAsUpEnabled(false);
                    mActionBar.setHomeButtonEnabled(false);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }else{
                    mActionBar.setDisplayHomeAsUpEnabled(true);
                    mActionBar.setHomeButtonEnabled(true);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MYWRITEREQUEST: {             // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mTitle = getTitle();
                    ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
                    File home = getExternalFilesDir(null);
                    if (home!=null) ChoreographerConstants.init(home.getPath());
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((ListFragment) mFragment).onNewFileClick();
                        }
                    });
                    DrawerLayout drawer =(DrawerLayout) findViewById(R.id.drawer_layout);
                    DanceCustomAdapter   danceListAdapter =
                            new DanceCustomAdapter(this, R.layout.dance_custom_list,
                                    ChoreographerConstants.DANCE_LIST_FILENAME,drawer);

                    mDrawerList.setAdapter(danceListAdapter);
                    // Set the list's click listener
                    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
                    ListFragment fragment = new ListFragment();
                    fragment.setDrawer(drawer);

                    selectItem(fragment,currentItem,drawer,mDrawerList);
                    setTitle(ChoreographerConstants.DANCE_LIST_FILENAME[currentItem]);
                    Syllabus.init(this);
                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar!= null) {
                        actionBar.setIcon(R.mipmap.ic_launcher);
                        actionBar.setDisplayShowHomeEnabled(true);
                        actionBar.setLogo(R.mipmap.ic_launcher);
                        actionBar.setDisplayUseLogoEnabled(true);
                        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(),R.color.app_color)));
                    }
                }
            }
            break;
        }
    }

    private static void showFragment(Fragment fragment) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.content_frame, fragment, "fragment").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            if (position<ChoreographerConstants.DANCE_LIST_NAME.length) {
                currentItem = position;
                DrawerLayout drawer =(DrawerLayout) findViewById(R.id.drawer_layout);
                ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
                ListFragment fragment = new ListFragment();
                fragment.setDrawer(drawer);

                selectItem(fragment,currentItem,drawer,mDrawerList);
            }
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    public static void selectItem(ListFragment fragment, int position, DrawerLayout drawer, ListView drawerList) {
        if (position == -1){
            position = currentItem;
        }
        // Create a new fragment
        mFragment = fragment;

        Bundle args = new Bundle();
        if (position >=0 && position<ChoreographerConstants.DANCE_LIST_NAME.length) {
            args.putString(ChoreographerConstants.TITLENAME, ChoreographerConstants.DANCE_LIST_NAME[position]);
            args.putString(ChoreographerConstants.FILE, ChoreographerConstants.DANCE_LIST_FILENAME[position]);

            mFragment.setArguments(args);
            showFragment(mFragment);

            mTitle = ChoreographerConstants.DANCE_LIST_NAME[position];
            // setting Toolbar as Action Bar for the App
            if (mActionBar!= null) mActionBar.setTitle(mTitle);

            ((DanceCustomAdapter)drawerList.getAdapter()).setClicked(position);
            drawer.closeDrawer(drawerList);
        }else {
            args.putString(ChoreographerConstants.TITLENAME, "");
            args.putString(ChoreographerConstants.FILE, "");

            mFragment.setArguments(args);
            showFragment(mFragment);

            mTitle = "";
            // setting Toolbar as Action Bar for the App
            if (mActionBar!= null) mActionBar.setTitle(mTitle);

            ((DanceCustomAdapter)drawerList.getAdapter()).setClicked(position);
            drawer.closeDrawer(drawerList);
       }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        // setting Toolbar as Action Bar for the App
        if (mActionBar!= null) mActionBar.setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {
        openQuitDialog();
    }

    private void openQuitDialog() {
        final android.app.AlertDialog.Builder quitDialog = new android.app.AlertDialog.Builder(this);
        quitDialog.setTitle(R.string.button_confirm);
        quitDialog.setNegativeButton(R.string.menu_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        quitDialog.setPositiveButton(R.string.menu_exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                saveBeforeToQuit();
            }
        });
        quitDialog.setIcon(R.mipmap.ic_launcher);
        quitDialog.show();
    }
    private void saveBeforeToQuit(){
        File home = getExternalFilesDir(null);
        ArrayList<String> fileList= new ArrayList<>();

        if (null != home){
            if (home.exists()){
                Log.d("Files", home.getAbsolutePath());
            }
            if (home.isDirectory()){
                File[] listOfFiles = home.listFiles();

                for (File file : listOfFiles) {
                    String filename = file.getName();
                    if (filename.endsWith("onscreen") &&  !file.isDirectory()){
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

            saveItOrNotDialog(fileList);
        }else{
            removeEmptyFiles();
            if (home!=null) ChoreographerConstants.init(home.getPath());
            DrawerLayout drawer =(DrawerLayout) findViewById(R.id.drawer_layout);

            DanceCustomAdapter danceListAdapter =
                    new DanceCustomAdapter(this, R.layout.dance_custom_list,
                            ChoreographerConstants.DANCE_LIST_FILENAME,drawer);

            ListView drawerList = (ListView) findViewById(R.id.left_drawer);
            drawerList.setAdapter(danceListAdapter);
            setCurrentItem(0);
            ListFragment fragment = new ListFragment();
            fragment.setDrawer(drawer);
            selectItem(fragment,0,drawer,drawerList);
            MainActivity.this.finishAffinity();
        }
    }

    private void removeEmptyFiles() {
        File home = this.getExternalFilesDir(null);

        ArrayList<String> fileList= new ArrayList<>();

        if (null != home) {
            if (home.exists()) {
                Log.d("Files", home.getAbsolutePath());
            }
            if (home.isDirectory()) {
                File[] listOfFiles = home.listFiles();
                for (File file : listOfFiles) {
                    String filename = file.getName();
                    if (!file.isDirectory()) {
                        fileList.add(filename);
                    }
                }
            }

            if (fileList.size() > 0) {
                Collections.sort(fileList, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return s1.compareTo(s2);
                    }
                });

                for (String name : fileList) {
                    String filePath = home.getPath() + "/" + name;

                    File file = new File(filePath);
                    if (file.length() == 0 && file.exists()) {
                        boolean isDeleted = file.delete();
                        String TAG = "DANCE";
                        Log.d(TAG, "Deleted(" + name + ")=" + (isDeleted ? "true" : "false"));
                    }
                }
            }
        }
    }


    private  void saveItOrNotDialog(final ArrayList<String> list) {
        final String name;
        File home = getExternalFilesDir(null);
        if (list.size() > 0 && home !=null) {
            final String homePath = home.getPath();
            name = list.get(0);
            list.remove(0);
            final android.app.AlertDialog.Builder saveItOrNotDialog = new android.app.AlertDialog.Builder(this);
            String title = getResources().getString(R.string.saveitornot) + " " + name.replace("onscreen", "");
            saveItOrNotDialog.setTitle(title);

            // Save button: rename filenameOnScreen to filename.
            saveItOrNotDialog.setNegativeButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    String filePathFrom = homePath + "/" + name;
                    String filePathTo = homePath + "/" + name.replace("onscreen", "");
                    File from = new File(filePathFrom);
                    File to = new File(filePathTo);
                    from.renameTo(to);
                    saveItOrNotDialog(list);
                }
            });
            // Cancel button: Do not save the file and stop the loop.
            saveItOrNotDialog.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });
            // Don't Save button: Do not save the file, delete the temporary file and continue the loop.
            saveItOrNotDialog.setNeutralButton(R.string.dontsave, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    String filePathFrom = homePath + "/" + name;
                    File from = new File(filePathFrom);
                    from.delete();
                    saveItOrNotDialog(list);
                }
            });

            saveItOrNotDialog.setIcon(R.mipmap.ic_launcher);
            saveItOrNotDialog.show();
        }else{
            removeEmptyFiles();
            if (home!=null) ChoreographerConstants.init(home.getPath());

            DrawerLayout drawer =(DrawerLayout) findViewById(R.id.drawer_layout);
            DanceCustomAdapter danceListAdapter =
                    new DanceCustomAdapter(MainActivity.this, R.layout.dance_custom_list,
                            ChoreographerConstants.DANCE_LIST_FILENAME,drawer);
            ListView drawerList = (ListView) findViewById(R.id.left_drawer);

            drawerList.setAdapter(danceListAdapter);

            setCurrentItem(0);
            ListFragment fragment = new ListFragment();
            fragment.setDrawer(drawer);

            selectItem(fragment,0, drawer,drawerList);
            MainActivity.this.finishAffinity();
        }
    }
}