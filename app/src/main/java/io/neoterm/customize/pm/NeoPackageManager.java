package io.neoterm.customize.pm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author kiva
 */

public class NeoPackageManager {
    private static final NeoPackageManager INSTANCE = new NeoPackageManager();

    public static NeoPackageManager get() {
        return INSTANCE;
    }

    private final Object lock = new Object();
    private boolean isRefreshing = false;
    private boolean queryEnabled = true;
    private HashMap<String, NeoPackageInfo> neoPackages;

    private NeoPackageManager() {
        neoPackages = new HashMap<>();
    }

    public NeoPackageInfo getPackageInfo(String packageName) {
        return queryEnabled ? neoPackages.get(packageName) : null;
    }

    public HashMap<String, NeoPackageInfo> getPackages() {
        return queryEnabled ? neoPackages : new HashMap<String, NeoPackageInfo>();
    }

    public int getPackageCount() {
        return queryEnabled ? neoPackages.size() : -1;
    }

    public void refreshPackageList(File packageListFile, boolean clearPrevious) throws IOException {
        synchronized (lock) {
            if (isRefreshing) {
                return;
            }
            isRefreshing = true;
        }
        tryParsePackages(packageListFile, clearPrevious);
        synchronized (lock) {
            isRefreshing = false;
        }
    }

    public void clearPackages() {
        if (isRefreshing) {
            return;
        }
        neoPackages.clear();
    }

    private void tryParsePackages(File packageListFile, final boolean clearPrevious) throws IOException {
        NeoPackageParser packageParser = new NeoPackageParser(new FileInputStream(packageListFile));
        packageParser.setStateListener(new NeoPackageParser.ParseStateListener() {
            @Override
            public void onStartState() {
                queryEnabled = false;
                if (clearPrevious) {
                    neoPackages.clear();
                }
            }

            @Override
            public void onEndState() {
                queryEnabled = true;
                for (NeoPackageInfo info : neoPackages.values()) {
                    resolveDepends(info);
                }
            }

            @Override
            public NeoPackageInfo onCreatePackageInfo() {
                return new NeoPackageInfo();
            }

            @Override
            public void onStartParsePackage(String name, NeoPackageInfo packageInfo) {
            }

            @Override
            public void onEndParsePackage(NeoPackageInfo packageInfo) {
                neoPackages.put(packageInfo.getPackageName(), packageInfo);
            }
        });
        packageParser.parse();
    }

    private void resolveDepends(NeoPackageInfo info) {
        String dep = info.getDependenciesString();
        if (dep == null) {
            return;
        }

        String[] splits = dep.split(",");
        NeoPackageInfo[] depends = new NeoPackageInfo[splits.length];
        info.setDependencies(depends);

        for (int i = 0; i < splits.length; ++i) {
            String item = splits[i].trim();
            depends[i] = getPackageInfo(item);
        }
    }
}
