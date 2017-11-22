package com.termux.component.pm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Sam
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

    NeoPackageParser(InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    void setStateListener(ParseStateListener stateListener) {
        this.stateListener = stateListener;
    }

    public void parse() throws IOException {
        if (stateListener == null) {
            return;
        }

        String line;
        String[] splits = new String[2];
        String key = null;
        String value = null;
        boolean appendMode = false;

        NeoPackageInfo packageInfo = null;

        stateListener.onStartState();
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }

            if (splitKeyAndValue(line, splits)) {
                key = splits[0];
                value = splits[1];
                appendMode = false;
            } else {
                if (key == null) {
                    // no key provided, we don't know where the value should be appended to
                    continue;
                }
                // the rest value to previous key
                value = line.trim();
                appendMode = true;
            }

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

            if (appendMode) {
                value = appendToLastValue(packageInfo, key, value);
            }

            switch (key) {
                case KEY_ARCH:
                    packageInfo.setArchitecture(Architecture.Companion.parse(value));
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

    private String appendToLastValue(NeoPackageInfo packageInfo, String key, String value) {
        // Currently, only descriptions can be multiline
        switch (key) {
            case KEY_DESC:
                return packageInfo.getDescription() + " " + value;
            default:
                return value;
        }
    }

    private boolean splitKeyAndValue(String line, String[] splits) {
        int valueIndex = line.indexOf(':');
        if (valueIndex < 0) {
            return false;
        }

        splits[0] = line.substring(0, valueIndex).trim();
        splits[1] = line.substring(valueIndex == line.length() ? valueIndex : valueIndex + 1).trim();
        return true;
    }
}
