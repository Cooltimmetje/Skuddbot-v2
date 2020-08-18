package me.Cooltimmetje.Skuddbot.Minigames.Challenge;

import lombok.Getter;
import me.Cooltimmetje.Skuddbot.Enums.Emoji;
import me.Cooltimmetje.Skuddbot.Listeners.Reactions.Events.ReactionButtonClickedEvent;
import me.Cooltimmetje.Skuddbot.Listeners.Reactions.ReactionButton;
import me.Cooltimmetje.Skuddbot.Listeners.Reactions.ReactionUtils;
import me.Cooltimmetje.Skuddbot.Profiles.ProfileManager;
import me.Cooltimmetje.Skuddbot.Profiles.Server.ServerSetting;
import me.Cooltimmetje.Skuddbot.Profiles.ServerManager;
import me.Cooltimmetje.Skuddbot.Profiles.Users.Stats.Stat;
import me.Cooltimmetje.Skuddbot.Profiles.Users.Stats.StatsContainer;
import me.Cooltimmetje.Skuddbot.Utilities.MessagesUtils;
import me.Cooltimmetje.Skuddbot.Utilities.RNGManager;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Represents a game of challenge between 2 users.
 *
 * @author Tim (Cooltimmetje)
 * @version 2.2.1
 * @since 2.1
 */
public class ChallengeGame {

    private static final ProfileManager pm = ProfileManager.getInstance();
    private static final ServerManager sm = ServerManager.getInstance();
    private static final RNGManager random = new RNGManager();

    private static final String HEADER = Emoji.DAGGER.getUnicode() + " **CHALLENGE** | *{0}*";
    private static final String NORMAL_FORMAT = HEADER + "\n\n" +
            "**{1}** has challenged **{2}** to a fight.\n\n" +
            ">>> {3}";
    private static final String OPEN_FORMAT = HEADER + "\n\n" +
            "**{0}** has put down an open fight! Anyone can accept it!\n\n" +
            ">>> {1}";
    private static final String IN_PROGRESS_FORMAT = HEADER + "\n\n" +
            "{1}";
    private static final String PLAYING_INSTRUCTION = "Click the " + Emoji.DAGGER.getUnicode() + " reaction to accept";
    private static final int WIN_REWARD = 100;
    private static final int STREAK_BONUS = 50;

    private Server server;
    @Getter private User challengerOne;
    @Getter private User challengerTwo;
    @Getter private Message initialMessage;
    private Message gameMessage;
    TextChannel channel;
    private ArrayList<Message> messages;
    private String log;
    private ReactionButton button;

    public ChallengeGame(User challengerOne, User challengerTwo, Message message, Server server){
        this.server = server;
        this.challengerOne = challengerOne;
        this.challengerTwo = challengerTwo;

        if(isOpen()){
            initialMessage = MessagesUtils.sendPlain(message.getChannel(), MessageFormat.format(OPEN_FORMAT, challengerOne.getDisplayName(server), PLAYING_INSTRUCTION));
        } else {
            initialMessage = MessagesUtils.sendPlain(message.getChannel(), MessageFormat.format(NORMAL_FORMAT, challengerOne.getDisplayName(server) + " vs " + challengerTwo.getDisplayName(server), challengerOne.getDisplayName(server), challengerTwo.getDisplayName(server), PLAYING_INSTRUCTION));
        }

        if(isOpen()) {
            button = ReactionUtils.registerButton(initialMessage, Emoji.DAGGER, this::reactionClicked);
        } else {
            assert challengerTwo != null;
            button = ReactionUtils.registerButton(initialMessage, Emoji.DAGGER, this::reactionClicked, challengerTwo.getId());
        }
        messages = new ArrayList<>();
        messages.add(message);
        channel = message.getChannel();
        messages.add(initialMessage);
        log = "";
    }

    public ChallengeGame(User challengerOne, Message message, Server server){
        this(challengerOne, null, message, server);
    }

    public void reactionClicked(ReactionButtonClickedEvent event){
        if(event.getUser().getId() == challengerOne.getId()) {
            event.undoReaction();
            return;
        }

        if(isOpen()) setChallengerTwo(event.getUser());
        fight();
    }


    public void fight(){
        if(isOpen()) throw new IllegalStateException("Challenge is still open, must add user before fight can be started.");
        button.unregister();
        ChallengeCommand.startCooldown(this);
        TextChannel channel = initialMessage.getChannel();
        deleteMessages();

        log += "**" + challengerOne.getDisplayName(server) + "** and **" + challengerTwo.getDisplayName(server) + "** go head to head in " + sm.getServer(server.getId()).getSettings().getString(ServerSetting.ARENA_NAME)
        + "! Who will win? *3*... *2*... *1*... **FIGHT!**";

        sendMessage(channel);
        channel.type();

        int winnerInt = random.integer(1,2);
        User winner = winnerInt == 1 ? challengerOne : challengerTwo;
        User loser = winnerInt == 1 ? challengerTwo : challengerOne;
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        exec.schedule(() -> {
            log += "\n" + "A cloud of dust appears in " + sm.getServer(server.getId()).getSettings().getString(ServerSetting.ARENA_NAME) + ", when the dust settles it becomes clear that **" + winner.getDisplayName(server) + "** has won the fight!";
            log += "\n" + award(winner, loser);

            sendMessage(channel);
            ChallengeCommand.cleanUp(this);
        }, 5, TimeUnit.SECONDS);
    }

    public void deleteMessages() {
        channel.deleteMessages(messages);
        messages.clear();
    }

    private String award(User winner, User loser){
        StringBuilder sb = new StringBuilder();
        StatsContainer winnerStats = pm.getUser(server.getId(), winner.getId()).getStats();
        StatsContainer loserStats = pm.getUser(server.getId(), loser.getId()).getStats();

        loserStats.incrementInt(Stat.CHALLENGE_LOSSES);
        loserStats.setInt(Stat.CHALLENGE_WINSTREAK, 0);

        sb.append("**").append(winner.getDisplayName(server)).append(":** ").append("*+").append(WIN_REWARD).append("* <:xp_icon:458325613015466004>");

        winnerStats.incrementInt(Stat.CHALLENGE_WINS);
        winnerStats.incrementInt(Stat.CHALLENGE_WINSTREAK);

        int winStreak = winnerStats.getInt(Stat.CHALLENGE_WINSTREAK);
        int bonusXp = winStreak * STREAK_BONUS;
        winnerStats.incrementInt(Stat.EXPERIENCE, WIN_REWARD + bonusXp);
        if(winStreak == 2){
            sb.append(" | **Winstreak started:** *").append(winnerStats.getInt(Stat.CHALLENGE_WINSTREAK)).append( " wins* (+").append(bonusXp).append(" <:xp_icon:458325613015466004>)");
        } else if(winStreak > 2) {
            sb.append(" | **Winstreak continued:** *").append(winnerStats.getInt(Stat.CHALLENGE_WINSTREAK)).append( " wins* (+").append(bonusXp).append(" <:xp_icon:458325613015466004>)");
        }

        if(winStreak > winnerStats.getInt(Stat.CHALLENGE_LONGEST_WINSTREAK) && winStreak > 1){
            winnerStats.setInt(Stat.CHALLENGE_LONGEST_WINSTREAK, winStreak);
            sb.append(" | **New longest winstreak!**");
        }

        return sb.toString().trim();
    }

    private void sendMessage(TextChannel channel){
        if(gameMessage == null)
            gameMessage = MessagesUtils.sendPlain(channel, getMessage());
        else
            gameMessage.edit(getMessage());
    }

    private String getMessage(){
        return MessageFormat.format(IN_PROGRESS_FORMAT, challengerOne.getDisplayName(server) + " vs " + challengerTwo.getDisplayName(server), log);
    }

    public void setChallengerTwo(User challengerTwo){
        if(!isOpen()) throw new IllegalStateException("Challenge is not open.");
        this.challengerTwo = challengerTwo;
    }

    public boolean isMatch(User user1, User user2){
        if(user1.getId() == user2.getId()) return false;
        if(isOpen() && user2.getId() == challengerOne.getId()) return true;
        return (user2.getId() == challengerOne.getId()) && (user1.getId() == challengerTwo.getId());
    }

    public boolean isMatch(Message message, User user2){
        if(user2.getId() == challengerOne.getId()) return false;
        boolean messageMatches = message.getId() == initialMessage.getId();
        if(messageMatches && isOpen()) return true;
        return messageMatches && user2.getId() == challengerTwo.getId();
    }

    public boolean isOpen(){
        return challengerTwo == null;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public long getServerId(){
        return server.getId();
    }
}
