package at.xirado.bean.data.content;

import at.xirado.bean.data.database.SQLBuilder;

import java.sql.SQLException;

public class DismissableContentState {

    private static final String UPDATE_SQL = """
            INSERT INTO dismissable_contents (user_id, identifier, state) values (?,?,?)
            ON DUPLICATE KEY UPDATE identifier = VALUES(identifier), state = VALUES(state)
            """;

    private final long userId;
    private final IDismissable<?> content;
    private State state;

    public DismissableContentState(long userId, IDismissable<?> content, State initialState) {
        this.userId = userId;
        this.content = content;
        this.state = initialState;
    }

    public DismissableContentState update() {
        try {
            new SQLBuilder(UPDATE_SQL, getUserId(), getContent().getIdentifier(), getState().toString()).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public IDismissable<?> getContent() {
        return content;
    }

    public State getState() {
        return state;
    }

    public DismissableContentState setState(State state) {
        this.state = state;
        return this;
    }

    public enum State {
        NONE(),
        SEEN(),
        ACKNOWLEDGED()
    }

}
