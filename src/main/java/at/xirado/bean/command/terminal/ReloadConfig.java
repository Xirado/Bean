package at.xirado.bean.command.terminal;

import at.xirado.bean.Bean;
import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.log.Shell;

public class ReloadConfig extends ConsoleCommand {
    public ReloadConfig() {
        this.invoke = "reload";
        this.description = "Reloads the config.json file";
    }

    @Override
    public void executeCommand(String invoke, String[] args) {
        Bean.getInstance().updateConfig();
        Shell.println("Successfully reloaded config!");
    }
}
