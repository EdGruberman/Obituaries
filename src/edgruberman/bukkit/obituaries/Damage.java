package edgruberman.bukkit.obituaries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
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
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 * Tracks the last damage received to identify the source of death.
 */
final class Damage {

    /**
     * Materials that can set a player on fire.
     */
    final static Set<Material> COMBUSTIBLES = new HashSet<Material>(Arrays.asList(new Material[] {
              Material.LAVA
            , Material.STATIONARY_LAVA
            , Material.FIRE
    }));

    static Map<Entity, Damage> last = new HashMap<Entity, Damage>();
    static Map<Entity, String> combuster = new HashMap<Entity, String>();

    EntityDamageEvent event;
    BlockState sourceBlock;
    ItemStack sourceItem;

    Damage(final EntityDamageEvent event) {
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

        Damage.last.put(event.getEntity(), this);
    }

    String describeDamager() {
        String description = null;

        if (this.event instanceof EntityDamageByEntityEvent) {
            // Entity description
            Entity damager = ((EntityDamageByEntityEvent) this.event).getDamager();
            description = Damage.describeEntity(damager);

            if ((damager instanceof Player) && (Coroner.weaponFormat != null)) {
                // Hand-held/direct PvP, append weapon used to inflict damage
                String weapon = null;
                if (this.sourceItem != null) {
                    weapon = Damage.describeMaterial(this.sourceItem.getData());
                    if (Coroner.enchanted != null && this.sourceItem.getEnchantments().size() > 0) weapon = String.format(Coroner.enchanted, weapon);
                } else {
                    weapon = Coroner.hand;
                }
                description = String.format(Coroner.weaponFormat, description, weapon);
            }

        } else if (this.event instanceof EntityDamageByBlockEvent) {
            // Block material name
            Block block = ((EntityDamageByBlockEvent) this.event).getDamager();
            if (block != null)
                description = Damage.describeMaterial(new MaterialData(block.getType(), block.getData()));

        } else if (this.event.getCause() == DamageCause.SUFFOCATION) {
            // Suffocating material
            description = Damage.describeMaterial(this.sourceBlock.getData());

        } else if (this.event.getCause() == DamageCause.FALL) {
            // Falling distance
            description = Integer.toString(this.event.getDamage() + 3);

        } else if (this.event.getCause() == DamageCause.FIRE_TICK) {
            // Combuster
            description = Damage.combuster.get(this.event.getEntity());

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
            if (Coroner.entityNames.containsKey(description))
                description = Coroner.entityNames.get(description);
        }

        // Include if entity has/is an owner of some type

        if (entity instanceof Tameable) {
            AnimalTamer tamer = ((Tameable) entity).getOwner();
            if (tamer instanceof Entity && Coroner.ownerFormats.containsKey("Tameable"))
                description = String.format(Coroner.ownerFormats.get("Tameable"), description, Damage.describeEntity((Entity) tamer));
        }

        if (entity instanceof Projectile) {
            if (Coroner.ownerFormats.containsKey("Projectile")) {
                LivingEntity shooter = ((Projectile) entity).getShooter();
                String shooterName = null;
                if (shooter == null) {
                    shooterName = Coroner.materialNames.get(Material.DISPENSER);
                } else if (shooter instanceof Entity) {
                    shooterName = Damage.describeEntity(shooter);
                }
                description = String.format(Coroner.ownerFormats.get("Projectile"), description, shooterName);
            }
        }

        // Vehicle
        if (!entity.isEmpty() && Coroner.ownerFormats.containsKey("Vehicle")) {
            description = String.format(Coroner.ownerFormats.get("Vehicle"), description, Damage.describeEntity(entity.getPassenger()));
        }

        // Causes cyclical reference for passenger/vehicle combinations
//        // Passenger
//        // TODO - compensate for a passenger that also has riders, or a passenger that is riding a vehicle
//        if (DeathMonitor.ownerFormats.containsKey("Passenger")) {
//            for (Entity e : entity.getNearbyEntities(16, 16, 16)) {
//                if (e.isEmpty()) continue;
//
//                if (e.getPassenger().equals(entity))
//                    description = String.format(DeathMonitor.ownerFormats.get("Passenger"), DamageReport.describeEntity(e));
//            }
//        }

        return description;
    }

    private static String describeMaterial(final MaterialData data) {
        String description = Coroner.materialDataNames.get(data);
        if (description == null)
            description = Coroner.materialNames.get(data.getItemType());

        if (description == null)
            description = data.getItemType().toString().toLowerCase();

        return description;
    }

    static void recordCombuster(final EntityCombustEvent event) {
        if (event instanceof EntityCombustByEntityEvent) {
            EntityCombustByEntityEvent byEntity = (EntityCombustByEntityEvent) event;
            Damage.combuster.put(event.getEntity(), Damage.describeEntity(byEntity.getCombuster()));
            return;
        }

//        System.out.println(event instanceof EntityCombustByBlockEvent);
//        try { throw new Exception(); } catch (Exception e) { e.printStackTrace(); }

        if (event instanceof EntityCombustByBlockEvent) {
            EntityCombustByBlockEvent byBlock = (EntityCombustByBlockEvent) event;
            Damage.combuster.put(event.getEntity(), Damage.describeMaterial(byBlock.getCombuster().getState().getData()));
            return;
        }

        // Assume block

        // Check current
        Location original = event.getEntity().getLocation();
        if (Damage.identifyCombuster(original.clone(), event.getEntity())) return;

        // Check closest on x
        Double adjustX = Damage.closestAdjustOnAxis(original.getX());
        if (adjustX != null)
            if (Damage.identifyCombuster(original.clone().add(adjustX, 0, 0), event.getEntity()))
                return;

        // Check closest on z
        Double adjustZ = Damage.closestAdjustOnAxis(original.getZ());
        if (adjustZ != null)
            if (Damage.identifyCombuster(original.clone().add(0, 0, adjustZ), event.getEntity()))
                return;

        // Check closest diagonal
        if (adjustX != null && adjustZ != null)
            if (Damage.identifyCombuster(original.clone().add(adjustX, 0, adjustZ), event.getEntity()))
                return;

    }

    private static Double closestAdjustOnAxis(double coord) {
        double fPart = Math.abs(coord - (long) coord);
        if (fPart == 0.5d) return null;

        return (fPart < 0.5d ? -1 : 1) * Math.signum(coord);
    }

    private static boolean identifyCombuster(final Location location, final Entity entity) {
        // Check foot block
        if (Damage.isCombustible(location)) {
            Damage.combuster.put(entity, Damage.describeMaterial(location.getBlock().getState().getData()));
            return true;
        }

        // Check head block
        location.add(0, 1, 0);
        if (Damage.isCombustible(location)) {
            Damage.combuster.put(entity, Damage.describeMaterial(location.getBlock().getState().getData()));
            return true;
        }

        // Check block above head if high enough
        double fPartY = location.getY() - (long) location.getY();
        if (fPartY > (2d - 1.62d)) {
            location.add(0, 1, 0);
            if (Damage.isCombustible(location)) {
                Damage.combuster.put(entity, Damage.describeMaterial(location.getBlock().getState().getData()));
                return true;
            }
        }

        return false;
    }

    private static boolean isCombustible(final Location location) {
        return Damage.COMBUSTIBLES.contains(location.getBlock().getType());
    }
}