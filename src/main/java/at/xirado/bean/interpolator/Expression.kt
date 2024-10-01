package at.xirado.bean.interpolator

import at.xirado.bean.interpolator.namespace.Env
import at.xirado.bean.interpolator.namespace.ExpressionNamespace

sealed interface Expression {
    val namespace: String

    fun evaluate(env: Env, namespaces: Set<ExpressionNamespace>): Any? {
        val expressionNamespace = namespaces.find { it.name == namespace }
            ?: throw IllegalStateException("No such namespace: $namespace")

        return when (this) {
            is PropertyExpression -> expressionNamespace.property(property, env)
            is FunctionExpression -> {
                val functionArguments = arguments.map {
                    it.evaluate(env, namespaces)
                }

                expressionNamespace.function(function, env, functionArguments)
            }
        }
    }
}

data class PropertyExpression(
    override val namespace: String,
    val property: String,
) : Expression

data class FunctionExpression(
    override val namespace: String,
    val function: String,
    val arguments: List<Expression>,
) : Expression