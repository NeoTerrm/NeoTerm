/*
Simple DirectMedia Layer
Java source code (C) 2009-2014 Sergii Pylypenko

This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not
   claim that you wrote the original software. If you use this software
   in a product, an acknowledgment in the product documentation would be
   appreciated but is not required. 
2. Altered source versions must be plainly marked as such, and must not be
   misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

package io.neoterm;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.text.Editable;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.content.res.Configuration;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.View.OnKeyListener;
import android.view.MenuItem;
import android.view.Menu;
import android.view.Gravity;
import android.text.method.TextKeyListener;
import java.util.LinkedList;
import java.io.SequenceInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Set;
import android.text.SpannedString;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import android.view.inputmethod.InputMethodManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import java.util.concurrent.Semaphore;
import android.content.pm.ActivityInfo;
import android.view.Display;
import android.util.DisplayMetrics;
import android.text.InputType;
import android.util.Log;
import android.view.Surface;
import android.app.ProgressDialog;
import android.app.KeyguardManager;
import android.view.ViewTreeObserver;
import android.graphics.Rect;
import android.net.Uri;
import android.content.ComponentName;


public class RunFromOtherApp extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Log.i("SDL", "Run from another app, getCallingActivity() is " +( getCallingActivity() == null ? "null" : "not null" ));

		Intent main = new Intent(this, MainActivity.class);
		main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if( getIntent().getScheme() != null && getIntent().getScheme().equals("x11") )
		{
			int port = getIntent().getData().getPort();
			if (port >= 0)
			{
				if (port >= 6000)
					port -= 6000;
				//Globals.CommandLine = Globals.CommandLine + " :" + port;
				main.putExtra(RestartMainActivity.SDL_RESTART_PARAMS, ":" + port);
			}
		}
		startActivity(main);

		new Thread(new Runnable()
		{
			public void run()
			{
				Log.i("SDL", "Waiting for env vars to be set");
				while( System.getenv("DISPLAY") == null || System.getenv("PULSE_SERVER") == null )
				{
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {}
				}
				Log.i("SDL", "Env vars set, returning result, getCallingActivity() is " + (getCallingActivity() == null ? "null" : "not null"));

				if( getCallingActivity() != null )
				{
					final ComponentName callingActivity = getCallingActivity().clone();
					Log.i("SDL", "Launching calling activity: " + getCallingActivity().toString());
					new Thread(new Runnable()
					{
						public void run()
						{
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {}
							Intent caller = new Intent();
							caller.setComponent(callingActivity);
							caller.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							Log.i("SDL", "Launching calling activity: " + caller.toString());
							startActivity(caller);
						}
					}).start();
				}

				Intent intent = new Intent(Intent.ACTION_RUN, Uri.parse("x11://run?DISPLAY=" + Uri.encode(System.getenv("DISPLAY")) + "&PULSE_SERVER=" + Uri.encode(System.getenv("PULSE_SERVER"))));
				intent.putExtra("DISPLAY", System.getenv("DISPLAY"));
				intent.putExtra("PULSE_SERVER", System.getenv("PULSE_SERVER"));
				intent.putExtra("run", "export DISPLAY=" + System.getenv("DISPLAY") + " ; export PULSE_SERVER=" + System.getenv("PULSE_SERVER"));
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		}).start();
	}
}
