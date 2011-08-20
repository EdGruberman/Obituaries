package edgruberman.bukkit.simpledeathnotices;

import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.Configuration;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public final class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    static ConfigurationFile configurationFile;
    static MessageManager messageManager;
    
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Main.configurationFile = new ConfigurationFile(this);
        this.loadConfiguration();
    }
    
    public void onEnable() {
        new DeathMonitor(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    private void loadConfiguration() {
        Configuration cfg = Main.configurationFile.getConfiguration();
        
        // Load default format.
        DeathMonitor.causeFormats.put(null, cfg.getString("default", DeathMonitor.DEFAULT_FORMAT));
        
        // Load damage cause specific formats.
        for (String name: cfg.getKeys("DamageCause")) {
            DamageCause cause = DamageCause.valueOf(name);
            if (cause == null) continue;
            
            DeathMonitor.causeFormats.put(cause, cfg.getString("DamageCause." + cause.name(), DeathMonitor.DEFAULT_FORMAT));
            Main.messageManager.log("DamageCause Format for " + cause.name() + ": " + DeathMonitor.causeFormats.get(cause), MessageLevel.CONFIG);
        }

        // Entity
        for (String name: cfg.getKeys("Entity")) {
            DeathMonitor.entityNames.put(name, cfg.getString("Entity." + name, name.toLowerCase()));
            Main.messageManager.log("Entity Name for " + name + ": " + DeathMonitor.entityNames.get(name), MessageLevel.CONFIG);
        }
        
        // owners
        for (String name: cfg.getKeys("owners")) {
            DeathMonitor.ownerFormats.put(name, cfg.getString("owners." + name));
            Main.messageManager.log("Owner Format for" + name + ": " + DeathMonitor.ownerFormats.get(name), MessageLevel.CONFIG);
        }
        
        // Material
        for (String name: cfg.getKeys("Material")) {
            Material material = Material.valueOf(name);
            if (material == null) continue;
            
            DeathMonitor.materialNames.put(material, cfg.getString("Material." + material.name(), material.name().toLowerCase()));
            Main.messageManager.log("Material Name for " + material.name() + ": " + DeathMonitor.materialNames.get(material), MessageLevel.CONFIG);
        }
    }
}