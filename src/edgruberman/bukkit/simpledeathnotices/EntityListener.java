package edgruberman.bukkit.simpledeathnotices;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public final class EntityListener extends org.bukkit.event.entity.EntityListener{
    
    private Map<Player, EntityDamageEvent> lastDamage = Collections.synchronizedMap(new HashMap<Player, EntityDamageEvent>());
    
    private Main main;
    
    EntityListener (Main plugin) {
        this.main = plugin;
        
        org.bukkit.plugin.PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.ENTITY_DEATH, this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) return;
        
        this.lastDamage.put((Player) event.getEntity(), event);
    }
    
    @Override
    public void onEntityDeath (EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        if (this.lastDamage.get((Player) event.getEntity()) == null) {
            this.main.describeEvent(event);
        } else {
            this.main.describeEvent(this.lastDamage.get((Player) event.getEntity()));
        }
    }
}
