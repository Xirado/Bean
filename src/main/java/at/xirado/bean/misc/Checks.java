package at.xirado.bean.misc;

public class Checks
{

    public static void nonNull(Object[] objects)
    {
        if(objects == null) throw new IllegalArgumentException("Value may not be null!");
        for(var obj : objects)
        {
            if(obj == null) throw new IllegalArgumentException("Value may not be null!");
        }
    }

    public static void nonNull(Object obj)
    {
        if(obj == null) throw new IllegalArgumentException("Value may not be null!");
    }

}
