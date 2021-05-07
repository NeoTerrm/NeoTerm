package io.neoterm.component.completion

import io.neoterm.component.completion.provider.FileCompletionProvider
import io.neoterm.component.completion.provider.ProgramCompletionProvider
import io.neoterm.frontend.completion.CompletionManager
import io.neoterm.frontend.component.NeoComponent

/**
 * @author kiva
 */
class CompletionComponent : NeoComponent {
  override fun onServiceInit() {
    CompletionManager.registerProvider(FileCompletionProvider())
    CompletionManager.registerProvider(ProgramCompletionProvider())
  }

  override fun onServiceDestroy() {
  }

  override fun onServiceObtained() {
  }
}