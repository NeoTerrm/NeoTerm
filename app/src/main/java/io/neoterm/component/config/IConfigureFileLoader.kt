package com.termux.component.config

import com.termux.frontend.config.NeoConfigureFile

/**
 * @author Sam
 */
interface IConfigureFileLoader {
    fun loadConfigure() : NeoConfigureFile?
}
