package io.neoterm.terminal.client

import android.util.Log
import android.view.KeyEvent
import io.neoterm.customize.completion.AutoCompleteManager
import io.neoterm.customize.completion.CompleteCandidate
import io.neoterm.view.AutoCompletePopupWindow
import io.neoterm.view.OnAutoCompleteListener
import io.neoterm.view.TerminalView
import java.util.*

/**
 * @author kiva
 */
class TermCompleteListener(var terminalView: TerminalView?) : OnAutoCompleteListener {

    private val inputStack = Stack<Char>()
    private val popupWindow = AutoCompletePopupWindow(terminalView!!.context)

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

        val candidates = AutoCompleteManager.filter(text)
        Log.e("NeoTerm-AC", "Completing for $text")
        candidates.forEach {
            Log.e("NeoTerm-AC", "    Candidate: ${it.completeString}")
        }
        if (candidates.isNotEmpty()) {
            showAutoCompleteCandidates(candidates)
        }
    }

    private fun showAutoCompleteCandidates(candidates: List<CompleteCandidate>) {
        popupWindow.candidates = candidates
        popupWindow.show(terminalView!!)
    }

    private fun getCurrentEditingText(): String {
        val builder = StringBuilder()
        val size = inputStack.size
        (0..(size - 1))
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