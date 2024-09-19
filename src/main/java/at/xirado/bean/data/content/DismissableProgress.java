/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
