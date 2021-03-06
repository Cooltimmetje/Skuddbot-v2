package me.VanadeysHaven.Skuddbot.Minigames.Blackjack.Hands;

import me.VanadeysHaven.Skuddbot.Enums.Emoji;
import me.VanadeysHaven.Skuddbot.Minigames.Blackjack.Card;

import java.util.ArrayList;

/**
 * Represents a hand in Blackjack.
 *
 * @author Tim (Vanadey's Haven)
 * @version 2.2.1
 * @since 2.0
 */
public abstract class BlackjackHand {

    public static final int ONE = 1;
    public static final int TWO = 2;

    protected ArrayList<Card> oneHand;
    protected boolean useGenderNeutralCards;

    protected BlackjackHand(boolean useGenderNeutralCards){
        oneHand = new ArrayList<>();
        this.useGenderNeutralCards = useGenderNeutralCards;
    }

    public int getHandValue(int hand){
        int aces = 0;
        int value = 0;
        for(Card card : getHand(hand)){
            if(card.getRank() == Card.Rank.ACE)
                aces++;
            else
                value += card.getRank().getValue();
        }

        while(aces > 0){
            value += 11;
            if(value > 21)
                value -= 10;

            aces--;
        }

        return value;
    }

    public void addCard(int hand, Card card){
        getHand(hand).add(card);
    }

    public boolean isBlackjack(int hand){
        return getHand(hand).size() == 2 && getHandValue(hand) == 21;
    }

    public String formatHand(int hand){
        ArrayList<Card> handList = getHand(hand);
        StringBuilder sb = new StringBuilder();
        for(Card card : handList)
            sb.append(" | ").append(card.toString(useGenderNeutralCards));

        if(handList.size() == 1)
            sb.append(" | ").append(Emoji.QUESTION.getUnicode()).append(Emoji.QUESTION.getUnicode());

        return sb.substring(3);
    }

    protected ArrayList<Card> getHand(int hand){
        if(hand != 1 && hand != 2) throw new IllegalArgumentException("Hand must be either 1 or 2.");
        return oneHand;
    }

}
