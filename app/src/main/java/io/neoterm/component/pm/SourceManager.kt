package com.termux.component.pm

import com.termux.App
import com.termux.R
import com.termux.frontend.preference.NeoPreference

/**
 * @author Sam
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
