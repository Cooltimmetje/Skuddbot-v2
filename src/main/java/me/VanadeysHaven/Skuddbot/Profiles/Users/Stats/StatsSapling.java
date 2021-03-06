package me.VanadeysHaven.Skuddbot.Profiles.Users.Stats;

import me.VanadeysHaven.Skuddbot.Database.Query;
import me.VanadeysHaven.Skuddbot.Database.QueryExecutor;
import me.VanadeysHaven.Skuddbot.Database.QueryResult;
import me.VanadeysHaven.Skuddbot.Profiles.Users.Identifier;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * This class is used to provide an easy "interface" between the database and the profile system. This handles stats only.
 *
 * @author Tim (Vanadey's Haven)
 * @since 2.0
 * @version 2.0
 */
public class StatsSapling {

    private Identifier id;
    private HashMap<Stat,String> stats;

    public StatsSapling(Identifier id){
        this.id = id;
        this.stats = new HashMap<>();

        QueryExecutor qe = null;
        try {
            qe = new QueryExecutor(Query.SELECT_STATS).setInt(1, id.getId());
            QueryResult qr = qe.executeQuery();
            while(qr.nextResult()){
                addStat(Stat.getByDbReference(qr.getString("stat_name")), qr.getString("stat_value"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            if(qe != null) qe.close();
        }
    }

    public void addStat(Stat stat, String value){
        stats.put(stat, value);
    }

    public String getStat(Stat stat){
        if (stats.containsKey(stat))
            return stats.get(stat);
        return null;
    }

    public StatsContainer grow(){
        return new StatsContainer(id, this);
    }

}
