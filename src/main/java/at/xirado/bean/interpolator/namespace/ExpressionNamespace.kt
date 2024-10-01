package at.xirado.bean.interpolator.namespace

typealias Env = Map<String, String>
typealias Arguments = List<Any?>

interface ExpressionNamespace {
    val name: String

    fun property(property: String, env: Env): Any?
    fun function(function: String, env: Env, arguments: Arguments): Any?
}