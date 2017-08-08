package io.neolang.ast.node

import io.neolang.ast.base.NeoLangBaseNode
import io.neolang.runtime.type.NeoLangArrayElement

/**
 * @author kiva
 */
class NeoLangArrayNode(val arrayNameNode: NeoLangStringNode, val elements: Array<NeoLangArrayElement>) : NeoLangBaseNode()