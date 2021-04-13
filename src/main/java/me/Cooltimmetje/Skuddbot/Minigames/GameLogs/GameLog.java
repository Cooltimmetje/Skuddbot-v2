package me.Cooltimmetje.Skuddbot.Minigames.GameLogs;

import me.Cooltimmetje.Skuddbot.Enums.Emoji;
import me.Cooltimmetje.Skuddbot.Main;
import me.Cooltimmetje.Skuddbot.Profiles.GlobalSettings.GlobalSetting;
import me.Cooltimmetje.Skuddbot.Profiles.GlobalSettings.GlobalSettingsContainer;
import me.Cooltimmetje.Skuddbot.Utilities.TimeUtils;
import org.javacord.api.entity.message.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A game log is a more verbose log of what has happened in a minigame, and is sent to Discord via a upload of a text file.
 *
 * @author Tim (Vanadey's Haven)
 * @version 2.3.1
 * @since 2.3.1
 */
public abstract class GameLog {

    private String fileName;
    private String header;
    private File file;

    public GameLog(String fileName, String header){
        this.fileName = fileName;
        this.header = header;
    }

    public void sendLog(Message message, Emoji emoji){
        sendLog(message, emoji,1800000);
    }

    public void sendLog(Message message, Emoji emoji, long expireAfter){
        saveFile();
        new GameLogSender(file, message, emoji, expireAfter);
    }

    public abstract String formatLog();

    public String formatFooter(){
        GlobalSettingsContainer gsc = Main.getSkuddbot().getGlobalSettings();

        return "Generated by: Skuddbot v" + gsc.getString(GlobalSetting.VERSION) + " | " + gsc.getString(GlobalSetting.BRANCH) + " > " + gsc.getString(GlobalSetting.COMMIT) +
                " - Generated on: " + TimeUtils.formatTime(System.currentTimeMillis()) + " (UTC)";
    }

    @Override
    public String toString() {
        return new NameFormatter().format(header + "\n\n" + formatLog() + "\n\n" + formatFooter());
    }

    private void saveFile(){
        if(file != null) throw new IllegalStateException("File has already been created.");
        String path = "GameLogs" + File.separator + fileName + ".txt";
        file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
