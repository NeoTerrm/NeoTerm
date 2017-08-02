package io.neoterm.frontend.service

/**
 * @author kiva
 */
interface NeoService {
    fun onServiceInit()
    fun onServiceDestroy()
    fun onServiceObtained()
}
