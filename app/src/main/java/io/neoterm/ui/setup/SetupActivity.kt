package io.neoterm.ui.setup

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import io.neoterm.App
import io.neoterm.R
import io.neoterm.component.pm.SourceHelper
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.setup.ResultListener
import io.neoterm.setup.SetupHelper
import io.neoterm.setup.SourceConnection
import io.neoterm.setup.connections.BackupFileConnection
import io.neoterm.setup.connections.LocalFileConnection
import io.neoterm.setup.connections.NetworkConnection
import io.neoterm.setup.helper.URLAvailability
import io.neoterm.utils.MediaUtils
import io.neoterm.utils.PackageUtils
import java.io.File
import java.lang.Exception


/**
 * @author kiva
 */
class SetupActivity : AppCompatActivity(), View.OnClickListener, ResultListener {
    companion object {
        private const val REQUEST_SELECT_PARAMETER = 520;
    }

    private var aptUpdated = false
    private var setupParameter = ""
    private var setupParameterUri: Uri? = null

    private val hintMapping = arrayOf(
            R.id.setup_method_online, R.string.setup_hint_online,
            R.id.setup_method_local, R.string.setup_hint_local,
            R.id.setup_method_backup, R.string.setup_hint_backup
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_setup)

        val parameterEditor = findViewById<EditText>(R.id.setup_source_parameter)

        val tipText = findViewById<TextView>(R.id.setup_url_tip_text)

        val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { button, checked ->
            if (checked) {
                val id = button.id
                val index = hintMapping.indexOf(id)
                if (index < 0 || index % 2 != 0) {
                    parameterEditor.setHint(R.string.setup_input_source_parameter)
                    return@OnCheckedChangeListener
                }
                parameterEditor.setHint(hintMapping[index + 1])
                tipText.setText(hintMapping[index + 1])
                setDefaultValue(parameterEditor, id)
            }
        }

        findViewById<RadioButton>(R.id.setup_method_online).setOnCheckedChangeListener(onCheckedChangeListener)
        findViewById<RadioButton>(R.id.setup_method_local).setOnCheckedChangeListener(onCheckedChangeListener)
        findViewById<RadioButton>(R.id.setup_method_backup).setOnCheckedChangeListener(onCheckedChangeListener)

        findViewById<Button>(R.id.setup_next).setOnClickListener(this)
        findViewById<Button>(R.id.setup_source_parameter_select).setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        val clickedId = view?.id ?: return
        when (clickedId) {
            R.id.setup_source_parameter_select -> doSelectParameter()
            R.id.setup_next -> doPrepareSetup()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == REQUEST_SELECT_PARAMETER && resultCode == RESULT_OK) {
            if (resultData != null) {
                setupParameterUri = resultData.data
                val path = MediaUtils.getPath(this, setupParameterUri!!)
                findViewById<EditText>(R.id.setup_source_parameter).setText(path)
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    private fun doPrepareSetup() {
        val id = findViewById<RadioGroup>(R.id.setup_method_group).checkedRadioButtonId
        val editor = findViewById<EditText>(R.id.setup_source_parameter)
        setupParameter = editor.text.toString()
        if (setupParameterUri == null) {
            when (id) {
                R.id.setup_method_backup,
                R.id.setup_method_local -> {
                    SetupHelper.makeErrorDialog(this, R.string.setup_error_parameter_null)
                            .show()
                    return
                }
            }
        }

        val dialog = SetupHelper.makeProgressDialog(this, getString(R.string.setup_preparing))
        dialog.show()

        Thread {
            val errorMessage = validateParameter(id, setupParameter)

            runOnUiThread {
                dialog.dismiss()
                editor.error = errorMessage
                if (errorMessage != null) {
                    SetupHelper.makeErrorDialog(this, errorMessage).show()
                    return@runOnUiThread
                }

                val connection = createSourceConnection(id, setupParameter, setupParameterUri)
                showConfirmDialog(connection)
            }
        }.start()
    }

    private fun doSelectParameter() {
        val id = findViewById<RadioGroup>(R.id.setup_method_group).checkedRadioButtonId
        when (id) {
            R.id.setup_method_backup,
            R.id.setup_method_local -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                try {
                    startActivityForResult( Intent.createChooser(intent, getString(R.string.setup_local)),
                            REQUEST_SELECT_PARAMETER)
                } catch (ignore: ActivityNotFoundException) {
                    Toast.makeText(this, R.string.no_file_picker, Toast.LENGTH_SHORT).show()
                }
            }

            R.id.setup_method_online -> {
                val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null, false)
                view.findViewById<TextView>(R.id.dialog_edit_text_info).text = getString(R.string.input_new_source_url)

                val edit = view.findViewById<EditText>(R.id.dialog_edit_text_editor)

                AlertDialog.Builder(this)
                        .setTitle(R.string.new_source)
                        .setView(view)
                        .setPositiveButton(android.R.string.yes, { _, _ ->
                            val newURL = edit.text.toString()
                            val parameterEditor = findViewById<EditText>(R.id.setup_source_parameter)
                            parameterEditor.setText(newURL)
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show()
            }
        }
    }

    private fun createSourceConnection(id: Int, parameter: String, parameterUri: Uri?): SourceConnection {
        return when (id) {
            R.id.setup_method_local -> LocalFileConnection(this, parameterUri!!)
            R.id.setup_method_online -> NetworkConnection(parameter)
            R.id.setup_method_backup -> BackupFileConnection(this, parameterUri!!)
            else -> throw IllegalArgumentException("Unexpected setup method!")
        }
    }

    private fun validateParameter(id: Int, parameter: String): String? {
        return when (id) {
            R.id.setup_method_online -> {
                val result = URLAvailability.checkUrlAvailability(this, parameter)
                return when (result) {
                    URLAvailability.ResultCode.URL_NO_INTERNET -> {
                        getString(R.string.setup_error_no_internet)
                    }
                    URLAvailability.ResultCode.URL_CONNECTION_FAILED -> {
                        getString(R.string.setup_error_connection_failed)
                    }
                    URLAvailability.ResultCode.URL_INVALID -> {
                        getString(R.string.setup_error_invalid_url)
                    }
                    else -> null
                }
            }
            R.id.setup_method_local,
            R.id.setup_method_backup -> {
                if (File(parameter).exists()) null
                else getString(R.string.setup_error_file_not_found)
            }
            else -> null
        }
    }

    private fun setDefaultValue(parameterEditor: EditText, id: Int) {
        setupParameter = when (id) {
            R.id.setup_method_online -> NeoTermPath.DEFAULT_MAIN_PACKAGE_SOURCE
            else -> ""
        }
        parameterEditor.setText(setupParameter)
    }

    private fun showConfirmDialog(connection: SourceConnection) {
        val needSetup = SetupHelper.needSetup()
        val titleId = if (needSetup) R.string.setup_confirm else R.string.setup_reset_confirm
        val messageId = if (needSetup) R.string.setup_confirm_text else R.string.setup_reset_confirm_text

        AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    doSetup(connection)
                })
                .setNegativeButton(android.R.string.no, null)
                .show()
    }

    private fun doSetup(connection: SourceConnection) {
        SetupHelper.setup(this, connection, this)
    }

    override fun onResult(error: Exception?) {
        if (error == null) {
            setResult(RESULT_OK)
            SourceHelper.syncSource()
            executeAptUpdate()

        } else {
            AlertDialog.Builder(this)
                    .setTitle(R.string.error)
                    .setMessage(error.toString())
                    .setNegativeButton(R.string.use_system_shell, { _, _ ->
                        setResult(RESULT_CANCELED)
                        finish()
                    })
                    .setNeutralButton(R.string.show_help, { _, _ ->
                        App.get().openHelpLink()
                    })
                    .setPositiveButton(android.R.string.yes, null)
                    .show()
        }
    }

    private fun executeAptUpdate() {
        PackageUtils.apt(this, "update", null, { exitStatus, dialog ->
            if (exitStatus == 0) {
                dialog.dismiss()
                aptUpdated = true
                executeAptUpgrade()
            } else {
                dialog.setTitle(getString(R.string.error))
            }
        })
    }

    private fun executeAptUpgrade() {
        PackageUtils.apt(this, "upgrade", arrayOf("-y"), { exitStatus, dialog ->
            if (exitStatus == 0) {
                dialog.dismiss()
                finish()
            } else {
                dialog.setTitle(getString(R.string.error))
            }
        })
    }
}