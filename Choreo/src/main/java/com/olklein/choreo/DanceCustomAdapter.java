package com.olklein.choreo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
 * Created by olklein on 15/07/2016.
 */
class DanceCustomAdapter extends ArrayAdapter<String> {

    private String[] listOfDance;
    //private final Context context;
    private final LayoutInflater inflator;
    private int checked;
    static final private String TAG ="DANCE";


    public DanceCustomAdapter(Context context, int resource, String[] listOfDance) {
        super(context, resource, listOfDance);
        setList(listOfDance);

        inflator = LayoutInflater.from(context);
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
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View myView = convertView;

        final Context context = getContext().getApplicationContext();
        ViewHolder holder;
        if(convertView == null){
            myView = inflator.inflate(R.layout.dance_custom_list, parent, false);
            holder = new ViewHolder();
            holder.textViewName = (TextView) myView.findViewById(R.id.textViewName);
            holder.deleteButton = (AppCompatImageView) myView.findViewById(R.id.deleteItem);

            myView.setTag(holder);
        }else{
            holder = (ViewHolder)myView.getTag();
        }
        holder.deleteButton.setBackgroundResource(R.drawable.buttonemptybackground);
        holder.deleteButton.setImageResource(R.drawable.ic_delete_forever_black_48px);

        holder.textViewName.setText(listOfDance[position]);
        holder.textViewName.setTextColor(0xFF696969);
        holder.textViewName.setTextSize(TypedValue.COMPLEX_UNIT_SP,22);
        holder.textViewName.setMaxLines(1);
        holder.textViewName.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.textViewName.getLayoutParams();
        params.setMargins(10,0,0,0);
        holder.textViewName.setLayoutParams(params);



        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked"+position);
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(MainActivity.context);
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
                                ChoreographerConstants.init(context);
                                DanceCustomAdapter danceListAdapter = new DanceCustomAdapter(context, R.layout.dance_custom_list, ChoreographerConstants.DANCE_LIST_FILENAME);
                                ListView drawerList = (ListView) MainActivity.context.findViewById(R.id.left_drawer);
                                drawerList.setAdapter(danceListAdapter);
                                //MainActivity.mDrawerList.setAdapter(MainActivity.danceListAdapter);
                                MainActivity.doSelectItem(0);
                                danceListAdapter.setClicked(0);
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
                alert.show();
            }
        });

        holder.textViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked"+position);
                //LinearLayout row = (LinearLayout) v.getParent();
                if (position<ChoreographerConstants.DANCE_LIST_NAME.length) {
                    MainActivity.setCurrentItem(position);
                    MainActivity.doSelectItem(MainActivity.currentItem);
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
