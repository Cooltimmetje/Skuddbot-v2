package me.VanadeysHaven.Skuddbot.Commands.HelpCommand;

import me.VanadeysHaven.Skuddbot.Profiles.Users.Identifier;

/**
 * Interface for the CommandManager to prevent unwanted changes to it.
 *
 * @author Tim (Vanadey's Haven)
 * @version 2.0
 * @since 2.0
 */
public interface HelpGenerator {

    String getHelp(Identifier id, int amount, int offset);

    String getHelp(long userId, long serverId, int amount, int offset);

    int getCommandAmount(Identifier id);

    int getCommandAmount(long userId, long serverId);

}
