package c.invariantAST

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import java.util.*
import kotlinx.serialization.Serializable

@Serializable
abstract class AbstractNode {
    protected open val abstractNodeList: List<AbstractNode>
        get() = emptyList()

    protected open val nodeInfo: Any
        get() {
            val className = javaClass.simpleName
            return className.replaceFirst("[A-Z][a-z]*".toRegex(), "").lowercase(Locale.getDefault())
        }

    protected open val nodeLabel: String
        get() = nodeInfo.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this.javaClass != other.javaClass) return false
        val that = other as AbstractNode
        return this.nodeInfo == that.nodeInfo && this.abstractNodeList == that.abstractNodeList
    }

    override fun hashCode(): Int {
        return Objects.hash(nodeInfo, abstractNodeList)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        buildString(sb)
        return sb.toString()
    }

    protected open fun canHaveEmptyChildList(): Boolean {
        return false
    }

    private fun buildString(sb: StringBuilder) {
        sb.append(nodeLabel)
        val childIterator = abstractNodeList.iterator()
        if (childIterator.hasNext()) {
            sb.append("(")
            childIterator.next().buildString(sb)
            while (childIterator.hasNext()) {
                sb.append(", ")
                val next = childIterator.next()
                next.buildString(sb)
            }
            sb.append(")")
        } else if (canHaveEmptyChildList()) sb.append("()")
    }

    companion object {
        @JvmStatic
        protected fun dataNode(data: Any, quote: String = ""): AbstractNode {
            return object : AbstractNode() {
                override val nodeInfo: Any
                    get() = data

                override val nodeLabel: String
                    get() = quote + data + quote
            }
        }

        protected fun dataNode(data: Char): AbstractNode {
            return dataNode(data, "'")
        }

        @JvmStatic
        protected fun dataNode(data: String): AbstractNode {
            return dataNode(data, "\"")
        }

        protected fun dataNode(value: AbstractNode?): AbstractNode {
            throw IllegalArgumentException("This is almost certainly a bug!")
        }

        protected fun listNode(list: List<AbstractNode>, prefix: String): AbstractNode {
            return object : AbstractNode() {
                override val abstractNodeList: List<AbstractNode>
                    get() = list

                override val nodeInfo: Any
                    get() = prefix

                override fun canHaveEmptyChildList(): Boolean {
                    return true
                }
            }
        }

        protected fun concat(list1: List<AbstractNode>, list2: List<AbstractNode>): List<AbstractNode> {
            return Lists.newArrayList(Iterables.concat(list1, list2))
        }

        protected fun cons(node: AbstractNode, list: List<AbstractNode>): List<AbstractNode> {
            return concat(listOf(node), list)
        }

        protected fun snoc(list: List<AbstractNode>, node: AbstractNode): List<AbstractNode> {
            return concat(list, listOf(node))
        }
    }
}

