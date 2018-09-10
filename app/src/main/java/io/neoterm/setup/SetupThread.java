package io.neoterm.setup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.system.Os;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.neoterm.backend.EmulatorDebug;
import io.neoterm.frontend.config.NeoTermPath;
import io.neoterm.frontend.logging.NLog;

/**
 * @author kiva
 */

final class SetupThread extends Thread {
    private final SourceConnection sourceConnection;
    private final File prefixPath;
    private final Activity activity;
    private final ResultListener resultListener;
    private final ProgressDialog progressDialog;

    public SetupThread(Activity activity, SourceConnection sourceConnection,
                       File prefixPath, ResultListener resultListener,
                       ProgressDialog progressDialog) {
        this.activity = activity;
        this.sourceConnection = sourceConnection;
        this.prefixPath = prefixPath;
        this.resultListener = resultListener;
        this.progressDialog = progressDialog;
    }

    @Override
    public void run() {
        try {
            final String stagingPrefixPath = NeoTermPath.ROOT_PATH + "/usr-staging";
            final File stagingPrefixFile = new File(stagingPrefixPath);

            if (stagingPrefixFile.exists()) {
                deleteFolder(stagingPrefixFile);
            }

            int totalReadBytes = 0;
            final byte[] buffer = new byte[8096];
            final List<Pair<String, String>> symlinks = new ArrayList<>(50);


            try (ZipInputStream zipInput = new ZipInputStream(sourceConnection.getInputStream())) {
                ZipEntry zipEntry;

                int totalBytes = sourceConnection.getSize();

                while ((zipEntry = zipInput.getNextEntry()) != null) {
                    totalReadBytes += zipEntry.getCompressedSize();

                    final int totalReadBytesFinal = totalReadBytes;
                    final int totalBytesFinal = totalBytes;

                    activity.runOnUiThread(() -> {
                        try {
                            double progressFloat = ((double) totalReadBytesFinal) / ((double) totalBytesFinal) * 100.0;
                            progressDialog.setProgress((int) progressFloat);
                        } catch (RuntimeException ignore) {
                            // activity dismissed
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
                            String newPath = stagingPrefixPath + "/" + parts[1];
                            symlinks.add(Pair.create(oldPath, newPath));
                        }
                    } else {
                        String zipEntryName = zipEntry.getName();
                        File targetFile = new File(stagingPrefixPath, zipEntryName);
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

            sourceConnection.close();

            if (symlinks.isEmpty())
                throw new RuntimeException("No SYMLINKS.txt encountered");
            for (Pair<String, String> symlink : symlinks) {
                NLog.INSTANCE.e("Setup", "Linking " + symlink.first + " to " + symlink.second);
                Os.symlink(symlink.first, symlink.second);
            }

            if (!stagingPrefixFile.renameTo(prefixPath)) {
                throw new RuntimeException("Unable to rename staging folder");
            }

            activity.runOnUiThread(() -> resultListener.onResult(null));
        } catch (final Exception e) {
            NLog.INSTANCE.e(EmulatorDebug.LOG_TAG, "Bootstrap error", e);
            activity.runOnUiThread(() -> {
                try {
                    resultListener.onResult(e);
                } catch (RuntimeException e1) {
                    // Activity already dismissed - ignore.
                }
            });
        } finally {
            activity.runOnUiThread(() -> {
                try {
                    progressDialog.dismiss();
                } catch (RuntimeException e) {
                    // Activity already dismissed - ignore.
                }
            });
        }
    }

    private static void deleteFolder(File fileOrDirectory) throws IOException {
        if (fileOrDirectory.getCanonicalPath().equals(fileOrDirectory.getAbsolutePath()) && fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();

            if (children != null) {
                for (File child : children) {
                    deleteFolder(child);
                }
            }
        }

        if (!fileOrDirectory.delete()) {
            throw new RuntimeException("Unable to delete "
                    + (fileOrDirectory.isDirectory() ? "directory " : "file ")
                    + fileOrDirectory.getAbsolutePath());
        }
    }
}
