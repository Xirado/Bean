package at.xirado.bean.handlers;

import at.xirado.bean.Bean;
import at.xirado.bean.misc.ReactionHelper;
import net.dv8tion.jda.api.entities.Role;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ReactionRoleManager
{
    private final ConcurrentHashMap<Long, HashMap<String,Long>> reactionRoles;
    public ReactionRoleManager()
    {
        this.reactionRoles = new ConcurrentHashMap<>();
    }

    public void removeAllReactionRoles(long messageID)
    {
        reactionRoles.remove(messageID);
        ReactionHelper.removeAllReactions(messageID);
    }

    public Role getRoleIfAvailable(long messageID, String emoticon)
    {
        if(this.reactionRoles.containsKey(messageID))
        {
            HashMap<String, Long> hm = reactionRoles.get(messageID);
            if(hm.containsKey(emoticon))
            {
                return Bean.instance.jda.getRoleById(hm.get(emoticon));
            }else{
                Role r = ReactionHelper.getRoleIfAvailable(messageID, emoticon);
                if(r != null)
                {
                    hm.put(emoticon, r.getIdLong());
                    this.reactionRoles.replace(messageID, hm);
                }
                return r;
            }

        }
        Role r = ReactionHelper.getRoleIfAvailable(messageID,emoticon);
        if(r != null)
        {
            HashMap<String, Long> hm = new HashMap<>();
            hm.put(emoticon, r.getIdLong());
            this.reactionRoles.put(messageID,hm);
        }
        return r;
    }
}
