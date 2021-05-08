package io.neoterm.component.completion

import java.io.File

interface ICandidateProvider {
  val providerName: String
  fun provideCandidates(text: String): List<CompletionCandidate>
  fun canComplete(text: String): Boolean
}

open class FileCompletionProvider : ICandidateProvider {
  override val providerName: String
    get() = "NeoTermProvider.FileCompletionProvider"

  override fun provideCandidates(text: String): List<CompletionCandidate> {
    var file = File(text)
    var filter: ((File) -> Boolean)? = null

    if (!file.isDirectory) {
      val partName = file.name
      file = file.parentFile
      filter = { pathname -> pathname.name.startsWith(partName) }
    }

    return generateCandidateList(file, filter)
  }

  override fun canComplete(text: String): Boolean {
    return text.startsWith(File.separatorChar) || text.startsWith("\\./")
  }

  private fun listDirectory(path: File, filter: ((File) -> Boolean)?): Array<File> {
    return if (filter != null) path.listFiles(filter) else path.listFiles()
  }

  private fun generateCandidateList(file: File, filter: ((File) -> Boolean)?) =
    if (file.canRead()) listDirectory(file, filter).map {
      val candidate = CompletionCandidate(it.name)
      candidate.description = generateDesc(it)
      candidate.displayName = generateDisplayName(it)
      candidate
    }.toList()
    else listOf()

  open fun generateDisplayName(file: File): String {
    return if (file.isDirectory) "${file.name}/" else file.name
  }

  open fun generateDesc(file: File): String? {
    return null
  }
}

class ProgramCompletionProvider : FileCompletionProvider() {
  override val providerName: String
    get() = "NeoTermProvider.ProgramCompletionProvider"


  override fun generateDesc(file: File): String? {
    return if (file.canExecute()) "<Program>" else super.generateDesc(file)
  }
}
