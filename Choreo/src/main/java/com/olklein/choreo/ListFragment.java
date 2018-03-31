package com.olklein.choreo;

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

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragListView;
import com.woxthebox.draglistview.swipe.ListSwipeHelper;
import com.woxthebox.draglistview.swipe.ListSwipeItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
import static android.graphics.Bitmap.createBitmap;
import static android.support.v4.content.FileProvider.getUriForFile;
import static com.olklein.choreo.R.string.Add_title;
import static com.olklein.choreo.R.string.Figure_Editor;
import static com.olklein.choreo.Syllabus.setDance;

public class ListFragment extends Fragment {

    private static ArrayList<DanceFigure> mItemArray;
    private static DragListView mDragListView;

    final static private String TAG ="DANCE";

    private static final int IMPORT_REQUEST   = 202;
    private static final int VIDEO_IMPORT_REQUEST   = 203;
    private static final int MEDIA_CAPTURE_REQUEST   = 204;


    private static DanceItemAdapter listAdapter;
    private static int sCreatedItems = 0;

    private static String dance_file;
    private boolean isSyllabus=false;
    private static boolean isMediaFolder=false;

    private static String mExternalFilesDir;
    private static String mLoadingFileString;
    private DrawerLayout mDrawer;
    private static ContentResolver mContentResolver;
    private static Resources mResources;

    public static boolean isMediaFolder() {
        return isMediaFolder;
    }

    public static void setMediaFolder(boolean mediaFolder) {
        ListFragment.isMediaFolder = mediaFolder;
    }

    public void setDrawer(DrawerLayout drawer)
    {
        mDrawer = drawer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity mActivity = getActivity();
        File home = mActivity.getExternalFilesDir(null);

        mContentResolver = mActivity.getContentResolver();
        mResources = mActivity.getResources();


        if (home!=null) {
            mExternalFilesDir = home.getPath();
        }

        Bundle bundle = getArguments();
        dance_file = bundle.getString(ChoreographerConstants.FILE);
        mLoadingFileString = getString(R.string.action_loadongoing);
        sCreatedItems = 0;
        setHasOptionsMenu(true);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Log.d(TAG,"In create");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.list_layout, container, false);

        Bundle bundle = getArguments();

        dance_file = bundle.getString(ChoreographerConstants.FILE);

        if (dance_file != null && dance_file.equals("")){
            onNewFileClick();
            onOpenFirstFile (getContext());
        }

        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {}

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                //save only if not a syllabus
                if(!isSyllabus && !isMediaFolder) {
                    try {
                        saveDance(dance_file + "onscreen");
                        updateFragmentList();
                        loadDance(dance_file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mDragListView.setSwipeListener(new ListSwipeHelper.OnSwipeListener() {
            @Override
            public void onItemSwipeStarted(ListSwipeItem item) {
                item.setSupportedSwipeDirection(ListSwipeItem.SwipeDirection.LEFT);
            }

            @Override
            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
                mDragListView.resetSwipedViews(null);
            }

            @Override
            public void onItemSwiping(final ListSwipeItem item, float swipedDistanceX) {
            }
        });

        mItemArray = new ArrayList<>();
        try {
            loadDance(dance_file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupListRecyclerView();
        registerForContextMenu(mDragListView);

        Log.d(TAG,"In createView");
        return view;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_list, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(Syllabus.getDanceId()).setChecked(true);
        menu.findItem(R.id.action_syllabus).setTitle(getResources().getString(R.string.syllabus) + ": " + Syllabus.getName());
        menu.findItem(R.id.action_syllabus).setTitleCondensed(Syllabus.getDanceShortName());
        DrawerLayout dr = (DrawerLayout) this.getActivity().findViewById(R.id.drawer_layout);
        Boolean isDrawerOpen= false;
        if (dr!=null) {
            isDrawerOpen = dr.isDrawerOpen(GravityCompat.START);
        }

        if (isDrawerOpen) {
            menu.findItem(R.id.action_syllabus).setVisible(false);
            menu.findItem(R.id.action_show_comment).setVisible(false);
            menu.findItem(R.id.action_hide_comment).setVisible(false);
            menu.findItem(R.id.action_add_figure).setVisible(false);
            menu.findItem(R.id.action_video).setVisible(false);
            menu.findItem(R.id.action_restore).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(false);
            menu.findItem(R.id.action_saveaspdf).setVisible(false);
            menu.findItem(R.id.action_import_export).setVisible(false);
            menu.findItem(R.id.action_show_syllabus).setVisible(false);
            menu.findItem(R.id.action_view).setVisible(false);
            menu.findItem(R.id.action_new).setVisible(false);
            menu.findItem(R.id.action_show_passport).setVisible(false);
        }else {
            if (isMediaFolder) {
                menu.findItem(R.id.action_syllabus).setVisible(false);
                menu.findItem(R.id.action_show_comment).setVisible(false);
                menu.findItem(R.id.action_hide_comment).setVisible(false);
                menu.findItem(R.id.action_add_figure).setVisible(false);

                if (isMediaFolderEmpty(getContext())) {
                    menu.findItem(R.id.action_video).setVisible(false);
                }else{
                    menu.findItem(R.id.action_video).setVisible(true);
                    menu.findItem(R.id.action_video).setTitle(getResources().getString(R.string.clean));
                }

                menu.findItem(R.id.action_restore).setVisible(false);
                menu.findItem(R.id.action_save).setVisible(false);
                menu.findItem(R.id.action_saveaspdf).setVisible(false);
                menu.findItem(R.id.action_import_export).setVisible(false);
                menu.findItem(R.id.action_new).setVisible(false);
                menu.findItem(R.id.action_show_syllabus).setVisible(false);
                menu.findItem(R.id.action_show_passport).setVisible(false);
                menu.findItem(R.id.action_view).setVisible(false);
            } else{
                menu.findItem(R.id.action_show_passport).setVisible(true);
                if (!isSyllabus) {
                    menu.findItem(R.id.action_syllabus).setVisible(true);
                    menu.findItem(R.id.action_import_export).setVisible(true);
                    if (Syllabus.getDanceId() == R.id.allDances) {
                        menu.findItem(R.id.action_show_syllabus).setVisible(false);
                    } else {
                        menu.findItem(R.id.action_show_syllabus).setVisible(true);
                    }
                    if (dance_file.equals("")) {
                        menu.findItem(R.id.action_add_figure).setVisible(false);
                        menu.findItem(R.id.action_video).setVisible(false);
                        menu.findItem(R.id.action_show_comment).setVisible(false);
                        menu.findItem(R.id.action_hide_comment).setVisible(false);
                        menu.findItem(R.id.action_restore).setVisible(false);
                        menu.findItem(R.id.action_view).setVisible(false);
                        menu.findItem(R.id.action_save).setVisible(false);
                        menu.findItem(R.id.action_saveaspdf).setVisible(false);
                        menu.findItem(R.id.action_import_export).setVisible(true);
                        menu.findItem(R.id.action_new).setVisible(true);
                    } else {
                        menu.findItem(R.id.action_add_figure).setVisible(true);
                        menu.findItem(R.id.action_video).setVisible(true);
                        menu.findItem(R.id.action_video).setTitle(getResources().getString(R.string.action_audiovideoPDF));
                        menu.findItem(R.id.action_show_comment).setVisible(!listAdapter.isCommentEnabled());
                        menu.findItem(R.id.action_hide_comment).setVisible(listAdapter.isCommentEnabled());
                        menu.findItem(R.id.action_restore).setVisible(true);
                        menu.findItem(R.id.action_view).setVisible(false);
                        menu.findItem(R.id.action_save).setVisible(true);
                        menu.findItem(R.id.action_saveaspdf).setVisible(true);
                        menu.findItem(R.id.action_import_export).setVisible(true);
                        menu.findItem(R.id.action_new).setVisible(false);
                    }
                } else {
                    menu.findItem(R.id.action_syllabus).setVisible(true);
                    menu.findItem(R.id.action_show_comment).setVisible(!listAdapter.isCommentEnabled());
                    menu.findItem(R.id.action_hide_comment).setVisible(listAdapter.isCommentEnabled());
                    menu.findItem(R.id.action_add_figure).setVisible(false);
                    menu.findItem(R.id.action_video).setVisible(false);
                    menu.findItem(R.id.action_restore).setVisible(false);
                    menu.findItem(R.id.action_save).setVisible(true);
                    menu.findItem(R.id.action_saveaspdf).setVisible(true);
                    menu.findItem(R.id.action_import_export).setVisible(false);
                    menu.findItem(R.id.action_new).setVisible(false);
                    if (Syllabus.getDanceId() == R.id.allDances) {
                        menu.findItem(R.id.action_show_syllabus).setVisible(false);
                    } else {
                        menu.findItem(R.id.action_show_syllabus).setVisible(true);
                    }
                    menu.findItem(R.id.action_view).setVisible(false);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
            {
                onNewFileClick();
            }
            return true;
            case R.id.action_view:
            {Context context = getContext();
                Resources resource = context.getResources();
                Intent exportIntent = new Intent(Intent.ACTION_VIEW);
                Uri fileURI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    File path = new File(mExternalFilesDir, "");
                    File newFile = new File(path, dance_file+"onscreen");
                    if (!newFile.exists())  newFile = new File(path, dance_file);
                    fileURI= getUriForFile(context, "com.olklein.choreo.fileProvider", newFile);
                    exportIntent.setDataAndType(fileURI,"text/plain");
                    exportIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
                    startActivity(Intent.createChooser(exportIntent, resource.getString(R.string.action_view)));
                }else {
                    String filePath = mExternalFilesDir+"/"+dance_file+"onscreen";
                    File newFile = new File(filePath);
                    if (!newFile.exists())  filePath = mExternalFilesDir+"/"+dance_file;
                    fileURI = Uri.fromFile(new File(filePath));
                    exportIntent.setDataAndType(fileURI,"text/plain");
                    exportIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
                    startActivity(Intent.createChooser(exportIntent, resource.getString(R.string.action_view)));
                }
                return true;
            }
            case R.id.action_import_export: {
                final Context context = getContext();
                final AlertDialog.Builder  alert = new AlertDialog.Builder(context);
                alert.setIcon(R.mipmap.ic_launcher);
                alert.setTitle(R.string.action_import_export);
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                final ArrayAdapter<String> CommandsArrayAdapter = new ArrayAdapter<String> (context, android.R.layout.simple_list_item_1 ) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            text1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        }
                        text1.setBackgroundResource(R.drawable.borderfilled);
                        return view;
                    }
                };
                if (dance_file.equals("")) {
                    if(!isSyllabus) {
                        if (dance_file.equals("")) {
                            String type =context.getResources().getStringArray(R.array.ImportExportCommands)[0];
                            CommandsArrayAdapter.add(type);
                        }else{
                            for (String type :context.getResources().getStringArray(R.array.ImportExportCommands))
                            {
                                CommandsArrayAdapter.add(type);
                            }
                        }
                    }
                }else{
                    for (String type :context.getResources().getStringArray(R.array.ImportExportCommands))
                    {
                        CommandsArrayAdapter.add(type);
                    }
                }
                alert.setAdapter(CommandsArrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int command) {

                        if (command == 0) {
                            Intent si = new Intent(Intent.ACTION_GET_CONTENT);
                            si.setType("*/*");
                            si.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivityForResult(si, IMPORT_REQUEST);
                        }
                        if (command == 1) {
                            Resources resource = context.getResources();
                            Intent exportIntent = new Intent(Intent.ACTION_SEND);

                            Uri fileURI;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                File path = new File(mExternalFilesDir, "");
                                File newFile = new File(path, dance_file+"onscreen");
                                if (!newFile.exists())  newFile = new File(path, dance_file);
                                fileURI= getUriForFile(context, "com.olklein.choreo.fileProvider", newFile);
                            }else {
                                String filePath = mExternalFilesDir+"/"+dance_file+"onscreen";
                                File newFile = new File(filePath);
                                if (!newFile.exists())  filePath = mExternalFilesDir+"/"+dance_file;
                                fileURI = Uri.fromFile(new File(filePath));
                            }

                            exportIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
                            exportIntent.putExtra(Intent.EXTRA_STREAM, fileURI);
                            exportIntent.setType("*/*");

                            exportIntent.putExtra(Intent.EXTRA_SUBJECT, resource.getString(R.string.mail_subjet, dance_file));
                            exportIntent.putExtra(Intent.EXTRA_TEXT, resource.getString(R.string.mail_text));
                            exportIntent.setType("message/rfc822");
                            startActivity(Intent.createChooser(exportIntent, resource.getString(R.string.action_export)));
                        }

                    }
                });

                final AlertDialog dialog = alert.create();
                dialog.show();

                return true;
            }
            case R.id.action_save:
            {
                final Context context = getContext();
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.saveas_dialog, null);

                final AlertDialog.Builder  alert = new AlertDialog.Builder(context);
                alert.setView(promptsView);
                alert.setIcon(R.mipmap.ic_launcher);
                alert.setTitle(R.string.save_as);
                final EditText input1 = (EditText) promptsView.findViewById(R.id.filename);


                input1.setSingleLine();
                input1.setText(dance_file);
                alert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name=input1.getText().toString();
                        if (name.equals(dance_file)){
                            try {
                                saveDance(dance_file);
                                deleteTemporaryFile(dance_file);
                                updateFragmentList();
                                loadDance(dance_file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ChoreographerConstants.init(mExternalFilesDir);
                            doRefreshMenus(context,mDrawer,dance_file,isMediaFolder);
                        }else {
                            File testFile = new File(mExternalFilesDir + "/" + name);
                            // name may just differ from the case point of view. So we check if the file exist
                            if (name.toLowerCase().equals(dance_file.toLowerCase())) {
                                final android.app.AlertDialog.Builder alreadyExistDialog = new android.app.AlertDialog.Builder(context);
                                String title = context.getResources().getString(R.string.saveAlreadyExist, dance_file);
                                alreadyExistDialog.setTitle(title);

                                // Save button: rename filenameonscreen to filename.
                                alreadyExistDialog.setNegativeButton(R.string.replace, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        {
                                            dance_file = input1.getText().toString();
                                            // Non case sensitive management
                                            try {
                                                String filePath = mExternalFilesDir + "/" + dance_file;
                                                File file = new File(filePath);
                                                boolean deleted = file.delete();
                                                Log.d(TAG, "Deleted=" + (deleted ? "true" : "false"));

                                                file = new File(filePath + "onscreen");

                                                if (file.exists()) {
                                                    deleted = file.delete();
                                                    Log.d(TAG, "Deleted=" + (deleted ? "true" : "false"));
                                                }
                                                // Non case sensitive management End


                                                saveDance(dance_file);
                                                deleteTemporaryFile(dance_file);
                                                updateFragmentList();
                                                loadDance(dance_file);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            ChoreographerConstants.init(mExternalFilesDir);
                                            doRefreshMenus(getActivity(), mDrawer, dance_file, isMediaFolder);
                                        }
                                    }
                                });
                                // Cancel button: Do not save the file and stop the loop.
                                alreadyExistDialog.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                    }
                                });

                                alreadyExistDialog.setIcon(R.mipmap.ic_launcher);
                                alreadyExistDialog.show();

                            } else {
                                if (!testFile.exists()) {
                                    dance_file = input1.getText().toString();
                                    try {
                                        saveDance(dance_file);
                                        deleteTemporaryFile(dance_file);
                                        updateFragmentList();
                                        loadDance(dance_file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    ChoreographerConstants.init(mExternalFilesDir);
                                    doRefreshMenus(context,mDrawer,dance_file, isMediaFolder);
                                } else {
                                    final android.app.AlertDialog.Builder alreadyExistDialog = new android.app.AlertDialog.Builder(context);
                                    String conflictName = getEquivalent(testFile);
                                    String title = context.getResources().getString(R.string.saveAlreadyExist, conflictName);

                                    alreadyExistDialog.setTitle(title);

                                    // Save button: rename filenameonscreen to filename.
                                    alreadyExistDialog.setNegativeButton(R.string.replace, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            {
                                                dance_file = input1.getText().toString();
                                                // Non case sensitive management
                                                try {
                                                    String filePath = mExternalFilesDir + "/" + dance_file;
                                                    File file = new File(filePath);
                                                    boolean deleted = file.delete();
                                                    Log.d(TAG, "Deleted=" + (deleted ? "true" : "false"));

                                                    file = new File(filePath + "onscreen");

                                                    if (file.exists()) {
                                                        deleted = file.delete();
                                                        Log.d(TAG, "Deleted=" + (deleted ? "true" : "false"));
                                                    }
                                                    // Non case sensitive management End


                                                    saveDance(dance_file);
                                                    deleteTemporaryFile(dance_file);
                                                    updateFragmentList();
                                                    loadDance(dance_file);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                ChoreographerConstants.init(mExternalFilesDir);
                                                doRefreshMenus(context,mDrawer,dance_file, isMediaFolder);
                                            }
                                        }
                                    });
                                    // Cancel button: Do not save the file and stop the loop.
                                    alreadyExistDialog.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                        }
                                    });

                                    alreadyExistDialog.setIcon(R.mipmap.ic_launcher);
                                    alreadyExistDialog.show();
                                }
                            }
                        }

                    }
                });

                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                final AlertDialog dialog = alert.create();
                dialog.show();
                input1.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (input1.getText().toString().trim().length()<1){
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setVisibility(View.GONE);
                        }else{
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int st, int b, int c)
                    { }
                    @Override
                    public void beforeTextChanged(CharSequence s, int st, int c, int a)
                    { }
                });

                dialog.show();
            }

            return true;
            case R.id.action_saveaspdf:
            {
                saveAsPDFDance(dance_file);
//                activeContentProvider();

            }
            return true;
            case R.id.nav_Licence_Logiciel: {
                final Context context = getContext();

                android.app.AlertDialog.Builder quitDialog = new android.app.AlertDialog.Builder(context);
                quitDialog.setTitle(R.string.action_Licence_Logiciel);
                quitDialog.setMessage(R.string.action_Licence_Logiciel_Info);

                quitDialog.setPositiveButton(R.string.OK, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });
                quitDialog.setIcon(R.mipmap.ic_launcher);
                quitDialog.show();

                return true;
            }
            case R.id.action_add_figure: {
                if (!dance_file.equals("")){
                    addFigure();
                    try {
                        saveDance(dance_file+"onscreen");
                        updateFragmentList();
                        loadDance(dance_file+"onscreen");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
            case R.id.action_restore: {
                final Context context = getContext();
                final android.app.AlertDialog.Builder restoreDialog = new android.app.AlertDialog.Builder(context);
                restoreDialog.setTitle(R.string.button_restore_confirm);
                restoreDialog.setNegativeButton(R.string.menu_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                restoreDialog.setPositiveButton(R.string.menu_restore, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {
                            restoreDance(dance_file);
                            if (listAdapter!= null) {
                                listAdapter.notifyDataSetChanged();
                                listAdapter.setLastPosition(-1);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            updateFragmentList();
                        }

                    }
                });
                restoreDialog.setIcon(R.mipmap.ic_launcher);
                restoreDialog.show();
                return true;
            }
            case R.id.action_show_comment:
                listAdapter.setCommentEnabled(true);
                listAdapter.notifyDataSetChanged();
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.action_hide_comment:
                listAdapter.setCommentEnabled(false);
                listAdapter.notifyDataSetChanged();
                getActivity().invalidateOptionsMenu();
                return true;

            case R.id.action_show_syllabus: {
                final Context context = getContext();
                dance_file= Syllabus.getName();
                isSyllabus=true;
                getActivity().invalidateOptionsMenu();

                DanceCustomAdapter danceListAdapter =
                        new DanceCustomAdapter(context, R.layout.dance_custom_list,
                                ChoreographerConstants.DANCE_LIST_FILENAME,
                                mDrawer);
                ListView drawerList = (ListView) getActivity().findViewById(R.id.left_drawer);
                drawerList.setAdapter(danceListAdapter);
                danceListAdapter.setClicked(-1);
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(R.string.syllabus);
                }
                updateFragmentList();
                Comparator<String[]> comparator = new Comparator<String[]>() {
                    @Override
                    public int compare(String s1[], String s2[]) {
                        return s1[0].compareTo(s2[0]);
                    }
                };
                Collections.sort(Syllabus.figuresWithTempo,comparator);
                for (String[] figure : Syllabus.figuresWithTempo){
                    addFigure(figure);
                }
                return true;
            }
            case R.id.action_show_passport: {
                final Context context = getContext();

                final AlertDialog.Builder  alert = new AlertDialog.Builder(context);
                alert.setIcon(R.mipmap.ic_launcher);
                alert.setTitle(R.string.action_show_passport_latin_or_standard);
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                final ArrayAdapter<String> TypeArrayAdapter = new ArrayAdapter<String> (context, android.R.layout.simple_list_item_1 ) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            text1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        }
                        text1.setBackgroundResource(R.drawable.borderfilled);
                        return view;
                    }
                };

                for (String type :context.getResources().getStringArray(R.array.PassportType))
                {
                    TypeArrayAdapter.add(type);
                }

                alert.setAdapter(TypeArrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int discipline) {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        final int d= discipline;
                        alert.setIcon(R.mipmap.ic_launcher);
                        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });
                        alert.setTitle(R.string.action_show_passport_for);
                        final int items ;
                        if (d==0) {
                            items = R.array.PassportDanceS;
                        }else{
                            items = R.array.PassportDanceL;
                        }
                        final ArrayAdapter<String> DanceArrayAdapter = new ArrayAdapter<String> (context, android.R.layout.simple_list_item_1 ) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    text1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                }
                                text1.setBackgroundResource(R.drawable.borderfilled);
                                return view;
                            }
                        };

                        for (String cmd :context.getResources().getStringArray(items))
                        {
                            DanceArrayAdapter.add(cmd);
                        }
                        DialogInterface.OnClickListener passportMenuListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                passportDialogOnClickListener(which,context,discipline);
                            }
                        };
                        alert.setAdapter(DanceArrayAdapter, passportMenuListener );
                        final AlertDialog dial = alert.create();
                        dial.show();
                    }
                });
                final AlertDialog dialog = alert.create();
                dialog.show();

                return true;
            }
            case R.id.action_video: {
                if (isMediaFolder) {
                    final Context context = getContext();

                    if (!isMediaFolderEmpty(context)) {
                        clean_dialogOpen(context);
                        return true;//
                    }
                    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle(R.string.action_Manage_media_content);
                    alert.setIcon(R.mipmap.ic_launcher);
                    alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });
                    final int commands;
                    commands = R.array.MediaManagementActions;

                    final ArrayAdapter<String> MediaCommandArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                text1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            }
                            text1.setBackgroundResource(R.drawable.borderfilled);
                            return view;
                        }
                    };

                    for (String command : context.getResources().getStringArray(commands)) {
                        MediaCommandArrayAdapter.add(command);
                    }
                    DialogInterface.OnClickListener passportMenuListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                clean_dialogOpen(context);
                            }
                            if (which == 1) {
                                show_mediaContent(context);
                            }
                        }
                    };
                    alert.setAdapter(MediaCommandArrayAdapter, passportMenuListener);


                    final AlertDialog dial = alert.create();
                    dial.show();
                    return true;
                }else{// !isMediafolder
                    final Context context = getContext();
                    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setIcon(R.mipmap.ic_launcher);
                    alert.setTitle(R.string.action_audiovideoPDF);
                    alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    final ArrayAdapter<String> VideoCommandsArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                text1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            }
                            text1.setBackgroundResource(R.drawable.borderfilled);
                            return view;
                        }
                    };

                    for (String type : context.getResources().getStringArray(R.array.VideoCommands)) {
                        VideoCommandsArrayAdapter.add(type);
                    }

                    if (!isMediaFolderEmpty(context)) {
                        for (String type : context.getResources().getStringArray(R.array.VideoCommands2)) {
                            VideoCommandsArrayAdapter.add(type);
                        }
                    }

                    alert.setAdapter(VideoCommandsArrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int command) {

                            if (command == 0 || command == 1) {
                                Intent captureIntent;
                                String extension;
                                if (command == 0) {
                                    captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    extension = "jpeg";
                                } else {
                                    captureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                    extension = "mpeg";
                                }
                                if (captureIntent.resolveActivity(context.getPackageManager()) != null) {
                                    File mediaFile = null;
                                    mediaFile = createMediaFile(extension);

                                    Uri contentURI;
                                    // Continue only if the File was successfully created
                                    if (mediaFile != null) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            contentURI = getUriForFile(context,
                                                    "com.olklein.choreo.fileProvider",
                                                    mediaFile);
                                        } else {
                                            contentURI = Uri.fromFile(mediaFile);
                                        }

                                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentURI);
                                        startActivityForResult(captureIntent, MEDIA_CAPTURE_REQUEST);
                                    }
                                }
                            }
                            if (command == 2) {
                                try {
                                    Intent si = new Intent(Intent.ACTION_GET_CONTENT);
                                    si.setType("*/*");
                                    si.addCategory(Intent.CATEGORY_OPENABLE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        String[] types = {"video/*", "audio/*", "application/pdf", "image/*"};
                                        si.putExtra(Intent.EXTRA_MIME_TYPES, types);
                                    }

                                    startActivityForResult(si, VIDEO_IMPORT_REQUEST);
                                } catch (ActivityNotFoundException anfe) {
                                    Toast.makeText(context, mResources.getString(R.string.action_no_apps), Toast.LENGTH_LONG).show();
                                }
                            }
                            if (command == 3) {
                                final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                alert.setIcon(R.mipmap.ic_launcher);
                                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Canceled.
                                    }
                                });
                                alert.setTitle(R.string.action_Manage_media_content);
                                final int items;
                                items = R.array.MediaManagementActions;

                                final ArrayAdapter<String> MediaCommandArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);
                                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                            text1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                        }
                                        text1.setBackgroundResource(R.drawable.borderfilled);
                                        return view;
                                    }
                                };

                                for (String item : context.getResources().getStringArray(items)) {
                                    MediaCommandArrayAdapter.add(item);
                                }
                                DialogInterface.OnClickListener passportMenuListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            clean_dialogOpen(context);
                                        }
                                        if (which == 1) {
                                            show_mediaContent(context);
                                        }
                                    }
                                };
                                alert.setAdapter(MediaCommandArrayAdapter, passportMenuListener);

                                final AlertDialog dial = alert.create();
                                dial.show();
                            }
                        }
                    });

                    final AlertDialog dialog = alert.create();
                    dialog.show();
                    return true;
                }
            }
            case R.id.allDances:
            case R.id.slowWaltz:
            case R.id.tango:
            case R.id.vienneseWaltz:
            case R.id.Slowfox:
            case R.id.quickstep:
            case R.id.samba:
            case R.id.rumba:
            case R.id.chacha:
            case R.id.paso:
            case R.id.jive:
            {
                final Context context = getContext();
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                if (dance_file != null && dance_file.equals("")){
                    isSyllabus=true;
                    isMediaFolder=false;
                }
                setDance(context,item.getItemId());
                getActivity().invalidateOptionsMenu();
                if (isSyllabus) {
                    dance_file= Syllabus.getName();
                    isSyllabus=true;
                    isMediaFolder=false;
                    getActivity().invalidateOptionsMenu();

                    DanceCustomAdapter danceListAdapter =
                            new DanceCustomAdapter(context, R.layout.dance_custom_list,
                                    ChoreographerConstants.DANCE_LIST_FILENAME,
                                    mDrawer);
                    ListView drawerList = (ListView) getActivity().findViewById(R.id.left_drawer);
                    drawerList.setAdapter(danceListAdapter);
                    danceListAdapter.setClicked(-1);
                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setTitle(R.string.syllabus);
                    }
                    updateFragmentList();
                    Comparator<String[]> comparator = new Comparator<String[]>() {
                        @Override
                        public int compare(String s1[], String s2[]) {
                            return s1[0].compareTo(s2[0]);
                        }
                    };
                    Collections.sort(Syllabus.figuresWithTempo,comparator);
                    for (String[] figure : Syllabus.figuresWithTempo){
                        addFigure(figure);
                    }
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void show_passport(final Context context, int discipline, int dance_id, int color) {
        com.olklein.choreo.Passport.setDance(context,discipline,dance_id,color);
        dance_file = ChoreographerConstants.addNew(context, Passport.getName());
        doRefreshMenus(context,mDrawer,dance_file,false);

        updateFragmentList();

        for (String[] figure : Passport.figuresWithTempo){
            addFigure(figure);
        }
        try {
            saveDance(dance_file);
            saveDance(dance_file + "onscreen");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getEquivalent(final File file) {
        File dir = file.getParentFile();
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().equals(file.getName().toLowerCase());
            }
        });
        if (files!=null && files.length>0)
            return files[0].getName();
        return file.getName();
    }

    public static void refresh(File dst){
        for (DanceFigure item : mItemArray) {
            if (item.getComment().equals(mLoadingFileString) && item.getTempo().contains(dst.getName())){
                item.setComment("");
                Uri uri = Uri.parse(item.getTempo().replaceFirst("VideoURI-", ""));
                String title = getTitle(uri);

                if (title.equals("")) {
                    item.setName(" " + dst.getName());
                }else{
                    item.setName(" "+title);
                }
            }
        }
        try {
            saveDance(dance_file+"onscreen");
            updateFragmentList();
            loadDance(dance_file+"onscreen");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addVideo(String[] figure) {
        String line="";
        String separator=System.getProperty("line.separator");
        if (figure!=null && figure.length==2) {
            line = ++sCreatedItems + ";" + figure[0].replaceAll("<br>",separator) + ";" + figure[1].replaceAll("<br>",separator) + "; ";
        }
        if (figure!=null && figure.length>=3)
        {
            line =    ++sCreatedItems +";"+ figure[0].replaceAll("<br>",separator)+";"+ figure[1].replaceAll("<br>",separator)+"; "+
                    figure[2].replaceAll("<br>",separator) +"; ";
        }
        String[] strings = TextUtils.split(line, ";");
        mItemArray.add(listAdapter.getLastPosition()+1,new DanceFigure(Long.parseLong(strings[0]),strings[1].trim(), strings[2].trim(),strings[3].trim()));
        listAdapter.setLastPosition(listAdapter.getLastPosition()+1);
        listAdapter.notifyDataSetChanged();
    }

    private void addFigure(String[] figure) {
        String line="";
        String separator=System.getProperty("line.separator");
        if (figure!=null && figure.length==2) {
            line = ++sCreatedItems + ";" + figure[0].replaceAll("<br>",separator) + ";" + figure[1].replaceAll("<br>",separator) + "; ";
        }
        if (figure!=null && figure.length>=3)
        {
            line =    ++sCreatedItems +";"+ figure[0].replaceAll("<br>",separator)+";"+ figure[1].replaceAll("<br>",separator)+"; "+
                    figure[2].replaceAll("<br>",separator) +"; ";
        }
        String[] strings = TextUtils.split(line, ";");
        mItemArray.add(listAdapter.getLastPosition()+1,new DanceFigure(Long.parseLong(strings[0]),strings[1].trim(), strings[2].trim(),strings[3].trim()));
        listAdapter.setLastPosition(listAdapter.getLastPosition()+1);
        listAdapter.notifyDataSetChanged();
    }

    private void deleteTemporaryFile(String name) {
        String filePathFrom = mExternalFilesDir + "/" + name+"onscreen";
        File from = new File(filePathFrom);
        if (from.exists()) from.delete();
    }

    private void addFigure() {
        String line =    ++sCreatedItems +";"+ getResources().getString(R.string.pressHereToEdit)+"; ; ";
        String[] strings = TextUtils.split(line, ";");
        mItemArray.add(listAdapter.getLastPosition()+1,new DanceFigure(Long.parseLong(strings[0]),strings[1].trim(), strings[2].trim(),strings[3].trim()));
        listAdapter.setLastPosition(listAdapter.getLastPosition()+1);
        listAdapter.notifyDataSetChanged();
    }

    private void duplicate(int position) {
        DanceFigure fig = mItemArray.get(position);
        mItemArray.add(listAdapter.getLastPosition()+1,new DanceFigure(++sCreatedItems ,fig.getName(), fig.getTempo(),fig.getComment()));
        listAdapter.setLastPosition(listAdapter.getLastPosition()+1);
        listAdapter.notifyDataSetChanged();
    }

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(getContext()));
        listAdapter = new DanceItemAdapter(mItemArray, R.layout.list_figure_item, R.id.image, false,getResources());
        listAdapter.setCommentEnabled(true);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setDragEnabled(true);
        mDragListView.setCustomDragItem(new MydragItem(getContext(), R.layout.list_figure_item));
    }

    private void saveAsPDFDance(String file) {
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/Choreo";
        File outDir=new File(downloadPath);
        if(!outDir.exists()){
            outDir.mkdirs();
        }
        String filename = Util.getNewName(file+".pdf",new File(downloadPath));
        String filePath = downloadPath+"/"+filename;
        saveDanceAsPDFFromPath(getContext(), file, filePath);
    }

    private static void saveDanceAsPDFFromPath(Context context, String headerTitle, String filePath) {
        Log.d(TAG, "saveAsPDF...");

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        if (out != null) {
            PDFCreator creator = new PDFCreator();
            Resources resources = context.getResources();
            creator.createPdf(context, filePath, headerTitle,listAdapter.isCommentEnabled(),
                    resources.getDrawable(R.drawable.choreologo),mItemArray,out);

            NotificationManager mNotifyManager;
            Notification.Builder mBuilder;
            mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationUtils notification = new NotificationUtils(context);
            notification.createChannels();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder = new Notification.Builder(context,NotificationUtils.CHOREO_CHANNEL_ID);
            }else{
                mBuilder = new Notification.Builder(context);
            }
            int color = resources.getColor(android.R.color.holo_blue_light);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setContentTitle(resources.getString(R.string.app_name))
                        .setContentText(resources.getString(R.string.pdf_file_uploading,new File(filePath).getName()))
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setAutoCancel(false)
                        .setColor(color);
            }else{
                mBuilder.setContentTitle(resources.getString(R.string.app_name))
                        .setContentText(resources.getString(R.string.pdf_file_uploading,new File(filePath).getName()))
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setAutoCancel(false);
            }

            if (mNotifyManager!=null) mNotifyManager.notify(1234, mBuilder.build());
        }
    }

    private static void saveDance(String file) throws IOException {
        Log.d(TAG, "save...");
        String filePath = mExternalFilesDir+"/"+file;
        saveDanceFromPath(filePath);
    }
    private static void saveDanceFromPath(String filePath) throws IOException {
        Log.d(TAG, "save...");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter
                    (new OutputStreamWriter(new FileOutputStream(filePath), "ISO-8859-1"));
        } catch (UnsupportedEncodingException | FileNotFoundException e1) {
            e1.printStackTrace();
        }
        if (writer != null) {
            int id = 0;
            for (DanceFigure item : mItemArray) {
                String txt = id++ + ";" +
                        item.getName().replaceAll("\n","<br>") + ";" +
                        item.getTempo().replaceAll("\n","<br>") + ";" +
                        item.getComment().replaceAll("\n","<br>") + "\n";
                writer.write(txt);
            }
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void loadDanceFile(String filePath) throws IOException {
        InputStream inputStream = new FileInputStream(filePath);
        final Scanner reader = new Scanner(inputStream, "ISO-8859-1");
        mItemArray.clear();
        isMediaFolder=false;
        long index=0;
        try {
            String line;
            int lineNumber=0;

            while (reader.hasNextLine()) {
                line = reader.nextLine();
                String[] strings = TextUtils.split(line, ";");
                if (lineNumber++>300) break;
                if (strings.length == 2) {
                    mItemArray.add(new DanceFigure(index++,
                            strings[0],
                            strings[1].replaceAll("<br>",System.getProperty("line.separator"))));
                }
                if (strings.length == 3)mItemArray.add(new DanceFigure(
                        index++,
                        strings[1].replaceAll("<br>",System.getProperty("line.separator")),
                        strings[2].replaceAll("<br>",System.getProperty("line.separator"))));
                if (strings.length >= 4)mItemArray.add(new DanceFigure(
                        index++,
                        strings[1].replaceAll("<br>",System.getProperty("line.separator")),
                        strings[2].replaceAll("<br>",System.getProperty("line.separator")),
                        strings[3].replaceAll("<br>",System.getProperty("line.separator"))));
            }
        } finally {
            reader.close();
        }
        sCreatedItems=mItemArray.size();
        if (listAdapter!= null) listAdapter.notifyDataSetChanged();
    }

    private static void restoreDance(String file) throws IOException {
        String filePath=mExternalFilesDir+"/"+file;
        File testFile= new File(filePath);
        if (testFile.exists()){
            loadDanceFile(filePath);
        }
        if(!isMediaFolder) {
            try {
                saveDance(dance_file + "onscreen");
                updateFragmentList();
                loadDance(dance_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void loadDance(String file) throws IOException {
        String filePath=mExternalFilesDir+"/"+file;
        loadDanceFromPath(filePath);
    }

    private static void loadDanceFromPath(String filePath) throws IOException {
        File testFile= new File(filePath+"onscreen");
        if (testFile.exists()){
            filePath=filePath+"onscreen";
        }
        loadDanceFile(filePath);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (!isMediaFolder) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.figure_item_menu, menu);
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final int position =listAdapter.getLastPosition();
        switch (item.getItemId()) {
            case R.id.duplicate:
                duplicate(position);
                listAdapter.notifyDataSetChanged();
                //sav
                try {
                    saveDance(dance_file+"onscreen");
                    updateFragmentList();
                    loadDance(dance_file+"onscreen");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private static void updateFragmentList(){
        if (mItemArray!=null) mItemArray.clear();
        sCreatedItems = 0;
        if (listAdapter != null) {
            listAdapter.setLastPosition(-1);
            listAdapter.notifyDataSetChanged();
        }
    }



    public void onNewFileClick()
    {final Context context =getActivity();
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.saveas_dialog, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setView(promptsView);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setTitle(R.string.action_new);

        final EditText input1 = (EditText) promptsView.findViewById(R.id.filename);
        input1.setSingleLine();

        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(ChoreographerConstants.getMaxFilenameLength());
        input1.setFilters(filterArray);


        if (Syllabus.getName().equals(context.getResources().getString(R.string.allDances))) {
            input1.setText(dance_file);
        } else {
            input1.setText(Syllabus.getName());
        }

        alert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                dance_file = input1.getText().toString();
                if (dance_file.replaceAll(" ","").equals(""))return;
                ChoreographerConstants.init(mExternalFilesDir);
                dance_file = ChoreographerConstants.addNew(context, dance_file);
                doRefreshMenus(context,mDrawer,dance_file,false);
                updateFragmentList();

                try {
                    saveDance(dance_file);
                    saveDance(dance_file + "onscreen");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();
        input1.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (input1.getText().toString().trim().length()<1){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setVisibility(View.GONE);
                }else{
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c)
            { }
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a)
            { }
        });
    }

    private void onOpenFirstFile(final Context context)
    {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.openfirstfile_dialog, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setView(promptsView);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setTitle(R.string.action_help);

        alert.setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
            DrawerLayout mDrawerLayout = (DrawerLayout)  getActivity().findViewById(R.id.drawer_layout);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMPORT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String displayName = getDisplayName(uri);
                    String filepath = "";
                    String extension = getExtension(uri);

                    int lastDotIndex =displayName.lastIndexOf('.');
                    String shortDisplayName;
                    if (lastDotIndex<0 || lastDotIndex<displayName.length()-5){
                        displayName=displayName+"."+extension;
                        lastDotIndex =displayName.lastIndexOf('.');
                    }
                    shortDisplayName = displayName.substring(0, lastDotIndex);
                    shortDisplayName = cleanFileName(shortDisplayName);

                    if (lastDotIndex > 1)
                    {
                        displayName =
                                displayName.substring(0, lastDotIndex).replace(".", "")
                                        + displayName.substring(lastDotIndex);
                    }

                    filepath = uri.getPath();
                    if (!filepath.equals("")) {
                        File src = new File(filepath);
                        String fileName;
                        if (displayName != null && !displayName.equals("")){
                            fileName = displayName;
                        }else{
                            fileName = src.getName();
                        }
                        Context ctxt = getActivity().getBaseContext();

                        fileName = fileName.replaceAll("\\?","");
                        fileName = fileName.replaceAll("%","");

                        File test = new File(ctxt.getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/" + fileName);

                        Uri fileUri = Uri.parse(test.getAbsolutePath());
                        if (!isExtensionSupported(extension) &&
                                !ListFragment.isMimeTypeSupported(fileUri)){
                            dance_file = ChoreographerConstants.addNew(getActivity().getBaseContext(), fileName);
                            importChoreoFile(ctxt, uri, fileName);
                        }else {
                            if (dance_file.equals("")) {
                                dance_file = ChoreographerConstants.addNew(getActivity().getBaseContext(), shortDisplayName);
                                doRefreshMenus(getActivity().getBaseContext(),mDrawer,dance_file,false);
                                updateFragmentList();

                                try {
                                    saveDance(dance_file);
                                    saveDance(dance_file + "onscreen");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ////
                                importMediaFile(ctxt, uri, fileName);
                            }else{
                                importMediaFile(ctxt, uri, fileName);
                            }
                        }
                    }
                }
            }
        }
        if (requestCode == VIDEO_IMPORT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String filepath = "";
                    String displayName = getDisplayName(uri);

                    String extension = getExtension(uri);
                    int lastDotIndex =displayName.lastIndexOf('.');
                    if (lastDotIndex<0 || lastDotIndex<displayName.length()-5){
                        displayName=displayName+"."+extension;
                        lastDotIndex =displayName.lastIndexOf('.');
                    }
                    if (lastDotIndex > 1)
                    {
                        displayName =
                                displayName.substring(0, lastDotIndex).replace(".", "_")
                                        + displayName.substring(lastDotIndex);
                    }

                    filepath = uri.getPath();
                    if (!filepath.equals("")) {
                        File src = new File(filepath);
                        String fileName;
                        if (displayName != null && !displayName.equals("")){
                            fileName = displayName;
                        }else{
                            fileName = src.getName();
                        }
                        Context ctxt = getActivity().getBaseContext();

                        fileName = fileName.replace("?","");
                        fileName = fileName.replace("%","");

                        File test = new File(ctxt.getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/" + fileName);

                        Uri videoUri = Uri.parse(test.getAbsolutePath());
                        if (!isExtensionSupported(extension) &&
                                !ListFragment.isMimeTypeSupported(videoUri)){
                            //Toast.makeText(getContext(), mResources.getString(R.string.action_video_unreadable), Toast.LENGTH_LONG).show();
                            String[] video = {"", "VideoURI-" + videoUri.toString(),fileName};
                            addVideo(video);
                        }else {
                            importMediaFile(ctxt, uri, fileName);
                        }
                    }
                }
            }
        }

        if (requestCode == MEDIA_CAPTURE_REQUEST && resultCode == RESULT_OK) {
            Uri videoUri = Uri.parse(mCurrentMediaFilePath);
            String[] video = {mCurrentMediaFileName, "VideoURI-" + videoUri.toString()};
            addVideo(video);
//            galleryAddPic();

            try {
                saveDance(dance_file + "onscreen");
                updateFragmentList();
                loadDance(dance_file + "onscreen");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String cleanFileName(String shortDisplayName) {
        shortDisplayName = shortDisplayName.replaceAll("%","");
        shortDisplayName = shortDisplayName.replaceAll("\\[","");
        shortDisplayName = shortDisplayName.replaceAll("\\]","");
        shortDisplayName = shortDisplayName.replaceAll("\\?","");
        shortDisplayName = shortDisplayName.replaceAll( "à", "a");
        shortDisplayName = shortDisplayName.replaceAll( "â", "a");
        shortDisplayName = shortDisplayName.replaceAll( "ç", "c");
        shortDisplayName = shortDisplayName.replaceAll( "è", "e");
        shortDisplayName = shortDisplayName.replaceAll( "é", "e");
        shortDisplayName = shortDisplayName.replaceAll( "ê", "e");
        shortDisplayName = shortDisplayName.replaceAll( "ë", "e");
        shortDisplayName = shortDisplayName.replaceAll( "ï", "i");
        shortDisplayName = shortDisplayName.replaceAll( "î", "i");
        shortDisplayName = shortDisplayName.replaceAll( "ö", "o");
        shortDisplayName = shortDisplayName.replaceAll( "ô", "o");
        shortDisplayName = shortDisplayName.replaceAll( "ü", "u");
        shortDisplayName = shortDisplayName.replaceAll( "û", "u");
        shortDisplayName = shortDisplayName.replaceAll( "ù", "u");
        shortDisplayName = shortDisplayName.replaceAll("\"","");
        shortDisplayName = shortDisplayName.replace("~","");

        return shortDisplayName;
    }

    private boolean isExtensionSupported(String extension) {
        if (extension.equals("")){
            return false;
        }else{
            final MimeTypeMap mime = MimeTypeMap.getSingleton();

            String mimeType = mime.getMimeTypeFromExtension(extension);
            if (mimeType == null) return false;
            if (!mimeType.startsWith("video") && !mimeType.startsWith("audio")
                    && !mimeType.startsWith("image") && !mimeType.startsWith("application/pdf")) return false;
        }
        return true;
    }

    private String getExtension(Uri uri) {
        String uriString = uri.toString();
        String extension="";
        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = mContentResolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final MimeTypeMap mime = MimeTypeMap.getSingleton();
                    extension = mime.getExtensionFromMimeType(mContentResolver.getType(uri));
                    if (extension==null) {
                        extension = "";
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            File myFile = new File(uriString);
            extension = myFile.getName();
            int lastIndex = extension.lastIndexOf('.');
            if (lastIndex > 0) {
                extension = extension.substring(lastIndex);
            } else {
                extension = "";
            }
        }
        return extension;
    }

    private String getDisplayName(Uri uri) {
        String uriString = uri.toString();
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index>=0){
                        displayName = cursor.getString(index);
                        Log.d("Choreo", "DisplayName is " + displayName);
                    }else {
                        displayName = "";
                        Log.d("Choreo", "DisplayName is empty!" + displayName);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            File myFile = new File(uriString);
            displayName = myFile.getName();
        }
        return displayName;
    }

    private void importChoreoFile(Context ctxt, Uri uri, String fileName) {
        File dest = new File(mExternalFilesDir + "/" + dance_file);

        try {
            copy(uri, dest);
        } catch (IOException e) {
            Log.d(TAG, "error =" + fileName);
            e.printStackTrace();
            Toast.makeText(ctxt,
                    mResources.getString(R.string.action_unvalid_file),
                    Toast.LENGTH_LONG).show();
            return;
        }
        try {
            updateFragmentList();
            loadDance(dance_file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ChoreographerConstants.init(mExternalFilesDir);
        doRefreshMenus(getContext(),mDrawer,dance_file,isMediaFolder);
    }

    private void importMediaFile(Context ctxt, Uri uri, String fileName) {
        File dest = new File(ctxt.getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/" + fileName);
        File destTMP = new File(ctxt.getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/" + fileName + ".tmp");

        Uri videoUri = Uri.parse(dest.getAbsolutePath());
        if (!ListFragment.isMimeTypeSupported(videoUri)){
            //Toast.makeText(getContext(), mResources.getString(R.string.action_video_unreadable), Toast.LENGTH_LONG).show();
            String[] video = {"", "VideoURI-" + videoUri.toString(),fileName};
            addVideo(video);
        }else {
            dest.setReadable(false);

            String[] video = {"", "VideoURI-" + videoUri.toString(), mLoadingFileString};
            addVideo(video);

            BkgCopier creator = new BkgCopier();
            creator.createCopy(getActivity().getBaseContext(), uri, destTMP, dest);
            SystemClock.sleep(TimeUnit.SECONDS.toMillis(1));

            try {
                saveDance(dance_file + "onscreen");
                updateFragmentList();
                loadDance(dance_file + "onscreen");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void copy(Uri uri, File dst) throws IOException {
        InputStream in = getContext().getContentResolver().openInputStream(uri);
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


    public static void openItemDialog(final View view, final int pos) {
        final Context context = view.getContext();
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.edit_dialog, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setView(promptsView);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setTitle(Figure_Editor);
        final AutoCompleteTextView input1 = (AutoCompleteTextView) promptsView.findViewById(R.id.name);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, Syllabus.figures);

        input1.setAdapter(adapter);
        input1.setThreshold(1);

        final AutoCompleteTextView input2 = (AutoCompleteTextView) promptsView.findViewById(R.id.rhythm);
        input2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ArrayList<String> list = Syllabus.getRhythmFor(input1.getText().toString());
                if (list.size() > 0) {
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, list);
                    input2.setAdapter(adapter2);
                    input2.setThreshold(0);
                }
            }
        });
        input2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> list = Syllabus.getRhythmFor(input1.getText().toString());
                Log.d(TAG, "List Size: " + list.size());
                if (list.size() > 0) {
                    Log.d(TAG, "List[0]: " + list.get(0));
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, list);
                    input2.setAdapter(adapter2);
                    input2.setThreshold(0);
                }

            }
        });
        final EditText input3 = (EditText) promptsView.findViewById(R.id.comment);

        input1.setSingleLine();
        if (view.getResources().getString(R.string.pressHereToEdit).equals(mItemArray.get(pos).getName() + "")) {
            input1.setText("");
        } else {
            input1.setText(mItemArray.get(pos).getName());
        }
        input2.setSingleLine();
        input2.setText(mItemArray.get(pos).getTempo());
        input3.setSingleLine();
        String txt =mItemArray.get(pos).getComment();
        txt=txt.replace("\n","<br>");
        input3.setText(txt);

        alert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                input1.setSingleLine();
                if (input1.getText().toString().equals("")) {
                    mItemArray.get(pos).setName(view.getResources().getString(R.string.pressHereToEdit));
                } else {
                    mItemArray.get(pos).setName(input1.getText().toString());
                }
                mItemArray.get(pos).setTempo(input2.getText().toString());
                mItemArray.get(pos).setComment(input3.getText().toString());
                listAdapter.notifyDataSetChanged();

                //sav
                try {
                    String filePath =mExternalFilesDir+"/"+dance_file + "onscreen";
                    saveDanceFromPath(filePath);
                    updateFragmentList();
                    loadDanceFromPath(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.setNeutralButton(R.string.remove, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mItemArray.remove(pos);
                if (mItemArray.size()==pos) listAdapter.setLastPosition(pos-1);
                listAdapter.notifyDataSetChanged();
                //sav
                try {
                    String filePath =mExternalFilesDir+"/"+dance_file + "onscreen";
                    saveDanceFromPath(filePath);
                    updateFragmentList();
                    loadDanceFromPath(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        alert.show();
    }

    public static String isMimeTypeValid(Uri contentUri){
        String mimeType = getMimeType(contentUri);
        if (mimeType == null) return null;
        if (!mimeType.startsWith("video") && !mimeType.startsWith("audio")
                && !mimeType.startsWith("image") && !mimeType.startsWith("application/pdf")) return null;
        if ((new File(contentUri.getPath())).exists() && (new File(contentUri.getPath())).canRead()) return mimeType ;
        return null;
    }

    public static boolean isMimeTypeSupported(Uri contentUri){
        String mimeType = getMimeType(contentUri);
        if (mimeType == null) return false;
        if (!mimeType.startsWith("video") && !mimeType.startsWith("audio")
                && !mimeType.startsWith("image") && !mimeType.startsWith("application/pdf")) return false;
        return true ;
    }


    private static void openEditVideoItemDialog(final Context context, final int pos) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.edit_video_dialog, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setView(promptsView);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setTitle(Add_title);

        final EditText input1 = (EditText) promptsView.findViewById(R.id.name);
        input1.setSingleLine();
        String txtComment = mItemArray.get(pos).getComment();
        String txtName = mItemArray.get(pos).getName();
        if (txtComment.equals(mLoadingFileString)) {
            input1.setText("");
        }else {
            if (txtComment.equals("") && !txtName.equals("")){
                input1.setText(txtName);
            }else{
                input1.setText(txtComment);
            }
        }

        alert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                input1.setSingleLine();
                {
                    mItemArray.get(pos).setName(input1.getText().toString());
                    mItemArray.get(pos).setComment(input1.getText().toString());
                }
                listAdapter.notifyDataSetChanged();

                try {
                    String filePath =mExternalFilesDir+"/"+dance_file + "onscreen";
                    saveDanceFromPath(filePath);
                    updateFragmentList();
                    loadDanceFromPath(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private static void openMediaContentItemDialog(final View view, final int pos, final Uri videoURI) {
        final Context context = view.getContext();
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setIcon(R.mipmap.ic_launcher);

        final String mimeType = isMimeTypeValid(videoURI);

        String type="*/*";
        int buttonID=R.string.open;
        alert.setTitle(R.string.MultimediaContent);

        if (mimeType!=null) {
            if (mimeType.startsWith("video")){
                type="video/*";
                buttonID=R.string.playVideo;
                alert.setTitle(R.string.video_file);
            }
            if (mimeType.startsWith("audio")){
                type="audio/*";
                buttonID= R.string.playAudio;
                alert.setTitle(R.string.audio_file);
            }
            if (mimeType.startsWith("image")) {
                type="image/*";
                buttonID= R.string.open;
                alert.setTitle(R.string.image_file);
            }
            if (mimeType.startsWith("application/pdf")) {
                type="application/pdf";
                buttonID= R.string.open;
                alert.setTitle(R.string.pdf_file);
            }

            final String finalType = type;
            alert.setPositiveButton(buttonID, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri contentUri = getUriForFile(context, "com.olklein.choreo.fileProvider", new File(videoURI.getPath()));
                            intent.setDataAndType(contentUri, finalType);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            context.startActivity(Intent.createChooser(intent, mResources.getString(R.string.action_open_file)));
                        } else {
                            intent.setDataAndType(Uri.fromFile(new File(videoURI.getPath())), finalType);
                            context.startActivity(Intent.createChooser(intent, mResources.getString(R.string.action_open_file)));
                        }
                    } catch (ActivityNotFoundException anfe) {
                        Toast.makeText(context, mResources.getString(R.string.action_no_apps), Toast.LENGTH_LONG).show();
                    }
                }
            });

            alert.setNegativeButton(R.string.details, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Backup
                    File contentFile = new File(videoURI.getPath());
                    File home = new File(mExternalFilesDir, "");
                    showUsage(context,home,contentFile);
                }
            });

        } else {
            alert.setTitle(R.string.action_lostfile_delete);
            alert.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
        }

        alert.setNeutralButton(R.string.export, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Backup
                File contentFile = new File(videoURI.getPath());
                String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/Choreo";
                File outDir=new File(downloadPath);
                if(!outDir.exists()){
                    outDir.mkdirs();
                }
                String fileName = Util.getNewName(contentFile.getName(),new File(downloadPath));
                String filePath = downloadPath+"/"+fileName;
                File dest = new File(filePath);

                BkgMediaContentBackup backup = new BkgMediaContentBackup();
                NotificationManager mNotifyManager;
                Notification.Builder mBuilder;
                mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


                NotificationUtils notification = new NotificationUtils(context);
                notification.createChannels();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mBuilder = new Notification.Builder(context,NotificationUtils.CHOREO_CHANNEL_ID);
                }else{
                    mBuilder = new Notification.Builder(context);
                }
                Resources mResources = context.getResources();
                int color = mResources.getColor(android.R.color.holo_blue_light);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBuilder.setContentTitle(mResources.getString(R.string.app_name))
                            .setContentText(mResources.getString(R.string.backupongoing,contentFile.getName()))
                            .setSmallIcon(android.R.drawable.stat_sys_download)
                            .setAutoCancel(false)
                            .setColor(color);
                }else{
                    mBuilder.setContentTitle(mResources.getString(R.string.app_name))
                            .setContentText(mResources.getString(R.string.backupongoing,contentFile.getName()))
                            .setSmallIcon(android.R.drawable.stat_sys_download)
                            .setAutoCancel(false);
                }
                if (mNotifyManager!=null) mNotifyManager.notify(1334, mBuilder.build());

                backup.createCopy(context, Uri.fromFile(new File(videoURI.getPath())),dest);
            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();
    }

    public static void openItemVideoDialog(final View view, final int pos, final Uri videoURI) {
        if (isMediaFolder) {
            openMediaContentItemDialog(view, pos, videoURI);
            return;
        }
        final Context context = view.getContext();
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setIcon(R.mipmap.ic_launcher);

        final String mimeType = isMimeTypeValid(videoURI);
        if (mimeType!=null) {
            alert.setNegativeButton(R.string.Editer, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Edit
                    openEditVideoItemDialog(context,pos);
                }
            });

            String type="*/*";
            alert.setTitle(R.string.MultimediaContent);
            int buttonID=R.string.open;

            if (mimeType.startsWith("video")){
                type="video/*";
                alert.setTitle(R.string.video_file);
                buttonID=R.string.playVideo;
            }
            if (mimeType.startsWith("audio")){
                type="audio/*";
                alert.setTitle(R.string.audio_file);
                buttonID=R.string.playAudio;
            }
            if (mimeType.startsWith("image")) {
                type="image/*";
                alert.setTitle(R.string.image_file);
            }
            if (mimeType.startsWith("application/pdf")) {
                type="application/pdf";
                alert.setTitle(R.string.pdf_file);
            }

            final String finalType = type;
            alert.setPositiveButton(buttonID, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri contentUri = getUriForFile(context, "com.olklein.choreo.fileProvider", new File(videoURI.getPath()));
                            intent.setDataAndType(contentUri, finalType);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            context.startActivity(Intent.createChooser(intent, mResources.getString(R.string.action_open_file)));
                        } else {
                            intent.setDataAndType(Uri.fromFile(new File(videoURI.getPath())), finalType);
                            context.startActivity(Intent.createChooser(intent, mResources.getString(R.string.action_open_file)));
                        }
                    } catch (ActivityNotFoundException anfe) {
                        Toast.makeText(context, mResources.getString(R.string.action_no_apps), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            alert.setTitle(R.string.action_lostfile_delete);
            alert.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
        }

        alert.setNeutralButton(R.string.remove, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mItemArray.remove(pos);
                if (mItemArray.size() == pos) listAdapter.setLastPosition(pos - 1);
                listAdapter.notifyDataSetChanged();

                try {
                    String filePath = mExternalFilesDir + "/" + dance_file + "onscreen";
                    saveDanceFromPath(filePath);
                    updateFragmentList();
                    loadDanceFromPath(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();
    }


    private static String getMimeType(Uri uri) {
        String mimeType = null;
        String scheme = uri.getScheme();
        if (scheme!=null && scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            mimeType = mContentResolver.getType(uri);
        } else {
            String name;
            name=uri.toString();
            name=name.replaceAll(" ","");
            name=name.replaceAll("'","");
            name=name.replaceAll("&","");
            name=cleanFileName(name);

            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(name).toLowerCase();
            if (fileExtension.equals( "flv" ))  return ("video/x-flv");
            if (fileExtension.equals( "mov" ))  return ("video/quicktime") ;
            if (fileExtension.equals( "wm"  ))  return ("video/x-ms-wm");
            if (fileExtension.equals( "wmv" ))  return ("video/x-ms-wmv");
            if (fileExtension.equals( "wmx" ))  return ("video/x-ms-wmx");
            if (fileExtension.equals( "wvx" ))  return ("video/x-ms-wvx");
            if (fileExtension.equals( "avi" ))  return ("video/x-msvideo");
            if (fileExtension.equals("movie")) return ("video/x-sgi-movie");

            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);

        }
        return mimeType;
    }

    public static String getTitle(Uri fileUri){

        MediaMetadataRetriever mdr = new MediaMetadataRetriever();
        String path = fileUri.getPath();
        try {
            mdr.setDataSource(path);
            String title = mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (title!=null){
                mdr.release();
                return title;
            } else{
                mdr.release();
                return "";
            }
        }catch(Exception e ){
            mdr.release();
            return "";
        }
    }


    public static Bitmap getThumbnail(Uri fileUri, String mimeType){
        if (mimeType!=null && mimeType.startsWith("image")){
            return BitmapFactory.decodeFile(fileUri.getPath());
        }
        if (mimeType!=null && (mimeType.startsWith("application/pdf") || mimeType.startsWith("text"))){
            return CreatePageImageExtract(fileUri);
        }

        MediaMetadataRetriever mdr = new MediaMetadataRetriever();
        String path = fileUri.getPath();
        try {

            mdr.setDataSource(path);
            String length = mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String hasVideo = mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
            if (hasVideo!=null){
                int duration=0;
                try {
                    duration = Integer.parseInt(length)/2;
                }catch(Exception e){
                    duration = 1;
                }
                return mdr.getFrameAtTime(duration*1000);
            } else{
                byte[] albumArt = mdr.getEmbeddedPicture();

                if (albumArt != null) {
                    return BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                }
                return null;
            }
        }catch(Exception e ){
            e.printStackTrace();
            mdr.release();
            return null;
        }
    }


    private static Bitmap CreatePageImageExtract( Uri uri) {
        File fileIn = new File(uri.getPath());
        PdfRenderer.Page mCurrentPage;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ParcelFileDescriptor mFileDescriptor = null;
            PdfRenderer mPdfRenderer = null;
            try {
                mFileDescriptor = ParcelFileDescriptor.open(fileIn, ParcelFileDescriptor.MODE_READ_ONLY);
                mPdfRenderer = new PdfRenderer(mFileDescriptor);


                if (mPdfRenderer == null) {
                    return null;
                }
                mCurrentPage = mPdfRenderer.openPage(0);

                float h;
                h = (float) (1024*(float)mCurrentPage.getHeight()/(float)mCurrentPage.getWidth());
                Bitmap bitmap = createBitmap((int) 1024, (int) h, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(0xffFFFFFF);
                mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private String mCurrentMediaFilePath;
    private String mCurrentMediaFileName;

    private File createMediaFile(String ext) {
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
        String imageFileName = ext.toUpperCase(Locale.FRANCE) + "_" + timeStamp;
        File storageDir = getActivity().getBaseContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File image = new File(storageDir+"/"+imageFileName+"."+ext);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentMediaFilePath = image.getAbsolutePath();
        mCurrentMediaFileName = image.getName();

        return image;
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentMediaFilePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().getBaseContext().getApplicationContext().sendBroadcast(mediaScanIntent);
    }

    private void passportDialogOnClickListener(int id, final Context context, final int discipline) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        final int danceID= id;
        alert.setIcon(R.mipmap.ic_launcher);

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        int items = R.array.PassportColor;
        int itemColors = R.array.PassportItemColors;
        int itemBoxes =  R.array.PassportItemBoxes;

        if (discipline==0){
            alert.setTitle(R.string.action_show_passport_level);
            if (danceID ==2) {
                items = R.array.PassportColorFromPurple;
                itemColors = R.array.PassportItemColorsFromPurple;
                itemBoxes =  R.array.PassportItemBoxesFromPurple;
            }
            if (danceID ==3){
                items = R.array.PassportColorFromGreen;
                itemColors = R.array.PassportItemColorsFromGreen;
                itemBoxes =  R.array.PassportItemBoxesFromGreen;
            }
            if (danceID ==4){
                items = R.array.PassportColorFromOrange;
                itemColors = R.array.PassportItemColorsFromOrange;
                itemBoxes =  R.array.PassportItemBoxesFromOrange;
            }
        }
        if (discipline==1){
            alert.setTitle(R.string.action_show_passport_level);
            if (danceID ==0){
                items = R.array.PassportColorFromGreen;
                itemColors = R.array.PassportItemColorsFromGreen;
                itemBoxes =  R.array.PassportItemBoxesFromGreen;
            }
            if (danceID ==2){
                items = R.array.PassportColorFromOrange;                                                    itemColors = R.array.PassportItemColorsFromPurple;
                itemColors = R.array.PassportItemColorsFromOrange;
                itemBoxes =  R.array.PassportItemBoxesFromOrange;
            }
            if (danceID ==3){
                items = R.array.PassportColorFromPurple;
                itemColors = R.array.PassportItemColorsFromPurple;
                itemBoxes =  R.array.PassportItemBoxesFromPurple;
            }
        }
        final int colors = itemColors;
        final int boxes = itemBoxes;
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (context, R.layout.passportcoloritem ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    text1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
                text1.setBackgroundResource(R.drawable.borderfilled);
                String colorStr= context.getResources().getStringArray(colors)[position];

                text1.setTextColor(Color.parseColor(colorStr));
                {
                    int resID = context.getResources().getIntArray(boxes)[position];

                    switch (resID){
                        case 1:
                            text1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.yellowbox,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                            break;
                        case 2:
                            text1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.orangebox,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);

                            break;
                        case 3:
                            text1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.greenbox,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                            break;
                        case 4:
                            text1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.purplebox,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                            break;
                        case 5:
                            text1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bluebox,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                            break;
                        case 6:
                            text1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.redbox,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                            break;
                    }
                }
                return view;
            }
        };

        for (String item :context.getResources().getStringArray(items))
        {
            arrayAdapter.add(item);
        }
        alert.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        show_passport(context, discipline,danceID, which);
                    }
                }
        );

        final AlertDialog dial = alert.create();
        dial.show();

    }

    private void show_mediaContent(final Context context) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(context.getResources().getString(R.string.MultimediaContent));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            DrawerLayout mDrawerLayout = (DrawerLayout)  getActivity().findViewById(R.id.drawer_layout);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        isSyllabus=false;
        isMediaFolder=true;
        getActivity().invalidateOptionsMenu();

        updateFragmentList();

        File mHomeMovies =  context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        ArrayList<File> movieFileList = getFileList(mHomeMovies);
        int contentNumber=0;

        for (File movie : movieFileList) {
            Uri contentUri = Uri.parse(movie.getAbsolutePath());
            String[] content = {movie.getName(), "VideoURI-" + contentUri.toString(), movie.getName()};

            mItemArray.add(listAdapter.getLastPosition() + 1, new DanceFigure(contentNumber++, content[0].trim(), content[1].trim(), content[2].trim()));
            listAdapter.setLastPosition(listAdapter.getLastPosition() + 1);
            listAdapter.notifyDataSetChanged();

        }
    }

    public static void refresh_mediaContent(File mHomeMovies) {
        updateFragmentList();

        ArrayList<File> movieFileList = getFileList(mHomeMovies);
        int contentNumber=0;

        for (File movie : movieFileList) {
            Uri contentUri = Uri.parse(movie.getAbsolutePath());
            String[] content = {"", "VideoURI-" + contentUri.toString(), movie.getName()};

            mItemArray.add(listAdapter.getLastPosition() + 1, new DanceFigure(contentNumber++, content[0].trim(), content[1].trim(), content[2].trim()));
            listAdapter.setLastPosition(listAdapter.getLastPosition() + 1);
            listAdapter.notifyDataSetChanged();

        }
    }

    static private ArrayList<File> getFileList(File home){
        ArrayList<File> fileList= new ArrayList<>();

        if (null != home){
            if (home.exists() && home.isDirectory()){
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
    private void doRefreshMenus(Context context, DrawerLayout drawer, String fileName, boolean mediaFolder) {
        DanceCustomAdapter danceListAdapter =
                new DanceCustomAdapter(context,
                        R.layout.dance_custom_list,
                        ChoreographerConstants.DANCE_LIST_FILENAME,
                        drawer);
        ListView drawerList = (ListView) getActivity().findViewById(R.id.left_drawer);
        drawerList.setAdapter(danceListAdapter);
        int index = ChoreographerConstants.getIndex(fileName);
        if (index != -1) {
            danceListAdapter.setClicked(index);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(fileName);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
                DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
            isSyllabus = false;
            isMediaFolder = mediaFolder;
            MainActivity.setCurrentItem(index);
        }

        getActivity().invalidateOptionsMenu();
    }


    private boolean isMediaFolderEmpty(Context context) {
        File mHomeMovies = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        ArrayList<File> movieFileList = getFileList(mHomeMovies);
        return movieFileList.isEmpty();
    }

    private static void clean_dialogOpen(final Context context) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setTitle(R.string.cleaning);
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.setPositiveButton(R.string.removeButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                BkgCheckUnused cleaner = new BkgCheckUnused();
                cleaner.setup(context);
            }
        });

        final AlertDialog subDialog = alert.create();
        subDialog.show();
    }

    private static void showUsage(final Context context, File home, final File movie) {

        ArrayList<File> fileList = getFileList(home);

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.contentusage_dialog, null);

        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setCancelable(true);
        final ListView list = (ListView) promptsView.findViewById(R.id.list);
        final TextView message1 = (TextView) promptsView.findViewById(R.id.message);
        final TextView message2 = (TextView) promptsView.findViewById(R.id.message2);

        alert.setIcon(R.mipmap.ic_launcher);


        final ArrayAdapter<String> ChoreoListArrayAdapter = new ArrayAdapter<String> (context, android.R.layout.simple_list_item_1 ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    text1.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                }
                text1.setBackgroundResource(R.drawable.whiteborderfilled);
                return view;
            }
        };

        final String movieFileName = movie.getAbsolutePath();
        for (File file : fileList) {
            if(isFileContains(file,movieFileName))
            {
                String filename = file.getName();
                if (filename.endsWith("onscreen")){
                    if (filename.contains("onscreen")) {
                        filename = filename.replace("onscreen", "").concat("*");
                    }
                }
                ChoreoListArrayAdapter.add(filename);
            }
        }
        alert.setTitle(R.string.details);
        alert.setView(promptsView);
        message1.setText(mResources.getString(R.string.UsedSize, humanReadableByteCount(movie.length())));

        if (ChoreoListArrayAdapter.isEmpty()){
            alert.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    movie.delete();
                    final Notification.Builder mBuilder;
                    final Resources mResources ;
                    final NotificationManager mNotifyManager;
                    final File mHome;
                    final File mHomeMovies;

                    NotificationUtils notification = new NotificationUtils(context);
                    notification.createChannels();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mBuilder = new Notification.Builder(context,NotificationUtils.CHOREO_CHANNEL_ID);
                    }else{
                        mBuilder = new Notification.Builder(context);
                    }
                    mResources = context.getResources();
                    mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mHome = context.getExternalFilesDir(null);
                    mHomeMovies =  context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);

                    int color = mResources.getColor(android.R.color.holo_blue_light);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBuilder.setContentTitle(mResources.getString(R.string.app_name))
                                .setContentText(mResources.getString(R.string.contentDelete,movie.getName()))
                                .setSmallIcon(android.R.drawable.ic_dialog_info)
                                .setAutoCancel(true)
                                .setColor(color);
                    }else{
                        mBuilder.setContentTitle(mResources.getString(R.string.app_name))
                                .setContentText(mResources.getString(R.string.contentDelete,movie.getName()))
                                .setSmallIcon(android.R.drawable.ic_dialog_info)
                                .setAutoCancel(true);
                    }

                    if(mNotifyManager!=null) mNotifyManager.notify(1237, mBuilder.build());
                    if (ListFragment.isMediaFolder()){
                        ListFragment.refresh_mediaContent(mHomeMovies);
                    }
                }
            });
            message2.setText(mResources.getString(R.string.UseCleantosave, humanReadableByteCount(movie.length())));
            list.setVisibility(View.GONE);

        }else {
            alert.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            message2.setText(R.string.UsedIn);
            list.setAdapter(ChoreoListArrayAdapter);
            list.setVisibility(View.VISIBLE);
        }
        final AlertDialog dialog = alert.create();
        dialog.show();

    }
    private static boolean isFileContains(File file, String movieFileName)  {
        InputStream inputStream;
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

    private static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1) + "" ;
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private static String getRythm(String filePath) throws IOException {
        InputStream inputStream = new FileInputStream(filePath);
        final Scanner reader = new Scanner(inputStream, "ISO-8859-1");
        String rythm="";
        try {
            String line;

            while (reader.hasNextLine()) {
                line = reader.nextLine();
                String[] strings = TextUtils.split(line, ";");
                if (strings.length > 2) {
                    if (!strings[2].contains("VideoURI-")) {
                        rythm=rythm.concat(strings[2].replaceAll("<br>", ""));
                    }
                }
            }
        } finally {
            reader.close();
        }
        return rythm;
    }
    private static void displayChoreoLength(Context context, String file) throws IOException {
        String filePath=mExternalFilesDir+"/"+file;
        File testFile= new File(filePath+"onscreen");
        if (testFile.exists()){
            filePath=filePath+"onscreen";
        }

        int[] info = Util.ChoreoLength(getRythm(filePath));
        Toast.makeText(context, "S: "+ info[0]+"Q: "+ info[1]+"beats: "+ info[2]+"total beats: "+ info[3], Toast.LENGTH_LONG).show();
    }

    private static final String AUTHORITY = "com.olklein.choreo.contentProvider.mediaContent";
    void activeContentProvider(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getActivity().getContentResolver().notifyChange(DocumentsContract.buildRootsUri
                    (AUTHORITY), null, false);
        }
        // END_INCLUDE(notify_change)
    }






}