package io.neoterm.customize.config

import io.neoterm.frontend.config.NeoConfigureFile
import java.io.File

/**
 * @author kiva
 */
interface IConfigureFileLoader {
    fun loadConfigure() : NeoConfigureFile?
}
