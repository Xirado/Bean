package at.xirado.bean.data.database;

import at.xirado.bean.misc.Util;
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLBuilder
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLBuilder.class);

    private final String sqlString;
    private final List<Object> parameters = new ArrayList<>();
    private Connection connection = null;
    private boolean closeConnection = true;

    public SQLBuilder(String sqlString, Object... parameters)
    {
        this.sqlString = sqlString;
        this.parameters.addAll(Arrays.asList(parameters));
    }

    public SQLBuilder(String sqlString)
    {
        this.sqlString = sqlString;
    }

    public SQLBuilder addParameter(Object object)
    {
        Checks.notNull(object, "Object");
        this.parameters.add(object);
        return this;
    }

    public SQLBuilder addParameters(Object... objects)
    {
        Checks.notEmpty(objects, "Objects");
        this.parameters.addAll(Arrays.asList(objects));
        return this;
    }

    public SQLBuilder useConnection(Connection connection)
    {
        this.connection = connection;
        this.closeConnection = false;
        return this;
    }

    public ResultSet executeQuery() throws SQLException
    {
        Connection connection = this.connection == null ? Database.getConnectionFromPool() : this.connection;
        if (connection == null)
            throw new SQLException("Could not acquire connection instance!");
        try (var ps = connection.prepareStatement(sqlString))
        {
            if (!parameters.isEmpty())
            {
                int index = 1;
                for (Object object : parameters)
                {
                    ps.setObject(index, object);
                    index++;
                }
            }
            return ps.executeQuery();
        }
        finally
        {
            if (closeConnection)
                Util.closeQuietly(connection);
        }
    }

    public boolean execute() throws SQLException
    {
        Connection connection = this.connection == null ? Database.getConnectionFromPool() : this.connection;
        if (connection == null)
            throw new SQLException("Could not acquire connection instance!");
        try (var ps = connection.prepareStatement(sqlString))
        {
            if (!parameters.isEmpty())
            {
                int index = 1;
                for (Object object : parameters)
                {
                    ps.setObject(index, object);
                    index++;
                }
            }
            return ps.execute();
        }
        finally
        {
            if (closeConnection)
                Util.closeQuietly(connection);
        }
    }
}
