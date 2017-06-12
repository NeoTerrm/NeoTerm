package io.neoterm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.neoterm.terminal.EmulatorDebug;
import io.neoterm.terminal.TerminalSession;

/**
 * @author kiva
 */

public class NeoTermService extends Service {
    public class NeoTermBinder extends Binder {
        public NeoTermService service = NeoTermService.this;
    }

    public static final String ACTION_SERVICE_STOP = "neoterm.action.service.stop";

    private static final int NOTIFICATION_ID = 52019;

    private final NeoTermBinder neoTermBinder = new NeoTermBinder();
    private final List<TerminalSession> mTerminalSessions = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return neoTermBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_SERVICE_STOP.equals(action)) {
            for (int i = 0; i < mTerminalSessions.size(); i++)
                mTerminalSessions.get(i).finishIfRunning();
            stopSelf();
        } else if (action != null) {
            Log.e(EmulatorDebug.LOG_TAG, "Unknown NeoTermService action: '" + action + "'");
        }

        if ((flags & START_FLAG_REDELIVERY) == 0) {
            // Service is started by WBR, not restarted by system, so release the WakeLock from WBR.
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);

        for (int i = 0; i < mTerminalSessions.size(); i++)
            mTerminalSessions.get(i).finishIfRunning();
        mTerminalSessions.clear();
    }

    public List<TerminalSession> getSessions() {
        return mTerminalSessions;
    }

    TerminalSession createTermSession(String executablePath, String[] arguments, String cwd, String[] env, TerminalSession.SessionChangedCallback sessionCallback) {
        if (cwd == null) cwd = getFilesDir().getAbsolutePath();

        boolean isLoginShell = false;

        if (executablePath == null) {
            // Fall back to system shell as last resort:
            executablePath = "/system/bin/sh";
            isLoginShell = true;
        }

        if (arguments == null) {
            arguments = new String[]{executablePath};
        }

        int lastSlashIndex = executablePath.lastIndexOf('/');
        String processName = (isLoginShell ? "-" : "") +
                (lastSlashIndex == -1 ? executablePath : executablePath.substring(lastSlashIndex + 1));

        TerminalSession session = new TerminalSession(executablePath, cwd, arguments, env, sessionCallback);
        mTerminalSessions.add(session);
        updateNotification();
        return session;
    }

    public int removeTermSession(TerminalSession sessionToRemove) {
        int indexOfRemoved = mTerminalSessions.indexOf(sessionToRemove);
        mTerminalSessions.remove(indexOfRemoved);
        updateNotification();
        return indexOfRemoved;
    }

    private void updateNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, createNotification());
    }

    private Notification createNotification() {
        Intent notifyIntent = new Intent(this, NeoTermActivity.class);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);

        int sessionCount = mTerminalSessions.size();
        String contentText = sessionCount + " session" + (sessionCount == 1 ? "" : "s");

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getText(R.string.app_name));
        builder.setContentText(contentText);
        builder.setSmallIcon(R.drawable.ic_service_notification);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setShowWhen(false);
        builder.setColor(0xFF000000);
        return builder.build();
    }
}
