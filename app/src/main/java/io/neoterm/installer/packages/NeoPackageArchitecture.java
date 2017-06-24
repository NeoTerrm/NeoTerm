package io.neoterm.installer.packages;

/**
 * @author kiva
 */

public enum NeoPackageArchitecture {
    ALL, ARM, AARCH64, X86, X86_64;

    public static NeoPackageArchitecture parse(String arch) {
        switch (arch) {
            case "arm":
                return ARM;
            case "aarch64":
                return AARCH64;
            case "x86":
                return X86;
            case "x86_64":
                return X86_64;
            default:
                return ALL;
        }
    }
}
