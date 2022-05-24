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

public class SQLBuilder {
    private final String sqlString;
    private final List<Object> parameters = new ArrayList<>();

    public SQLBuilder(String sqlString, Object... parameters) {
        this.sqlString = sqlString;
        this.parameters.addAll(Arrays.asList(parameters));
    }

    public SQLBuilder(String sqlString) {
        this.sqlString = sqlString;
    }

    public SQLBuilder addParameter(Object object) {
        Checks.notNull(object, "Object");
        this.parameters.add(object);
        return this;
    }

    public SQLBuilder addParameters(Object... objects) {
        Checks.notEmpty(objects, "Objects");
        this.parameters.addAll(Arrays.asList(objects));
        return this;
    }

    public ResultSet executeQuery() throws SQLException {
        Connection connection = Database.getConnection();
        try (var ps = connection.prepareStatement(sqlString)) {
            if (!parameters.isEmpty()) {
                int index = 1;
                for (Object object : parameters) {
                    ps.setObject(index, object);
                    index++;
                }
            }
            return ps.executeQuery();
        }
    }

    public boolean execute() throws SQLException {
        Connection connection = Database.getConnection();
        try (var ps = connection.prepareStatement(sqlString)) {
            if (!parameters.isEmpty()) {
                int index = 1;
                for (Object object : parameters) {
                    ps.setObject(index, object);
                    index++;
                }
            }
            return ps.execute();
        }
    }
}
