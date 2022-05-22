package at.xirado.bean.backend;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IPositionableChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

public class ObjectBuilder {
    public static DataObject serializeJDAGuild(Guild guild) {
        DataObject object = DataObject.empty()
                .put("name", guild.getName())
                .put("id", guild.getId())
                .put("icon", guild.getIconUrl())
                .put("owner_id", guild.getOwnerIdLong())
                .put("afk_channel_id", guild.getAfkChannel() == null ? null : guild.getAfkChannel().getIdLong())
                .put("afk_timeout", guild.getAfkTimeout().getSeconds());
        DataArray roles = DataArray.empty();
        for (Role role : guild.getRoles()) {
            DataObject roleObject = DataObject.empty()
                    .put("id", role.getIdLong())
                    .put("name", role.getName())
                    .put("color", role.getColorRaw())
                    .put("hoist", role.isHoisted())
                    .put("position", role.getPosition())
                    .put("permissions", role.getPermissionsRaw())
                    .put("managed", role.isManaged())
                    .put("mentionable", role.isMentionable());
            roles.add(roleObject);
        }
        object.put("roles", roles);
        DataArray channels = DataArray.empty();
        for (GuildChannel channel : guild.getChannels()) {
            if (!(channel instanceof IPositionableChannel positionableChannel))
                continue;
            DataObject channelObject = DataObject.empty()
                    .put("id", channel.getIdLong())
                    .put("type", channel.getType().getId())
                    .put("position", positionableChannel.getPosition())
                    .put("name", channel.getName());
            channels.add(channelObject);
        }
        object.put("channels", channels);
        return object;
    }
}
