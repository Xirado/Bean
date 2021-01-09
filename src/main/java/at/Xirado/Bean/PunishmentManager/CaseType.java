package at.Xirado.Bean.PunishmentManager;

import java.awt.*;

public enum CaseType
{
    WARN("Warn", Color.decode("#FFFF00")), // Gelb
    MUTE("Mute", Color.decode("#E24C00")), // Orange
    KICK("Kick", Color.decode("#800080")), // Violett
    BAN("Ban", Color.decode("#990000"));   // Rot

    private final String friendlyName;
    private final Color embedColor;
    CaseType(String friendlyName, Color embedColor)
    {
        this.friendlyName = friendlyName;
        this.embedColor = embedColor;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
    public Color getEmbedColor() { return embedColor;}
}
