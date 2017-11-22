package com.termux

import com.termux.component.codegen.CodeGenComponent
import com.termux.component.color.DefaultColorScheme
import com.termux.frontend.component.ComponentManager
import org.junit.Test

/**
 * @author Sam
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
