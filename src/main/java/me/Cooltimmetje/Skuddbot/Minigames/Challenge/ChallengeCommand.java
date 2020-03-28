package me.Cooltimmetje.Skuddbot.Minigames.Challenge;

import me.Cooltimmetje.Skuddbot.Commands.Managers.Command;
import me.Cooltimmetje.Skuddbot.Enums.Emoji;
import me.Cooltimmetje.Skuddbot.Main;
import me.Cooltimmetje.Skuddbot.Utilities.MessagesUtils;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;

import java.util.ArrayList;

/**
 * Command for controlling challenges.
 *
 * @author Tim (Cooltimmetje)
 * @version ALPHA-2.1
 * @since ALPHA-2.1
 */
public class ChallengeCommand extends Command {

    private static ArrayList<ChallengeGameManager> managers;

    public ChallengeCommand(){
        super(new String[]{"challenge", "duel", "fight", "1v1"}, "Fight someone!", Location.SERVER);
        managers = new ArrayList<>();
    }

    @Override
    public void run(Message message, String content) {
        String[] args = content.split(" ");
        Server server = message.getServer().orElse(null); assert server != null;
        User author = message.getAuthor().asUser().orElse(null); assert author != null;
        ChallengeGameManager manager = getManager(server.getId());
        if(manager.isOnCooldown(author.getId())){
            MessagesUtils.addReaction(message, Emoji.HOURGLASS_FLOWING_SAND, "You are still wounded from the last fight! You need to wait 1 minute between fights!");
            return;
        }
        if(args.length < 2) {
            MessagesUtils.addReaction(message, Emoji.X, "Not enough arguments! - Usage: `!challenge <mention/open/cancel>`");
            return;
        }
        if(!message.getMentionedUsers().isEmpty()){
            User mentionedUser = message.getMentionedUsers().get(0);
            if(mentionedUser.getId() == Main.getSkuddbot().getApi().getYourself().getId()){
                MessagesUtils.addReaction(message, Emoji.X, "You can't challenge me!");
                return;
            }
            if(mentionedUser.getId() == message.getAuthor().getId()){
                MessagesUtils.addReaction(message, Emoji.X, "You can't challenge yourself.");
                return;
            }

            manager.process(author, message.getMentionedUsers().get(0), message);
        } else if(args[1].equalsIgnoreCase("open")) {
            if(manager.hasOutstandingGame(author)){
                MessagesUtils.addReaction(message, Emoji.X, "You have a outstanding challenge, you can cancel it with `!challenge cancel`");
            } else {
                manager.process(author, message);
            }
        } else if(args[1].equalsIgnoreCase("cancel")) {
            if(manager.hasOutstandingGame(author)) {
                manager.cancelGame(author);
                MessagesUtils.addReaction(message, Emoji.WHITE_CHECK_MARK, "Challenge cancelled.");
            } else
                MessagesUtils.addReaction(message, Emoji.X, "You have no outstanding challenge. Start one with `!challenge <mention/open>`");
        }
    }

    private static ChallengeGameManager getManager(long serverId){
        for(ChallengeGameManager manager : managers)
            if(serverId == manager.getServerId())
                return manager;

        ChallengeGameManager manager = new ChallengeGameManager(serverId);
        managers.add(manager);
        return manager;
    }

    public static void cleanUp(ChallengeGame game){
        getManager(game.getServerId()).removeGame(game);
    }

    public static void startCooldown(ChallengeGame game) {
        getManager(game.getServerId()).startCooldown(game);
    }

    public static void onReaction(ReactionAddEvent event){
        if(event.getUser().isBot()) return;
        Server server = event.getServer().orElse(null); assert server != null;
        Message message = event.getMessage().orElse(null); assert message != null;
        getManager(server.getId()).processReaction(message, event.getUser());

    }
}
