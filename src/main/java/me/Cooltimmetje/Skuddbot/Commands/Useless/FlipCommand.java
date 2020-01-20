package me.Cooltimmetje.Skuddbot.Commands.Useless;

import me.Cooltimmetje.Skuddbot.Commands.Command;
import me.Cooltimmetje.Skuddbot.Utilities.MessagesUtils;
import me.Cooltimmetje.Skuddbot.Utilities.MiscUtils;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

/**
 * (╯°□°）╯︵ n ou
 *
 * @author Tim (Cooltimmetje)
 * @version ALPHA-2.0
 * @since ALPHA-2.0
 */
public class FlipCommand extends Command {

    public FlipCommand(){
        super(new String[]{"flip"}, "(╯°□°）╯︵ n ou", Location.BOTH);
    }

    @Override
    public void run(Message message, String content) {
        Server server = message.getServer().orElse(null);
        String[] args = content.split(" ");
        if(args.length < 2) {
            MessagesUtils.sendPlain(message.getChannel(), "(╯°□°）╯︵ " + MiscUtils.flipText("WHAT DO YOU WANT TO FLIP?!"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        int currentMention = 0;
        for(int i=1; i < args.length; i++) {
            if(!message.getMentionedUsers().isEmpty() && currentMention < message.getMentionedUsers().size()) {
                User user = message.getMentionedUsers().get(currentMention);
                if (user.isYourself()) user = message.getUserAuthor().orElse(null);
                assert user != null;
                sb.append("@");
                if (server != null) {
                    sb.append(user.getDisplayName(server));
                } else {
                    sb.append(user.getName());
                }
                sb.append(" ");
                currentMention++;
            } else {
                sb.append(args[i]).append(" ");
            }
        }
        MessagesUtils.sendPlain(message.getChannel(), "(╯°□°）╯︵ " + MiscUtils.flipText(sb.toString().trim()));
    }
}
