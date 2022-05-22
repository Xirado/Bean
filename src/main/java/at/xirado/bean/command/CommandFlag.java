package at.xirado.bean.command;

public enum CommandFlag {
    ALLOWED_IN_DMS(),
    DEVELOPER_ONLY(),
    AUTO_DELETE_MESSAGE(),
    DISABLED(),
    PRIVATE_COMMAND(),
    MODERATOR_ONLY(),
    MUST_BE_IN_VC(),
    DJ_ONLY(),
    MUST_BE_IN_SAME_VC(),
    REQUIRES_LAVALINK_NODE()
}
