package io.neoterm.ui.support

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.widget.ImageView
import io.neoterm.R
import java.net.URISyntaxException

object Donation {
    // 旧版支付宝二维码通用 Intent Scheme Url 格式
    private val INTENT_URL_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
            "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{payCode}%3F_s" +
            "%3Dweb-other&_t=1472443966571#Intent;" +
            "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"

    fun donateByAlipay(activity: Activity, payCode: String): Boolean {
        return startIntentUrl(activity, INTENT_URL_FORMAT.replace("{payCode}", payCode))
    }

    fun donateByQQ(context: Context) {
        val qrImage = ImageView(context)
        qrImage.setImageResource(R.drawable.donation_qq)
        AlertDialog.Builder(context)
                .setView(qrImage)
                .setPositiveButton(android.R.string.yes, null)
                .show()
    }

    private fun startIntentUrl(activity: Activity, intentFullUrl: String): Boolean {
        return try {
            val intent = Intent.parseUri(
                    intentFullUrl,
                    Intent.URI_INTENT_SCHEME
            )
            activity.startActivity(intent)
            true
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            false
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }
}