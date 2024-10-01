package at.xirado.bean.interpolator

import at.xirado.bean.interpolator.namespace.ExpressionNamespace
import at.xirado.bean.interpolator.namespace.RootNamespace
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

private const val EXPRESSION_REGEX_STR = """(?:([\w.]+)\s*->\s*)?([\w.]+)(?:\s*\(([^)]*)\))?"""
private const val INTERPOLATION_REGEX_STR = """(?<!\\)\$\{$EXPRESSION_REGEX_STR}"""

private val EXPRESSION_REGEX = EXPRESSION_REGEX_STR.toRegex()
private val INTERPOLATION_REGEX = INTERPOLATION_REGEX_STR.toRegex()

@Single
class Interpolator : KoinComponent {
    private val namespaces = getNamespaces()

    fun interpolate(
        string: String,
        context: InterpolationContext,
    ): String {
        val arguments = context.arguments
        val allowedNamespaces = context.namespaces
        val env = context.env

        val root = RootNamespace(arguments)

        val namespaceInstances = namespaces.filter { it::class in allowedNamespaces } + root

        return string.replace(INTERPOLATION_REGEX) {
            val expression = parseExpression(it)
            expression.evaluate(env, namespaceInstances.toSet()).toString()
        }
    }

    private fun parseExpression(match: MatchResult): Expression {
        val groups = match.groups.drop(1)

        val namespace = groups[0]?.value ?: "<ROOT>"
        val identifier = groups[1]?.value
            ?: throw IllegalStateException("No identifier in passed match!")

        val arguments = groups[2]?.value

        when {
            arguments == null -> {
                return PropertyExpression(namespace, identifier)
            }
            else -> {
                val argumentsSplit = arguments.split(',').map(String::trim)
                val argumentExpressions = argumentsSplit.map {
                    val matchResult = EXPRESSION_REGEX.matchEntire(it)
                        ?: throw IllegalStateException("\"$it\": not an expression")
                    parseExpression(matchResult)
                }

                return FunctionExpression(namespace, identifier, argumentExpressions)
            }
        }
    }

    private fun getNamespaces(): Set<ExpressionNamespace> {
        return getKoin().getAll<ExpressionNamespace>().toSet()
    }
}