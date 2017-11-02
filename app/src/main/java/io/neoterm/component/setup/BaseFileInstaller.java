package io.neoterm.component.setup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.system.Os;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.neoterm.R;
import io.neoterm.backend.EmulatorDebug;
import io.neoterm.frontend.logging.NLog;
import io.neoterm.frontend.preference.NeoPreference;
import io.neoterm.frontend.preference.NeoTermPath;

public final class BaseFileInstaller {
    public interface ResultListener {
        void onResult(Exception error);
    }

    public static boolean needSetup() {
        final File PREFIX_FILE = new File(NeoTermPath.USR_PATH);
        return !PREFIX_FILE.isDirectory();
    }

    public static void installBaseFiles(final Activity activity, final ResultListener resultListener) {
        if (!needSetup()) {
            resultListener.onResult(null);
            return;
        }

        final File PREFIX_FILE = new File(NeoTermPath.USR_PATH);

        final ProgressDialog progress = makeProgressDialog(activity);
        progress.setMax(100);
        progress.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    final String STAGING_PREFIX_PATH = NeoTermPath.ROOT_PATH + "/usr-staging";
                    final File STAGING_PREFIX_FILE = new File(STAGING_PREFIX_PATH);

                    if (STAGING_PREFIX_FILE.exists()) {
                        deleteFolder(STAGING_PREFIX_FILE);
                    }

                    int totalBytes = 0;
                    int totalReadBytes = 0;
                    final byte[] buffer = new byte[8096];
                    final List<Pair<String, String>> symlinks = new ArrayList<>(50);

                    HttpURLConnection connection = openBaseFileConnection();
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    totalBytes = connection.getContentLength();

                    try (ZipInputStream zipInput = new ZipInputStream(connection.getInputStream())) {
                        ZipEntry zipEntry;

                        while ((zipEntry = zipInput.getNextEntry()) != null) {
                            totalReadBytes += zipEntry.getCompressedSize();

                            final int totalReadBytesFinal = totalReadBytes;
                            final int totalBytesFinal = totalBytes;

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        double progressFloat = ((double) totalReadBytesFinal) / ((double) totalBytesFinal) * 100.0;
                                        progress.setProgress((int) progressFloat);
                                    } catch (RuntimeException ignore) {
                                        // activity dismissed
                                    }
                                }
                            });

                            if (zipEntry.getName().contains("SYMLINKS.txt")) {
                                BufferedReader symlinksReader = new BufferedReader(new InputStreamReader(zipInput));
                                String line;
                                while ((line = symlinksReader.readLine()) != null) {
                                    if (line.isEmpty()) {
                                        continue;
                                    }
                                    String[] parts = line.split("←");
                                    if (parts.length != 2)
                                        throw new RuntimeException("Malformed symlink line: " + line);
                                    String oldPath = parts[0];
                                    String newPath = STAGING_PREFIX_PATH + "/" + parts[1];
                                    symlinks.add(Pair.create(oldPath, newPath));
                                }
                            } else {
                                String zipEntryName = zipEntry.getName();
                                File targetFile = new File(STAGING_PREFIX_PATH, zipEntryName);
                                if (zipEntry.isDirectory()) {
                                    if (!targetFile.mkdirs())
                                        throw new RuntimeException("Failed to create directory: " + targetFile.getAbsolutePath());
                                } else {
                                    try (FileOutputStream outStream = new FileOutputStream(targetFile)) {
                                        int readBytes;
                                        while ((readBytes = zipInput.read(buffer)) != -1) {
                                            outStream.write(buffer, 0, readBytes);
                                        }
                                    }
                                    if (zipEntryName.startsWith("bin/") || zipEntryName.startsWith("libexec") || zipEntryName.startsWith("lib/apt/methods")) {
                                        //noinspection OctalInteger
                                        Os.chmod(targetFile.getAbsolutePath(), 0700);
                                    }
                                }
                            }
                        }
                    }

                    connection.disconnect();

                    if (symlinks.isEmpty())
                        throw new RuntimeException("No SYMLINKS.txt encountered");
                    for (Pair<String, String> symlink : symlinks) {
                        NLog.INSTANCE.e("Setup", "Linking " + symlink.first + " to " + symlink.second);
                        Os.symlink(symlink.first, symlink.second);
                    }

                    if (!STAGING_PREFIX_FILE.renameTo(PREFIX_FILE)) {
                        throw new RuntimeException("Unable to rename staging folder");
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultListener.onResult(null);
                        }
                    });
                } catch (final Exception e) {
                    NLog.INSTANCE.e(EmulatorDebug.LOG_TAG, "Bootstrap error", e);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                resultListener.onResult(e);
                            } catch (RuntimeException e) {
                                // Activity already dismissed - ignore.
                            }
                        }
                    });
                } finally {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                progress.dismiss();
                            } catch (RuntimeException e) {
                                // Activity already dismissed - ignore.
                            }
                        }
                    });
                }
            }
        }.start();
    }

    private static HttpURLConnection openBaseFileConnection() throws IOException {
        String arch = determineArchName();
        String baseUrl = NeoTermPath.INSTANCE.getSERVER_BASE_URL();

        // Use the same source
        NeoPreference.INSTANCE.store(R.string.key_package_source, baseUrl);

        return (HttpURLConnection) new URL(baseUrl + "/boot/" + arch + ".zip").openConnection();
    }

    private static String determineArchName() {
        for (String androidArch : Build.SUPPORTED_ABIS) {
            switch (androidArch) {
                case "arm64-v8a":
                    return "aarch64";
                case "armeabi-v7a":
                    return "arm";
                case "x86_64":
                    return "x86_64";
//                case "x86":
//                    return "i686";
            }
        }
        throw new RuntimeException("Unable to determine arch from Build.SUPPORTED_ABIS =  " +
                Arrays.toString(Build.SUPPORTED_ABIS));
    }

    private static void deleteFolder(File fileOrDirectory) {
        File[] children = fileOrDirectory.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteFolder(child);
            }
        }
        if (!fileOrDirectory.delete()) {
            throw new RuntimeException("Unable to delete " + (fileOrDirectory.isDirectory() ? "directory " : "file ") + fileOrDirectory.getAbsolutePath());
        }
    }

    public static ProgressDialog makeProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.installer_message));
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        return dialog;
    }
}
