package edgruberman.bukkit.simpledeathnotices;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * Removes damage reports when a player leaves to avoid a build up of reports
 * which could cause a memory leak.
 */
final class ReportShredder extends PlayerListener {
    
    ReportShredder (final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onPlayerQuit(final PlayerQuitEvent quit) {
        DamageReport.last.remove(quit.getPlayer());
    }
}
