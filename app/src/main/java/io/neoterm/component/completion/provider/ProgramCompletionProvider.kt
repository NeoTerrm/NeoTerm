package io.neoterm.component.completion.provider

import java.io.File

/**
 * @author kiva
 */
class ProgramCompletionProvider : FileCompletionProvider() {
  override val providerName: String
    get() = "NeoTermProvider.ProgramCompletionProvider"


  override fun generateDesc(file: File): String? {
    return if (file.canExecute()) "<Program>" else super.generateDesc(file)
  }
}