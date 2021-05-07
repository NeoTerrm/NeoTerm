package io.neoterm.ui.pm

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.component.pm.*
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.config.NeoPreference
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.floating.TerminalDialog
import io.neoterm.ui.pm.adapter.PackageAdapter
import io.neoterm.ui.pm.model.PackageModel
import io.neoterm.ui.pm.utils.StringDistance
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

  lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
  lateinit var adapter: PackageAdapter
  lateinit var models: ArrayList<PackageModel>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.ui_pm_single_tab)
    val toolbar = findViewById<Toolbar>(R.id.pm_toolbar)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    recyclerView = findViewById(R.id.pm_package_list)
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
        .execute(
          NeoTermPath.APT_BIN_PATH,
          arrayOf("apt", "install", "-y", packageName)
        )
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
    val sourceManager = ComponentManager.getComponent<PackageComponent>().sourceManager
    val sourceList = sourceManager.getAllSources()

    AlertDialog.Builder(this)
      .setTitle(R.string.pref_package_source)
      .setMultiChoiceItems(sourceList.map { "${it.url} :: ${it.repo}" }.toTypedArray(),
        sourceList.map { it.enabled }.toBooleanArray(), { dialog, which, isChecked ->
          sourceList[which].enabled = isChecked
        })
      .setPositiveButton(android.R.string.yes, { _, _ ->
        changeSourceInternal(sourceManager, sourceList)
      })
      .setNeutralButton(R.string.new_source, { _, _ ->
        changeSourceToUserInput(sourceManager)
      })
      .setNegativeButton(android.R.string.no, null)
      .show()
  }

  @SuppressLint("SetTextI18n")
  private fun changeSourceToUserInput(sourceManager: SourceManager) {
    val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_two_text, null, false)
    view.findViewById<TextView>(R.id.dialog_edit_text_info).text = getString(R.string.input_new_source_url)
    view.findViewById<TextView>(R.id.dialog_edit_text2_info).text = getString(R.string.input_new_source_repo)

    val urlEditor = view.findViewById<EditText>(R.id.dialog_edit_text_editor)
    val repoEditor = view.findViewById<EditText>(R.id.dialog_edit_text2_editor)
    repoEditor.setText("stable main")

    AlertDialog.Builder(this)
      .setTitle(R.string.pref_package_source)
      .setView(view)
      .setNegativeButton(android.R.string.no, null)
      .setPositiveButton(android.R.string.yes, { _, _ ->
        val url = urlEditor.text.toString()
        val repo = repoEditor.text.toString()
        var errored = false
        if (url.trim().isEmpty()) {
          urlEditor.error = getString(R.string.error_new_source_url)
          errored = true
        }
        if (repo.trim().isEmpty()) {
          repoEditor.error = getString(R.string.error_new_source_repo)
          errored = true
        }
        if (errored) {
          return@setPositiveButton
        }
        val source = urlEditor.text.toString()
        sourceManager.addSource(source, repo, true)
        postChangeSource(sourceManager)
      })
      .show()
  }

  private fun changeSourceInternal(sourceManager: SourceManager, source: List<Source>) {
    sourceManager.updateAll(source)
    postChangeSource(sourceManager)
  }

  private fun postChangeSource(sourceManager: SourceManager) {
    sourceManager.applyChanges()
    NeoPreference.store(R.string.key_package_source, sourceManager.getMainPackageSource())
    SourceHelper.syncSource(sourceManager)
    executeAptUpdate()
  }

  private fun executeAptUpdate() {
    PackageUtils.apt(this, "update", null, { exitStatus, dialog ->
      if (exitStatus != 0) {
        dialog.setTitle(getString(R.string.error))
        return@apt
      }
      Toast.makeText(this, R.string.apt_update_ok, Toast.LENGTH_SHORT).show()
      dialog.dismiss()
      refreshPackageList()
    })
  }

  private fun executeAptUpgrade() {
    PackageUtils.apt(this, "update", null, { exitStatus, dialog ->
      if (exitStatus != 0) {
        dialog.setTitle(getString(R.string.error))
        return@apt
      }
      dialog.dismiss()

      PackageUtils.apt(this, "upgrade", arrayOf("-y"), out@{ exitStatus, dialog ->
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
      val pm = ComponentManager.getComponent<PackageComponent>()
      val sourceFiles = SourceHelper.detectSourceFiles()

      pm.clearPackages()
      sourceFiles.forEach { pm.reloadPackages(it, false) }
      pm.packages.values.mapTo(models, { PackageModel(it) })

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

  private fun sortDistance(
    models: List<PackageModel>, query: String,
    mapper: (NeoPackageInfo) -> String
  ): List<Pair<PackageModel, Int>> {
    return models
      .map({
        Pair(it, StringDistance.distance(mapper(it.packageInfo).toLowerCase(), query.toLowerCase()))
      })
      .sortedWith(Comparator { l, r -> r.second.compareTo(l.second) })
      .toList()
  }

  private fun filter(models: List<PackageModel>, query: String): List<PackageModel> {
    val filteredModelList = mutableListOf<PackageModel>()
    val prepared = models.filter {
      it.packageInfo.packageName!!.contains(query, true)
        || it.packageInfo.description!!.contains(query, true)
    }

    sortDistance(prepared, query, { it.packageName!! }).mapTo(filteredModelList, { it.first })
    sortDistance(prepared, query, { it.description!! }).mapTo(filteredModelList, { it.first })
    return filteredModelList
  }

  override fun onQueryTextSubmit(text: String?): Boolean {
    return false
  }

  override fun onQueryTextChange(text: String?): Boolean {
    if (text != null) {
      val filteredModelList = filter(models, text)
      adapter.edit()
        .replaceAll(filteredModelList)
        .commit()
    }
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