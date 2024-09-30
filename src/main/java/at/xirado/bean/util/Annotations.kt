package at.xirado.bean.util

import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation

inline fun <reified T : Annotation> Any.findFunctionWithAnnotation(): Pair<T, KFunction<*>>? = this::class
    .declaredMemberFunctions.firstNotNullOfOrNull { it.findAnnotation<T>()?.to(it) }