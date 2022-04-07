package at.xirado.bean.i18n

import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.DataType

class I18n(val name: String, val data: DataObject) {

    companion object {
        private fun format(layout: String, vararg attributes: Pair<String, Any>): String {
            var output = layout
            attributes.forEach { output =  output.replace("{${it.first}}", it.second.toString()) }
            return output
        }
    }

    fun get(path: String, vararg attributes: Pair<String, Any>): String? {
        val paths = path.split(".")

        var current = data

        paths.forEachIndexed { index, subPath ->
            if (current.isNull(subPath))
                throw NullPointerException("Cannot access path \"$path\" because \"$subPath\" is null!")

            if (index + 1 == paths.size && !current.isType(subPath, DataType.STRING))
                throw IllegalArgumentException("Provided path \"$path\" is not a string!")

            if (current.isType(subPath, DataType.OBJECT)) {
                current = current.getObject(subPath)
                return@forEachIndexed
            }

            if (!current.isType(subPath, DataType.STRING))
                throw IllegalArgumentException("Provided path \"$path\" is not a string!")

            return format(current.getString(subPath), *attributes)
        }
        return null
    }
}