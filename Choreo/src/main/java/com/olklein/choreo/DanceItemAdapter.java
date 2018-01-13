/*
  Copyright 2014 Magnus Woxblom

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.olklein.choreo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class DanceItemAdapter extends DragItemAdapter<DanceFigure, DanceItemAdapter.ViewHolder> {

    private final int mLayoutId;
    private final int mGrabHandleId;
    private final boolean mDragOnLongPress;


    public int getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    private int lastPosition=-1;
    private boolean CommentEnabled;

    public DanceItemAdapter(ArrayList<DanceFigure> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        super();
        mDragOnLongPress =dragOnLongPress;
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public DanceItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).getId();
    }

    @Override
    public void onBindViewHolder(DanceItemAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position).getName();

        holder.mName.setText(text);
        text = mItemList.get(position).getTempo();
        holder.mRhythm.setText(text);

        holder.mComment.setVisibility(View.GONE);
        if(isCommentEnabled()){
            if (!mItemList.get(position).getComment().equals("")) {
                holder.mComment.setVisibility(View.VISIBLE);
            }
        }
        String txtComment =mItemList.get(position).getComment().replace("\n", System.getProperty("line.separator"));
        holder.mComment.setText(txtComment);

        holder.itemView.setTag(mItemList.get(position));

    }


    public boolean isCommentEnabled() {
        return CommentEnabled;
    }

    public void setCommentEnabled(boolean commentEnabled) {
        CommentEnabled = commentEnabled;
    }


    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public final TextView mName;
        public final TextView mRhythm;
        public final TextView mComment;
        public final TextView mItemLeft;
        public final TextView mItemRight;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId,mDragOnLongPress);
            mName = (TextView) itemView.findViewById(R.id.nameTv);
            mRhythm = (TextView) itemView.findViewById(R.id.rhythmTv);
            mComment = (TextView) itemView.findViewById(R.id.commentTv);
            mItemLeft = (TextView) itemView.findViewById(R.id.item_left);
            mItemRight = (TextView) itemView.findViewById(R.id.item_right);
        }

        @Override
        public void onItemClicked(View view) {
            lastPosition = getPositionForItemId(this.mItemId);
            ListFragment.openItemDialog(view, lastPosition);
        }

        @Override
        public boolean onItemLongClicked(View view) {
            lastPosition = getPositionForItemId(this.mItemId);
            return false;
        }

    }
}
