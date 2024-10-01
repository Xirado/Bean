package at.xirado.bean.interpolator.namespace

import org.koin.core.annotation.Single

@Single
class EnvVarExpressionNamespace : ExpressionNamespace {
    override val name = "env"

    override fun property(property: String, env: Env): Any? {
        return System.getenv(property)
    }

    override fun function(function: String, env: Env, arguments: Arguments): Any? {
        throw IllegalStateException("Unsupported")
    }
}