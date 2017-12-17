package io.neoterm.utils

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager

/**
 * @author kiva
 */
object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        return getNetworkType(context) != null
    }

    fun getNetworkType(context: Context): String? {
        var networkType: String? = null

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager ?: return null

        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                networkType = "WIFI"

            } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                when (networkInfo.subtype) {
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_IDEN -> networkType = "2G"

                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP -> networkType = "3G"

                    TelephonyManager.NETWORK_TYPE_LTE -> networkType = "4G"

                    else -> {
                        val subtypeName = networkInfo.subtypeName
                        if (subtypeName.equals("TD-SCDMA", ignoreCase = true)
                                || subtypeName.equals("WCDMA", ignoreCase = true)
                                || subtypeName.equals("CDMA2000", ignoreCase = true)) {
                            networkType = "3G"
                        } else {
                            networkType = subtypeName
                        }
                    }
                }
            }
        }

        return networkType
    }
}