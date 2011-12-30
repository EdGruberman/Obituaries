package edgruberman.bukkit.simpledeathnotices;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;

final class DeathMonitor extends EntityListener {
    
    static final String DEFAULT_FORMAT = "%1$s died"; // 1 = Victim, 2 = Killer
    static final String DEFAULT_WEAPON_FORMAT = "%1$s with %2$s"; // 1 = Killer, 2 = Weapon
    static final String DEFAULT_HAND = "a bare fist";
    static final String DEFAULT_ENCHANTED = " that is enchanted";
    
    static Map<DamageCause, String> causeFormats = new HashMap<DamageCause, String>();
    static Map<String, String> entityNames = new HashMap<String, String>();
    static Map<String, String> ownerFormats = new HashMap<String, String>();
    static Map<Material, String> materialNames = new HashMap<Material, String>();
    static Map<MaterialData, String> materialDataNames = new HashMap<MaterialData, String>();
    static String weaponFormat = DeathMonitor.DEFAULT_WEAPON_FORMAT;
    static String hand = DeathMonitor.DEFAULT_HAND;
    static String enchanted = DeathMonitor.DEFAULT_ENCHANTED;
    
    DeathMonitor (final Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.ENTITY_COMBUST, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.ENTITY_DEATH, this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onEntityDamage(final EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) return;
        new DamageReport(event);
    }
    
    @Override
    public void onEntityCombust(final EntityCombustEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) return;
        DamageReport.recordCombuster(event);
    }
    
    @Override
    public void onEntityDeath (final EntityDeathEvent death) {
        if (!(death instanceof PlayerDeathEvent)) return;
        
        DamageReport damage = DamageReport.last.get(death.getEntity());
        
        String format = DeathMonitor.causeFormats.get((damage != null ? damage.event.getCause() : null));
        
        // Use default death message if no format specified
        if (format == null) return;
        
        // Remove default death message
        PlayerDeathEvent pde = (PlayerDeathEvent) death;
        pde.setDeathMessage(null);
        
        // Show custom death message
        String damager = damage.describeDamager();
        String message = String.format(format
                , ((Player) death.getEntity()).getDisplayName()
                , damager
        );
        Main.messageManager.broadcast(message, MessageLevel.EVENT);
    }
}