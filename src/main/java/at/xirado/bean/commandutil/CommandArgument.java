package at.xirado.bean.commandutil;

import at.xirado.bean.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandArgument
{

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandArgument.class);
	private String Command;
	private String[] args;
	public String getCommandName()
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

	public CommandArgument(String argumentString, long guildID)
	{
		if(argumentString.isBlank())
		{
			LOGGER.error("String is empty!", new IllegalArgumentException());
			return;
		}
		String[] argumentArray = argumentString.split(" +");
		this.Command = argumentArray[0].substring(Bean.instance.prefixManager.getPrefix(guildID).length());
		List<String> arguments = new ArrayList<String>(Arrays.asList(argumentArray).subList(1, argumentArray.length));
		args = new String[arguments.size()];
		arguments.toArray(args);
		
	}
}
