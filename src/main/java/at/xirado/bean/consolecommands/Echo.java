package at.xirado.bean.consolecommands;

import at.xirado.bean.logging.ColorCode;
import at.xirado.bean.objects.ConsoleCommand;

public class Echo extends ConsoleCommand
{
    public Echo()
    {
        this.invoke = "echo";
        this.description = "Prints out text on the terminal";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        if(args.length == 0){
            System.out.println(ColorCode.translateChatColor('&', "&cUsage: echo [Text]"));
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(String arg : args)
        {
            sb.append(arg).append(" ");
        }
        String text = ColorCode.translateChatColor('&', sb.toString().trim());
        System.out.println(text);

    }
}
