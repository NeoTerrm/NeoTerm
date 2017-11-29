package io.neoterm.frontend.xorg

import android.view.View
import android.widget.FrameLayout
import io.neoterm.NeoAudioThread
import io.neoterm.NeoGLView
import io.neoterm.xorg.NeoXorgViewClient
import java.util.*

/**
 * @author kiva
 */
class XSessionData {
    var videoLayout: FrameLayout? = null
    var audioThread: NeoAudioThread? = null
    var screenKeyboard: View? = null
    var glView: NeoGLView? = null

    var isPaused = false
    var client: NeoXorgViewClient? = null

    var keyboardWithoutTextInputShown = false
    var screenKeyboardHintMessage: String? = null
    var textInput = LinkedList<Int>()
}