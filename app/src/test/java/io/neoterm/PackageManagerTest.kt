package io.neoterm

import io.neoterm.component.pm.PackageComponent
import io.neoterm.component.pm.SourceHelper
import io.neoterm.frontend.component.ComponentManager
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * @author kiva
 */
class PackageManagerTest {
    @Test
    fun testSourceUrl() {
        val url = "http://7sp0th.iok.la:81/neoterm"
        println(SourceHelper.detectSourceFilePrefix(url))
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

    @Test
    fun testReplaceAll() {
        assertEquals("/root/boom.sh".replace("/", "_"), "_root_boom.sh")
    }
}