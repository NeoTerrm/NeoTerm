package io.neoterm.setup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;

import java.io.File;
import java.util.Arrays;

import io.neoterm.R;
import io.neoterm.frontend.config.NeoTermPath;

/**
 * @author kiva
 */
public final class SetupHelper {
    public static boolean needSetup() {
        final File PREFIX_FILE = new File(NeoTermPath.USR_PATH);
        return !PREFIX_FILE.isDirectory();
    }

    public static void setup(final Activity activity, final SourceConnection connection,
                             final ResultListener resultListener) {
        if (!needSetup()) {
            resultListener.onResult(null);
            return;
        }

        final File prefixFile = new File(NeoTermPath.USR_PATH);

        final ProgressDialog progress = makeProgressDialog(activity);
        progress.setMax(100);
        progress.show();

        new SetupThread(activity, connection, prefixFile, resultListener, progress);
    }

    private static ProgressDialog makeProgressDialog(Context context) {
        return makeProgressDialog(context, context.getString(R.string.installer_message));
    }

    public static ProgressDialog makeProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        return dialog;
    }

    public static String determineArchName() {
        for (String androidArch : Build.SUPPORTED_ABIS) {
            switch (androidArch) {
                case "arm64-v8a":
                    return "aarch64";
                case "armeabi-v7a":
                    return "arm";
                case "x86_64":
                    return "x86_64";
            }
        }
        throw new RuntimeException("Unable to determine arch from Build.SUPPORTED_ABIS =  " +
                Arrays.toString(Build.SUPPORTED_ABIS));
    }
}
