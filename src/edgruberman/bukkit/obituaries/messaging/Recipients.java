package edgruberman.bukkit.obituaries.messaging;

import edgruberman.bukkit.obituaries.messaging.messages.Confirmation;

public interface Recipients {

    public abstract Confirmation send(Message message);

}
