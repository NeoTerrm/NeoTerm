package io.neomodule.module;

/**
 * @author kiva
 */

public class ModuleInfo {
    private String moduleName;
    private String moduleVersion;

    public ModuleInfo() {
        this("Unnamed", "0.1");
    }

    public ModuleInfo(String moduleName, String moduleVersion) {
        this.moduleName = moduleName;
        this.moduleVersion = moduleVersion;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }
}
