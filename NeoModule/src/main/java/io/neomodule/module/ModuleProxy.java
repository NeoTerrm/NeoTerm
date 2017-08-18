package io.neomodule.module;

import io.neomodule.event.EventEmitter;

/**
 * @author kiva
 */

public class ModuleProxy extends EventEmitter {
    private Module module;

    public ModuleProxy(Module module) {
        this.module = module;

        on("create", args -> module.onCreate());

        on("destroy", args -> module.onDestroy());
    }
}
