package at.xirado.bean.interpolator.namespace

class RootNamespace(val arguments: Map<String, Any?>) : ExpressionNamespace {
    override val name = "<ROOT>"

    override fun property(property: String, env: Env): Any? {
        if (property !in arguments)
            throw IllegalStateException("Property $property not present")

        return arguments[property]?.toString()
    }

    override fun function(function: String, env: Env, arguments: Arguments): Any? {
        throw IllegalArgumentException("No function $function() in this namespace")
    }
}