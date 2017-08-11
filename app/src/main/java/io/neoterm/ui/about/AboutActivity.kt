package io.neoterm.ui.about

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.GnuGeneralPublicLicense30
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices
import io.neoterm.R


/**
 * @author kiva
 */
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_about)
        setSupportActionBar(findViewById<Toolbar>(R.id.about_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        try {
            val version = packageManager.getPackageInfo(packageName, 0).versionName
            (findViewById<TextView>(R.id.app_version)).text = version
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        findViewById<View>(R.id.developersView).setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle(R.string.about_developers_label)
                    .setMessage(Html.fromHtml(getString(R.string.about_developers)))
                    .show()
        }

        findViewById<View>(R.id.licensesView).setOnClickListener {
            val notices = Notices()
            notices.addNotice(Notice("ADBToolkitInstaller", "https://github.com/Crixec/ADBToolKitsInstaller", "Copyright 2017 Crixec", GnuGeneralPublicLicense30()))
            notices.addNotice(Notice("Android-Terminal-Emulator", "https://github.com/jackpal/Android-Terminal-Emulator", "Copyright (C) 2011-2016 Steven Luo", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("ChromeLikeTabSwitcher", "https://github.com/michael-rapp/ChromeLikeTabSwitcher", "Copyright 2016 - 2017 Michael Rapp", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("EventBus", "http://greenrobot.org", "Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("LicenseDialog", "http://psdev.de/LicensesDialog", "Copyright 2013-2017 Philip Schiffer", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("ModularAdapter", "https://wrdlbrnft.github.io/ModularAdapter", "Copyright 2017 Wrdlbrnft", MITLicense()))
            notices.addNotice(Notice("RecyclerView-FastScroll", "Copyright (c) 2016, Tim Malseed", "Copyright (c) 2016, Tim Malseed", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("SortedListAdapter", "https://wrdlbrnft.github.io/SortedListAdapter/", "Copyright 2017 Wrdlbrnft", MITLicense()))
            notices.addNotice(Notice("Termux", "https://termux.com", "Copyright 2016-2017 Fredrik Fornwall", GnuGeneralPublicLicense30()))
            LicensesDialog.Builder(this)
                    .setNotices(notices)
                    .setIncludeOwnLicense(true)
                    .build()
                    .show()
        }

        findViewById<View>(R.id.sourceCodeView).setOnClickListener {
            openUrl("https://github.com/NeoTerm/NeoTerm")
        }

        findViewById<View>(R.id.donateView).setOnClickListener {
            Toast.makeText(this, "我知道没人会捐赠的，所以这部分没写", Toast.LENGTH_LONG).show()
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home ->
                finish()
        }
        return super.onOptionsItemSelected(item)
    }
}