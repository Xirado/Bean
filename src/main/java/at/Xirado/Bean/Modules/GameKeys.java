package at.Xirado.Bean.Modules;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import at.Xirado.Bean.Main.DiscordBot;
import com.iwebpp.crypto.TweetNaclFast;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameKeys extends Command
{
    public GameKeys(JDA jda)
    {
        super(jda);
        this.invoke = "magiccommand";
        this.global = false;
        this.neededPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.enabledGuilds = new Long[]{713469621532885002L};
        this.commandType = CommandType.EXCLUDED;
    }

    @Override
    public void execute(CommandEvent e)
    {
        try
        {
            if(!e.getMember().hasPermission(Permission.ADMINISTRATOR) || e.getMember().getIdLong() != 184654964122058752L)
            {
                System.out.println("No Permission!");
                return;
            }
            if(e.getArguments().getArguments().length != 1) return;
            File file = new File("gamekeys.txt");
            if(!file.exists())
            {
                System.out.println("File does not exist!");
                return;
            }
            Scanner reader = new Scanner(file);
            ArrayList<String> keys = new ArrayList<>();
            HashMap<Long,String> sentSuccessfully = new HashMap<>();
            while (reader.hasNext()){
                keys.add(reader.next());
            }
            if(keys.size() < 100)
            {
                System.out.println("Found only "+keys.size()+" keys! Aborting");
                return;
            }
            System.out.println("Found "+keys.size()+" keys!");
            TextChannel announcementChannel = e.getGuild().getTextChannelById(715639112861745173L);
            if(announcementChannel == null)
            {
                System.out.println("Channel does not exist!");
                return;
            }
            announcementChannel.retrieveMessageById(788722235052523570L).queue(
                    (message) ->
                    {
                        message.retrieveReactionUsers(e.getGuild().getEmoteById(717356844112281630L)).queue(
                                (response) ->
                                {
                                    List<User> users = new ArrayList<>(response);
                                    Collections.shuffle(users);
                                    AtomicInteger sentIndex = new AtomicInteger(0);
                                    for(User user : users)
                                    {
                                        if(sentIndex.get() == 100) break;
                                        String key = keys.get(sentIndex.get());
                                        String toSend = "__***Gratulation!***__\n" +
                                                "\n" +
                                                "Du hast einen der 100 Codes für die Closed Beta von **M.A.R.S. Mercenary Assault & Recon Squad** gewonnen!\n" +
                                                "\n" +
                                                "Hier der Code: **"+key+"**\n" +
                                                "\n" +
                                                "**Jetzt musst du nur noch https://subagames.com/MARS/Home.aspx besuchen, dir ein Konto erstellen, auf Download klicken, deinen Code eingeben und bis 20:00 Uhr warten**.\n" +
                                                "\n" +
                                                "**Außerdem hast du hier noch den Link zum Trailer: https://www.youtube.com/watch?v=wKbQFWEX3Zw**\n" +
                                                "\n" +
                                                "__***Viel Spaß!***__";
                                        try
                                        {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException interruptedException)
                                        {
                                            interruptedException.printStackTrace();
                                        }
                                        if(e.getArguments().getAsString(0).equalsIgnoreCase("DoNowThereIsNoReturn"))
                                        {
                                            user.openPrivateChannel().queue(
                                                    privateChannel -> {
                                                        privateChannel.sendMessage(toSend).queue(
                                                                (success) ->
                                                                {
                                                                    sentIndex.getAndIncrement();
                                                                    System.out.println("Sent code to "+user.getAsTag()+" successfully! "+key);
                                                                    sentSuccessfully.put(user.getIdLong(), key);
                                                                    if(sentIndex.get() == 100)
                                                                    {
                                                                        System.out.println("Done! Sent "+sentIndex+" keys!");
                                                                        StringBuilder sb = new StringBuilder();
                                                                        for(Map.Entry<Long, String> entry : sentSuccessfully.entrySet())
                                                                        {
                                                                            Long userID = entry.getKey();
                                                                            String key1 = entry.getValue();
                                                                            sb.append(DiscordBot.instance.jda.getUserById(userID).getAsTag()).append(" : ").append(key1).append("\n");
                                                                        }
                                                                        System.out.println(sb.toString());

                                                                    }
                                                                },
                                                                (error) ->
                                                                {
                                                                    System.out.println(user.getAsTag()+" has their DMs disabled! Sent "+sentIndex.get()+" keys so far!");
                                                                }
                                                        );

                                                    },
                                                    (error) ->
                                                    {

                                                    }
                                            );
                                        }else
                                        {
                                            user.openPrivateChannel().queue(
                                                    privateChannel -> {
                                                        System.out.println(user.getAsTag()+": "+key);
                                                        sentIndex.getAndIncrement();
                                                        sentSuccessfully.put(user.getIdLong(), key);
                                                        if(sentIndex.get() == 100)
                                                        {
                                                            System.out.println("Done! Sent "+sentIndex+" keys!");
                                                            StringBuilder sb = new StringBuilder();
                                                            for(Map.Entry<Long, String> entry : sentSuccessfully.entrySet())
                                                            {
                                                                Long userID = entry.getKey();
                                                                String key1 = entry.getValue();
                                                                sb.append(DiscordBot.instance.jda.getUserById(userID).getAsTag()).append(" : ").append(key1).append("\n");
                                                            }
                                                            System.out.println(sb.toString());

                                                        }
                                                    }
                                            );
                                        }


                                    }
                                },
                                (error) ->
                                {

                                }
                        );
                    },
                    (error) ->
                    {

                    }
            );
        }catch (Exception ignored)
        {
            ignored.printStackTrace();
        }
    }
}
