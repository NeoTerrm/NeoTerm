package io.neoterm.installer.packages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author kiva
 */

public class NeoPackageManager {
    private static final NeoPackageManager INSTANCE = new NeoPackageManager();

    public static NeoPackageManager getInstance() {
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

    public int getPackageCount() {
        return queryEnabled ? neoPackages.size() : -1;
    }

    public void refreshPackageList(File packageListFile) throws IOException {
        synchronized (lock) {
            if (isRefreshing) {
                return;
            }
            isRefreshing = true;
        }
        tryParsePackages(packageListFile);
        synchronized (lock) {
            isRefreshing = false;
        }
    }

    private void tryParsePackages(File packageListFile) throws IOException {
        NeoPackageParser packageParser = new NeoPackageParser(new FileInputStream(packageListFile));
        packageParser.setStateListener(new NeoPackageParser.ParseStateListener() {
            @Override
            public void onStartState() {
                queryEnabled = false;
                neoPackages.clear();
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
