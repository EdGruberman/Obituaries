package edgruberman.bukkit.obituaries.messaging.recipients;

import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.obituaries.messaging.Message;
import edgruberman.bukkit.obituaries.messaging.Recipients;
import edgruberman.bukkit.obituaries.messaging.messages.Confirmation;

public class Private implements Recipients {

    protected CommandSender sender;
    protected CommandSender recipient;

    public Private(final CommandSender sender, final CommandSender recipient) {
        this.sender = sender;
        this.recipient = recipient;
    }

    @Override
    public Confirmation send(final Message message) {
        this.sender.sendMessage(message.formatFor(this.sender));
        this.recipient.sendMessage(message.formatFor(this.recipient));
        return new PrivateConfirmation(message.toString());
    }

    private Level getLevel() {
        return (this.sender instanceof Player && this.recipient instanceof Player ? Level.FINER : Level.FINEST);
    }



    public class PrivateConfirmation extends Confirmation {

        public PrivateConfirmation(final String message) {
            super(Private.this.getLevel(), 2, "[TELL@%2$s] %1$s", message, Private.this.recipient.getName());
        }

    }

}
