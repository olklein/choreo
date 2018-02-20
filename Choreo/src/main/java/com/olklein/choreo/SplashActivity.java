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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import java.util.concurrent.TimeUnit;

public class SplashActivity extends FragmentActivity {

	private static final int MYWRITEREQUEST         = 223;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SystemClock.sleep(TimeUnit.SECONDS.toMillis(1));
		/// Permission Management
		Activity activity = this;

		if (ContextCompat.checkSelfPermission(activity,	Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
			ActivityCompat.requestPermissions(activity,	new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MYWRITEREQUEST);
		} else if ((ContextCompat.checkSelfPermission(activity,	Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
		{
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}
	@TargetApi(23)
	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		switch (requestCode) {
			case MYWRITEREQUEST:
			{             // If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					{
						if ((ContextCompat.checkSelfPermission(this,	Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
						{
							Intent intent = new Intent(this, MainActivity.class);
							startActivity(intent);
							finish();
						}
					}
				}
			}break;
		}
	}
}