package io.neoterm.component.config

import io.neoterm.frontend.config.NeoConfigureFile

/**
 * @author kiva
 */
interface IConfigureLoader {
    fun loadConfigure() : NeoConfigureFile?
}
