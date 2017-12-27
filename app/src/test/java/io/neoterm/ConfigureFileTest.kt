package io.neoterm

import io.neoterm.component.colorscheme.ColorSchemeComponent
import io.neoterm.component.colorscheme.NeoColorScheme
import io.neoterm.component.extrakey.NeoExtraKey
import io.neoterm.component.font.FontComponent
import io.neoterm.component.profile.NeoProfile
import io.neoterm.component.profile.ProfileComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.session.shell.ShellProfile
import org.junit.Test
import java.io.File

/**
 * @author kiva
 */
class ConfigureFileTest {
    @Test
    fun colorConfigureTest() {
        try {
            TestInitializer.init()
        } catch (ignore: Throwable) {
        }

        val color = NeoColorScheme()
        if (color.testLoadConfigure(File("NeoLang/example/color-scheme.nl"))) {
            println("colorName:    ${color.colorName}")
            println("colorVersion: ${color.colorVersion}")
            println("background:   ${color.backgroundColor}")
            println("foreground:   ${color.foregroundColor}")
            println("color1:       ${color.color[1]}")
            println("color2:       ${color.color[2]}")
        }
    }

    @Test
    fun extraKeyConfigureTest() {
        try {
            TestInitializer.init()
        } catch (ignore: Throwable) {
        }

        val extraKey = NeoExtraKey()
        if (extraKey.testLoadConfigure(File("app/src/main/assets/eks/vim.nl"))) {
            println("programs:     ${extraKey.programNames}")
            println("version:      ${extraKey.version}")
            println("with-default: ${extraKey.withDefaultKeys}")
            println("keys:         ${extraKey.shortcutKeys}")
        }
    }

    @Test
    fun profileConfigureTest() {
        try {
            TestInitializer.init()
        } catch (ignore: Throwable) {
        }

        val profile = ShellProfile()
        profile.testLoadConfigure(File("NeoLang/example/profile.nl"))

        println(profile.profileMetaName)
        println(profile.profileName)
    }

    @Test
    fun configureFileTest() {
        colorConfigureTest()
        extraKeyConfigureTest()
        profileConfigureTest()
    }
}