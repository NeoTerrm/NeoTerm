package io.neoterm

import io.neoterm.component.codegen.CodeGenComponent
import io.neoterm.component.color.DefaultColorScheme
import io.neoterm.frontend.component.ComponentManager
import org.junit.Test

/**
 * @author kiva
 */
class CodeGenTest {
    @Test
    fun testCodeGen() {
        try {
            TestInitializer.init()
        } catch (ignore: Throwable) {
        }

        val codeGenComponent = ComponentManager.getComponent<CodeGenComponent>()
        val colorScheme = DefaultColorScheme
        val generator = codeGenComponent.newGenerator(colorScheme)

        println("Generating using " + generator.getGeneratorName())
        println("Result: ")
        println(generator.generateCode(colorScheme))
    }
}