package com.olklein.choreo;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItem;

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

        dragView.setBackgroundColor(ContextCompat.getColor(dragView.getContext(),R.color.list_item_background));
    }
}

