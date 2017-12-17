package io.neoterm.ui.setup

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import io.neoterm.R
import io.neoterm.component.setup.SetupHelper
import io.neoterm.component.setup.SourceConnection
import io.neoterm.component.setup.connection.AssetsFileConnection
import io.neoterm.component.setup.connection.BackupFileConnection
import io.neoterm.component.setup.connection.LocalFileConnection
import io.neoterm.component.setup.connection.NetworkConnection
import io.neoterm.component.setup.helper.URLAvailability
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.utils.PackageUtils
import java.io.File


/**
 * @author kiva
 */
class SetupActivity : AppCompatActivity(), View.OnClickListener {

    private var aptUpdated = false
    private var setupParameter = ""

    private val hintMapping = arrayOf(
            R.id.setup_method_online, R.string.setup_hint_online,
            R.id.setup_method_local, R.string.setup_hint_local,
            R.id.setup_method_assets, R.string.setup_hint_assets,
            R.id.setup_method_backup, R.string.setup_hint_backup
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_setup)

        val parameterEditor = findViewById<EditText>(R.id.setup_source_parameter)

        val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { button, checked ->
            if (checked) {
                val id = button.id
                val index = hintMapping.indexOf(id)
                if (index < 0 || index % 2 != 0) {
                    parameterEditor.setHint(R.string.setup_input_source_parameter)
                    return@OnCheckedChangeListener
                }
                parameterEditor.setHint(hintMapping[index + 1])
                setDefaultValue(parameterEditor, id)
            }
        }

        findViewById<RadioButton>(R.id.setup_method_online).setOnCheckedChangeListener(onCheckedChangeListener)
        findViewById<RadioButton>(R.id.setup_method_local).setOnCheckedChangeListener(onCheckedChangeListener)
        findViewById<RadioButton>(R.id.setup_method_assets).setOnCheckedChangeListener(onCheckedChangeListener)
        findViewById<RadioButton>(R.id.setup_method_backup).setOnCheckedChangeListener(onCheckedChangeListener)

        findViewById<Button>(R.id.setup_next).setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        val id = findViewById<RadioGroup>(R.id.setup_method_group).checkedRadioButtonId
        val editor = findViewById<EditText>(R.id.setup_source_parameter)
        setupParameter = editor.text.toString()

        val dialog = SetupHelper.makeProgressDialog(this, getString(R.string.setup_preparing))
        dialog.show()
        Thread {
            val errorMessage = validateParameter(id, setupParameter)

            SetupActivity@this.runOnUiThread {
                dialog.dismiss()
                if (errorMessage != null) {
                    editor.error = errorMessage
                    return@runOnUiThread
                }

                val connection = createSourceConnection(id, setupParameter)
                setup(connection)
            }
        }.start()
    }

    private fun createSourceConnection(id: Int, parameter: String): SourceConnection {
        return when (id) {
            R.id.setup_method_local -> LocalFileConnection(parameter)
            R.id.setup_method_online -> NetworkConnection(parameter)
            R.id.setup_method_assets -> AssetsFileConnection()
            R.id.setup_method_backup -> BackupFileConnection(parameter)
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
            R.id.setup_method_local or R.id.setup_method_backup -> {
                if (File(parameter).exists()) null
                else getString(R.string.setup_error_file_not_found)
            }
            else -> null
        }
    }

    private fun setDefaultValue(parameterEditor: EditText, id: Int) {
        setupParameter = when (id) {
            R.id.setup_method_online -> NeoTermPath.DEFAULT_SOURCE
            else -> ""
        }
        parameterEditor.setText(setupParameter)
    }

    private fun setup(connection: SourceConnection) {
        // TODO: Refactor
//        var resultListener: SetupHelper.ResultListener? = null
//        resultListener = SetupHelper.ResultListener { error ->
//            if (error == null) {
//                setResult(Activity.RESULT_OK)
//                PackageUtils.syncSource()
//                executeAptUpdate()
//            } else {
//                AlertDialog.Builder(this@SetupActivity)
//                        .setTitle(R.string.error)
//                        .setMessage(error.toString())
//                        .setNegativeButton(R.string.use_system_shell, { _, _ ->
//                            setResult(Activity.RESULT_CANCELED)
//                            finish()
//                        })
//                        .setPositiveButton(R.string.retry, { dialog, _ ->
//                            dialog.dismiss()
//                            SetupHelper.setup(this@SetupActivity, resultListener)
//                        })
//                        .setNeutralButton(R.string.show_help, { _, _ ->
//                            App.get().openHelpLink()
//                        })
//                        .show()
//            }
//        }
//        SetupHelper.setup(this, resultListener)
    }

    private fun executeAptUpdate() {
        PackageUtils.executeApt(this, "update", null, { exitStatus, dialog ->
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
        PackageUtils.executeApt(this, "upgrade", arrayOf("-y"), { exitStatus, dialog ->
            if (exitStatus == 0) {
                dialog.dismiss()
                finish()
            } else {
                dialog.setTitle(getString(R.string.error))
            }
        })
    }
}