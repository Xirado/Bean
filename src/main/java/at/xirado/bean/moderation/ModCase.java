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

package at.xirado.bean.moderation;

import at.xirado.bean.Bean;
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ModCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModCase.class);

    private final UUID uuid;
    private final CaseType caseType;
    private final long guildId;
    private final long targetId;
    private final long moderatorId;
    private final long duration;
    private final long createdAt;
    private final String reason;

    private ModCase(UUID uuid, CaseType caseType, long guildId,
                    long targetId, long moderatorId, String reason,
                    long createdAt, long duration) {
        this.uuid = uuid;
        this.caseType = caseType;
        this.guildId = guildId;
        this.targetId = targetId;
        this.moderatorId = moderatorId;
        this.reason = reason;
        this.createdAt = createdAt;
        this.duration = duration;
    }

    private ModCase(UUID uuid, CaseType caseType, long guildId,
                    long targetId, long moderatorId, long createdAt,
                    String reason) {
        this(uuid, caseType, guildId, targetId, moderatorId, reason, createdAt, -1);
    }

    public static ModCase createModCase(CaseType type, long guildId, long targetId, long moderatorId, String reason) {
        return createModCase(type, guildId, targetId, moderatorId, -1, reason);
    }

    public static ModCase createModCase(CaseType type, long guildId, long targetId, long moderatorId, long duration, String reason) {
        try (Connection connection = Bean.getInstance().getDatabase().getConnectionFromPool();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO modcases (uuid, caseType, guild, user, moderator, reason, createdAt, duration) values (?,?,?,?,?,?,?,?)")) {
            UUID uuid = generateUUID(connection);
            long creationTime = System.currentTimeMillis();
            ps.setString(1, uuid.toString());
            ps.setByte(2, type.getId());
            ps.setLong(3, guildId);
            ps.setLong(4, targetId);
            ps.setLong(5, moderatorId);
            ps.setString(6, reason);
            ps.setLong(7, creationTime);
            ps.setLong(8, duration);
            ps.execute();
            return new ModCase(uuid, type, guildId, targetId, moderatorId, reason, creationTime, duration);
        } catch (Exception ex) {
            LOGGER.error("Could not create Mod-Case!", ex);
            return null;
        }
    }

    private static UUID generateUUID(@Nonnull Connection connection) {
        Checks.notNull(connection, "Connection");
        while (true) {
            UUID uuid = UUID.randomUUID();
            if (uuidAlreadyExists(connection, uuid))
                continue;
            return uuid;
        }
    }

    private static boolean uuidAlreadyExists(@Nonnull Connection connection, @Nonnull UUID uuid) {
        Checks.notNull(connection, "Connection");
        Checks.notNull(uuid, "UUID");
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM modcases WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            LOGGER.error("Could not check if UUID already exists!", ex);
            return false;
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public CaseType getCaseType() {
        return caseType;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getTargetId() {
        return targetId;
    }

    public long getModeratorId() {
        return moderatorId;
    }

    public String getReason() {
        return reason;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getDuration() {
        return duration;
    }
}
