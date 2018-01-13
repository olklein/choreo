package com.olklein.choreo;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItem;

/**
 * Created by olklein on 26/10/2017.
 */

class MydragItem extends DragItem {

    public MydragItem(Context context, int layoutId) {
        super(context, layoutId);
    }

    @Override
    public void onBindDragView(View clickedView, View dragView) {
        CharSequence text = ((TextView) clickedView.findViewById(R.id.nameTv)).getText();
        ((TextView) dragView.findViewById(R.id.nameTv)).setText(text);
        text = ((TextView) clickedView.findViewById(R.id.rhythmTv)).getText();
        ((TextView) dragView.findViewById(R.id.rhythmTv)).setText(text);
        text = ((TextView) clickedView.findViewById(R.id.commentTv)).getText();
        ((TextView) dragView.findViewById(R.id.commentTv)).setText(text);

        //dragView.setBackgroundColor(dragView.getResources().getColor(R.color.list_item_background));
        dragView.setBackgroundColor(ContextCompat.getColor(dragView.getContext(),R.color.list_item_background));
    }
}

