package me.Cooltimmetje.Skuddbot.Profiles.Users.Stats;

import me.Cooltimmetje.Skuddbot.Database.QueryExecutor;
import me.Cooltimmetje.Skuddbot.Enums.Query;
import me.Cooltimmetje.Skuddbot.Enums.Stat;
import me.Cooltimmetje.Skuddbot.Enums.ValueType;
import me.Cooltimmetje.Skuddbot.Profiles.Users.Identifier;
import me.Cooltimmetje.Skuddbot.Utilities.MiscUtils;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * This class holds the stats for users.
 *
 * @author Tim (Cooltimmetje)
 * @since ALPHA-2.0
 * @version ALPHA-2.0
 */
public class StatsContainer {

    private Identifier id;
    private HashMap<Stat,String> stats;

    public StatsContainer(Identifier id, StatsSapling sapling){
        this.id = id;
        this.stats = new HashMap<>();
        processStatsSapling(sapling);
    }

    private void processStatsSapling(StatsSapling sapling){
        for(Stat stat : Stat.values()){
            String value = sapling.getStat(stat);
            if(value != null) {
                setString(stat, value, false);
            } else {
                setString(stat, stat.getDefaultValue(), false);
            }
        }
    }

    public void setString(Stat stat, String value){
        setString(stat, value, true);
    }

    public void setString(Stat stat, String value, boolean save){
        if(!checkType(value, stat)) throw new IllegalArgumentException("Value " + value + " is unsuitable for stat " + stat + "; not of type " + stat.getType());
        this.stats.put(stat, value);
        if(save) save(stat);
    }

    public String getString(Stat stat){
        return this.stats.get(stat);
    }

    public void setInt(Stat stat, int value){
        setString(stat, value+"");
    }

    public int getInt(Stat stat){
        if(stat.getType() != ValueType.INTEGER) throw new IllegalArgumentException("Stat is not of type INTEGER");
        return Integer.parseInt(getString(stat));
    }

    public void incrementInt(Stat stat){
        if(stat.getType() != ValueType.INTEGER) throw new IllegalArgumentException("Stat is not of type INTEGER");
        incrementInt(stat, 1);
    }

    public void incrementInt(Stat stat, int incrementBy) {
        if(stat.getType() != ValueType.INTEGER) throw new IllegalArgumentException("Stat is not of type INTEGER");
        setInt(stat, getInt(stat) + incrementBy);
    }

    public String getFavouriteTeammate(){
        //TODO
        return "hi";
    }

    private void save(Stat stat){
        QueryExecutor qe = null;
        if (getString(stat).equals(stat.getDefaultValue())) {
            try {
                qe = new QueryExecutor(Query.DELETE_STAT_VALUE).setInt(1, id.getId()).setString(2, stat.getDbReference());
                qe.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (qe != null) qe.close();
            }
        } else {
            try {
                qe = new QueryExecutor(Query.UPDATE_STAT_VALUE).setString(1, stat.getDbReference()).setInt(2, id.getId()).setString(3, getString(stat)).and(4);
                qe.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (qe != null) qe.close();
            }
        }
    }

    public void save(){
        for(Stat stat : Stat.values()){
            save(stat);
        }
    }

    private boolean checkType(String input, Stat stat){
        ValueType type = stat.getType();
        if(type == ValueType.INTEGER){
            return MiscUtils.isInt(input);
        }
        if(type == ValueType.JSON){
            //TODO
            return input.equals("{}");
        }

        return type == ValueType.STRING;
    }
}