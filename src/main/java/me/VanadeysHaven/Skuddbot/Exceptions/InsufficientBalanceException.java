package me.VanadeysHaven.Skuddbot.Exceptions;

import lombok.Getter;
import me.VanadeysHaven.Skuddbot.Profiles.Users.Currencies.Currency;

/**
 * Can be thrown whenever we encounter a user without insufficient balance of a certain currency.
 *
 * @author Tim (Vanadey's Haven)
 * @version 2.3
 * @since 2.3
 */
@Getter
public class InsufficientBalanceException extends Exception {

    private final Currency currency;
    private final int amount;

    public InsufficientBalanceException(Currency currency, int amount){
        super("You do not have enough balance of currency " + currency + ". Amount required: " + amount);
        this.currency = currency;
        this.amount = amount;
    }

}
