package at.Xirado.Bean.Misc;

import at.Xirado.Bean.Listeners.*;
import at.Xirado.Bean.Main.DiscordBot;
import ch.qos.logback.classic.Level;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class Util
{
	public static MessageEmbed SimpleEmbed(Color color, String content)
	{
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(color)
				.setFooter("Developed by Xirado")
				.setTimestamp(Instant.now())
				.setDescription(content);
		return builder.build();
	}


	/**
	 * Makes any number ordinal
	 * e.g. 2 -> 2nd
	 * @param i Number to format
	 * @return Ordinal number
	 */
	public static String ordinal(int i) {
		String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
		switch (i % 100) {
			case 11:
			case 12:
			case 13:
				return i + "th";
			default:
				return i + suffixes[i % 10];

		}
	}
	public static MessageEmbed NoPermissions(Member member)
	{
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(Color.red)
				.setAuthor(member.getUser().getAsTag(), null, member.getUser().getAvatarUrl())
				.setDescription("⛔ `You don't have permission for this command!` ⛔")
				.setTimestamp(Instant.now())
				.setFooter("Insufficient permissions");
		return builder.build();
	}


	public static String postHaste(String text, boolean raw){
		try {
			byte[] postData = text.getBytes(StandardCharsets.UTF_8);
			int postDataLength = postData.length;

			String requestURL = "https://hastebin.com/documents";
			URL url = new URL(requestURL);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", "Hastebin Java Api");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			conn.setUseCaches(false);

			String response = null;
			DataOutputStream wr;
			wr = new DataOutputStream(conn.getOutputStream());
			wr.write(postData);
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			response = reader.readLine();

			if (response.contains("\"key\"")) {
				response = response.substring(response.indexOf(":") + 2, response.length() - 2);

				String postURL = raw ? "https://hastebin.com/raw/" : "https://hastebin.com/";
				response = postURL + response;
			}

			return response;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Sends a private message to a User
	 * @param user The User
	 * @param content The Message to be sent
	 */
	public static void sendPrivateMessage(User user, Message content)
	{
		user.openPrivateChannel().queue(
				(c) ->
				{
					c.sendMessage(content).queue(null,null);
				}
		);

	}
	public static void setLoggingLevel(Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

	public static String BadWord(Member m, Guild g)
	{
		if(g.getIdLong() == 713469621532885002L)
		{
			return "Hey "+m.getAsMention()+", das darfst du nicht sagen!";
		}
		else
		{
			return "Hey "+m.getAsMention()+", you can't say that!";
		}
	}
	public static ErrorHandler ignoreAllErrors()
	{
		return new ErrorHandler().ignore(EnumSet.allOf(ErrorResponse.class));
	}
	public static void sendPrivateMessage(User user, MessageEmbed embed)
	{
		user.openPrivateChannel()
				.flatMap(privateChannel -> privateChannel.sendMessage(embed))
				.queue(null, new ErrorHandler()
						.ignore(EnumSet.allOf(ErrorResponse.class)));
	}

	public static ErrorHandler handle(TextChannel c)
	{
		return new ErrorHandler()
				.handle(
						EnumSet.allOf(ErrorResponse.class),
						(ex) -> {
							ex.printStackTrace();
							if(c != null)
								c.sendMessage("An error occured!\n```"+ex.getErrorResponse().toString()+"\n"+ex.getMeaning()+"```").queue();
						}
				);
	}


	public static void removeBan(long guildid, long userid)
	{
		try
		{
			String qry = "SELECT 1 FROM tempbanned WHERE guild = ? AND user = ?";
			PreparedStatement ps = SQL.con.prepareStatement(qry);
			ps.setLong(1, guildid);
			ps.setLong(2, userid);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				PreparedStatement ps1 = SQL.con.prepareStatement("DELETE FROM tempbanned WHERE guild = ? AND user = ?");
				ps1.setLong(1, guildid);
				ps1.setLong(2,userid);
				ps1.execute();
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void addBan(long guildid, long userid, long bantime)
	{
		try {
			String qry = "SELECT 1 FROM tempbanned WHERE guild = ? AND user = ?";
			PreparedStatement ps = SQL.con.prepareStatement(qry);
			ps.setLong(1, guildid);
			ps.setLong(2, userid);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				PreparedStatement ps1 = SQL.con.prepareStatement("DELETE FROM tempbanned WHERE guild = ? AND user = ?");
				ps1.setLong(1, guildid);
				ps1.setLong(2,userid);
				ps.execute();
			}
			PreparedStatement ps1 = SQL.con.prepareStatement("INSERT INTO tempbanned (guild,user,deadline) values (?,?,?)");
			ps1.setLong(1, guildid);
			ps1.setLong(2, userid);
			ps1.setLong(3, bantime);
			ps1.execute();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}
	
	public static void addListeners()
	{
		JDA jda = DiscordBot.instance.jda;
		jda.addEventListener(new LogListeners());
		jda.addEventListener(new GuildJoin());
		jda.addEventListener(new GuildReceiveMessage());
		jda.addEventListener(new GuildMemberJoin());
		jda.addEventListener(new GuildMessageReactionAdd());
		jda.addEventListener(new GuildMessageReactionRemove());
		jda.addEventListener(new GuildMessageDelete());
		jda.addEventListener(new PrivateMessageReceived());
	}
	
	public static String getPath()
	{
		try
		{
			String path2 = DiscordBot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String decodedPath = URLDecoder.decode(path2, "UTF-8");
			decodedPath = decodedPath.substring(0,decodedPath.lastIndexOf("/"));
			return decodedPath;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static void loadFile(String filename)
	{
		File cfgFile = new File(DiscordBot.instance.path, filename);
		if (!cfgFile.exists()) {
            try {
            	InputStream in = DiscordBot.class.getResourceAsStream("/"+filename);
                if(in != null) {
                	Files.copy(in, cfgFile.toPath());
                }else {
                	cfgFile.createNewFile();
                }
            } catch (IOException e) {
            	e.printStackTrace();
            }
		}
	}

	public static String getOption(Guild g, String SETTING)
	{
		try
		{
			String tablename = "guild_"+g.getIdLong();
			String qry = "SELECT value FROM "+tablename+" WHERE setting = ?";
			PreparedStatement ps = SQL.con.prepareStatement(qry);
			ps.setString(1, SETTING.toUpperCase());
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				return rs.getString("value");
			}
			return null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static TextChannel getLogChannel(@NotNull Guild g)
	{
		return DiscordBot.instance.logChannelManager.getLogChannel(g.getIdLong());
		
	}
	public static void UpdateOption(Guild g, String SETTING, String VALUE)
	{
		try
		{
			String tablename = "guild_"+g.getIdLong();
			String qry = "INSERT INTO "+tablename+" (setting,value) values (?,?) ON DUPLICATE KEY UPDATE value = ?";
			PreparedStatement ps = SQL.con.prepareStatement(qry);
			ps.setString(1, SETTING.toUpperCase());
			ps.setString(2, VALUE);
			ps.setString(3, VALUE);
			ps.execute();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	public static String getLengthWithDelimiter(long seconds, String delimiter)
	{
		if(seconds == -1)
		{
			return "∞";
		}
		long days = TimeUnit.SECONDS.toDays(seconds);
		long hours = TimeUnit.SECONDS.toHours(seconds) - (days *24);
		long minutes = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
		long seconds1 = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
		StringBuilder ges = new StringBuilder();
		if(days != 0) {
			if(days == 1) {
				ges.append(days).append(" day"+delimiter);
			}else {
				ges.append(days).append(" days"+delimiter);
			}
		}
		if(hours != 0) {
			if(hours == 1) {
				ges.append(hours).append(" hour"+delimiter);
			}else {
				ges.append(hours).append(" hours"+delimiter);
			}
		}
		if(minutes != 0) {
			if(minutes == 1) {
				ges.append(minutes).append(" minute"+delimiter);
			}else {
				ges.append(minutes).append(" minutes"+delimiter);
			}
		}
		if(seconds != 0) {
			if(seconds == 1) {
				ges.append(seconds1).append(" second"+delimiter);
			}else {
				ges.append(seconds1).append(" seconds"+delimiter);
			}
		}
		String length = ges.toString();
		return length.substring(0, length.length()-delimiter.length());
	}
	public static String getLength(long seconds, boolean... comma) {
		if(seconds == -1) {
			return "∞";
		}
		long Monate = 0;
		long Tage = TimeUnit.SECONDS.toDays(seconds);        
        long Stunden = TimeUnit.SECONDS.toHours(seconds) - (Tage *24);
        long Minuten = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
        long Sekunden = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
        StringBuilder ges = new StringBuilder();
        if(Tage != 0) {
        	if(Tage == 1) {
            	ges.append(Tage).append(" day, ");
        	}else {
            	ges.append(Tage).append(" days, ");
        	}
        }
        if(Stunden != 0) {
        	if(Stunden == 1) {
            	ges.append(Stunden).append(" hour, ");
        	}else {
            	ges.append(Stunden).append(" hours, ");
        	}
        }
        if(Minuten != 0) {
        	if(Minuten == 1) {
            	ges.append(Minuten).append(" minute, ");
        	}else {
            	ges.append(Minuten).append(" minutes, ");
        	}
        }
        if(Sekunden != 0) {
        	if(Sekunden == 1) {
            	ges.append(Sekunden).append(" second  ");
        	}else {
            	ges.append(Sekunden).append(" seconds  ");
        	}
        }
        String length = ges.toString();
        return length.substring(0, length.length()-2);
	}
	
	
	public static void doAsynchronously(Runnable r)
	{
		DiscordBot.instance.scheduledExecutorService.submit(r);
	}

	
}
