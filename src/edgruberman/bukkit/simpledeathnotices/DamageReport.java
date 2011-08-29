package edgruberman.bukkit.simpledeathnotices;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.material.MaterialData;

class DamageReport {
    
    static Map<Entity, DamageReport> last = new HashMap<Entity, DamageReport>();
    
    EntityDamageEvent event;
    MaterialData source;
    
    DamageReport(final EntityDamageEvent event) {
        
        this.event = event;
        
        // Capture volatile relevant status information for later reference
        if (event.getCause() == DamageCause.SUFFOCATION) {
            Player victim = (Player) event.getEntity();
            this.source = new MaterialData(victim.getEyeLocation().getBlock().getType(), victim.getEyeLocation().getBlock().getData());
            
        } else if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Player) {
                Player damagerPlayer = (Player) damager;
                this.source = damagerPlayer.getItemInHand().getData();
            }
        }
        
        DamageReport.last.put(event.getEntity(), this);
    }
    
    String describeDamager() {
        String description = null;
        
        if (this.event instanceof EntityDamageByEntityEvent) {
            // Entity description
            Entity damager = ((EntityDamageByEntityEvent) this.event).getDamager();
            description = DamageReport.describeEntity(damager);
            if ((damager instanceof Player) && (DeathMonitor.weaponFormat != null)) {
                // PvP, append weapon used to inflict damage
                String weapon = DamageReport.describeMaterial(this.source);
                if (weapon == null) weapon = DeathMonitor.hand;
                description = String.format(DeathMonitor.weaponFormat, description, weapon);
            }
            
        } else if (this.event instanceof EntityDamageByBlockEvent) {
            // Block material name
            Block block = ((EntityDamageByBlockEvent) this.event).getDamager();
            if (block != null)
                description = DamageReport.describeMaterial(new MaterialData(block.getType(), block.getData()));
            
        } else if (this.event.getCause() == DamageCause.SUFFOCATION) {
            // Suffocating material
            description = DeathMonitor.materialNames.get(this.source);
            
        } else if (this.event.getCause() == DamageCause.FALL) {
            // Falling distance
            description = Integer.toString(this.event.getDamage() + 3);
            
        }
        
        return description;
    }
    
    private static String describeEntity(final Entity entity) {
        String description = null;
        
        if (entity instanceof Player) {
            // For players, use their current display name
            description = ((Player) entity).getDisplayName();
            
        } else {
            // For other entities, use their Bukkit class name
            String[] entityClass = entity.getClass().getName().split("\\.");
            description = entityClass[entityClass.length - 1].substring("Craft".length());
            
            // Override with localization if specified in configuration
            if (DeathMonitor.entityNames.containsKey(description))
                description = DeathMonitor.entityNames.get(description);
        }
        
        // Include if entity has an owner of some type
        
        if (entity instanceof Tameable) {
            AnimalTamer tamer = ((Tameable) entity).getOwner();
            if (tamer instanceof Entity && DeathMonitor.ownerFormats.containsKey("Tameable"))
                description = String.format(DeathMonitor.ownerFormats.get("Tameable"), description, DamageReport.describeEntity((Entity) tamer));
        }
        
        if (entity instanceof Projectile) {
            if (DeathMonitor.ownerFormats.containsKey("Projectile")) {
                LivingEntity shooter = ((Projectile) entity).getShooter();
                String shooterName = null;
                if (shooter == null) {
                    shooterName = DeathMonitor.materialNames.get(Material.DISPENSER);
                } else if (shooter instanceof Entity) {
                    shooterName = DamageReport.describeEntity(shooter);
                }
                description = String.format(DeathMonitor.ownerFormats.get("Projectile"), description, shooterName);
            }
        }
        
        if (entity instanceof Vehicle) {
            Entity passenger = ((Vehicle) entity).getPassenger();
            if (passenger instanceof Entity && DeathMonitor.ownerFormats.containsKey("Vehicle"))
                description = String.format(DeathMonitor.ownerFormats.get("Vehicle"), description, DamageReport.describeEntity(passenger));
        }
        
        return description;
    }
    
    private static String describeMaterial(final MaterialData data) {
        String description = DeathMonitor.materialDataNames.get(data);
        if (description == null)
            description = DeathMonitor.materialNames.get(data.getItemType());
        
        if (description == null)
            description = data.getItemType().toString().toLowerCase();
        
        return description;
    }
}