package com.termux.component.color

/**
 * @author Sam
 */
object DefaultColorScheme : NeoColorScheme() {
    init {
        /* NOTE: Keep in sync with assets/colors/Default.nl */
        colorName = "Default"

        foregroundColor = "#ffffff"
        backgroundColor = "#14181c"
        cursorColor = "#a9aaa9"
    }
}
