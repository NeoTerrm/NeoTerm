package io.neoterm.component.config

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.system.ErrnoException
import android.system.Os
import android.util.TypedValue
import io.neolang.frontend.ConfigVisitor
import io.neolang.frontend.NeoLangParser
import io.neoterm.App
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.component.NeoComponent
import io.neoterm.services.NeoTermService
import io.neoterm.utils.NLog
import java.io.File
import java.nio.file.Files

class ConfigureComponent : NeoComponent {
  override fun onServiceInit() {
  }

  override fun onServiceDestroy() {
  }

  override fun onServiceObtained() {
  }

  fun getLoaderVersion(): Int {
    return CONFIG_LOADER_VERSION
  }

  fun newLoader(configFile: File): IConfigureLoader {
    return when (configFile.extension) {
      "nl" -> NeoLangConfigureLoader(configFile)
      else -> OldConfigureLoader(configFile)
    }
  }

  companion object {
    private const val CONFIG_LOADER_VERSION = 20
  }
}

open class NeoConfigureFile(val configureFile: File) {
  private val configParser = NeoLangParser()
  protected open var configVisitor: ConfigVisitor? = null

  fun getVisitor() = configVisitor ?: throw IllegalStateException("Configure file not loaded or parse failed.")

  open fun parseConfigure() = kotlin.runCatching {
    val programCode = String(Files.readAllBytes(configureFile.toPath()))
    configParser.setInputSource(programCode)

    val ast = configParser.parse()
    val astVisitor = ast.visit().getVisitor(ConfigVisitor::class.java) ?: return false
    astVisitor.start()
    configVisitor = astVisitor.getCallback()
  }.isSuccess
}

object NeoPreference {
  const val KEY_HAPPY_EGG = "neoterm_fun_happy"
  const val KEY_FONT_SIZE = "neoterm_general_font_size"
  const val KEY_CURRENT_SESSION = "neoterm_service_current_session"
  const val KEY_SYSTEM_SHELL = "neoterm_core_system_shell"
  const val KEY_SOURCES = "neoterm_package_enabled_sources"

  const val VALUE_HAPPY_EGG_TRIGGER = 8

  var MIN_FONT_SIZE: Int = 0
    private set
  var MAX_FONT_SIZE: Int = 0
    private set

  private var preference: SharedPreferences? = null

  fun init(context: Context) {
    preference = PreferenceManager.getDefaultSharedPreferences(context)

    // This is a bit arbitrary and sub-optimal. We want to give a sensible default for minimum font size
    // to prevent invisible text due to zoom be mistake:
    val dipInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics)
    MIN_FONT_SIZE = (4f * dipInPixels).toInt()
    MAX_FONT_SIZE = 256

    // load apt source
    val sourceFile = File(NeoTermPath.SOURCE_FILE)
    kotlin.runCatching {
      Files.readAllBytes(sourceFile.toPath())?.let {
        val source = String(it).trim().trimEnd()
        val array = source.split(" ")
        if (array.size >= 2 && array[0] == "deb") {
          store(R.string.key_package_source, array[1])
        }
      }
    }
  }

  fun store(key: Int, value: Any) {
    store(App.get().getString(key), value)
  }

  fun store(key: String, value: Any) {
    when (value) {
      is Int -> preference!!.edit().putInt(key, value).apply()
      is String -> preference!!.edit().putString(key, value).apply()
      is Boolean -> preference!!.edit().putBoolean(key, value).apply()
    }
  }

  fun loadInt(key: Int, defaultValue: Int): Int {
    return loadInt(App.get().getString(key), defaultValue)
  }

  fun loadString(key: Int, defaultValue: String?): String {
    return loadString(App.get().getString(key), defaultValue)
  }

  fun loadBoolean(key: Int, defaultValue: Boolean): Boolean {
    return loadBoolean(App.get().getString(key), defaultValue)
  }

  fun loadInt(key: String?, defaultValue: Int): Int {
    return preference!!.getInt(key, defaultValue)
  }

  fun loadString(key: String?, defaultValue: String?): String {
    return preference!!.getString(key, defaultValue)
  }

  fun loadBoolean(key: String?, defaultValue: Boolean): Boolean {
    return preference!!.getBoolean(key, defaultValue)
  }

  fun storeCurrentSession(session: TerminalSession) {
    preference!!.edit()
      .putString(KEY_CURRENT_SESSION, session.mHandle)
      .apply()
  }

  fun getCurrentSession(termService: NeoTermService?): TerminalSession? {
    val sessionHandle = PreferenceManager.getDefaultSharedPreferences(termService!!)
      .getString(KEY_CURRENT_SESSION, "")

    return termService.sessions
      .singleOrNull { it.mHandle == sessionHandle }
  }

  fun setLoginShellName(loginProgramName: String?): Boolean {
    if (loginProgramName == null) {
      return false
    }

    val loginProgramPath = findLoginProgram(loginProgramName) ?: return false

    store(R.string.key_general_shell, loginProgramName)
    symlinkLoginShell(loginProgramPath)
    return true
  }

  fun getLoginShellName(): String {
    return loadString(R.string.key_general_shell, DefaultValues.loginShell)
  }

  fun getLoginShellPath(): String {
    val loginProgramName = getLoginShellName()

    // Some programs like ssh needs it
    val shell = File(NeoTermPath.NEOTERM_LOGIN_SHELL_PATH)
    val loginProgramPath = findLoginProgram(loginProgramName) ?: {
      setLoginShellName(DefaultValues.loginShell)
      "${NeoTermPath.USR_PATH}/bin/${DefaultValues.loginShell}"
    }()

    if (!shell.exists()) {
      symlinkLoginShell(loginProgramPath)
    }

    return loginProgramPath
  }

  fun validateFontSize(fontSize: Int): Int {
    return Math.max(MIN_FONT_SIZE, Math.min(fontSize, MAX_FONT_SIZE))
  }

  private fun symlinkLoginShell(loginProgramPath: String) {
    File(NeoTermPath.CUSTOM_PATH).mkdirs()
    try {
      val shellSymlink = File(NeoTermPath.NEOTERM_LOGIN_SHELL_PATH)
      if (shellSymlink.exists()) {
        shellSymlink.delete()
      }
      Os.symlink(loginProgramPath, NeoTermPath.NEOTERM_LOGIN_SHELL_PATH)
      Os.chmod(NeoTermPath.NEOTERM_LOGIN_SHELL_PATH, 448 /* Decimal of 0700 */)
    } catch (e: ErrnoException) {
      NLog.e("Preference", "Failed to symlink login shell: ${e.localizedMessage}")
      e.printStackTrace()
    }
  }

  fun findLoginProgram(loginProgramName: String): String? {
    val file = File("${NeoTermPath.USR_PATH}/bin", loginProgramName)
    return if (file.canExecute()) file.absolutePath else null
  }

  fun getFontSize(): Int {
    return loadInt(
      KEY_FONT_SIZE,
      DefaultValues.fontSize
    )
  }

  fun getInitialCommand(): String {
    return loadString(
      R.string.key_general_initial_command,
      DefaultValues.initialCommand
    )
  }

  fun isBellEnabled(): Boolean {
    return loadBoolean(
      R.string.key_general_bell,
      DefaultValues.enableBell
    )
  }

  fun isVibrateEnabled(): Boolean {
    return loadBoolean(
      R.string.key_general_vibrate,
      DefaultValues.enableVibrate
    )
  }

  fun isExecveWrapperEnabled(): Boolean {
    return loadBoolean(
      R.string.key_general_use_execve_wrapper,
      DefaultValues.enableExecveWrapper
    )
  }

  fun isSpecialVolumeKeysEnabled(): Boolean {
    return loadBoolean(
      R.string.key_general_volume_as_control,
      DefaultValues.enableSpecialVolumeKeys
    )
  }

  fun isAutoCompletionEnabled(): Boolean {
    return loadBoolean(
      R.string.key_general_auto_completion,
      DefaultValues.enableAutoCompletion
    )
  }

  fun isBackButtonBeMappedToEscapeEnabled(): Boolean {
    return loadBoolean(
      R.string.key_generaL_backspace_map_to_esc,
      DefaultValues.enableBackButtonBeMappedToEscape
    )
  }

  fun isExtraKeysEnabled(): Boolean {
    return loadBoolean(
      R.string.key_ui_eks_enabled,
      DefaultValues.enableExtraKeys
    )
  }

  fun isExplicitExtraKeysWeightEnabled(): Boolean {
    return loadBoolean(
      R.string.key_ui_eks_weight_explicit,
      DefaultValues.enableExplicitExtraKeysWeight
    )
  }

  fun isFullScreenEnabled(): Boolean {
    return loadBoolean(
      R.string.key_ui_fullscreen,
      DefaultValues.enableFullScreen
    )
  }

  fun isHideToolbarEnabled(): Boolean {
    return loadBoolean(
      R.string.key_ui_hide_toolbar,
      DefaultValues.enableAutoHideToolbar
    )
  }

  fun isNextTabEnabled(): Boolean {
    return loadBoolean(
      R.string.key_ui_next_tab_anim,
      DefaultValues.enableSwitchNextTab
    )
  }

  fun isWordBasedImeEnabled(): Boolean {
    return loadBoolean(
      R.string.key_general_enable_word_based_ime,
      DefaultValues.enableWordBasedIme
    )
  }

  /**
   * TODO
   * To print the job name about to be executed in bash:
   * $ trap 'echo -ne "\e]0;${BASH_COMMAND%% *}\x07"' DEBUG
   * $ PS1='$(echo -ne "\e]0;$PWD\x07")\$ '
   */
}
