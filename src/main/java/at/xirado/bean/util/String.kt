package at.xirado.bean.util

import java.security.MessageDigest
import java.util.*

fun String.snakeCaseToCamelCase(): String {
    return split("_")
        .mapIndexed { index, word ->
            if (index == 0) word else word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }.joinToString("")
}

fun String.snakeCase() = map {
    if (it.isUpperCase())
        "_${it.lowercase()}"
    else
        it
}.joinToString(separator = "")

fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(toByteArray(Charsets.UTF_8))
    return hashBytes.joinToString("") { "%02x".format(it) }
}
