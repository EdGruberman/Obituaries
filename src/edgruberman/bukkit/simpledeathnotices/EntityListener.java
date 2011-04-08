package edgruberman.bukkit.simpledeathnotices;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityListener extends org.bukkit.event.entity.EntityListener{
    
    private Map<Player, EntityDamageEvent> lastDamage = Collections.synchronizedMap(new HashMap<Player, EntityDamageEvent>());
    
    private Main main;
    
    public EntityListener (Main main) {
        this.main = main;
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) { return; }
        lastDamage.put((Player) event.getEntity(), event);
    }
    
    @Override
    public void onEntityDeath (EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) { return; }
        if (this.lastDamage.get((Player) event.getEntity()) == null) {
            this.main.describeEvent(event);
        } else {
            this.main.describeEvent(this.lastDamage.get((Player) event.getEntity()));
        }
        
    }

}
