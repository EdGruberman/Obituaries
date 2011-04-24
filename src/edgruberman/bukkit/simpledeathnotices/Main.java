package edgruberman.bukkit.simpledeathnotices;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;

import edgruberman.bukkit.simpledeathnotices.MessageManager.MessageLevel;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    private final String DEFAULT_LOG_LEVEL       = "CONFIG";
    private final String DEFAULT_BROADCAST_LEVEL = "EVENT";

    public static MessageManager messageManager = null;
    	
    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Configuration.load(this);
        Main.messageManager.setLogLevel(MessageLevel.parse(      this.getConfiguration().getString("logLevel",       this.DEFAULT_LOG_LEVEL)));
        Main.messageManager.setBroadcastLevel(MessageLevel.parse(this.getConfiguration().getString("broadcastLevel", this.DEFAULT_BROADCAST_LEVEL)));
 
        Main.messageManager.log(MessageLevel.CONFIG,
            "timestamp: " + this.getConfiguration().getString("timestamp")
            + "; format: " + this.getConfiguration().getString("format")
        );
        
        this.registerEvents();
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        //TODO Unregister listeners when Bukkit supports it.
        
        Main.messageManager.log("Plugin Disabled");
        Main.messageManager = null;
    }
    
    private void registerEvents() {
        EntityListener entityListener = new EntityListener(this);
        
        org.bukkit.plugin.PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Monitor, this);
        pluginManager.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Monitor, this);
    }
    
    public void describeEvent(EntityEvent event) {
        Entity damager = null;
        String damagerName = "";
        
        if (event instanceof EntityDamageByEntityEvent){
            damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Player) {
                damagerName = " " + ((Player) damager).getDisplayName();
            } else {
                String[] damagerClass = damager.getClass().getName().split("\\.");
                damagerName = " a " + damagerClass[damagerClass.length - 1].substring("Craft".length());
            }
        }
        
        String deathCause;
        if (event instanceof EntityDeathEvent) {
            deathCause = this.getCause(null);
        } else {
            deathCause = this.getCause(((EntityDamageEvent) event).getCause());
        }
        
        String deathNotice = this.getConfiguration().getString("format")
            .replace(
                  "%TIMESTAMP%"
                , (new SimpleDateFormat(this.getConfiguration().getString("timestamp")).format(new Date()))
            )
            .replace(
                  "%VICTIM%"
                , ((Player) event.getEntity()).getDisplayName()
            )
            .replace(
                  "%CAUSE%"
                , deathCause
            )
            .replace(
                  "%KILLER%"
                , damagerName
            )
        ;
        
        Main.messageManager.broadcast(MessageLevel.EVENT, deathNotice);
    }
    
    public String getCause(DamageCause damageCause) {
        String deathCause;
        switch (damageCause) {
            case ENTITY_ATTACK:    deathCause = "being hit by";           break;
            case ENTITY_EXPLOSION: deathCause = "an explosion from";      break;
            case CONTACT:          deathCause = "contact";                break;
            case SUFFOCATION:      deathCause = "suffocation";            break;
            case FALL:             deathCause = "falling";                break;
            case FIRE:             deathCause = "fire";                   break;
            case FIRE_TICK:        deathCause = "burning";                break;
            case LAVA:             deathCause = "lava";                   break;
            case DROWNING:         deathCause = "drowning";               break;
            case BLOCK_EXPLOSION:  deathCause = "an explosion";           break;
            case VOID:             deathCause = "falling in to the void"; break;
            case CUSTOM:           deathCause = "something custom";       break;
            default:               deathCause = "something";              break;
        }
        return deathCause;
    }

}
