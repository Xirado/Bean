package at.xirado.bean.util

import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation

inline fun <reified T : Annotation> Any.findFunctionWithAnnotation(): KFunction<*>? = this::class
    .declaredMemberFunctions
    .find { it.hasAnnotation<T>() }