package edgruberman.bukkit.simpledeathnotices;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

final class DamageReport {
    
    static Map<Entity, DamageReport> last = new HashMap<Entity, DamageReport>();
    static Map<Entity, String> combuster = new HashMap<Entity, String>();
    
    EntityDamageEvent event;
    BlockState sourceBlock;
    ItemStack sourceItem;
    
    DamageReport(final EntityDamageEvent event) {
        this.event = event;
        
        // Capture volatile relevant status information for later reference
        if (event.getCause() == DamageCause.SUFFOCATION) {
            Player victim = (Player) event.getEntity();
            this.sourceBlock = victim.getEyeLocation().getBlock().getState();
            
        } else if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Player) {
                Player damagerPlayer = (Player) damager;
                this.sourceItem = damagerPlayer.getItemInHand().clone();
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
                // Hand-held/direct PvP, append weapon used to inflict damage
                String weapon = null;
                if (this.sourceItem != null) {
                    weapon = DamageReport.describeMaterial(this.sourceItem.getData());
                    if (this.sourceItem.getEnchantments().size() > 0) weapon += DeathMonitor.enchanted;
                } else {
                    weapon = DeathMonitor.hand;
                }
                description = String.format(DeathMonitor.weaponFormat, description, weapon);
            }
            
        } else if (this.event instanceof EntityDamageByBlockEvent) {
            // Block material name
            Block block = ((EntityDamageByBlockEvent) this.event).getDamager();
            if (block != null)
                description = DamageReport.describeMaterial(new MaterialData(block.getType(), block.getData()));
            
        } else if (this.event.getCause() == DamageCause.SUFFOCATION) {
            // Suffocating material
            description = DamageReport.describeMaterial(this.sourceBlock.getData());
            
        } else if (this.event.getCause() == DamageCause.FALL) {
            // Falling distance
            description = Integer.toString(this.event.getDamage() + 3);
            
        } else if (this.event.getCause() == DamageCause.FIRE_TICK) {
            // Combuster
            description = DamageReport.combuster.get(this.event.getEntity());
            
        }
        
        return description;
    }
    
    /**
     * Describes an entity under the context of being killed by it.  Players will be their in-game names.
     * Other entities will default to their Bukkit class name if a config.yml localized name does not match.
     * Projectiles, Tameables, and Vehicles will include descriptions of their owners if format in config.yml
     * is specified.
     * 
     * Examples:
     *   Player = EdGruberman
     *   Arrow = EdGruberman with an arrow
     *   Fireball = a ghast with a fireball
     * 
     * @param entity entity to describe
     * @return description of entity
     */
    private static String describeEntity(final Entity entity) {
        String description = null;
        
        if (entity instanceof Player) {
            // For players, use their current display name
            description = ((Player) entity).getDisplayName();
            
        } else {
            // For other entities, use their Bukkit class name
            String[] entityClass = entity.getClass().getName().split("\\.");
            description = entityClass[entityClass.length - 1].substring("Craft".length());
            
            if (entity instanceof Creeper) {
                Creeper creeper = (Creeper) entity;
                if (creeper.isPowered()) description = "PoweredCreeper";
            }
            
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
    
    static void recordCombuster(final EntityCombustEvent event) {
        if (event instanceof EntityCombustByEntityEvent) {
            EntityCombustByEntityEvent byEntity = (EntityCombustByEntityEvent) event;
            DamageReport.combuster.put(event.getEntity(), DamageReport.describeEntity(byEntity.getCombuster()));
            return;
        }
        
        if (event instanceof EntityCombustByBlockEvent) {
            EntityCombustByBlockEvent byBlock = (EntityCombustByBlockEvent) event;
            DamageReport.combuster.put(event.getEntity(), DamageReport.describeMaterial(byBlock.getCombuster().getState().getData()));
            return;
        }
    }
}