package edgruberman.bukkit.messaging.recipients;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import edgruberman.bukkit.messaging.Message;
import edgruberman.bukkit.messaging.Recipients;
import edgruberman.bukkit.messaging.messages.Confirmation;

public class LocalPlayers implements Recipients {

    protected static double range = -1;
    protected static double rangeSquared = -1;

    public static void setRange(final double range) {
        LocalPlayers.range = range;
        LocalPlayers.rangeSquared = Math.pow(range, 2);
    }



    protected Location origin;

    public LocalPlayers(final Location origin) {
        this.origin = origin;
    }

    @Override
    public Confirmation send(final Message message) {
        int count = 0;
        for (final Player player : Bukkit.getServer().getOnlinePlayers())
            if (this.origin.distanceSquared(player.getLocation()) <= LocalPlayers.rangeSquared) {
                player.sendMessage(message.formatFor(player));
                count++;
            }

        return new LocalConfirmation(message.toString(), count);
    }



    public class LocalConfirmation extends Confirmation {

        public LocalConfirmation(final String message, final int count) {
            super(Level.FINER, count, "[LOCAL:%4$d:%2$s(%3$d)] %1$s", message, LocalPlayers.this.origin, count, LocalPlayers.range);

        }

    }

}
