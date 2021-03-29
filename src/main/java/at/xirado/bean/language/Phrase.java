package at.xirado.bean.language;

import net.dv8tion.jda.api.entities.Guild;

import java.util.Locale;

public enum Phrase
{
    HELLO_IM_BEAN("Hi, I'm Bean! Type `+help` to get started!","Hey, ich bin Bean! Nutze `+help` um loszulegen!"),
    INVALID_ARGUMENTS("Invalid arguments", "Ungültige Argumente"),
    ALIASES("Aliases", "Aliasse"),
    INSUFFICIENT_PERMISSIONS("Insufficient permissions", "Unzureichende Rechte"),
    YOU_DONT_HAVE_PERMISSION_TO_DO_THIS("You don't have permission to do this", "Du hast dafür keine Rechte"),
    MISSING_BOT_PERMISSIONS("Missing bot permissions", "Fehlende Bot-Rechte"),
    MISSING_BOT_PERMISSIONS1("Oops, it seems as i don't have the permission to do this", "Es scheint, als hätte ich dafür keine Rechte"),
    MISSING_PERMISSIONS("Missing permissions", "Fehlende Rechte"),
    DJ_PRIVILEGES_INFO("\n**Note:** To get DJ privileges, you either need to have a role called \"DJ\", have Manage Channel permissions or be alone in the channel with the bot", "**Achtung**: Um DJ-Befehle ausführen zu können, benötigst du entweder eine Rolle namens \"DJ\" oder \"Kanäle verwalten\" Rechte. (Oder du bist mit dem Bot alleine im Channel)"),
    BAN_DESCRIPTION("permanently bans an user from the server", "Bannt einen Nutzer permanent vom Server"),
    ID_MAY_NOT_BE_EMPTY("ID may not be empty", "ID darf nicht leer sein"),
    USER_DOES_NOT_EXIST("This user does not exist", "Dieser Nutzer existiert nicht"),
    AN_ERROR_OCCURED("\uD83D\uDEE0 An error occured", "\uD83D\uDEE0 Es ist ein Fehler aufgetreten"),
    YOU_CANNOT_BAN_THIS_MEMBER("You cannot ban this member", "Du kannst diesen Nutzer nicht bannen"),
    I_CANNOT_BAN_THIS_MEMBER("I cannot ban this member", "Ich kann diesen Nutzer nicht bannen"),
    YOU_CANNOT_BAN_A_MODERATOR("You cannot ban a moderator", "Du kannst einen Moderator nicht bannen"),
    CANNOT_BAN_ALREADY_BANNED("You cannot ban a user that is already banned", "Du kannst keinen Nutzer bannen, der bereits gebannt ist"),
    NO_REASON_SPECIFIED("No reason specified", "Keinen Grund angegeben"),
    YOU_HAVE_BEEN_BANNED("You have been banned from %guild%", "Du wurdest von %guild% gebannt"),
    REASON("Reason", "Grund"),
    COULD_NOT_BAN_USER("Could not ban user", "Nutzer konnte nicht gebannt werden"),
    HAS_BEEN_BANNED("%user% has been banned", "%user% wurde gebannt"),
    TARGET("Target", "Ziel");
    private final String en_US;
    private final String de_DE;
    Phrase(String en_US, String de_DE)
    {
        this.en_US = en_US;
        this.de_DE = de_DE;
    }

    public String getEnglish()
    {
        return en_US;
    }

    public String getTranslated(Guild g)
    {
        if(g.getLocale() == Locale.GERMAN || g.getLocale() == Locale.GERMANY)
        {
            return this.de_DE;
        }else
        {
            return this.en_US;
        }
    }
    public String getGerman()
    {
        return de_DE;
    }

}
