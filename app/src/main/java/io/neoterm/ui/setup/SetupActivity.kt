package io.neoterm.ui.setup

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioButton
import io.neoterm.R
import io.neoterm.utils.PackageUtils


/**
 * @author kiva
 */
class SetupActivity : AppCompatActivity() {
    private var aptUpdated = false

    private val MAPPING = arrayOf(
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
                val index = MAPPING.indexOf(id)
                if (index < 0 || index % 2 != 0) {
                    parameterEditor.setHint(R.string.setup_input_source_parameter)
                    return@OnCheckedChangeListener
                }
                parameterEditor.setHint(MAPPING[index + 1])
            }
        }

        findViewById<RadioButton>(R.id.setup_method_online).setOnCheckedChangeListener(onCheckedChangeListener)
        findViewById<RadioButton>(R.id.setup_method_local).setOnCheckedChangeListener(onCheckedChangeListener)
        findViewById<RadioButton>(R.id.setup_method_assets).setOnCheckedChangeListener(onCheckedChangeListener)
        findViewById<RadioButton>(R.id.setup_method_backup).setOnCheckedChangeListener(onCheckedChangeListener)
    }

    private fun setup() {
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