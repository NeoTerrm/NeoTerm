package io.neomodule.module;

import io.neomodule.module.abs.ModuleLifecycleCallback;

/**
 * @author kiva
 */

public class Module implements ModuleLifecycleCallback {
    private ModuleInfo moduleInfo;

    public Module(ModuleInfo moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    public ModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }
}
