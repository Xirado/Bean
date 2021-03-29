package at.xirado.bean.commandmanager;

import at.xirado.bean.main.DiscordBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandArgument
{

	private String Command;
	private String[] args;
	public String getCommand()
	{
		return Command;
	}
	public String[] toStringArray()
	{
		return args;
	}


	public String toString(int startIndex)
	{
		String[] args = this.args;
		if(args == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for(int i = startIndex; i < args.length; i++)
		{
			sb.append(args[i]).append(" ");
		}
		return sb.toString().trim();
	}

	public CommandArgument(String whole, long guildid)
	{
		String[] a = whole.split(" +");
		if(a.length < 1)
			return;
		this.Command = a[0].substring(DiscordBot.instance.prefixManager.getPrefix(guildid).length());
		List<String> arguments = new ArrayList<String>(Arrays.asList(a).subList(1, a.length));
		args = new String[arguments.size()];
		arguments.toArray(args);
		
	}
}
