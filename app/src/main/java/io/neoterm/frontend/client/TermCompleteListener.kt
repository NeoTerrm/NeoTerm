package io.neoterm.frontend.client

import android.util.Log
import android.view.KeyEvent
import io.neoterm.BuildConfig
import io.neoterm.frontend.completion.CompletionManager
import io.neoterm.frontend.completion.listener.OnAutoCompleteListener
import io.neoterm.frontend.completion.listener.OnCandidateSelectedListener
import io.neoterm.frontend.completion.model.CompletionCandidate
import io.neoterm.frontend.completion.model.CompletionResult
import io.neoterm.frontend.completion.view.CandidatePopupWindow
import io.neoterm.frontend.terminal.TerminalView
import java.util.*

/**
 * @author kiva
 */
class TermCompleteListener(var terminalView: TerminalView?) : OnAutoCompleteListener, OnCandidateSelectedListener {
    private val inputStack = Stack<Char>()
    private var popupWindow: CandidatePopupWindow? = null
    private var lastCompletedIndex = 0

    override fun onKeyCode(keyCode: Int, keyMod: Int) {
        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                popChar()
                fixLastCompletedIndex()
                triggerCompletion()
            }

            KeyEvent.KEYCODE_ENTER -> {
                clearChars()
                popupWindow?.dismiss()
            }
        }
    }

    private fun fixLastCompletedIndex() {
        val currentText = getCurrentEditingText()
        lastCompletedIndex = minOf(lastCompletedIndex, currentText.length - 1)
    }

    override fun onCompletionRequired(newText: String?) {
        if (newText == null || newText.isEmpty()) {
            return
        }
        pushString(newText)
        triggerCompletion()
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

    override fun onCandidateSelected(candidate: CompletionCandidate) {
        val session = terminalView?.currentSession ?: return
        val textNeedCompletion = getCurrentEditingText().substring(lastCompletedIndex + 1)
        val newText = candidate.completeString

        val deleteLength = newText.indexOf(textNeedCompletion) + textNeedCompletion.length
        if (deleteLength > 0) {
            for (i in 0..deleteLength - 1) {
                session.write("\b")
                popChar()
            }
        }

        if (BuildConfig.DEBUG) {
            Log.e("NeoTerm-AC", "currentEditing: $textNeedCompletion, " +
                    "deleteLength: $deleteLength, completeString: $newText")
        }

        pushString(newText)
        session.write(newText)

        // Trigger next completion
        lastCompletedIndex = inputStack.size
        triggerCompletion()
    }

    private fun triggerCompletion() {
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
            onFinishCompletion()
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
            popWindow.onCandidateSelectedListener = this
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
            // Yes, it is -1, we will do `start + 1` below.
            start = -1
        }

        IntRange(start + 1, size - 1)
                .map { inputStack[it] }
                .takeWhile { !(it == 0.toChar() || it == ' ') }
                .forEach { builder.append(it) }
        return builder.toString()
    }

    private fun clearChars() {
        inputStack.clear()
        lastCompletedIndex = 0
    }

    private fun popChar() {
        if (inputStack.isNotEmpty()) {
            inputStack.pop()
        }
    }

    private fun pushString(string: String) {
        string.toCharArray().forEach { pushChar(it) }
    }

    private fun pushChar(char: Char) {
        inputStack.push(char)
    }
}