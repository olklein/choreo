package com.olklein.choreo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

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

class DanceCustomAdapter extends ArrayAdapter<String> {

    private String[] listOfDance;
    private final LayoutInflater inflater;
    private int checked;
    static final private String TAG ="DANCE";
    private static String mHomePath;

    final private DrawerLayout mDrawer;

    public DanceCustomAdapter(Context context, int resource, String[] listOfDance, DrawerLayout drawer) {
        super(context, resource, listOfDance);
        setList(listOfDance);
        mHomePath = context.getExternalFilesDir(null).getPath();
        mDrawer = drawer;
        inflater = LayoutInflater.from(context);
        checked=0;
    }

    private void setList(String[] list) {
        this.listOfDance = list;
    }

    private class ViewHolder{
        TextView textViewName;
        AppCompatImageView  deleteButton;
    }


    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        View myView = convertView;
        ViewHolder holder;
        if(convertView == null){
            myView = inflater.inflate(R.layout.dance_custom_list, parent, false);
            holder = new ViewHolder();
            holder.textViewName = (TextView) myView.findViewById(R.id.textViewName);
            holder.deleteButton = (AppCompatImageView) myView.findViewById(R.id.deleteItem);

            myView.setTag(holder);
        }else{
            holder = (ViewHolder)myView.getTag();
        }

        final Context context = myView.getContext();
        holder.deleteButton.setBackgroundResource(R.drawable.buttonemptybackground);
        holder.deleteButton.setImageResource(R.drawable.ic_delete_forever_black_48px);

        holder.textViewName.setText(listOfDance[position]);
        holder.textViewName.setTextColor(0xFF696969);
        holder.textViewName.setTextSize(TypedValue.COMPLEX_UNIT_SP,22);
        holder.textViewName.setMaxLines(1);
        holder.textViewName.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.textViewName.getLayoutParams();
        params.setMargins(10,0,0,0);
        holder.textViewName.setLayoutParams(params);

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked"+position);
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(context);
                builder
                        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String filePath = getContext().getExternalFilesDir(null)+"/"+ChoreographerConstants.DANCE_LIST_FILENAME[position];
                                File file = new File(filePath);
                                boolean deleted = file.delete();
                                Log.d(TAG,"Deleted="+(deleted?"true":"false"));

                                file = new File(filePath+"onscreen");

                                if (file.exists())
                                {
                                    deleted = file.delete();
                                    Log.d(TAG,"Deleted="+(deleted?"true":"false"));
                                }
                                ChoreographerConstants.init(mHomePath);
                                DanceCustomAdapter danceListAdapter =
                                        new DanceCustomAdapter(getContext(),
                                                R.layout.dance_custom_list,
                                                ChoreographerConstants.DANCE_LIST_FILENAME,mDrawer);
                                ListView drawerList = (ListView) parent.findViewById(R.id.left_drawer);

                                drawerList.setAdapter(danceListAdapter);
                                if (!ListFragment.isMediaFolder()) {
                                    ListFragment fragment = new ListFragment();
                                    fragment.setDrawer(mDrawer);

                                    MainActivity.selectItem(fragment, 0, mDrawer, drawerList);
                                    danceListAdapter.setClicked(0);
                                }
                            }
                        });
                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and return it
                String title =getContext().getResources().getString(R.string.deletethefilewithname,ChoreographerConstants.DANCE_LIST_FILENAME[position]);
                builder.setTitle(title);
                builder.setIcon(R.mipmap.ic_launcher);
                AlertDialog alert = builder.create();
                try {
                    alert.show();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        holder.textViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked"+position);
                if (position<ChoreographerConstants.DANCE_LIST_NAME.length) {
                    ListView drawerList = (ListView) parent.findViewById(R.id.left_drawer);
                    MainActivity.setCurrentItem(position);
                    ListFragment fragment = new ListFragment();
                    fragment.setDrawer(mDrawer);
                    MainActivity.selectItem(fragment,-1,mDrawer,drawerList);
                }
            }
        });

        if (position == checked){
            myView.setBackgroundColor(ContextCompat.getColor(context,R.color.list_item_selected_color_dark));
        }else{
            myView.setBackground(ContextCompat.getDrawable(context,R.drawable.list_item_background));
        }
        return myView;
    }

    public void setClicked(int position) {
        checked = position;
        // Notify that some data has been changed
        notifyDataSetChanged();
    }
}