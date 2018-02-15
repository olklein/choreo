package com.olklein.choreo;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

class BkgCopier {
    public void createCopy(Context ctxt, Uri uri, File dsttmp, File dst) {
        new GetResults(ctxt, uri, dsttmp, dst).execute();
    }

    static class GetResults extends AsyncTask<String, Void, String> {
        final Context ctxt;
        final Uri mUri;
        final File mDst;
        final File mDstTMP;


        GetResults(Context context,Uri uri, File dsttmp, File dst){
            ctxt= context;
            mUri = uri;
            mDst = dst;
            mDstTMP = dsttmp;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                    copy(mUri, mDstTMP, mDst);
                } catch (IOException e) {
                    Log.d("Choreo", "error during copy=");
                    e.printStackTrace();
                }

            return "Done";
        }

        protected void onPostExecute(String result) {
            Log.d("Choreo", "Copy post execute");
            ListFragment.refresh(mDst);

        }
        private void copy(Uri uri, File dsttmp, File dst) throws IOException {
            InputStream in = ctxt.getContentResolver().openInputStream(uri);
            if (in != null) {
                try {
                    OutputStream out = new FileOutputStream(dsttmp);
                    try {
                        // Transfer bytes from in to out
                        byte[] buf = new byte[1024];
                        int len;

                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    } finally {
                        out.close();
                        dsttmp.renameTo(dst);
                        dst.setReadable(true);
                    }
                } finally {
                    in.close();
                }
            }
        }
    }
}