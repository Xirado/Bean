package at.xirado.bean.moderation;

import java.awt.*;

public enum CaseType
{
    WARN("Warn", Color.decode("#FFFF00"), (byte) 3),
    MUTE("Mute", Color.decode("#E24C00"), (byte) 2),
    KICK("Kick", Color.decode("#800080"), (byte) 1),
    BAN("Ban", Color.decode("#990000"), (byte) 0),
    TEMPBAN("Tempban", Color.decode("#990000"), (byte) 4),
    SOFTBAN("Softban", Color.decode("#800080"), (byte) 5);

    private final String friendlyName;
    private final Color embedColor;
    private final byte id;

    CaseType(String friendlyName, Color embedColor, byte id)
    {
        this.friendlyName = friendlyName;
        this.embedColor = embedColor;
        this.id = id;
    }

    public String getFriendlyName()
    {
        return friendlyName;
    }

    public Color getEmbedColor()
    {
        return embedColor;
    }

    public byte getId()
    {
        return id;
    }

    public static CaseType fromId(byte id)
    {
        for (CaseType caseType : values())
        {
            if (caseType.getId() == id)
                return caseType;
        }
        return null;
    }
}
