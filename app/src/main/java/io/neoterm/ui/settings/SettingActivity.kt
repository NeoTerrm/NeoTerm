package io.neoterm.ui.settings

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatPreferenceActivity
import android.view.MenuItem
import io.neoterm.R

/**
 * @author Lody
 */
class SettingActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar.title = getString(R.string.settings)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        addPreferencesFromResource(R.xml.settings_main)
        findPreference(getString(R.string.about)).setOnPreferenceClickListener {
            AlertDialog.Builder(this@SettingActivity)
                    .setTitle("为什么我们选择开发NeoTerm?")
                    .setMessage("安卓上的终端，一直以来就有这样的诟病：" +
                            "只能用安卓自带的程序，要想用用户体验更好的终端程序，" +
                            "你需要额外配置一堆烦琐的环境，" +
                            "甚至需要自己动手编译符合安卓设备的版本。" +
                            "就在这时，Termux出现了，" +
                            "很完美，一行命令就能安装一个原来想都不敢想的软件，" +
                            "比如MySQL, clang。\n" +
                            "但用着用着，感觉这样的终端还差点什么，" +
                            "仅仅有丰富的软件包是不够的，" +
                            "Termux在部分功能上可以说是欠妥甚至缺乏，" +
                            "再者安卓并非自带键盘，" +
                            "在小小的屏幕上触摸虚拟键盘来跟命令行打交道实属残忍，" +
                            "但我们又没法强迫所有用户在使用终端的时候再额外接一个键盘，" +
                            "我们只能从终端的层面来解决问题，" +
                            "于是我们开发了这款app，" +
                            "并取名Neo Term(Neo正是new的意思)，" +
                            "并希望它可以改善用户在终端下的体验，" +
                            "让软件包与终端功能两不误。" +
                            "不可否认，开发过程中Termux给了我们很大帮助，" +
                            "不仅提供了很好的terminal-view，" +
                            "还提供了庞大的软件包仓库，" +
                            "这一点我们对原作者表示忠诚的感谢。" +
                            "日后的开发中，我们会紧跟用户反馈，" +
                            "努力打造安卓上最好用的终端。\n" +
                            "\n" +
                            "NeoTerm 开发者们\n" +
                            "2017.6.19 00:16")
                    .setPositiveButton(android.R.string.yes, null)
                    .show()
            return@setOnPreferenceClickListener true
        }
    }

    override fun onBuildHeaders(target: MutableList<Header>?) {
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home ->
                finish()
        }
        return super.onOptionsItemSelected(item)
    }
}