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
