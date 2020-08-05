package me.Cooltimmetje.Skuddbot.Listeners.Reactions;

import me.Cooltimmetje.Skuddbot.Listeners.Reactions.Events.ReactionButtonClickedEvent;

/**
 * Event runnable for reaction buttons
 *
 * @author Tim (Cooltimmetje)
 * @version ALPHA-2.2.1
 * @since ALPHA-2.1.1
 */
public interface ReactionButtonClickedCallback {

    void run(ReactionButtonClickedEvent event);

}