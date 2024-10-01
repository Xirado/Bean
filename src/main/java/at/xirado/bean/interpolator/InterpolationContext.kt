package at.xirado.bean.interpolator

import at.xirado.bean.interpolator.namespace.Env
import at.xirado.bean.interpolator.namespace.ExpressionNamespace
import kotlin.reflect.KClass

data class InterpolationContext(
    val env: Env,
    val namespaces: Set<KClass<out ExpressionNamespace>>,
    val arguments: Map<String, Any?>,
)