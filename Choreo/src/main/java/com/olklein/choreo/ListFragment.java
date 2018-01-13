package com.olklein.choreo;

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

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.FileProvider.getUriForFile;
import static com.olklein.choreo.R.string.Figure_Editor;
import static com.olklein.choreo.Syllabus.setDance;

public class ListFragment extends Fragment {

    private static ArrayList<DanceFigure> mItemArray;
    private static DragListView mDragListView;

    final static private String TAG ="DANCE";


    private static final int IMPORT_REQUEST   = 202;

    private static DanceItemAdapter listAdapter;
    private static int sCreatedItems = 0;

    private static String dance_file;
    private boolean isSyllabus=false;


    public static ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        dance_file = bundle.getString(ChoreographerConstants.FILE);

        sCreatedItems = 0;
        setHasOptionsMenu(true);
        Log.d(TAG,"In create");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.list_layout, container, false);
        Bundle bundle = getArguments();

        dance_file = bundle.getString(ChoreographerConstants.FILE);

        if (dance_file != null && dance_file.equals("")){
            onNewFileClick( getContext());
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
                if(!isSyllabus) {
                    try {
                        saveDance(dance_file + "onscreen");
                        mItemArray.clear();
                        if (listAdapter != null) listAdapter.notifyDataSetChanged();
                        loadDance(dance_file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //
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
        if (MainActivity.isDrawerOpen()) {
            menu.findItem(R.id.action_syllabus).setVisible(false);
            menu.findItem(R.id.action_show_comment).setVisible(false);
            menu.findItem(R.id.action_hide_comment).setVisible(false);
            menu.findItem(R.id.action_add_figure).setVisible(false);
            menu.findItem(R.id.action_restore).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(false);
            menu.findItem(R.id.action_saveaspdf).setVisible(false);
            menu.findItem(R.id.action_export).setVisible(false);
            menu.findItem(R.id.action_import).setVisible(false);
            menu.findItem(R.id.action_show_syllabus).setVisible(false);
            menu.findItem(R.id.action_view).setVisible(false);
            menu.findItem(R.id.action_new).setVisible(false);
        }else{
            if(!isSyllabus) {
                menu.findItem(R.id.action_syllabus).setVisible(true);
                menu.findItem(R.id.action_import).setVisible(true);
                if (Syllabus.getDanceId()== R.id.allDances){
                    menu.findItem(R.id.action_show_syllabus).setVisible(false);
                }else{
                    menu.findItem(R.id.action_show_syllabus).setVisible(true);
                }
                if (dance_file.equals("")) {
                    menu.findItem(R.id.action_add_figure).setVisible(false);
                    menu.findItem(R.id.action_show_comment).setVisible(false);
                    menu.findItem(R.id.action_hide_comment).setVisible(false);
                    menu.findItem(R.id.action_restore).setVisible(false);
                    menu.findItem(R.id.action_view).setVisible(false);
                    menu.findItem(R.id.action_save).setVisible(false);
                    menu.findItem(R.id.action_saveaspdf).setVisible(false);
                    menu.findItem(R.id.action_export).setVisible(false);
                    menu.findItem(R.id.action_new).setVisible(true);
                }else{
                    menu.findItem(R.id.action_add_figure).setVisible(true);
                    menu.findItem(R.id.action_show_comment).setVisible(!listAdapter.isCommentEnabled());
                    menu.findItem(R.id.action_hide_comment).setVisible(listAdapter.isCommentEnabled());
                    menu.findItem(R.id.action_restore).setVisible(true);
                    menu.findItem(R.id.action_view).setVisible(true);
                    menu.findItem(R.id.action_save).setVisible(true);
                    menu.findItem(R.id.action_saveaspdf).setVisible(true);
                    menu.findItem(R.id.action_export).setVisible(true);
                    menu.findItem(R.id.action_new).setVisible(false);
                }
            }else{
                menu.findItem(R.id.action_syllabus).setVisible(true);
                menu.findItem(R.id.action_show_comment).setVisible(!listAdapter.isCommentEnabled());
                menu.findItem(R.id.action_hide_comment).setVisible(listAdapter.isCommentEnabled());
                menu.findItem(R.id.action_add_figure).setVisible(false);
                menu.findItem(R.id.action_restore).setVisible(false);
                menu.findItem(R.id.action_save).setVisible(true);
                menu.findItem(R.id.action_saveaspdf).setVisible(true);
                menu.findItem(R.id.action_export).setVisible(false);
                menu.findItem(R.id.action_import).setVisible(false);
                menu.findItem(R.id.action_new).setVisible(false);
                if (Syllabus.getDanceId()== R.id.allDances){
                    menu.findItem(R.id.action_show_syllabus).setVisible(false);
                }else{
                    menu.findItem(R.id.action_show_syllabus).setVisible(true);
                }
                menu.findItem(R.id.action_view).setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
            {
                onNewFileClick(getContext());
            }
            return true;
            case R.id.action_view:
            {
                Resources resource = getContext().getResources();
                Intent exportIntent = new Intent(Intent.ACTION_VIEW);

                Uri fileURI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    File path = new File(getContext().getExternalFilesDir(null), "");
                    File newFile = new File(path, dance_file+"onscreen");
                    if (!newFile.exists())  newFile = new File(path, dance_file);
                    fileURI= getUriForFile(getContext(), "com.olklein.choreo.fileProvider", newFile);
                    exportIntent.setDataAndType(fileURI,"text/plain");
                    exportIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
                    startActivity(Intent.createChooser(exportIntent, resource.getString(R.string.action_view)));

                }else {
                    String filePath = getActivity().getBaseContext().getExternalFilesDir(null)+"/"+dance_file+"onscreen";
                    File newFile = new File(filePath);
                    if (!newFile.exists())  filePath = getActivity().getBaseContext().getExternalFilesDir(null)+"/"+dance_file;
                    fileURI = Uri.fromFile(new File(filePath));
                    exportIntent.setDataAndType(fileURI,"text/plain");
                    exportIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
                    startActivity(Intent.createChooser(exportIntent, resource.getString(R.string.action_view)));

                }

                return true;
            }

            case R.id.action_export:
            {
                Resources resource = getContext().getResources();
                Intent exportIntent = new Intent(Intent.ACTION_SEND);

                Uri fileURI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    File path = new File(getContext().getExternalFilesDir(null), "");
                    File newFile = new File(path, dance_file+"onscreen");
                    if (!newFile.exists())  newFile = new File(path, dance_file);
                    fileURI= getUriForFile(getContext(), "com.olklein.choreo.fileProvider", newFile);
                }else {
                    String filePath = getActivity().getBaseContext().getExternalFilesDir(null)+"/"+dance_file+"onscreen";
                    File newFile = new File(filePath);
                    if (!newFile.exists())  filePath = getActivity().getBaseContext().getExternalFilesDir(null)+"/"+dance_file;
                    fileURI = Uri.fromFile(new File(filePath));
                }

                exportIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
                exportIntent.putExtra(Intent.EXTRA_STREAM, fileURI);
                exportIntent.setType("*/*");

                exportIntent.putExtra(Intent.EXTRA_SUBJECT, resource.getString(R.string.mail_subjet, dance_file));
                exportIntent.putExtra(Intent.EXTRA_TEXT, resource.getString(R.string.mail_text));
                exportIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(exportIntent, resource.getString(R.string.action_export)));
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
                                deleteTemporaryFile(context, dance_file);
                                mItemArray.clear();
                                if (listAdapter != null) listAdapter.notifyDataSetChanged();
                                loadDance();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ChoreographerConstants.init(context);
                            DanceCustomAdapter danceListAdapter = new DanceCustomAdapter(context, R.layout.dance_custom_list, ChoreographerConstants.DANCE_LIST_FILENAME);
                            ListView drawerList = (ListView) MainActivity.context.findViewById(R.id.left_drawer);
                            drawerList.setAdapter(danceListAdapter);
                            int index = ChoreographerConstants.getIndex(dance_file);
                            if (index != -1) {
                                danceListAdapter.setClicked(index);
                                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                                if (actionBar != null) {
                                    actionBar.setTitle(dance_file);
                                    actionBar.setDisplayHomeAsUpEnabled(true);
                                    actionBar.setHomeButtonEnabled(true);
                                    DrawerLayout mDrawerLayout = (DrawerLayout)  getActivity().findViewById(R.id.drawer_layout);
                                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                }
                                isSyllabus=false;
                            }
                            getActivity().supportInvalidateOptionsMenu();
                        }else {
                            File testFile = new File(getActivity().getBaseContext().getExternalFilesDir(null) + "/" + name);
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
                                                String filePath = getContext().getExternalFilesDir(null) + "/" + dance_file;
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
                                                deleteTemporaryFile(context, dance_file);
                                                mItemArray.clear();
                                                if (listAdapter != null)
                                                    listAdapter.notifyDataSetChanged();
                                                loadDance();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            ChoreographerConstants.init(context);
                                            DanceCustomAdapter danceListAdapter = new DanceCustomAdapter(context, R.layout.dance_custom_list, ChoreographerConstants.DANCE_LIST_FILENAME);
                                            ListView drawerList = (ListView) MainActivity.context.findViewById(R.id.left_drawer);
                                            drawerList.setAdapter(danceListAdapter);
                                            int index = ChoreographerConstants.getIndex(dance_file);
                                            if (index != -1) {
                                                danceListAdapter.setClicked(index);
                                                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                                                if (actionBar != null) {
                                                    actionBar.setTitle(dance_file);
                                                    actionBar.setDisplayHomeAsUpEnabled(true);
                                                    actionBar.setHomeButtonEnabled(true);
                                                    DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                                                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                                }
                                                isSyllabus = false;
                                            }
                                            getActivity().supportInvalidateOptionsMenu();
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
                                        deleteTemporaryFile(context, dance_file);
                                        mItemArray.clear();
                                        if (listAdapter != null) listAdapter.notifyDataSetChanged();
                                        loadDance();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    ChoreographerConstants.init(context);
                                    DanceCustomAdapter danceListAdapter = new DanceCustomAdapter(context, R.layout.dance_custom_list, ChoreographerConstants.DANCE_LIST_FILENAME);
                                    ListView drawerList = (ListView) MainActivity.context.findViewById(R.id.left_drawer);
                                    drawerList.setAdapter(danceListAdapter);
                                    int index = ChoreographerConstants.getIndex(dance_file);
                                    if (index != -1) {
                                        danceListAdapter.setClicked(index);
                                        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                                        if (actionBar != null) {
                                            actionBar.setTitle(dance_file);
                                            actionBar.setDisplayHomeAsUpEnabled(true);
                                            actionBar.setHomeButtonEnabled(true);
                                            DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                        }
                                        isSyllabus = false;
                                    }

                                    getActivity().supportInvalidateOptionsMenu();
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
                                                    String filePath = getContext().getExternalFilesDir(null) + "/" + dance_file;
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
                                                    deleteTemporaryFile(context, dance_file);
                                                    mItemArray.clear();
                                                    if (listAdapter != null)
                                                        listAdapter.notifyDataSetChanged();
                                                    loadDance();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                ChoreographerConstants.init(context);
                                                DanceCustomAdapter danceListAdapter = new DanceCustomAdapter(context, R.layout.dance_custom_list, ChoreographerConstants.DANCE_LIST_FILENAME);
                                                ListView drawerList = (ListView) MainActivity.context.findViewById(R.id.left_drawer);
                                                drawerList.setAdapter(danceListAdapter);
                                                int index = ChoreographerConstants.getIndex(dance_file);
                                                if (index != -1) {
                                                    danceListAdapter.setClicked(index);
                                                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                                                    if (actionBar != null) {
                                                        actionBar.setTitle(dance_file);
                                                        actionBar.setDisplayHomeAsUpEnabled(true);
                                                        actionBar.setHomeButtonEnabled(true);
                                                        DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                                                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                                    }
                                                    isSyllabus = false;
                                                }
                                                getActivity().supportInvalidateOptionsMenu();
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
                            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setVisibility(View.GONE);
                        }else{
                            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
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
                final Context context = getContext();
                try {
                    saveAsPDFDance(dance_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
                        mItemArray.clear();
                        if (listAdapter != null) listAdapter.notifyDataSetChanged();
                        loadDance(dance_file+"onscreen");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
            case R.id.action_restore: {
                //
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
                            loadDance();
                            if (listAdapter!= null) {
                                listAdapter.notifyDataSetChanged();
                                listAdapter.setLastPosition(-1);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            mItemArray.clear();
                            if (listAdapter!= null) {
                                listAdapter.notifyDataSetChanged();
                                listAdapter.setLastPosition(-1);
                            }
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
                getActivity().supportInvalidateOptionsMenu();
                return true;
            case R.id.action_hide_comment:
                listAdapter.setCommentEnabled(false);
                listAdapter.notifyDataSetChanged();
                getActivity().supportInvalidateOptionsMenu();
                return true;
            case R.id.action_import: {
                Intent si = new Intent(Intent.ACTION_GET_CONTENT);
                si.setType("*/*");
                startActivityForResult(si, IMPORT_REQUEST);
                return true;
            }
            case R.id.action_show_syllabus: {
                final Context context = getContext();
                dance_file= Syllabus.getName();
                isSyllabus=true;
                getActivity().supportInvalidateOptionsMenu();

                DanceCustomAdapter danceListAdapter = new DanceCustomAdapter(context, R.layout.dance_custom_list, ChoreographerConstants.DANCE_LIST_FILENAME);
                ListView drawerList = (ListView) MainActivity.context.findViewById(R.id.left_drawer);
                drawerList.setAdapter(danceListAdapter);
                danceListAdapter.setClicked(-1);
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(R.string.syllabus);
                }
                if (mItemArray!=null) mItemArray.clear();
                sCreatedItems = 0;
                if (listAdapter != null) {
                    listAdapter.setLastPosition(-1);
                    listAdapter.notifyDataSetChanged();
                }
                Comparator<String[]> comparator = new Comparator<String[]>() {
                    @Override
                    public int compare(String s1[], String s2[]) {
                        return s1[0].compareTo(s2[0]);
                    }
                };
                Collections.sort(Syllabus.figuresWithTempo,comparator);
                for (String[] figure : Syllabus.figuresWithTempo){
                    //for (String[] figure : Syllabus.figuresWithTempo;){
                    addFigure(figure);
                }
                return true;
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
                }
                setDance(context,item.getItemId());
                getActivity().supportInvalidateOptionsMenu();
                if (isSyllabus) {
                    dance_file= Syllabus.getName();
                    isSyllabus=true;
                    getActivity().supportInvalidateOptionsMenu();

                    DanceCustomAdapter danceListAdapter = new DanceCustomAdapter(context, R.layout.dance_custom_list, ChoreographerConstants.DANCE_LIST_FILENAME);
                    ListView drawerList = (ListView) MainActivity.context.findViewById(R.id.left_drawer);
                    drawerList.setAdapter(danceListAdapter);
                    danceListAdapter.setClicked(-1);
                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setTitle(R.string.syllabus);
                    }
                    if (mItemArray!=null) mItemArray.clear();
                    sCreatedItems = 0;
                    if (listAdapter != null) {
                        listAdapter.setLastPosition(-1);
                        listAdapter.notifyDataSetChanged();
                    }
                    Comparator<String[]> comparator = new Comparator<String[]>() {
                        @Override
                        public int compare(String s1[], String s2[]) {
                            return s1[0].compareTo(s2[0]);
                        }
                    };
                    Collections.sort(Syllabus.figuresWithTempo,comparator);
                    for (String[] figure : Syllabus.figuresWithTempo){
                        //for (String[] figure : Syllabus.figuresWithTempo;){
                        addFigure(figure);
                    }
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void deleteTemporaryFile(Context context, String name) {
        String filePathFrom = context.getExternalFilesDir(null) + "/" + name+"onscreen";
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
        listAdapter = new DanceItemAdapter(mItemArray, R.layout.list_figure_item, R.id.image, false);
        listAdapter.setCommentEnabled(true);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setDragEnabled(true);
        mDragListView.setCustomDragItem(new MydragItem(getContext(), R.layout.list_figure_item));
    }

    private void saveAsPDFDance(String file) throws IOException {
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        String filePath = downloadPath+"/"+file+".pdf";
        saveDanceAsPDFFromPath(getContext(), file, filePath);
    }

    private static void saveDanceAsPDFFromPath(Context context, String headerTitle, String filePath) throws IOException {
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


            creator.createPdf(context, filePath, headerTitle,listAdapter.isCommentEnabled(),resources.getDrawable(R.drawable.choreologo),mItemArray,out);

            NotificationManager mNotifyManager;
            NotificationCompat.Builder mBuilder;
            mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


            mBuilder = new NotificationCompat.Builder(context);
            int color = resources.getColor(android.R.color.holo_blue_light);
            mBuilder.setContentTitle(resources.getString(R.string.app_name))
                    .setContentText(resources.getString(R.string.pdf_file_uploading,dance_file))
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setAutoCancel(false)
                    .setColor(color);
            mNotifyManager.notify(1234, mBuilder.build());
        }
    }

    private void saveDance(String file) throws IOException {
        Log.d(TAG, "save...");
        String filePath = getActivity().getBaseContext().getExternalFilesDir(null)+"/"+file;
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
        long index=0;
        try {
            String line;
            while (reader.hasNextLine()) {
                line = reader.nextLine();
                String[] strings = TextUtils.split(line, ";");

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
    private void loadDance() throws IOException {
        String filePath = getActivity().getBaseContext().getExternalFilesDir(null)+"/"+dance_file;
        loadDanceFile(filePath);
    }

    private  void loadDance(String file) throws IOException {
        String filePath=getActivity().getBaseContext().getExternalFilesDir(null)+"/"+file;
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
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.figure_item_menu, menu);

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
                    mItemArray.clear();
                    if (listAdapter!= null) listAdapter.notifyDataSetChanged();
                    loadDance(dance_file+"onscreen");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onNewFileClick(final Context context)
    {
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
                ChoreographerConstants.init(context);
                dance_file = input1.getText().toString();
                if (dance_file.replaceAll(" ","").equals(""))return;
                dance_file = ChoreographerConstants.addNew(context, dance_file);
                DanceCustomAdapter danceListAdapter = new DanceCustomAdapter(context, R.layout.dance_custom_list, ChoreographerConstants.DANCE_LIST_FILENAME);
                ListView drawerList = (ListView) MainActivity.context.findViewById(R.id.left_drawer);
                drawerList.setAdapter(danceListAdapter);

                int index = ChoreographerConstants.getIndex(dance_file);
                if (index != -1) {
                    danceListAdapter.setClicked(index);
                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setTitle(dance_file);
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        actionBar.setHomeButtonEnabled(true);
                        DrawerLayout mDrawerLayout = (DrawerLayout)  getActivity().findViewById(R.id.drawer_layout);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    }
                    isSyllabus=false;
                    getActivity().supportInvalidateOptionsMenu();

                }
                if (mItemArray!=null) mItemArray.clear();
                sCreatedItems = 0;
                if (listAdapter != null) {
                    listAdapter.setLastPosition(-1);
                    listAdapter.notifyDataSetChanged();
                }

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
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                            .setVisibility(View.GONE);
                }else{
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
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
        if (requestCode== IMPORT_REQUEST) {
            if (resultCode == RESULT_OK) {

                Uri uri = data.getData();

                //New getname
                String uriString = uri.toString();
                File myFile = new File(uriString);
                String displayName = null;

                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    } finally {
                        cursor.close();
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.getName();
                }
                if (displayName!=null) Log.d("Choreo", "filename is :"+displayName);


                String filepath = uri.getPath();
                //getRealFilePath(uri);
                if (!filepath.equals("")) {
                    File src = new File(filepath);
                    String fileName = src.getName();
                    if (displayName!=null) fileName=displayName;
                    dance_file = ChoreographerConstants.addNew(getActivity().getBaseContext(), fileName);
                    File dest = new File(getActivity().getBaseContext().getExternalFilesDir(null) + "/" + dance_file);

                    try {
                        copy(uri, dest);
                    } catch (IOException e) {
                        Log.d(TAG, "error =" + fileName);
                        e.printStackTrace();
                    }
                    try {
                        mItemArray.clear();
                        if (listAdapter != null) listAdapter.notifyDataSetChanged();
                        loadDance();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ChoreographerConstants.init(MainActivity.context);
                    DanceCustomAdapter danceListAdapter = new DanceCustomAdapter(MainActivity.context, R.layout.dance_custom_list, ChoreographerConstants.DANCE_LIST_FILENAME);

                    ListView drawerList = (ListView) MainActivity.context.findViewById(R.id.left_drawer);

                    drawerList.setAdapter(danceListAdapter);
                    int index = ChoreographerConstants.getIndex(dance_file);
                    if (index != -1) {
                        danceListAdapter.setClicked(index);
                        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setTitle(dance_file);
                            actionBar.setDisplayHomeAsUpEnabled(true);
                            actionBar.setHomeButtonEnabled(true);
                            DrawerLayout mDrawerLayout = (DrawerLayout)  getActivity().findViewById(R.id.drawer_layout);
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                        }
                    }
                    getActivity().supportInvalidateOptionsMenu();

                }
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
                    String filePath =view.getContext().getExternalFilesDir(null)+"/"+dance_file + "onscreen";
                    saveDanceFromPath(filePath);
                    mItemArray.clear();
                    if (listAdapter != null) listAdapter.notifyDataSetChanged();
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

        alert.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Delete
                mItemArray.remove(pos);
                if (mItemArray.size()==pos) listAdapter.setLastPosition(pos-1);
                listAdapter.notifyDataSetChanged();
                //sav
                //sav
                try {
                    String filePath =view.getContext().getExternalFilesDir(null)+"/"+dance_file + "onscreen";
                    saveDanceFromPath(filePath);
                    mItemArray.clear();
                    if (listAdapter != null) listAdapter.notifyDataSetChanged();
                    loadDanceFromPath(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        alert.show();
    }
}
