// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.concurrent.TimeUnit;

public class PlayCmd extends MusicCommand
{
    private static final String LOAD = "\ud83d\udce5";
    private static final String CANCEL = "\ud83d\udeab";
    private final String loadingEmoji;
    
    public PlayCmd(final Bot bot) {
        super(bot);
        this.loadingEmoji = bot.getConfig().getLoading();
        this.name = "play";
        this.arguments = "<title|URL|subcommand>";
        this.help = "plays the provided song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
        this.children = new Command[] { new PlaylistCmd(bot) };
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        if (!event.getArgs().isEmpty() || !event.getMessage().getAttachments().isEmpty()) {
            final String args = (event.getArgs().startsWith("<") && event.getArgs().endsWith(">")) ? event.getArgs().substring(1, event.getArgs().length() - 1) : (event.getArgs().isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : event.getArgs());
            event.reply(this.loadingEmoji + " Loading... `[" + args + "]`", m -> this.bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new ResultHandler(m, event, false)));
            return;
        }
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getPlayer().getPlayingTrack() != null && handler.getPlayer().isPaused()) {
            if (DJCommand.checkDJPermission(event)) {
                handler.getPlayer().setPaused(false);
                event.replySuccess("Resumed **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**.");
            }
            else {
                event.replyError("Only DJs can unpause the player!");
            }
            return;
        }
        final StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Play Commands:\n");
        builder.append("\n`").append(event.getClient().getPrefix()).append(this.name).append(" <song title>` - plays the first result from Youtube");
        builder.append("\n`").append(event.getClient().getPrefix()).append(this.name).append(" <URL>` - plays the provided song, playlist, or stream");
        for (final Command cmd : this.children) {
            builder.append("\n`").append(event.getClient().getPrefix()).append(this.name).append(" ").append(cmd.getName()).append(" ").append(cmd.getArguments()).append("` - ").append(cmd.getHelp());
        }
        event.reply(builder.toString());
    }
    
    private class ResultHandler implements AudioLoadResultHandler
    {
        private final Message m;
        private final CommandEvent event;
        private final boolean ytsearch;
        
        private ResultHandler(final Message m, final CommandEvent event, final boolean ytsearch) {
            this.m = m;
            this.event = event;
            this.ytsearch = ytsearch;
        }
        
        private void loadSingle(final AudioTrack track, final AudioPlaylist playlist) {
            if (PlayCmd.this.bot.getConfig().isTooLong(track)) {
                this.m.editMessage(FormatUtil.filter(this.event.getClient().getWarning() + " This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `" + FormatUtil.formatTime(track.getDuration()) + "` > `" + FormatUtil.formatTime(PlayCmd.this.bot.getConfig().getMaxSeconds() * 1000L) + "`")).queue();
                return;
            }
            final AudioHandler handler = (AudioHandler)this.event.getGuild().getAudioManager().getSendingHandler();
            final int pos = handler.addTrack(new QueuedTrack(track, this.event.getAuthor())) + 1;
            final String addMsg = FormatUtil.filter(this.event.getClient().getSuccess() + " Added **" + track.getInfo().title + "** (`" + FormatUtil.formatTime(track.getDuration()) + "`) " + ((pos == 0) ? "to begin playing" : (" to the queue at position " + pos)));
            if (playlist == null || !this.event.getSelfMember().hasPermission(this.event.getTextChannel(), Permission.MESSAGE_ADD_REACTION)) {
                this.m.editMessage(addMsg).queue();
            }
            else {
                new ButtonMenu.Builder()
                        .setText(addMsg + "\n" + this.event.getClient().getWarning() + " This track has a playlist of **" + playlist.getTracks().size() + "** tracks attached. Select " + "\ud83d\udce5" + " to load playlist.")
                        .setChoices("\ud83d\udce5", "\ud83d\udeab")
                        .setEventWaiter(PlayCmd.this.bot.getWaiter())
                        .setTimeout(30L, TimeUnit.SECONDS)
                        .setAction(re -> {
                    if (re.getName().equals("\ud83d\udce5")) {
                        this.m.editMessage("\n" + this.event.getClient().getSuccess() + " Loaded **" + this.loadPlaylist(playlist, track) + "** additional tracks!").queue();
                    }
                    else {
                        this.m.editMessage("").queue();
                    }
                }).setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    }
                    catch (PermissionException ex) {}
                }).build().display(this.m);
            }
        }
        
        private int loadPlaylist(final AudioPlaylist playlist, final AudioTrack exclude) {
            final int[] count = { 0 };
            final Object o;
            final int n;
            playlist.getTracks().stream().forEach(track -> {
                if (!PlayCmd.this.bot.getConfig().isTooLong(track) && !track.equals(exclude)) {
                    AudioHandler handler = (AudioHandler)this.event.getGuild().getAudioManager().getSendingHandler();
                    handler.addTrack(new QueuedTrack(track, this.event.getAuthor()));
                    count[0]++;
                }
            });
            return count[0]+1;
        }
        
        @Override
        public void trackLoaded(final AudioTrack track) {
            this.loadSingle(track, null);
        }
        
        @Override
        public void playlistLoaded(final AudioPlaylist playlist) {
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
                final AudioTrack single = (playlist.getSelectedTrack() == null) ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
                this.loadSingle(single, null);
            }
            else if (playlist.getSelectedTrack() != null) {
                final AudioTrack single = playlist.getSelectedTrack();
                this.loadSingle(single, playlist);
            }
            else {
                final int count = this.loadPlaylist(playlist, null);
                if (count == 0) {
                    this.m.editMessage(FormatUtil.filter(this.event.getClient().getWarning() + " All entries in this playlist " + ((playlist.getName() == null) ? "" : ("(**" + playlist.getName() + "**) ")) + "were longer than the allowed maximum (`" + PlayCmd.this.bot.getConfig().getMaxTime() + "`)")).queue();
                }
                else {
                    this.m.editMessage(FormatUtil.filter(this.event.getClient().getSuccess() + " Found " + ((playlist.getName() == null) ? "a playlist" : ("playlist **" + playlist.getName() + "**")) + " with `" + playlist.getTracks().size() + "` entries; added to the queue!" + ((count < playlist.getTracks().size()) ? ("\n" + this.event.getClient().getWarning() + " Tracks longer than the allowed maximum (`" + PlayCmd.this.bot.getConfig().getMaxTime() + "`) have been omitted.") : ""))).queue();
                }
            }
        }
        
        @Override
        public void noMatches() {
            if (this.ytsearch) {
                this.m.editMessage(FormatUtil.filter(this.event.getClient().getWarning() + " No results found for `" + this.event.getArgs() + "`.")).queue();
            }
            else {
                PlayCmd.this.bot.getPlayerManager().loadItemOrdered(this.event.getGuild(), "ytsearch:" + this.event.getArgs(), new ResultHandler(this.m, this.event, true));
            }
        }
        
        @Override
        public void loadFailed(final FriendlyException throwable) {
            if (throwable.severity == FriendlyException.Severity.COMMON) {
                this.m.editMessage(this.event.getClient().getError() + " Error loading: " + throwable.getMessage()).queue();
            }
            else {
                this.m.editMessage(this.event.getClient().getError() + " Error loading track.").queue();
            }
        }
    }
    
    public class PlaylistCmd extends MusicCommand
    {
        public PlaylistCmd(final Bot bot) {
            super(bot);
            this.name = "playlist";
            this.aliases = new String[] { "pl" };
            this.arguments = "<name>";
            this.help = "plays the provided playlist";
            this.beListening = true;
            this.bePlaying = false;
        }
        
        @Override
        public void doCommand(final CommandEvent event) {
            // 
            // This method could not be decompiled.
            // 
            // Original Bytecode:
            // 
            //     1: invokevirtual   com/jagrosh/jdautilities/command/CommandEvent.getArgs:()Ljava/lang/String;
            //     4: invokevirtual   java/lang/String.isEmpty:()Z
            //     7: ifeq            42
            //    10: aload_1         /* event */
            //    11: new             Ljava/lang/StringBuilder;
            //    14: dup            
            //    15: invokespecial   java/lang/StringBuilder.<init>:()V
            //    18: aload_1         /* event */
            //    19: invokevirtual   com/jagrosh/jdautilities/command/CommandEvent.getClient:()Lcom/jagrosh/jdautilities/command/CommandClient;
            //    22: invokeinterface com/jagrosh/jdautilities/command/CommandClient.getError:()Ljava/lang/String;
            //    27: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
            //    30: ldc             " Please include a playlist name."
            //    32: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
            //    35: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
            //    38: invokevirtual   com/jagrosh/jdautilities/command/CommandEvent.reply:(Ljava/lang/String;)V
            //    41: return         
            //    42: aload_0         /* this */
            //    43: getfield        com/jagrosh/jmusicbot/commands/music/PlayCmd$PlaylistCmd.bot:Lcom/jagrosh/jmusicbot/Bot;
            //    46: invokevirtual   com/jagrosh/jmusicbot/Bot.getPlaylistLoader:()Lcom/jagrosh/jmusicbot/playlist/PlaylistLoader;
            //    49: aload_1         /* event */
            //    50: invokevirtual   com/jagrosh/jdautilities/command/CommandEvent.getArgs:()Ljava/lang/String;
            //    53: invokevirtual   com/jagrosh/jmusicbot/playlist/PlaylistLoader.getPlaylist:(Ljava/lang/String;)Lcom/jagrosh/jmusicbot/playlist/PlaylistLoader$Playlist;
            //    56: astore_2        /* playlist */
            //    57: aload_2         /* playlist */
            //    58: ifnonnull       93
            //    61: aload_1         /* event */
            //    62: new             Ljava/lang/StringBuilder;
            //    65: dup            
            //    66: invokespecial   java/lang/StringBuilder.<init>:()V
            //    69: ldc             "I could not find `"
            //    71: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
            //    74: aload_1         /* event */
            //    75: invokevirtual   com/jagrosh/jdautilities/command/CommandEvent.getArgs:()Ljava/lang/String;
            //    78: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
            //    81: ldc             ".txt` in the Playlists folder."
            //    83: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
            //    86: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
            //    89: invokevirtual   com/jagrosh/jdautilities/command/CommandEvent.replyError:(Ljava/lang/String;)V
            //    92: return         
            //    93: aload_1         /* event */
            //    94: invokevirtual   com/jagrosh/jdautilities/command/CommandEvent.getChannel:()Lnet/dv8tion/jda/api/entities/MessageChannel;
            //    97: new             Ljava/lang/StringBuilder;
            //   100: dup            
            //   101: invokespecial   java/lang/StringBuilder.<init>:()V
            //   104: aload_0         /* this */
            //   105: getfield        com/jagrosh/jmusicbot/commands/music/PlayCmd$PlaylistCmd.this$0:Lcom/jagrosh/jmusicbot/commands/music/PlayCmd;
            //   108: invokestatic    com/jagrosh/jmusicbot/commands/music/PlayCmd.access$700:(Lcom/jagrosh/jmusicbot/commands/music/PlayCmd;)Ljava/lang/String;
            //   111: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
            //   114: ldc             " Loading playlist **"
            //   116: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
            //   119: aload_1         /* event */
            //   120: invokevirtual   com/jagrosh/jdautilities/command/CommandEvent.getArgs:()Ljava/lang/String;
            //   123: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
            //   126: ldc             "**... ("
            //   128: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
            //   131: aload_2         /* playlist */
            //   132: invokevirtual   com/jagrosh/jmusicbot/playlist/PlaylistLoader$Playlist.getItems:()Ljava/util/List;
            //   135: invokeinterface java/util/List.size:()I
            //   140: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
            //   143: ldc             " items)"
            //   145: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
            //   148: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
            //   151: invokeinterface net/dv8tion/jda/api/entities/MessageChannel.sendMessage:(Ljava/lang/CharSequence;)Lnet/dv8tion/jda/api/requests/restaction/MessageAction;
            //   156: aload_0         /* this */
            //   157: aload_1         /* event */
            //   158: aload_2         /* playlist */
            //   159: invokedynamic   BootstrapMethod #0, accept:(Lcom/jagrosh/jmusicbot/commands/music/PlayCmd$PlaylistCmd;Lcom/jagrosh/jdautilities/command/CommandEvent;Lcom/jagrosh/jmusicbot/playlist/PlaylistLoader$Playlist;)Ljava/util/function/Consumer;
            //   164: invokeinterface net/dv8tion/jda/api/requests/restaction/MessageAction.queue:(Ljava/util/function/Consumer;)V
            //   169: return         
            //    StackMapTable: 00 02 2A FC 00 32 07 00 65
            // 
            // The error that occurred was:
            // 
            // java.lang.IllegalStateException: Could not infer any expression.
            //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:374)
            //     at com.strobel.decompiler.ast.TypeAnalysis.run(TypeAnalysis.java:96)
            //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:344)
            //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
            //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
            //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
            //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
            //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
            //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
            //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
            //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
            //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:576)
            //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
            //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
            //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
            //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
            //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
            //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
            //     at com.strobel.decompiler.DecompilerDriver.decompileType(DecompilerDriver.java:330)
            //     at com.strobel.decompiler.DecompilerDriver.decompileJar(DecompilerDriver.java:251)
            //     at com.strobel.decompiler.DecompilerDriver.main(DecompilerDriver.java:126)
            // 
            throw new IllegalStateException("An error occurred while decompiling this method.");
        }
    }
}
