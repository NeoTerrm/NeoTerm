package com.termux

import com.termux.component.pm.PackageComponent
import com.termux.component.pm.SourceUtils
import com.termux.frontend.component.ComponentManager
import org.junit.Test
import java.io.File

/**
 * @author kiva
 */
class PackageManagerTest {
    @Test
    fun testSourceUrl() {
        val url = "http://7sp0th.iok.la:81/neoterm"
        println(SourceUtils.detectSourceFilePrefix(url))
    }

    @Test
    fun testMultilineInListFile() {
        try {
            ComponentManager.registerComponent(PackageComponent::class.java)
        } catch (ignore: Throwable) {
        }

        val pm = ComponentManager.getComponent<PackageComponent>();
        pm.reloadPackages(File("/Users/kiva/1"), false)

        System.err.println(pm.packages["rcs"]?.description)
    }
}
