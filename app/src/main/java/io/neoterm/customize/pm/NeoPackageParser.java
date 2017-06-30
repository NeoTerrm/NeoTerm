package io.neoterm.customize.pm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author kiva
 */

public class NeoPackageParser {
    public interface ParseStateListener {
        void onStartState();

        void onEndState();

        NeoPackageInfo onCreatePackageInfo();

        void onStartParsePackage(String name, NeoPackageInfo packageInfo);

        void onEndParsePackage(NeoPackageInfo packageInfo);
    }

    private static final String
            KEY_PACKAGE_NAME = "Package",
            KEY_VERSION = "Version",
            KEY_ESSENTIAL = "Essential",
            KEY_ARCH = "Architecture",
            KEY_MAINTAINER = "Maintainer",
            KEY_INSTALLED_SIZE = "Installed-Size",
            KEY_DEPENDS = "Depends",
            KEY_FILENAME = "Filename",
            KEY_SIZE = "Size",
            KEY_MD5 = "MD5sum",
            KEY_SHA1 = "SHA1",
            KEY_SHA256 = "SHA256",
            KEY_HOMEPAGE = "Homepage",
            KEY_DESC = "Description";

    private BufferedReader reader;
    private ParseStateListener stateListener;

    public NeoPackageParser(InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public NeoPackageParser setStateListener(ParseStateListener stateListener) {
        this.stateListener = stateListener;
        return this;
    }

    public void parse() throws IOException {
        if (stateListener == null) {
            return;
        }

        String line;
        String[] splits = new String[2];
        String key;
        String value;

        NeoPackageInfo packageInfo = null;

        stateListener.onStartState();
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }

            splitKeyAndValue(line, splits);
            key = splits[0];
            value = splits[1];

            if (key.equals(KEY_PACKAGE_NAME)) {
                if (packageInfo != null) {
                    stateListener.onEndParsePackage(packageInfo);
                }
                packageInfo = stateListener.onCreatePackageInfo();
                packageInfo.setPackageName(value);
                stateListener.onStartParsePackage(value, packageInfo);
            }

            if (packageInfo == null) {
                continue;
            }

            switch (key) {
                case KEY_ARCH:
                    packageInfo.setArchitecture(NeoPackageArchitecture.Companion.parse(value));
                    break;
                case KEY_DEPENDS:
                    packageInfo.setDependenciesString(value);
                    break;
                case KEY_DESC:
                    packageInfo.setDescription(value);
                    break;
                case KEY_ESSENTIAL:
                    packageInfo.setEssential(value.equals("yes"));
                    break;
                case KEY_FILENAME:
                    packageInfo.setFileName(value);
                    break;
                case KEY_HOMEPAGE:
                    packageInfo.setHomePage(value);
                    break;
                case KEY_INSTALLED_SIZE:
                    packageInfo.setInstalledSizeInBytes(Long.parseLong(value));
                    break;
                case KEY_MAINTAINER:
                    packageInfo.setMaintainer(value);
                    break;
                case KEY_MD5:
                    packageInfo.setMd5(value);
                    break;
                case KEY_SHA1:
                    packageInfo.setSha1(value);
                    break;
                case KEY_SHA256:
                    packageInfo.setSha256(value);
                    break;
                case KEY_SIZE:
                    packageInfo.setSizeInBytes(Long.parseLong(value));
                    break;
                case KEY_VERSION:
                    packageInfo.setVersion(value);
                    break;
            }
        }
        if (packageInfo != null) {
            stateListener.onEndParsePackage(packageInfo);
        }
        stateListener.onEndState();
    }

    private void splitKeyAndValue(String line, String[] splits) {
        splits[0] = line.substring(0, line.indexOf(':')).trim();
        int valueIndex = line.indexOf(':');
        splits[1] = line.substring(valueIndex == line.length() ? valueIndex : valueIndex + 1).trim();
    }
}
