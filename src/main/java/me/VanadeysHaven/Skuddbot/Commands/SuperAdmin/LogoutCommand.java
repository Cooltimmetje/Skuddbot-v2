package me.VanadeysHaven.Skuddbot.Commands.SuperAdmin;

import me.VanadeysHaven.Skuddbot.Commands.Managers.Command;
import me.VanadeysHaven.Skuddbot.Enums.Emoji;
import me.VanadeysHaven.Skuddbot.Enums.PermissionLevel;
import me.VanadeysHaven.Skuddbot.Main;
import me.VanadeysHaven.Skuddbot.Utilities.MessagesUtils;
import org.javacord.api.entity.message.Message;

/**
 * Logout command
 *
 * @author Tim (Vanadey's Haven)
 * @version 2.2.1
 * @since 2.0
 */
public class LogoutCommand extends Command {

    public LogoutCommand(){
        super(new String[]{"logout"}, "Logs the bot out and saves all data.", null, PermissionLevel.TIMMY, Location.BOTH);
    }

    @Override
    public void run(Message message, String content) {
        if(!message.getMentionedUsers().isEmpty() && message.getMentionedUsers().get(0).getId() == Main.getSkuddbot().getApi().getYourself().getId()){
            MessagesUtils.addReaction(message, Emoji.WHITE_CHECK_MARK, "yes");
            Main.getSkuddbot().logout();
        } else {
            MessagesUtils.addReaction(message, Emoji.X, "no");
        }
    }
}
