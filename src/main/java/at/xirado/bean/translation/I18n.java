package at.xirado.bean.translation;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.internal.utils.Checks;

public class I18n
{
    private final String name;
    private final DataObject object;

    public I18n(String name, DataObject object)
    {
        this.name = name;
        this.object = object;
    }

    public String getString(String query, Attribute... attributes)
    {
        Checks.notNull(query, "Query");
        String[] querySplit = query.split("\\.");

        if (object.isType(querySplit[0], DataType.STRING))
            return format(object.getString(querySplit[0]), attributes);

        if (object.isType(querySplit[0], DataType.ARRAY))
            throw new IllegalArgumentException(querySplit[0] + " is an array! (" + getName() + ")");

        if (object.isNull(querySplit[0]))
            throw new NullPointerException(querySplit[0] + " is null! (" + getName() + ")");

        DataObject current = object.getObject(querySplit[0]);

        for (int i = 1; i < querySplit.length; i++)
        {
            if (current.isType(querySplit[i], DataType.OBJECT))
            {
                current = current.getObject(querySplit[i]);
                continue;
            }
            if (current.isType(querySplit[i], DataType.ARRAY))
                throw new IllegalArgumentException(querySplit[i] + " is an array! (" + getName() + ")");

            if (current.isNull(querySplit[i]))
                throw new NullPointerException(querySplit[i] + " is null! (" + getName() + ")");

            return format(current.getString(querySplit[i]), attributes);
        }
        return null;
    }

    private static String format(String pattern, Attribute... attributes)
    {
        String output = pattern;
        for (Attribute attribute : attributes)
        {
            output = output.replace("{" + attribute.getKey() + "}", attribute.getValue());
        }
        return output;
    }

    public String getName()
    {
        return name;
    }
}
