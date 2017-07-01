package io.neoterm.ui.pm

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.DialogInterface
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
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import io.neoterm.R
import io.neoterm.customize.NeoTermPath
import io.neoterm.customize.pm.NeoPackageManager
import io.neoterm.preference.NeoPreference
import io.neoterm.ui.pm.adapter.PackageAdapter
import io.neoterm.ui.pm.model.PackageModel
import io.neoterm.utils.FileUtils
import io.neoterm.view.TerminalDialog
import java.io.File
import java.net.URL

/**
 * @author kiva
 */

class PackageManagerActivity : AppCompatActivity(), SearchView.OnQueryTextListener, SortedListAdapter.Callback {
    private val COMPARATOR = SortedListAdapter.ComparatorBuilder<PackageModel>()
            .setOrderForModel<PackageModel>(PackageModel::class.java) { a, b ->
                a!!.packageInfo.packageName!!.compareTo(b!!.packageInfo.packageName!!)
            }
            .build()

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: PackageAdapter
    lateinit var progressBar: ProgressBar
    lateinit var models: ArrayList<PackageModel>

    var mAnimator: Animator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_package_manager)
        val toolbar = findViewById(R.id.pm_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progressBar = findViewById(R.id.package_loading_progress_bar) as ProgressBar
        recyclerView = findViewById(R.id.package_list) as RecyclerView
        recyclerView.setHasFixedSize(true)
        adapter = PackageAdapter(this, COMPARATOR, object : PackageAdapter.Listener {
            override fun onModelClicked(model: PackageModel) {
                AlertDialog.Builder(this@PackageManagerActivity)
                        .setTitle(model.packageInfo.packageName)
                        .setMessage(model.getPackageDetails(this@PackageManagerActivity))
                        .setPositiveButton(R.string.install, { _, _ ->
                            val dialog = TerminalDialog(this@PackageManagerActivity, null)
                            dialog.execute("${NeoTermPath.USR_PATH}/bin/apt",
                                    arrayOf("apt", "install", "-y", model.packageInfo.packageName!!))
                            dialog.show("Installing ${model.packageInfo.packageName}")
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show()
            }

        }, FastScrollRecyclerView.SectionedAdapter {
            models[it].packageInfo.packageName?.substring(0, 1) ?: "#"
        })
        adapter.addCallback(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        models = ArrayList()
        refreshPackageList()
    }

    private fun detectSourceFiles(): ArrayList<File> {
        val sourceFiles = ArrayList<File>()
        val sourceUrl = NeoPreference.loadString(R.string.key_package_source, NeoTermPath.DEFAULT_SOURCE)
        val packageFilePrefix = detectSourceFilePrefix(sourceUrl)
        File(NeoTermPath.PACKAGE_LIST_DIR).listFiles().filterTo(sourceFiles) { it.name.startsWith(packageFilePrefix) }
        return sourceFiles
    }

    private fun detectSourceFilePrefix(sourceUrl: String): String {
        val url = URL(sourceUrl)
        val builder = StringBuilder()
        builder.append(url.host)
        builder.append("_")
        if (url.path.isNotEmpty()) {
            builder.append(url.path.substring(1)) // Skip '/'
        }
        builder.append("_dists_stable_main_binary-")
        return builder.toString()
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
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeSource() {
        val editText = EditText(this)
        editText.setText(NeoPreference.loadString(R.string.key_package_source, NeoTermPath.DEFAULT_SOURCE))
        AlertDialog.Builder(this)
                .setTitle(R.string.pref_package_source)
                .setView(editText)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    val source = editText.text.toString()
                    NeoPreference.store(R.string.key_package_source, source)

                    val sourceFile = File(NeoTermPath.SOURCE_FILE)
                    FileUtils.writeFile(sourceFile, generateSourceFile(source).toByteArray())
                    postChangeSource()
                })
                .show()
    }

    private fun postChangeSource() {
        val dialog = TerminalDialog(this@PackageManagerActivity, DialogInterface.OnCancelListener {
            refreshPackageList()
        })
        dialog.execute("${NeoTermPath.USR_PATH}/bin/apt", arrayOf("apt", "update"))
        dialog.show("apt update")
    }

    private fun refreshPackageList() {
        models.clear()
        progressBar.visibility = View.VISIBLE
        progressBar.alpha = 0.0f
        Thread {
            val pm = NeoPackageManager.getInstance()
            val sourceFiles = detectSourceFiles()

            pm.clearPackages()
            for (index in sourceFiles.indices) {
                pm.refreshPackageList(sourceFiles[index], false)
            }

            val packages = pm.packages
            for (packageInfo in packages.values) {
                models.add(PackageModel(packageInfo))
            }

            this@PackageManagerActivity.runOnUiThread {
                progressBar.visibility = View.GONE
                adapter.edit()
                        .replaceAll(models)
                        .commit()
            }
        }.start()
    }

    private fun generateSourceFile(source: String): String {
        return StringBuilder().append("# Generated by NeoTerm-Preference\n")
                .append("deb ")
                .append(source)
                .append(" stable main")
                .append("\n")
                .toString()
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
        if (progressBar.visibility != View.VISIBLE) {
            progressBar.visibility = View.VISIBLE
            progressBar.alpha = 0.0f
        }

        if (mAnimator != null) {
            mAnimator?.cancel()
        }

        mAnimator = ObjectAnimator.ofFloat(progressBar, View.ALPHA, 1.0f)
        mAnimator?.interpolator = AccelerateDecelerateInterpolator()
        mAnimator?.start()

        recyclerView.animate().alpha(0.5f)
    }

    override fun onEditFinished() {
        recyclerView.scrollToPosition(0)
        recyclerView.animate().alpha(1.0f)

        if (mAnimator != null) {
            mAnimator?.cancel()
        }

        mAnimator = ObjectAnimator.ofFloat(progressBar, View.ALPHA, 0.0f)
        mAnimator?.interpolator = AccelerateDecelerateInterpolator()
        mAnimator?.addListener(object : AnimatorListenerAdapter() {

            private var mCanceled = false

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                mCanceled = true
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (!mCanceled) {
                    progressBar.visibility = View.GONE
                }
            }
        })
        mAnimator?.start()
    }
}