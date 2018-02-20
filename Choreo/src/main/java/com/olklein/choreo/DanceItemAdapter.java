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

import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItemAdapter;

import java.io.File;
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
    private final String LostFileString;
    private final String LoadingFileString;
    private final String UnreadableFileString;


    public DanceItemAdapter(ArrayList<DanceFigure> list, int layoutId, int grabHandleId, boolean dragOnLongPress, Resources res) {
        super();
        mDragOnLongPress =dragOnLongPress;
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        setHasStableIds(true);
        LostFileString = res.getString(R.string.action_video_lost);
        LoadingFileString = res.getString(R.string.action_video_loadongoing);
        UnreadableFileString = res.getString(R.string.action_video_unreadable);

//        int itemBorderColor = ResourcesCompat.getColor(res,
//                R.color.list_item_color_dark/* 0x00E0ECF8*/,
//                null);
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
        holder.mName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.nobox,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);

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
        if (holder.mRhythm.getText().toString().startsWith("VideoURI-")){
            holder.mName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.nobox,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
            holder.mItemDrag.setImageResource(R.drawable.ic_theater_48);
            holder.mItemDrag.setBackgroundColor(0x00FFFFFF);
            holder.mLinearLayout.setBackgroundResource(R.drawable.bordermovie);
            holder.mComment.setVisibility(View.GONE);

            String path = Uri.parse(holder.mRhythm.getText().toString().replaceFirst("VideoURI-","")).getPath();
            holder.mName.setBackgroundColor(0x00E0ECF8); // R.color.list_item_color_dark 0x00E0ECF8

            boolean targetFileExist=doesFileExist(path);
            boolean tmpFileExist=doesFileExist(path+".tmp");

            if (!targetFileExist && !tmpFileExist ){
                holder.mName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bug_24x,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                if (LoadingFileString.equals(txtComment)){
                    holder.mName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hourglass_empty_24x,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                    holder.mName.setText(txtComment);
                }else{
                    holder.mName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bug_24x,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                    holder.mName.setText(LostFileString+" "+txtComment);
                }
            }
            else{
                if (tmpFileExist) {
                    holder.mName.setText(R.string.action_video_loadongoing);
                    holder.mName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hourglass_empty_24x,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                }else{ // !tmpFileExist && targetFileExist
                    if (isReadable(path)) {
                        if (txtComment.equals("")){
                            holder.mName.setText(R.string.clickToPlay);
                        }else{
                            holder.mName.setText(txtComment);
                         }
                        holder.mName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_24x,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                    }else{ // // !tmpFileExist && targetFileExist && !isReadable
                        holder.mName.setText(UnreadableFileString+" "+txtComment);
                        holder.mName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bug_24x,R.drawable.nobox,R.drawable.nobox,R.drawable.nobox);
                    }
                }
            }
            holder.mRhythm.setVisibility(View.GONE);
        }else{
            holder.mItemDrag.setImageResource(R.drawable.ic_drag_handle_black_48px);
            holder.mItemDrag.setBackgroundColor(0x00FFFFFF);
            holder.mLinearLayout.setBackgroundResource(R.drawable.border);
            holder.mRhythm.setVisibility(View.VISIBLE);
            holder.mName.setBackgroundColor(0x00FFFFFF);
        }
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
        public final ImageView mItemDrag;
        public final LinearLayout mLinearLayout;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId,mDragOnLongPress);
            mName = (TextView) itemView.findViewById(R.id.nameTv);
            mRhythm = (TextView) itemView.findViewById(R.id.rhythmTv);
            mComment = (TextView) itemView.findViewById(R.id.commentTv);
            mItemLeft = (TextView) itemView.findViewById(R.id.item_left);
            mItemRight = (TextView) itemView.findViewById(R.id.item_right);
            mItemDrag = (ImageView) itemView.findViewById(R.id.image);
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.item);
        }

        @Override
        public void onItemClicked(View view) {
            lastPosition = getPositionForItemId(this.mItemId);
            if (mRhythm.getText().toString().startsWith("VideoURI-")){
                Uri videoURI = Uri.parse(mRhythm.getText().toString().replaceFirst("VideoURI-",""));
                ListFragment.openItemVideoDialog(view,lastPosition,videoURI);
            }else {
                ListFragment.openItemDialog(view, lastPosition);
            }
        }

        @Override
        public boolean onItemLongClicked(View view) {
            lastPosition = getPositionForItemId(this.mItemId);
            return false;
        }
    }
    private static boolean doesFileExist(String path){

        File file = new File(path);
        return file.exists();
    }
    private static boolean isReadable(String path){
        File file = new File(path);
        return file.canRead();
    }
}