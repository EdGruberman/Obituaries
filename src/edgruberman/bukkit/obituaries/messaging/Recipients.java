package edgruberman.bukkit.obituaries.messaging;

import edgruberman.bukkit.obituaries.messaging.messages.Confirmation;

/**
 * collection of one or more message targets
 *
 * @author EdGruberman (ed@rjump.com)
 * @version 1.0.0
 */
public interface Recipients {

    /** format and send message to each target */
    public abstract Confirmation deliver(Message message);

}
