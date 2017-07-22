package io.neoterm.frontend.client

import android.view.KeyEvent
import io.neoterm.frontend.completion.widget.CandidatePopupWindow
import io.neoterm.frontend.completion.CompletionManager
import io.neoterm.frontend.completion.listener.OnAutoCompleteListener
import io.neoterm.frontend.completion.model.CompletionResult
import io.neoterm.view.TerminalView
import java.util.*

/**
 * @author kiva
 */
class TermCompleteListener(var terminalView: TerminalView?) : OnAutoCompleteListener {

    private val inputStack = Stack<Char>()
    private var popupWindow: CandidatePopupWindow? = null

    override fun onKeyCode(keyCode: Int, keyMod: Int) {
        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                popChar()
                activateAutoCompletion()
            }

            KeyEvent.KEYCODE_ENTER -> {
                clearChars()
                popupWindow?.dismiss()
            }
        }
    }

    override fun onAutoComplete(newText: String?) {
        if (newText == null || newText.isEmpty()) {
            return
        }
        newText.toCharArray().forEach { pushChar(it) }
        activateAutoCompletion()
    }

    override fun onCleanUp() {
        popupWindow?.dismiss()
        popupWindow?.cleanup()
        popupWindow = null
        terminalView = null
    }

    override fun onFinishCompletion(): Boolean {
        val popWindow = popupWindow ?: return false

        if (popWindow.isShowing()) {
            popWindow.dismiss()
            return true
        }
        return false
    }

    private fun activateAutoCompletion() {
        val text = getCurrentEditingText()
        if (text.isEmpty()) {
            return
        }

        val result = CompletionManager.tryCompleteFor(text)
        if (!result.hasResult()) {
            // A provider accepted the task
            // But no candidates are provided
            // Give it zero angrily!
            result.markScore(0)
            return
        }
        showAutoCompleteCandidates(result)
    }

    private fun showAutoCompleteCandidates(result: CompletionResult) {
        val termView = terminalView
        var popWindow = popupWindow

        if (termView == null) {
            return
        }

        if (popWindow == null) {
            popWindow = CandidatePopupWindow(termView.context)
            this.popupWindow = popWindow
        }

        popWindow.candidates = result.candidates
        popWindow.show(termView)
    }

    private fun getCurrentEditingText(): String {
        val builder = StringBuilder()
        val size = inputStack.size
        var start = inputStack.lastIndexOf(' ')
        if (start < 0) {
            start = 0
        }

        (start..(size - 1))
                .map { inputStack[it] }
                .takeWhile { !(it == 0.toChar() || it == ' ') }
                .forEach { builder.append(it) }
        return builder.toString()
    }

    private fun clearChars() {
        inputStack.clear()
    }

    private fun popChar() {
        if (inputStack.isNotEmpty()) {
            inputStack.pop()
        }
    }

    private fun pushChar(char: Char) {
        inputStack.push(char)
    }
}