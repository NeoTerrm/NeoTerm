package io.neoterm.component.pm

import io.neoterm.App
import io.neoterm.R
import io.neoterm.frontend.preference.NeoPreference

/**
 * @author kiva
 */
class SourceManager internal constructor() {
    val sources = mutableSetOf<String>()

    init {
        NeoPreference.loadStrings(NeoPreference.KEY_SOURCES).mapTo(sources, { it })
        if (sources.isEmpty()) {
            sources.addAll(App.get().resources.getStringArray(R.array.pref_package_source_values))
        }
    }

    fun addSource(sourceUrl: String) {
        sources.add(sourceUrl)
    }

    fun applyChanges() {
        NeoPreference.storeStrings(NeoPreference.KEY_SOURCES, sources)
    }
}
