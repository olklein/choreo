package com.olklein.choreo;

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