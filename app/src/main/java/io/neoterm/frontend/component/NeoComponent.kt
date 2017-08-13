package io.neoterm.frontend.component

/**
 * @author kiva
 */
interface NeoComponent {
    fun onServiceInit()
    fun onServiceDestroy()
    fun onServiceObtained()
}
