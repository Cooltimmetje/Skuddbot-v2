package me.VanadeysHaven.Skuddbot.Commands;

import me.VanadeysHaven.Skuddbot.Commands.Managers.Command;
import me.VanadeysHaven.Skuddbot.Enums.Emoji;
import me.VanadeysHaven.Skuddbot.Utilities.MessagesUtils;
import org.javacord.api.entity.message.Message;

/**
 * This class is a simple ping command.
 *
 * @author Tim (Vanadey's Haven)
 * @since 2.2.1
 * @version 2.0
 */
public class PingCommand extends Command {

    public PingCommand(){
        super(new String[] {"ping"}, "Ping command for testing bot responses.", null, Location.BOTH);
    }

    @Override
    public void run(Message message, String messageContent) {
        if(dm.isDonator(message.getAuthor().getId())){
            String text = dm.getUser(message.getAuthor().getId()).getPingMessage();
            if(text != null){
                MessagesUtils.sendEmoji(message.getChannel(), Emoji.WHITE_CHECK_MARK, text);
                return;
            }
        }

        MessagesUtils.addReaction(message, Emoji.WHITE_CHECK_MARK, "PONG!");
    }

}
