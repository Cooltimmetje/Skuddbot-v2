package me.VanadeysHaven.Skuddbot.Listeners.Reactions;

import lombok.Getter;
import me.VanadeysHaven.Skuddbot.Enums.Emoji;
import me.VanadeysHaven.Skuddbot.Utilities.MessagesUtils;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

/**
 * A reaction on a message a user can click for more information.
 *
 * @author Tim (Vanadey's Haven)
 * @version 2.2.1
 * @since 2.0
 */
public class DebugReaction {

    private String output;
    @Getter private Emoji emoji;
    @Getter private Message message;
    private TextChannel channel;
    @Getter private long validUntil;
    @Getter private boolean posted;
    @Getter private boolean ignoreUser;

    public DebugReaction(String output, Emoji emoji, Message message, TextChannel channel, long validUntil, boolean ignoreUser){
        this.output = output;
        this.emoji = emoji;
        this.message = message;
        this.channel = channel;
        this.validUntil = validUntil;
        posted = false;
        this.ignoreUser = ignoreUser;
    }

    public void post(){
        if(!posted && System.currentTimeMillis() < validUntil) {
            MessagesUtils.sendEmoji(channel, emoji, output);
            posted = true;
        }
    }


}
