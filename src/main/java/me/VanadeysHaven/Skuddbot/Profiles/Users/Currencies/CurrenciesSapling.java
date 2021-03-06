package me.VanadeysHaven.Skuddbot.Profiles.Users.Currencies;

import me.VanadeysHaven.Skuddbot.Database.Query;
import me.VanadeysHaven.Skuddbot.Database.QueryExecutor;
import me.VanadeysHaven.Skuddbot.Database.QueryResult;
import me.VanadeysHaven.Skuddbot.Profiles.Users.Identifier;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * This class is used to provide an easy "interface" between the database and the profile system. This handles currencies only.
 *
 * @author Tim (Vanadey's Haven)
 * @version 2.1
 * @since 2.1
 */
public class CurrenciesSapling {

    private Identifier id;
    private HashMap<Currency,String> currencies;

    public CurrenciesSapling(Identifier id){
        this.id = id;
        this.currencies = new HashMap<>();
        QueryExecutor qe = null;
        try {
            qe = new QueryExecutor(Query.SELECT_CURRENCIES).setInt(1, id.getId());
            QueryResult qr = qe.executeQuery();
            while (qr.nextResult()){
                addCurrency(Currency.getByDbReference(qr.getString("currency_name")), qr.getString("currency_value"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            if(qe != null) qe.close();
        }
    }

    public void addCurrency(Currency currency, String value){
        currencies.put(currency, value);
    }

    public String getCurrency(Currency currency){
        if(currencies.containsKey(currency))
            return currencies.get(currency);

        return currency.getDefaultValue();
    }

    public CurrenciesContainer grow(){
        return new CurrenciesContainer(id, this);
    }

}
