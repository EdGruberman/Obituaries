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
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

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
    static Map<ItemStack, String> itemStackNames = new HashMap<ItemStack, String>();
    static Map<PotionEffectType, String> potionEffectTypeNames = new HashMap<PotionEffectType, String>();
    static String weaponFormat = null;
    static String hand = null;
    static String enchanted = null;
    static String edible = null;
    static String drinkable = null;

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

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPotionSplash(final PotionSplashEvent event) {
//        if (event.isCancelled()) return;
//
//        for (PotionEffect effect : event.getPotion().getEffects())
//            if (effect.getType().equals(PotionEffectType.HARM))
//
//
//
////                || !(event.getAffectedEntities() instanceof Player)) return;
////
////        Damage.recordCombuster(event);
//    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPlayerInteract(final PlayerInteractEvent event) {
//        // TODO - use event.isCancelled() when bug is fixed that doesn't check right clicking on air with item returning true
//        if (event.useInteractedBlock() == Result.DENY && event.useItemInHand() == Result.DENY) return;
//
//        Damage.recordPoisoner(event);
//    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath (final EntityDeathEvent death) {
        if (!(death instanceof PlayerDeathEvent)) return;

        final Damage kill = Damage.last.get(death.getEntity());
        final String causeFormat = Coroner.causeFormats.get((kill != null ? kill.event.getCause() : DamageCause.CUSTOM));

        // Use default death message if no format specified
        if (causeFormat == null) return;

        // Show custom death message
        final String source = (kill != null ? kill.describeSource() : null);
        final String message = String.format(causeFormat, ((Player) death.getEntity()).getDisplayName(), source);
        Main.messageManager.broadcast(message, MessageLevel.EVENT);

        // Remove default death message
        final PlayerDeathEvent pde = (PlayerDeathEvent) death;
        pde.setDeathMessage(null);

        Damage.remove(death.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent quit) {
        Damage.remove(quit.getPlayer());
    }

}