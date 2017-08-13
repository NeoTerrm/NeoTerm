package io.neoterm.component.config

import io.neoterm.frontend.config.NeoConfigureFile

/**
 * @author kiva
 */
interface IConfigureFileLoader {
    fun loadConfigure() : NeoConfigureFile?
}
