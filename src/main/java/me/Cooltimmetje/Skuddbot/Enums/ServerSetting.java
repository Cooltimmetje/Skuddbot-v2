package me.Cooltimmetje.Skuddbot.Enums;

import lombok.Getter;
import me.Cooltimmetje.Skuddbot.Database.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Settings for servers.
 *
 * @author Tim (Cooltimmetje)
 * @version ALPHA-2.0
 * @since ALPHA-2.0
 */
@Getter
public enum ServerSetting {

    XP_MIN                  ("xp_min",                  "Minimum amount of XP gain per message on Discord.",                           ValueType.INTEGER, "10",                   Category.XP,              false), //TODO
    XP_MAX                  ("xp_max",                  "Maximum amount of XP gain per message on Discord.",                           ValueType.INTEGER, "15",                   Category.XP,              false), //TODO
    XP_MIN_TWITCH           ("xp_min_twitch",           "Minimum amount of XP gain per message on Twitch.",                            ValueType.INTEGER, "10",                   Category.XP,              false), //TODO
    XP_MAX_TWITCH           ("xp_max_twitch",           "Maximum amount of XP gain per message on Twitch.",                            ValueType.INTEGER, "15",                   Category.XP,              false), //TODO
    XP_BASE                 ("xp_base",                 "Amount of XP an user will need to level up from level 1 to level 2.",         ValueType.INTEGER, "1500",                 Category.XP,              false), //TODO
    XP_MULTIPLIER           ("xp_multiplier",           "Multiplier for XP levels. The higher this number, the steeper the XP curve.", ValueType.DOUBLE,  "1.2",                  Category.XP,              false), //TODO
    TWITCH_CHANNEL          ("twitch_channel",          "The Twitch channel this bot should keep track of stats.",                     ValueType.STRING,  "null",                 Category.TWITCH,          false), //TODO
    WELCOME_MESSAGE         ("welcome_message",         "This message will be posted when a new user joins the server.",               ValueType.STRING,  "null",                 Category.WELCOME_GOODBYE, true ), //TODO
    GOODBYE_MESSAGE         ("goodbye_message",         "This message will be posted when a user leaves the server.",                  ValueType.STRING,  "null",                 Category.WELCOME_GOODBYE, true ), //TODO
    WELCOME_GOODBYE_CHANNEL ("welcome_goodbye_channel", "This is the channel where the welcome/goodbye messages will be posted to.",   ValueType.LONG,    "null",                 Category.WELCOME_GOODBYE, false), //TODO
    ADMIN_ROLE              ("admin_role",              "This role will have access to commands that require elevated permissions.",   ValueType.STRING,  "null",                 Category.DISCORD,         true ),
    ROLE_ON_JOIN            ("role_on_join",            "This role will be granted to new users when they join the server.",           ValueType.STRING,  "null",                 Category.DISCORD,         true ), //TODO
    ARENA_NAME              ("arena_name",              "This is the name of the arena used in various minigames.",                    ValueType.STRING,  "Skuddbot's Colosseum", Category.MINIGAMES,       true ), //TODO
    COMMAND_PREFIX          ("command_prefix",          "The command prefix you can change this to avoid confilcts with other bots.",  ValueType.STRING,  "!",                    Category.COMMANDS,        false);

    private String dbReference;
    private String description;
    private ValueType type;
    private String defaultValue;
    private Category category;
    private boolean allowSpaces;

    ServerSetting(String dbReference, String description, ValueType type, String defaultValue, Category category, boolean allowSpaces){
        this.dbReference = dbReference;
        this.description = description;
        this.type = type;
        this.defaultValue = defaultValue;
        this.category = category;
        this.allowSpaces = allowSpaces;
    }

    public static ServerSetting getByDbReference(String reference){
        for(ServerSetting setting : values())
            if(setting.getDbReference().equals(reference))
                return setting;

        return null;
    }

    public static void saveToDatabase(){
        QueryExecutor qe = null;
        ResultSet rs = null;
        ArrayList<String> settings = new ArrayList<>();
        try {
            qe = new QueryExecutor(Query.SELECT_ALL_SERVER_SETTINGS);
            rs = qe.executeQuery();
            while(rs.next()){
                settings.add(rs.getString("setting_name"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            if(qe != null) qe.close();
        }
        for(ServerSetting setting : values()){
            if(settings.contains(setting.getDbReference())) continue;
            try {
                qe = new QueryExecutor(Query.INSERT_SERVER_SETTING).setString(1, setting.getDbReference());
                qe.execute();
            } catch (SQLException e){
                e.printStackTrace();
            } finally {
                if(qe != null) qe.close();
            }
        }
    }

    public enum Category {
        XP, DISCORD, TWITCH, WELCOME_GOODBYE, MINIGAMES, COMMANDS
    }

}
