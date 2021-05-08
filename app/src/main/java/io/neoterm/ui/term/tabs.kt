package io.neoterm.ui.term

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import de.mrapp.android.tabswitcher.Tab
import de.mrapp.android.tabswitcher.TabSwitcher
import de.mrapp.android.tabswitcher.TabSwitcherDecorator
import io.neoterm.NeoGLView
import io.neoterm.R
import io.neoterm.component.ComponentManager
import io.neoterm.component.colorscheme.ColorSchemeComponent
import io.neoterm.component.completion.OnAutoCompleteListener
import io.neoterm.component.config.DefaultValues
import io.neoterm.component.config.NeoPreference
import io.neoterm.component.session.XSession
import io.neoterm.frontend.session.terminal.*
import io.neoterm.frontend.session.view.TerminalView
import io.neoterm.frontend.session.view.extrakey.ExtraKeysView
import io.neoterm.utils.Terminals
import org.greenrobot.eventbus.EventBus

/**
 * @author kiva
 */
open class NeoTab(title: CharSequence) : Tab(title) {
  open fun onPause() {}
  open fun onResume() {}
  open fun onStart() {}
  open fun onStop() {}
  open fun onWindowFocusChanged(hasFocus: Boolean) {}
  open fun onDestroy() {}
  open fun onConfigurationChanged(newConfig: Configuration) {}
}

class NeoTabDecorator(val context: NeoTermActivity) : TabSwitcherDecorator() {
  companion object {
    private var VIEW_TYPE_COUNT = 0
    private val VIEW_TYPE_TERM = VIEW_TYPE_COUNT++
    private val VIEW_TYPE_X = VIEW_TYPE_COUNT++
  }

  private fun setViewLayerType(view: View?) = view?.setLayerType(View.LAYER_TYPE_NONE, null)

  override fun onInflateView(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): View {
    return when (viewType) {
      VIEW_TYPE_TERM -> {
        val view = inflater.inflate(R.layout.ui_term, parent, false)
        val terminalView = view.findViewById<TerminalView>(R.id.terminal_view)
        val extraKeysView = view.findViewById<ExtraKeysView>(R.id.extra_keys)
        Terminals.setupTerminalView(terminalView)
        Terminals.setupExtraKeysView(extraKeysView)

        val colorSchemeManager = ComponentManager.getComponent<ColorSchemeComponent>()
        colorSchemeManager.applyColorScheme(
          terminalView, extraKeysView,
          colorSchemeManager.getCurrentColorScheme()
        )
        view
      }

      VIEW_TYPE_X -> {
        inflater.inflate(R.layout.ui_xorg, parent, false)
      }

      else -> {
        throw RuntimeException("Unknown view type")
      }
    }
  }

  override fun onShowTab(
    context: Context, tabSwitcher: TabSwitcher,
    view: View, tab: Tab, index: Int, viewType: Int, savedInstanceState: Bundle?
  ) {
    // TODO: Improve

    val toolbar = this@NeoTabDecorator.context.toolbar
    toolbar.title = if (tabSwitcher.isSwitcherShown) null else tab.title

    val isQuickPreview = tabSwitcher.selectedTabIndex != index

    when (viewType) {
      VIEW_TYPE_TERM -> {
        val termTab = tab as TermTab
        termTab.toolbar = toolbar
        val terminalView = findViewById<TerminalView>(R.id.terminal_view)
        if (isQuickPreview) {
          bindTerminalView(termTab, terminalView, null)
        } else {
          val extraKeysView = findViewById<ExtraKeysView>(R.id.extra_keys)
          bindTerminalView(termTab, terminalView, extraKeysView)
          terminalView.requestFocus()
        }
      }

      VIEW_TYPE_X -> {
        toolbar.visibility = View.GONE
        bindXSessionView(tab as XSessionTab)
      }
    }
  }

  private fun bindXSessionView(tab: XSessionTab) {
    val sessionData = tab.sessionData ?: return

    if (sessionData.videoLayout == null) {
      val videoLayout = findViewById<FrameLayout>(R.id.xorg_video_layout)
      sessionData.videoLayout = videoLayout
      setViewLayerType(videoLayout)
    }

    val videoLayout = sessionData.videoLayout!!

    if (sessionData.glView == null) {
      Thread {
        sessionData.client?.runOnUiThread {
          sessionData.glView = NeoGLView(sessionData.client)
          sessionData.glView?.isFocusableInTouchMode = true
          sessionData.glView?.isFocusable = true
          sessionData.glView?.requestFocus()

          setViewLayerType(sessionData.glView)
          videoLayout.addView(
            sessionData.glView,
            FrameLayout.LayoutParams(
              FrameLayout.LayoutParams.MATCH_PARENT,
              FrameLayout.LayoutParams.MATCH_PARENT
            )
          )

          sessionData.glView?.pointerIcon =
            android.view.PointerIcon.getSystemIcon(
              context,
              android.view.PointerIcon.TYPE_NULL
            )

          val r = Rect()
          videoLayout.getWindowVisibleDisplayFrame(r)
          sessionData.glView?.callNativeScreenVisibleRect(r.left, r.top, r.right, r.bottom)
          videoLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            videoLayout.getWindowVisibleDisplayFrame(r)
            val heightDiff = videoLayout.rootView.height - videoLayout.height // Take system bar into consideration
            val widthDiff = videoLayout.rootView.width - videoLayout.width // Nexus 5 has system bar at the right side
            Log.v(
              "SDL",
              "Main window visible region changed: " + r.left + ":" + r.top + ":" + r.width() + ":" + r.height()
            )
            videoLayout.postDelayed(
              {
                sessionData.glView?.callNativeScreenVisibleRect(
                  r.left + widthDiff,
                  r.top + heightDiff,
                  r.width(),
                  r.height()
                )
              },
              300
            )
            videoLayout.postDelayed(
              {
                sessionData.glView?.callNativeScreenVisibleRect(
                  r.left + widthDiff,
                  r.top + heightDiff,
                  r.width(),
                  r.height()
                )
              },
              600
            )
          }
        }
      }.start()
    }
  }

  private fun bindTerminalView(
    tab: TermTab, view: TerminalView?,
    extraKeysView: ExtraKeysView?
  ) {
    val termView = view ?: return
    val termData = tab.termData

    termData.initializeViewWith(tab, termView, extraKeysView)
    termView.setEnableWordBasedIme(termData.profile?.enableWordBasedIme ?: DefaultValues.enableWordBasedIme)
    termView.setTerminalViewClient(termData.viewClient)
    termView.attachSession(termData.termSession)

    if (NeoPreference.loadBoolean(R.string.key_general_auto_completion, false)) {
      if (termData.onAutoCompleteListener == null) {
        termData.onAutoCompleteListener = createAutoCompleteListener(termView)
      }
      termView.onAutoCompleteListener = termData.onAutoCompleteListener
    }

    if (termData.termSession != null) {
      termData.viewClient?.updateExtraKeys(termData.termSession?.title, true)
    }
  }

  private fun createAutoCompleteListener(view: TerminalView): OnAutoCompleteListener? {
    return TermCompleteListener(view)
  }

  override fun getViewTypeCount(): Int {
    return VIEW_TYPE_COUNT
  }

  override fun getViewType(tab: Tab, index: Int): Int {
    if (tab is TermTab) {
      return VIEW_TYPE_TERM
    } else if (tab is XSessionTab) {
      return VIEW_TYPE_X
    }
    return -1
  }
}

class XSessionTab(title: CharSequence) : NeoTab(title) {
  var session: XSession? = null
  val sessionData
    get() = session?.mSessionData

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (!hasFocus) {
      onPause()
    } else {
      onResume()
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    session?.updateScreenOrientation()
  }

  override fun onPause() {
    session?.onPause()
    super.onPause()
  }

  override fun onDestroy() {
    super.onDestroy()
    session?.onDestroy()
  }

  override fun onResume() {
    super.onResume()
    session?.onResume()
  }
}

class TermTab(title: CharSequence) : NeoTab(title), TermUiPresenter {
  companion object {
    val PARAMETER_SHOW_EKS = "show_eks"
  }

  var termData = TermSessionData()
  var toolbar: Toolbar? = null

  fun updateColorScheme() {
    val colorSchemeManager = ComponentManager.getComponent<ColorSchemeComponent>()
    colorSchemeManager.applyColorScheme(
      termData.termView, termData.extraKeysView,
      colorSchemeManager.getCurrentColorScheme()
    )
  }

  fun cleanup() {
    termData.cleanup()
    toolbar = null
  }

  fun onFullScreenModeChanged(fullScreen: Boolean) {
    // Window token changed, we need to recreate PopupWindow
    resetAutoCompleteStatus()
  }

  override fun requireHideIme() {
    val terminalView = termData.termView
    if (terminalView != null) {
      val imm = terminalView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      if (imm.isActive) {
        imm.hideSoftInputFromWindow(terminalView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
      }
    }
  }

  override fun requireFinishAutoCompletion(): Boolean {
    return termData.onAutoCompleteListener?.onFinishCompletion() ?: false
  }

  override fun requireToggleFullScreen() {
    EventBus.getDefault().post(ToggleFullScreenEvent())
  }

  override fun requirePaste() {
    termData.termView?.pasteFromClipboard()
  }

  override fun requireClose() {
    requireHideIme()
    EventBus.getDefault().post(TabCloseEvent(this))
  }

  override fun requireUpdateTitle(title: String?) {
    if (title != null && title.isNotEmpty()) {
      this.title = title
      EventBus.getDefault().post(TitleChangedEvent(title))
      termData.viewClient?.updateExtraKeys(title)
    }
  }

  override fun requireOnSessionFinished() {
    // do nothing
  }

  override fun requireCreateNew() {
    EventBus.getDefault().post(CreateNewSessionEvent())
  }

  override fun requireSwitchToPrevious() {
    EventBus.getDefault().post(SwitchSessionEvent(toNext = false))
  }

  override fun requireSwitchToNext() {
    EventBus.getDefault().post(SwitchSessionEvent(toNext = true))
  }

  override fun requireSwitchTo(index: Int) {
    EventBus.getDefault().post(SwitchIndexedSessionEvent(index))
  }

  fun resetAutoCompleteStatus() {
    termData.onAutoCompleteListener?.onCleanUp()
    termData.onAutoCompleteListener = null
  }

  fun resetStatus() {
    resetAutoCompleteStatus()
    termData.extraKeysView?.updateButtons()
    termData.termView?.updateSize()
    termData.termView?.onScreenUpdated()
  }
}
