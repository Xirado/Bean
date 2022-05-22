package at.xirado.bean.data.content;

import at.xirado.bean.data.database.SQLBuilder;

import java.sql.SQLException;

public class DismissableProgress {

    private static final String UPDATE_SQL = """
            INSERT INTO dismissable_contents (user_id, identifier, state) values (?,?,?)
            ON DUPLICATE KEY UPDATE identifier = VALUES(identifier), state = VALUES(state)
            """;

    private final long userId;
    private final IDismissable<?> dismissable;
    private DismissableState state;

    public DismissableProgress(long userId, IDismissable<?> dismissable, DismissableState initialState) {
        this.userId = userId;
        this.dismissable = dismissable;
        this.state = initialState;
    }

    public DismissableProgress update() {
        try {
            new SQLBuilder(UPDATE_SQL, getUserId(), getDismissable().getIdentifier(), getState().toString()).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public IDismissable<?> getDismissable() {
        return dismissable;
    }

    public DismissableState getState() {
        return state;
    }

    public DismissableProgress setState(DismissableState state) {
        this.state = state;
        return this;
    }
}
