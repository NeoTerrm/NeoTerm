package io.neoterm.frontend.xorg

import io.neoterm.NeoAudioThread
import io.neoterm.NeoGLView
import io.neoterm.xorg.NeoXorgViewClient

/**
 * @author kiva
 */
class XSessionData {
    var audioThread: NeoAudioThread? = null
    var glView: NeoGLView? = null
    var client: NeoXorgViewClient? = null
}