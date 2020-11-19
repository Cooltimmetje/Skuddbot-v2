package me.Cooltimmetje.Skuddbot.Commands;

import me.Cooltimmetje.Skuddbot.Commands.Managers.Command;
import me.Cooltimmetje.Skuddbot.Enums.Emoji;
import me.Cooltimmetje.Skuddbot.Exceptions.CooldownException;
import me.Cooltimmetje.Skuddbot.Exceptions.SettingOutOfBoundsException;
import me.Cooltimmetje.Skuddbot.Profiles.ProfileManager;
import me.Cooltimmetje.Skuddbot.Profiles.Server.ServerSetting;
import me.Cooltimmetje.Skuddbot.Profiles.Server.SkuddServer;
import me.Cooltimmetje.Skuddbot.Profiles.ServerManager;
import me.Cooltimmetje.Skuddbot.Profiles.Users.Settings.LevelUpNotification;
import me.Cooltimmetje.Skuddbot.Profiles.Users.Settings.UserSetting;
import me.Cooltimmetje.Skuddbot.Profiles.Users.SkuddUser;
import me.Cooltimmetje.Skuddbot.Utilities.MessagesUtils;
import me.Cooltimmetje.Skuddbot.Utilities.TableUtilities.TableArrayGenerator;
import me.Cooltimmetje.Skuddbot.Utilities.TableUtilities.TableDrawer;
import me.Cooltimmetje.Skuddbot.Utilities.TableUtilities.TableRow;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Used for viewing and changing usersettings
 *
 * @author Tim (Cooltimmetje)
 * @version 2.2.1
 * @since 2.0
 */
public class UserSettingsCommand extends Command {

    private static final ProfileManager pm = ProfileManager.getInstance();
    private static final ServerManager sm = ServerManager.getInstance();
    private static Logger logger = LoggerFactory.getLogger(UserSettingsCommand.class);


    public UserSettingsCommand() {
        super(new String[]{"usersettings","usettings"}, "View and change user settings with this command.");
    }

    @Override
    public void run(Message message, String content) {
        String[] args = content.split(" ");
        MessageAuthor user = message.getAuthor();
        Server server = message.getServer().orElse(null); assert server != null;
        SkuddUser su = pm.getUser(server.getId(), user.getId());
        UserSetting setting = null;
        String newValue = "";

        if(args.length >= 2){
            setting = fromString(args[1]);
            if(setting == null){
                MessagesUtils.addReaction(message, Emoji.X, "Setting " + args[1] + " doesn't exist!");
                return;
            }

            if(args.length >= 3){
                if(!setting.isAllowSpaces()){
                    newValue = args[2];
                } else {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 2; i < args.length; i++)
                        sb.append(args[i]).append(" ");

                    newValue = sb.toString().trim();
                }
            }
        }

        if(args.length == 1){
            showAll(message, user, su);
        } else if(args.length == 2){
            showDetails(message, su, setting);
        } else if(args.length >= 3){
            alterSetting(message, su, setting, newValue);
        }
    }

    private void showAll(Message message, MessageAuthor user, SkuddUser su) {
        TableArrayGenerator tag = new TableArrayGenerator();
        tag.addRow(new TableRow("Setting", "Value"));
        for(UserSetting setting : UserSetting.values()){
            tag.addRow(new TableRow(setting.toString(), su.getSettings().getString(setting)));
        }
        String table = new TableDrawer(tag.generateArray()).drawTable();
        Server server = message.getServer().orElse(null);
        assert server != null;
        SkuddServer ss = sm.getServer(server.getId());
        String commandPrefix = ss.getSettings().getString(ServerSetting.COMMAND_PREFIX).replace("_", " ");

        String msg = "User settings for **" + user.getDisplayName() + "** in **" + server.getName() + "**\n```\n" + table + "\n```\n" +
                "Type `" + commandPrefix + "usersettings <setting>` for more information about that setting." +
                "Type `" + commandPrefix + "usersettings <setting> <newValue>` to change it.";

        MessagesUtils.sendPlain(message.getChannel(), msg);
    }

    private void showDetails(Message message, SkuddUser su, UserSetting setting){
        Server server = message.getServer().orElse(null);
        assert server != null;
        SkuddServer ss = sm.getServer(server.getId());
        String commandPrefix = ss.getSettings().getString(ServerSetting.COMMAND_PREFIX).replace("_", " ");
        String msg = "```\n" +
                "Setting:       " + setting + "\n" +
                "Description:   " + setting.getDescription() + "\n" +
                "Type:          " + setting.getType() + "\n" +
                "Default value: " + setting.getDefaultValue() + "\n" +
                "Current value: " + su.getSettings().getString(setting) + "\n" +
                "```\n" + "To change the value type: `" + commandPrefix + "usersettings " + setting + " <newValue>`";

        MessagesUtils.sendPlain(message.getChannel(), msg);
    }

    private void alterSetting(Message message, SkuddUser su, UserSetting setting, String newValue){
        if(setting == UserSetting.LEVEL_UP_NOTIFY) {
            changeLevelUp(message, su, newValue);
            return;
        }
        try {
            su.getSettings().setString(setting, newValue);
            MessagesUtils.addReaction(message, Emoji.WHITE_CHECK_MARK, "Successfully updated setting `" + setting + "` to `" + newValue + "`!");
        } catch (IllegalArgumentException | SettingOutOfBoundsException e){
            MessagesUtils.addReaction(message, Emoji.X, e.getMessage());
        } catch (CooldownException e){
            MessagesUtils.addReaction(message, Emoji.HOURGLASS_FLOWING_SAND, e.getMessage());
        }
    }

    private void changeLevelUp(Message message, SkuddUser su, String newValue){
        LevelUpNotification newVal;
        try {
            newVal = LevelUpNotification.valueOf(newValue.toUpperCase().replace("-", "_"));
            su.getSettings().setLevelUpNotify(newVal);
        } catch (IllegalArgumentException e){
            MessagesUtils.addReaction(message, Emoji.X, "Unsuitable value; must be one of the following: " + Arrays.toString(LevelUpNotification.values()));
        } catch (CooldownException e){
            MessagesUtils.addReaction(message, Emoji.HOURGLASS_FLOWING_SAND, e.getMessage());
        } catch (SettingOutOfBoundsException e) {
            MessagesUtils.addReaction(message, Emoji.X, e.getMessage());
        }
    }

    private UserSetting fromString(String input){
        String enumSetting = input.toUpperCase().replace("-", "_");
        UserSetting setting = null;
        try {
            setting = UserSetting.valueOf(enumSetting);
        } catch (IllegalArgumentException ignored){
        }

        return setting;
    }

}