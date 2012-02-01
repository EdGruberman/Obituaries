package edgruberman.bukkit.obituaries;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;

/**
 * Monitors damage and death events, generating public notices for deaths.
 */
final class Coroner implements Listener {

    static Map<DamageCause, String> causeFormats = new HashMap<DamageCause, String>();
    static Map<String, String> entityNames = new HashMap<String, String>();
    static Map<String, String> ownerFormats = new HashMap<String, String>();
    static Map<Material, String> materialNames = new HashMap<Material, String>();
    static Map<MaterialData, String> materialDataNames = new HashMap<MaterialData, String>();
    static String weaponFormat = null;
    static String hand = null;
    static String enchanted = null;

    Coroner (final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(final EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) return;

        new Damage(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityCombust(final EntityCombustEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) return;

        Damage.recordCombuster(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath (final EntityDeathEvent death) {
        if (!(death instanceof PlayerDeathEvent)) return;

        Damage kill = Damage.last.get(death.getEntity());
        String causeFormat = Coroner.causeFormats.get((kill != null ? kill.event.getCause() : null));

        // Use default death message if no format specified
        if (causeFormat == null) return;

        // Show custom death message
        String damager = kill.describeDamager();
        String message = String.format(causeFormat
                , ((Player) death.getEntity()).getDisplayName()
                , damager
        );
        Main.messageManager.broadcast(message, MessageLevel.EVENT);

        // Remove default death message
        PlayerDeathEvent pde = (PlayerDeathEvent) death;
        pde.setDeathMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent quit) {
        Damage.last.remove(quit.getPlayer());
    }

}