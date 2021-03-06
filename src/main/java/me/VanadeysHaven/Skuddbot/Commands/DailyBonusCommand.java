package me.VanadeysHaven.Skuddbot.Commands;

import lombok.Getter;
import me.VanadeysHaven.Skuddbot.Commands.Managers.Command;
import me.VanadeysHaven.Skuddbot.Enums.Emoji;
import me.VanadeysHaven.Skuddbot.Profiles.Server.ServerSetting;
import me.VanadeysHaven.Skuddbot.Profiles.Server.ServerSettingsContainer;
import me.VanadeysHaven.Skuddbot.Profiles.Server.SkuddServer;
import me.VanadeysHaven.Skuddbot.Profiles.Users.Currencies.CurrenciesContainer;
import me.VanadeysHaven.Skuddbot.Profiles.Users.Currencies.Currency;
import me.VanadeysHaven.Skuddbot.Profiles.Users.Settings.UserSetting;
import me.VanadeysHaven.Skuddbot.Profiles.Users.Settings.UserSettingsContainer;
import me.VanadeysHaven.Skuddbot.Profiles.Users.SkuddUser;
import me.VanadeysHaven.Skuddbot.Profiles.Users.Stats.Stat;
import me.VanadeysHaven.Skuddbot.Profiles.Users.Stats.StatsContainer;
import me.VanadeysHaven.Skuddbot.Utilities.MessagesUtils;
import me.VanadeysHaven.Skuddbot.Utilities.TimeUtils;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Command used to claim daily bonuses.
 *
 * @author Tim (Vanadey's Haven)
 * @version 2.3.13
 * @since 2.1.1
 */
public class DailyBonusCommand extends Command {

    private static final long MILLIS_IN_DAY = 86400000;
    private static final long MILLIS_IN_HOUR = 3600000;
    private static final int PENALTY = 5;

    @Getter
    private enum Bonus {

        PRIDE_WED1 (2, 6, 10000, 20000,  Emoji.GAY_FLAG.getUnicode() + " *HAPPY PRIDE MONTH! (+1 pride flag)*"),
        PRIDE_SAT1 (5, 6, 10000, 20000,  Emoji.GENDERFLUID_FLAG.getUnicode() + " *HAPPY PRIDE MONTH! (+1 pride flag)*"),
        PRIDE_WED2 (9, 6, 10000, 20000,  Emoji.LESBIAN_FLAG.getUnicode() + " *HAPPY PRIDE MONTH! (+1 pride flag)*"),
        PRIDE_SAT2 (12, 6, 10000, 20000, Emoji.TRANS_FLAG.getUnicode() + " *HAPPY PRIDE MONTH! (+1 pride flag)*"),
        PRIDE_WED3 (16, 6, 10000, 20000, Emoji.BI_FLAG.getUnicode() + " *HAPPY PRIDE MONTH! (+1 pride flag)*"),
        PRIDE_SAT3 (19, 6, 10000, 20000, Emoji.NONBINARY_FLAG.getUnicode() + " *HAPPY PRIDE MONTH! (+1 pride flag)*"),
        PRIDE_WED4 (23, 6, 10000, 20000, Emoji.ACE_FLAG.getUnicode() + " *HAPPY PRIDE MONTH! (+1 pride flag)*"),
        PRIDE_SAT4 (26, 6, 10000, 20000, Emoji.QUEER_FLAG.getUnicode() + " *HAPPY PRIDE MONTH! (+1 pride flag)*"),
        PRIDE_WED5 (30, 6, 10000, 20000, Emoji.PRIDE_FLAG.getUnicode() + " *HAPPY PRIDE MONTH!*");

        int day;
        int month;
        int currencyBonus;
        int xpBonus;
        String message;

        Bonus(int day, int month, int currencyBonus, int xpBonus, String message){
            this.day = day;
            this.month = month;
            this.currencyBonus = currencyBonus;
            this.xpBonus = xpBonus;
            this.message = message;
        }

        public static Bonus getForDay(int day, int month){
            for(Bonus b : values())
                if(day == b.getDay() && month == b.getMonth())
                    return b;

            return null;
        }

    }

    public static String MESSAGE_FORMAT = Emoji.GIFT.getUnicode() + " **DAILY BONUS** | *{0}*\n\n" +
            "Daily bonus claimed: {1}\n" +

            "+{2} Skuddbux\n" +
            "+{3} <:xp_icon:458325613015466004>\n" +
            "{4}";

    public DailyBonusCommand() {
        super(new String[]{"dailybonus", "claim", "db"}, "Claim your daily bonus.", "https://wiki.skuddbot.xyz/systems/daily-bonus");
    }

    @Override
    public void run(Message message, String content) {

        Server server = message.getServer().orElse(null); assert server != null;
        SkuddUser user = pm.getUser(server.getId(), message.getAuthor().getId());
        SkuddServer sServer = sm.getServer(server.getId());
        ServerSettingsContainer settings = sServer.getSettings();
        UserSettingsContainer uSettings = user.getSettings();
        CurrenciesContainer currencies = user.getCurrencies();
        StatsContainer stats = user.getStats();

        long currentTime = System.currentTimeMillis() + (MILLIS_IN_HOUR * uSettings.getInt(UserSetting.TIMEZONE));

        if(!canClaim(user, currentTime)){
            MessagesUtils.addReaction(message, Emoji.X, "You can't claim your daily bonus yet. - You can claim it again in " + formatTimeUntilNextClaim(currentTime));
            return;
        }

        int lastClaimStreak = -1;
        long daysMissed = 0;
        if(hasDaysMissed(user, currentTime)) {
            daysMissed = getDaysMissed(user, currentTime);
            lastClaimStreak = stats.getInt(Stat.DAILY_CURRENT_STREAK);

            int penalty = (int) Math.min(lastClaimStreak - 1, PENALTY * daysMissed);
            stats.incrementInt(Stat.DAILY_CURRENT_STREAK, penalty * -1);
        } else {
            stats.incrementInt(Stat.DAILY_CURRENT_STREAK);
        }

        int currentStreak = stats.getInt(Stat.DAILY_CURRENT_STREAK);
        int longestStreak = stats.getInt(Stat.DAILY_LONGEST_STREAK);

        boolean newLongest = false;
        if(currentStreak > longestStreak){
            newLongest = true;
            stats.setInt(Stat.DAILY_LONGEST_STREAK, currentStreak);
        }

        int currencyBonusBase = settings.getInt(ServerSetting.DAILY_CURRENCY_BONUS);
        int xpBonusBase = settings.getInt(ServerSetting.DAILY_XP_BONUS);
        int cap = settings.getInt(ServerSetting.DAILY_BONUS_CAP);
        double multiplier = Math.pow(settings.getDouble(ServerSetting.DAILY_BONUS_MULTIPLIER), Math.min(currentStreak, cap + 1) - 1);
        int currencyBonus = (int) (currencyBonusBase * multiplier);
        int xpBonus = (int) (xpBonusBase * multiplier);
        boolean applyWeekly = currentStreak > cap && (currentStreak - cap) % 7 == 0;
        boolean isEligibleForWeekly = stats.getInt(Stat.DAILY_DAYS_SINCE_WEEKLY) >= 7;

        if(applyWeekly && isEligibleForWeekly){
            xpBonus *= 2;
            currencyBonus *= 2;
            stats.setInt(Stat.DAILY_DAYS_SINCE_WEEKLY, 0);
        }
        Bonus b = Bonus.getForDay(getDay(currentTime), getMonth(currentTime));
        if(b != null) {
            xpBonus += b.getXpBonus();
            currencyBonus += b.getCurrencyBonus();

            if(getDay(currentTime) == 30){
                int flags = currencies.getInt(Currency.PRIDE_FLAGS);
                xpBonus *= Math.max(1, flags);
                currencyBonus *= Math.max(1, flags);
            } else {
                currencies.incrementInt(Currency.PRIDE_FLAGS);
            }
        }

        user.getCurrencies().incrementInt(Currency.SKUDDBUX, currencyBonus);
        stats.incrementInt(Stat.EXPERIENCE, xpBonus);

        String streakString = "";
        if (currentStreak == 1 && lastClaimStreak > 1)
            streakString = "**Claim streak lost:** *" + lastClaimStreak + " days* | " +
                    "**Days missed:** *" + daysMissed + " " + (daysMissed == 1 ? "day" : "days") + "*";
        else if(daysMissed >= 1 && lastClaimStreak > 1)
            streakString = "**Claim streak reduced:** *" + currentStreak + " days* (-" + (daysMissed * PENALTY) + " days) | " +
                    "**Days missed:** *" + daysMissed + " " + (daysMissed == 1 ? "day" : "days") + "*";
        else if (currentStreak == 2)
            streakString = "**Claim streak started:** *" + currentStreak + " days*";
        else if (currentStreak > 2)
            streakString = "**Claim streak continued:** *" + currentStreak + " days*";

        if(newLongest && currentStreak >= 2)
            streakString += " | **New longest streak!**";

        String bonusStr = applyWeekly ? (isEligibleForWeekly ? "\n**WEEKLY BONUS APPLIED:** *rewards doubled*" : "\n**WEEKLY BONUS NOT APPLIED:** *not eligible*") : "";
        if(b != null) {
            bonusStr += "\n**SEASONAL BONUS APPLIED:** " + b.getMessage();
            if(getDay(currentTime) == 30){
                bonusStr += " (" + currencies.getString(Currency.PRIDE_FLAGS) + " pride flags applied!)";
            }
        }

        String msg = MessageFormat.format(MESSAGE_FORMAT, message.getAuthor().getDisplayName(), bonusStr, currencyBonus, xpBonus, streakString);

        MessagesUtils.sendPlain(message.getChannel(), msg);
        stats.setLong(Stat.DAILY_LAST_CLAIM, getCurrentDay(currentTime));
        stats.incrementInt(Stat.DAILY_DAYS_SINCE_WEEKLY);
    }

    private int getDay(long currentTime){
        Date current = new Date(currentTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);

        return cal.get(Calendar.DAY_OF_MONTH);
    }

    private int getMonth(long currentTime){
        Date current = new Date(currentTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);

        return cal.get(Calendar.MONTH) + 1;
    }

    private long getCurrentDay(long currentTime){
        long currentWholeDayMillis = currentTime - (currentTime % MILLIS_IN_DAY);
        return currentWholeDayMillis / MILLIS_IN_DAY;
    }

    private boolean canClaim(SkuddUser su, long currentTime){
        if(su.getStats().getLong(Stat.DAILY_LAST_CLAIM) == -1) return true;
        return getCurrentDay(currentTime) > su.getStats().getLong(Stat.DAILY_LAST_CLAIM);
    }

    private long getDaysMissed(SkuddUser su, long currentTime){
        long claimDay = su.getStats().getLong(Stat.DAILY_LAST_CLAIM);
        long currentDay = getCurrentDay(currentTime);

        if(claimDay == -1) return 0;
        return (currentDay - claimDay) - 1;
    }

    private boolean hasDaysMissed(SkuddUser su, long currentTime){
        return getDaysMissed(su, currentTime) > 0;
    }

    private long getTimeUntilNextClaim(long currentTime){
        long nextDayStartsAt = currentTime + MILLIS_IN_DAY;
        nextDayStartsAt = nextDayStartsAt - (nextDayStartsAt % MILLIS_IN_DAY);

        return nextDayStartsAt - currentTime;
    }

    private String formatTimeUntilNextClaim(long currentTime){
        return TimeUtils.formatTimeRemaining(getTimeUntilNextClaim(currentTime));
    }

}
