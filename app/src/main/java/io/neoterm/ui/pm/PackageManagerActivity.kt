package io.neoterm.ui.pm

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.component.pm.NeoPackageComponent
import io.neoterm.component.pm.SourceManager
import io.neoterm.component.pm.SourceUtils
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.floating.TerminalDialog
import io.neoterm.frontend.preference.NeoPreference
import io.neoterm.frontend.preference.NeoTermPath
import io.neoterm.ui.pm.adapter.PackageAdapter
import io.neoterm.ui.pm.model.PackageModel
import io.neoterm.utils.PackageUtils

/**
 * @author kiva
 */

class PackageManagerActivity : AppCompatActivity(), SearchView.OnQueryTextListener, SortedListAdapter.Callback {
    private val COMPARATOR = SortedListAdapter.ComparatorBuilder<PackageModel>()
            .setOrderForModel<PackageModel>(PackageModel::class.java) { a, b ->
                a.packageInfo.packageName!!.compareTo(b.packageInfo.packageName!!)
            }
            .build()

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: PackageAdapter
    lateinit var models: ArrayList<PackageModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_pm)
        val toolbar = findViewById<Toolbar>(R.id.pm_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.package_list)
        recyclerView.setHasFixedSize(true)
        adapter = PackageAdapter(this, COMPARATOR, object : PackageAdapter.Listener {
            override fun onModelClicked(model: PackageModel) {
                AlertDialog.Builder(this@PackageManagerActivity)
                        .setTitle(model.packageInfo.packageName)
                        .setMessage(model.getPackageDetails(this@PackageManagerActivity))
                        .setPositiveButton(R.string.install, { _, _ ->
                            installPackage(model.packageInfo.packageName)
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show()
            }

        })
        adapter.addCallback(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        models = ArrayList()
        refreshPackageList()
    }

    private fun installPackage(packageName: String?) {
        if (packageName != null) {
            TerminalDialog(this@PackageManagerActivity)
                    .execute(NeoTermPath.APT_BIN_PATH,
                            arrayOf("apt", "install", "-y", packageName))
                    .onFinish(object : TerminalDialog.SessionFinishedCallback {
                        override fun onSessionFinished(dialog: TerminalDialog, finishedSession: TerminalSession?) {
                            dialog.setTitle(getString(R.string.done))
                        }
                    })
                    .imeEnabled(true)
                    .show("Installing $packageName")
            Toast.makeText(this, R.string.installing_topic, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pm, menu)
        val searchItem = menu!!.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.setOnQueryTextListener(this)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
            R.id.action_source -> changeSource()
            R.id.action_update_and_refresh -> executeAptUpdate()
            R.id.action_refresh -> refreshPackageList()
            R.id.action_upgrade -> executeAptUpgrade()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeSource() {
        val sourceManager = ComponentManager.getComponent<NeoPackageComponent>().sourceManager
        val sourceList = sourceManager.sources

        val currentSource = NeoPreference.loadString(R.string.key_package_source, NeoTermPath.DEFAULT_SOURCE)
        var checkedItem = sourceList.indexOf(currentSource)
        if (checkedItem == -1) {
            // Users may edit source.list on his own
            checkedItem = sourceList.size
            sourceManager.addSource(currentSource)
        }

        var selectedIndex = 0
        AlertDialog.Builder(this)
                .setTitle(R.string.pref_package_source)
                .setSingleChoiceItems(sourceList.toTypedArray(), checkedItem, { _, which ->
                    selectedIndex = which
                })
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    changeSourceInternal(sourceManager, sourceList.elementAt(selectedIndex))
                })
                .setNeutralButton(R.string.new_source, { _, _ ->
                    changeSourceToUserInput(sourceManager)
                })
                .setNegativeButton(android.R.string.no, null)
                .show()
    }

    private fun changeSourceToUserInput(sourceManager: SourceManager) {
        val editText = EditText(this)
        AlertDialog.Builder(this)
                .setTitle(R.string.pref_package_source)
                .setView(editText)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    val source = editText.text.toString()
                    changeSourceInternal(sourceManager, source)
                })
                .show()
    }

    private fun changeSourceInternal(sourceManager: SourceManager, source: String) {
        sourceManager.addSource(source)
        sourceManager.applyChanges()
        NeoPreference.store(R.string.key_package_source, source)
        PackageUtils.syncSource()
        executeAptUpdate()
    }

    private fun executeAptUpdate() {
        PackageUtils.executeApt(this, "update", null, { exitStatus, dialog ->
            if (exitStatus != 0) {
                dialog.setTitle(getString(R.string.error))
                return@executeApt
            }
            Toast.makeText(this, R.string.apt_update_ok, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            refreshPackageList()
        })
    }

    private fun executeAptUpgrade() {
        PackageUtils.executeApt(this, "update", null, { exitStatus, dialog ->
            if (exitStatus != 0) {
                dialog.setTitle(getString(R.string.error))
                return@executeApt
            }
            dialog.dismiss()

            PackageUtils.executeApt(this, "upgrade", arrayOf("-y"), out@ { exitStatus, dialog ->
                if (exitStatus != 0) {
                    dialog.setTitle(getString(R.string.error))
                    return@out
                }
                Toast.makeText(this, R.string.apt_upgrade_ok, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            })
        })
    }

    private fun refreshPackageList() {
        models.clear()
        Thread {
            val pm = ComponentManager.getComponent<NeoPackageComponent>()
            val sourceFiles = SourceUtils.detectSourceFiles()

            pm.clearPackages()
            for (index in sourceFiles.indices) {
                pm.reloadPackages(sourceFiles[index], false)
            }

            val packages = pm.packages
            for (packageInfo in packages.values) {
                models.add(PackageModel(packageInfo))
            }

            this@PackageManagerActivity.runOnUiThread {
                adapter.edit()
                        .replaceAll(models)
                        .commit()
                if (models.isEmpty()) {
                    Toast.makeText(this@PackageManagerActivity, R.string.package_list_empty, Toast.LENGTH_SHORT).show()
                    changeSource()
                }
            }
        }.start()
    }

    private fun filter(models: List<PackageModel>, query: String?): List<PackageModel> {
        val filteredModelList = ArrayList<PackageModel>()
        if (query != null) {
            val lowerCaseQuery = query.toLowerCase()
            for (model in models) {
                val name = model.packageInfo.packageName!!.toLowerCase()
                val desc = model.packageInfo.description!!.toLowerCase()
                if (name.contains(lowerCaseQuery) || desc.contains(lowerCaseQuery)) {
                    filteredModelList.add(model)
                }
            }
        }
        return filteredModelList
    }

    override fun onQueryTextSubmit(text: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(text: String?): Boolean {
        val filteredModelList = filter(models, text)
        adapter.edit()
                .replaceAll(filteredModelList)
                .commit()
        return true
    }

    override fun onEditStarted() {
        recyclerView.animate().alpha(0.5f)
    }

    override fun onEditFinished() {
        recyclerView.scrollToPosition(0)
        recyclerView.animate().alpha(1.0f)
    }
}