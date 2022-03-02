package at.xirado.bean.translation

import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.DataType

class I18n(val languageObject: DataObject, val name: String)
{

    fun get(query: String, vararg attributes: Attribute): String {
        val path = ArrayList(query.split("\\."))

        check(languageObject, path[0])

        if (canBeString(languageObject, path[0]))
            return format(languageObject.getString(path[0]), *attributes)

        var current = languageObject.getObject(path[0])

        for (i in 1..path.size) {
            if (current.isType(path[i], DataType.OBJECT)) {
                current = current.getObject(path[i])
                continue
            }

            check(current, path[i])

            return format(current.getString(path[i]), *attributes)
        }
        throw NullPointerException("[$name]: Key $query does not exist!")
    }

    private fun canBeString(dataObject: DataObject, key: String): Boolean {
        if (dataObject.isNull(key))
            return false

        return dataObject.isType(key, DataType.STRING)
                || dataObject.isType(key, DataType.BOOLEAN)
                || dataObject.isType(key, DataType.FLOAT)
                || dataObject.isType(key, DataType.INT)
    }

    private fun check(dataObject: DataObject, path: String) {
        if (dataObject.isNull(path))
            throw NullPointerException("[${name}]: Key $path does not exist!")

        if (languageObject.isType(path, DataType.ARRAY))
            throw IllegalArgumentException("[${name}]: Cannot access from array $path")
    }

    companion object {
        private fun format(layout: String, vararg attributes: Attribute): String {
            var output = layout

            for (attribute in attributes)
                output = output.replace("{${attribute.key}}", attribute.value)

            return output
        }
    }
}
