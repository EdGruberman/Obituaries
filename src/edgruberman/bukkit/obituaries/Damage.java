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
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffectType;

import edgruberman.bukkit.messagemanager.MessageLevel;

/**
 * Tracks the last damage received to identify the source of death.
 */
final class Damage {

    static Map<Entity, Damage> last = new HashMap<Entity, Damage>();
    static Map<Entity, String> combuster = new HashMap<Entity, String>();
    static Map<Entity, String> poisoner = new HashMap<Entity, String>();
    static Map<Entity, String> potion = new HashMap<Entity, String>();

    static void remove(final Entity entity) {
        Damage.last.remove(entity);
        Damage.combuster.remove(entity);
        Damage.poisoner.remove(entity);
        Damage.potion.remove(entity);
    }

    EntityDamageEvent event;
    BlockState sourceBlock;
    ItemStack sourceItem;

    Damage(final EntityDamageEvent event) {
        this.event = event;

        // Capture volatile information
        switch (event.getCause()) {
        case SUFFOCATION:
            // Identify current block at player's top half since player model will drop to floor on death
            final Player victim = (Player) event.getEntity();
            this.sourceBlock = victim.getEyeLocation().getBlock().getState();
            break;

        case ENTITY_ATTACK:
            // Sometimes damage event will be fired after source item is no longer in damager's hand
            final Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Player) {
                final Player damagerPlayer = (Player) damager;
                final ItemStack weapon = damagerPlayer.getItemInHand();
                if (weapon.getTypeId() != Material.AIR.getId())
                    this.sourceItem = weapon.clone();
            }
            break;

        case POISON:
            Damage.recordPoisoner(this.event);
            break;

        }

        Damage.last.put(event.getEntity(), this);
    }

    String describeSource() {
        String description = null;

        switch (this.event.getCause()) {
        case BLOCK_EXPLOSION:
        case CONTACT:
        case SUFFOCATION:
            // Block material
            final Block block = ((EntityDamageByBlockEvent) this.event).getDamager();
            if (block != null)
                description = Damage.describeMaterial(block.getState().getData());
            break;

        case ENTITY_ATTACK:
        case ENTITY_EXPLOSION:
        case PROJECTILE:
            // Entity description
            final Entity damager = ((EntityDamageByEntityEvent) this.event).getDamager();
            description = Damage.describeEntity(damager);

            if ((damager instanceof Player) && (Coroner.weaponFormat != null)) {
                String weapon = null;
                if (this.sourceItem != null) {
                    weapon = Damage.describeMaterial(this.sourceItem.getData());
                    if (Coroner.enchanted != null && this.sourceItem.getEnchantments().size() > 0) weapon = String.format(Coroner.enchanted, weapon);
                } else if (Damage.potion.get(this.event.getEntity()) != null) {
                    weapon = "a potion of " + Damage.potion.get(this.event.getEntity());
                } else {
                    weapon = Coroner.hand;
                }
                description = String.format(Coroner.weaponFormat, description, weapon);
            }
            break;

        case FALL:
            // Distance fallen
            description = Integer.toString(this.event.getDamage() + 3);
            break;

        case FIRE_TICK:
            // Combuster
            description = Damage.combuster.get(this.event.getEntity());
            break;

        case LIGHTNING:
            // Lightning word
            description = Damage.describeEntity(((EntityDamageByEntityEvent) this.event).getDamager());
            break;

        }

        return description;
    }

    private static String describeItemStack(final ItemStack item) {
        final ItemStack compare = item.clone();
        compare.setAmount(0);
        String description = Coroner.itemStackNames.get(compare);
        if (description == null)
            description = Damage.describeMaterial(item.getData());

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
            final String[] entityClass = entity.getClass().getName().split("\\.");
            description = entityClass[entityClass.length - 1].substring("Craft".length());

            if (entity instanceof Creeper) {
                final Creeper creeper = (Creeper) entity;
                if (creeper.isPowered()) description = "PoweredCreeper";
            }

            // Override with localization if specified in configuration
            if (Coroner.entityNames.containsKey(description))
                description = Coroner.entityNames.get(description);
        }

        // Include if entity has/is an owner of some type

        if (entity instanceof Tameable) {
            final AnimalTamer tamer = ((Tameable) entity).getOwner();
            if (tamer instanceof Entity && Coroner.ownerFormats.containsKey("Tameable"))
                description = String.format(Coroner.ownerFormats.get("Tameable"), description, Damage.describeEntity((Entity) tamer));
        }

        if (entity instanceof Projectile) {
            if (Coroner.ownerFormats.containsKey("Projectile")) {
                final LivingEntity shooter = ((Projectile) entity).getShooter();
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

    // TODO Determine how to attribute a poison death to the last source that caused a poisoning
    // This will attribute a poison source to the first one that started poisoning even if that doesn't have enough duration/amplitude to fully kill the entity
    static void recordPoisoner(final Event event) {
        String poisoner = null;
        LivingEntity target = null;
        if (event instanceof EntityDamageByEntityEvent) {
            // splash potion or cave spider bite
            // Assumes cause is POISON and entity is LivingEntity

            // Ignore if target is already poisoned
            final EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) event;
            target = (LivingEntity) edbee.getEntity();
            if (target.hasPotionEffect(PotionEffectType.POISON)) return;

            poisoner = Damage.describeEntity(edbee.getDamager());

        } else if (event instanceof PlayerInteractEvent) {
            // eating food or drinking potion
            final PlayerInteractEvent pie = (PlayerInteractEvent) event;

            // Ignore if target is already poisoned
            target = pie.getPlayer();
            if (target.hasPotionEffect(PotionEffectType.POISON)) return;

            // Ignore if no item is involved
            if (!pie.hasItem()) return;

            // TODO investigate useItemInHand and if it should be checked
            // Ignore if item is not being used
            if (pie.getAction() != Action.RIGHT_CLICK_AIR && pie.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            // Ignore if item is not drinkable or edible
            final ItemStack item = pie.getItem();
            if (!item.getType().isEdible() && item.getType() != Material.POTION) return;

            poisoner = Damage.describeItemStack(pie.getItem());
            if (item.getType() == Material.POTION) poisoner = String.format(Coroner.drinkable, poisoner);
            if (item.getType().isEdible()) poisoner = String.format(Coroner.edible, poisoner);

        } else if (event instanceof PlayerInteractEntityEvent) {
            // eating food or drinking potion
            final PlayerInteractEntityEvent piee = (PlayerInteractEntityEvent) event;

            // Ignore if target is already poisoned
            target = piee.getPlayer();
            if (target.hasPotionEffect(PotionEffectType.POISON)) return;

            // Ignore if no item is involved
            final ItemStack itemInHand = piee.getPlayer().getItemInHand();
            if (itemInHand.getType() == Material.AIR) return;

            // Ignore if item is not drinkable or edible
            if (!itemInHand.getType().isEdible() && itemInHand.getType() != Material.POTION) return;

            poisoner = Damage.describeItemStack(itemInHand);
            if (itemInHand.getType() == Material.POTION) poisoner = String.format(Coroner.drinkable, poisoner);
            if (itemInHand.getType().isEdible()) poisoner = String.format(Coroner.edible, poisoner);

        } else {
            // Not an event that can be used to identify a poison source
            return;
        }

        Damage.poisoner.put(target, poisoner);
    }

    static void recordCombuster(final EntityCombustEvent event) {
        if (event instanceof EntityCombustByEntityEvent) {
            final EntityCombustByEntityEvent byEntity = (EntityCombustByEntityEvent) event;
            Damage.combuster.put(event.getEntity(), Damage.describeEntity(byEntity.getCombuster()));
            return;
        }

//        System.out.println(event instanceof EntityCombustByBlockEvent);
//        try { throw new Exception(); } catch (Exception e) { e.printStackTrace(); }

        if (event instanceof EntityCombustByBlockEvent) {
            Main.messageManager.log("Block combust now triggers! Update Obituaries plugin", MessageLevel.FINE);
            final EntityCombustByBlockEvent byBlock = (EntityCombustByBlockEvent) event;
            Damage.combuster.put(event.getEntity(), Damage.describeMaterial(byBlock.getCombuster().getState().getData()));
            return;
        }

        // TODO Remove these assumptions when CraftBukkit fixes combust events for block contact
        // Assume block

        // Check current
        final Location original = event.getEntity().getLocation();
        if (Damage.identifyCombuster(original.clone(), event.getEntity())) return;

        // Check closest on x
        final Double adjustX = Damage.closestAdjustOnAxis(original.getX());
        if (adjustX != null)
            if (Damage.identifyCombuster(original.clone().add(adjustX, 0, 0), event.getEntity()))
                return;

        // Check closest on z
        final Double adjustZ = Damage.closestAdjustOnAxis(original.getZ());
        if (adjustZ != null)
            if (Damage.identifyCombuster(original.clone().add(0, 0, adjustZ), event.getEntity()))
                return;

        // Check closest diagonal
        if (adjustX != null && adjustZ != null)
            if (Damage.identifyCombuster(original.clone().add(adjustX, 0, adjustZ), event.getEntity()))
                return;

    }

    private static Double closestAdjustOnAxis(final double coord) {
        final double fPart = Math.abs(coord - (long) coord);
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
        final double fPartY = location.getY() - (long) location.getY();
        if (fPartY > (2d - 1.62d)) {
            location.add(0, 1, 0);
            if (Damage.isCombustible(location)) {
                Damage.combuster.put(entity, Damage.describeMaterial(location.getBlock().getState().getData()));
                return true;
            }
        }

        return false;
    }

    /**
     * Materials that can set a player on fire.
     */
    private final static Set<Material> COMBUSTIBLES = new HashSet<Material>(Arrays.asList(new Material[] {
              Material.LAVA
            , Material.STATIONARY_LAVA
            , Material.FIRE
    }));


    private static boolean isCombustible(final Location location) {
        return Damage.COMBUSTIBLES.contains(location.getBlock().getType());
    }

}