package io.neoterm.frontend.client

import android.util.Log
import android.view.KeyEvent
import io.neoterm.BuildConfig
import io.neoterm.customize.completion.widget.CandidatePopupWindow
import io.neoterm.frontend.completion.CompletionManager
import io.neoterm.frontend.completion.listener.OnAutoCompleteListener
import io.neoterm.frontend.completion.model.CompletionCandidate
import io.neoterm.frontend.completion.model.CompletionResult
import io.neoterm.view.TerminalView
import java.util.*

/**
 * @author kiva
 */
class TermCompleteListener(var terminalView: TerminalView?) : OnAutoCompleteListener {

    private val inputStack = Stack<Char>()
    private val popupWindow = CandidatePopupWindow(terminalView!!.context)

    override fun onKeyCode(keyCode: Int, keyMod: Int) {
        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                Log.e("NeoTerm-AC", "BackSpace")
                popChar()
                activateAutoCompletion()
            }

            KeyEvent.KEYCODE_ENTER -> {
                Log.e("NeoTerm-AC", "Clear Chars")
                clearChars()
                popupWindow.dismiss()
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
        popupWindow.cleanup()
        terminalView = null
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

        if (BuildConfig.DEBUG) {
            val candidates = result.candidates
            Log.e("NeoTerm-AC", "Completing for $text")
            candidates.forEach {
                Log.e("NeoTerm-AC", "    Candidate: ${it.completeString}")
            }
        }
        showAutoCompleteCandidates(result)
    }

    private fun showAutoCompleteCandidates(result: CompletionResult) {
        popupWindow.candidates = result.candidates
        popupWindow.show(terminalView!!)
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