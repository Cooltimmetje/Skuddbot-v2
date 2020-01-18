package me.Cooltimmetje.Skuddbot.Commands;

import me.Cooltimmetje.Skuddbot.Enums.PermissionLevel;
import me.Cooltimmetje.Skuddbot.Profiles.ProfileManager;
import me.Cooltimmetje.Skuddbot.Profiles.Users.PermissionManager;
import me.Cooltimmetje.Skuddbot.Profiles.Users.SkuddUser;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.util.Iterator;

/**
 * Command to check permission levels.
 *
 * @author Tim (Cooltimmetje)
 * @version ALPHA-2.0
 * @since ALPHA-2.0
 */
public class PermissionCheckCommand extends Command {

    private static ProfileManager pm = new ProfileManager();

    public PermissionCheckCommand() {
        super(new String[]{"permcheck"}, "Command used to check what permission levels you have access to.");
    }

    @Override
    public void run(Message message, String content) { //TODO formatting
        Server server = message.getServer().orElse(null);
        assert server != null;
        SkuddUser su = pm.getUser(server.getId(), message.getAuthor().getId());
        PermissionManager permissions = su.getPermissions();

        StringBuilder sb = new StringBuilder();
        Iterator<PermissionLevel> iterator = permissions.getPermissions();
        while(iterator.hasNext())
            sb.append(iterator.next()).append(",");
        String permString = sb.toString().trim();

        message.getChannel().sendMessage(permString.substring(0, permString.length() - 1));
    }
}
