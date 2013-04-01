package edgruberman.bukkit.obituaries;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/** monitors damage and death events, examines death events and reports findings */
class Coroner implements Listener {

    final Plugin plugin;
    final FireInvestigator investigator;
    final Alchemist alchemist;
    final Map<Entity, Damage> damages = new HashMap<Entity, Damage>();

    Coroner (final Plugin plugin) {
        this.plugin = plugin;
        this.investigator = new FireInvestigator(this);
        this.alchemist = new Alchemist(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    void clear() {
        HandlerList.unregisterAll(this);
        this.investigator.clear();
        this.alchemist.clear();
        this.damages.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        final Damage damage = new Damage(event);
        this.damages.put(damage.event.getEntity(), damage);
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent death) {
        // Create unknown damage report if none previously recorded
        if (!this.damages.containsKey(death.getEntity()))
            this.onEntityDamage(new EntityDamageEvent(death.getEntity(), DamageCause.CUSTOM, 0));

        final String message = this.describeDeath(death.getEntity());
        this.remove(death.getEntity());

        // Leave default death message if no format specified
        if (message == null) return;

        // Show custom death message
        death.setDeathMessage(message);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent quit) {
        this.remove(quit.getPlayer());
    }

    private void remove(final Entity entity) {
        this.damages.remove(entity);
        this.alchemist.remove(entity);
        this.investigator.remove(entity);
    }

    private String describeDeath(final Entity entity) {
        final Damage kill = this.damages.get(entity);
        final String format = Main.courier.format("damage-causes." + kill.event.getCause().name());
        if (format == null) return null;

        return MessageFormat.format(format, Translator.describeEntity(kill.event.getEntity()), this.describeSource(kill));
    }

    private String describeSource(final Damage damage) {
        String description = null;

        switch (damage.event.getCause()) {

        // Material
        case BLOCK_EXPLOSION:
            if (damage.sourceBlock == null) {
                // Possibility might exist in CraftBukkit to return a null block for a TNT explosion
                description = Translator.formatMaterial(Material.TNT);
            } else {
                description = Translator.formatMaterial(damage.sourceBlock);
            }
            break;

        // Material
        case CONTACT:
        case SUFFOCATION:
            description = Translator.formatMaterial(damage.sourceBlock);
            break;

        case ENTITY_EXPLOSION: // Entity
            final Entity exploder = ((EntityDamageByEntityEvent) damage.event).getDamager();
            description = this.describeKiller(exploder);
            break;

        case ENTITY_ATTACK: // Weapon
            description = this.describeDamager(damage);
            break;

        case PROJECTILE: // Shooter
            final Entity projectile = ((EntityDamageByEntityEvent) damage.event).getDamager();
            description = this.describeKiller(projectile);
            break;

        // Potion effects
        case MAGIC:
            description = this.alchemist.getPotion(damage.event.getEntity());
            break;

        // Distance fallen
        case FALL:
            description = Integer.toString(damage.event.getDamage() + 3);
            break;

        // Combuster
        case FIRE_TICK:
            description = this.investigator.getCombuster(damage.event.getEntity());
            break;

        // Lightning
        case LIGHTNING:
            final Entity lightning = ((EntityDamageByEntityEvent) damage.event).getDamager();
            description = this.describeKiller(lightning);
            break;

        default:
            break;

        }

        return description;
    }

    /**
     * Describes an entity under the context of being killed by it. Players will
     * use their display names. Other entities will default to their Bukkit
     * class name if a config.yml localized name does not match. Projectiles,
     * Tameables, and Vehicles will include descriptions of their owners if
     * a format in the language file is specified.
     *
     * Examples:
     *   Player = EdGruberman
     *   Arrow = EdGruberman with an arrow
     *   Fireball = a ghast with a fireball
     */
    String describeKiller(final Entity killer) {
        String description = Translator.describeEntity(killer);

        if (killer instanceof Tameable) {
            final AnimalTamer tamer = ((Tameable) killer).getOwner();
            if (tamer instanceof Entity) {
                description = Main.courier.format("owners.Tameable", description, this.describeKiller((Entity) tamer));
            }
        }

        if (killer instanceof Projectile) {
            final LivingEntity shooter = ((Projectile) killer).getShooter();
            String shooterName = null;
            if (shooter == null) {
                shooterName = Translator.formatMaterial(Material.DISPENSER);
            } else if (shooter instanceof Entity) {
                shooterName = this.describeKiller(shooter);
            }
            description = Main.courier.format("owners.Projectile", description, shooterName);
        }

        // Vehicle
        if (!killer.isEmpty()) {
            description = Main.courier.format("owners.Vehicle", description, this.describeKiller(killer.getPassenger()));
        }

        return description;
    }

    private String describeDamager(final Damage damage) {
        final Entity source = ((EntityDamageByEntityEvent) damage.event).getDamager();
        final String damager = this.describeKiller(source);

        String weapon = null;
        if (damage.sourceItem != null) {
            if (damage.sourceItem.getType() == Material.AIR) {
                weapon = Main.courier.format("item.defaults." + source.getType().name());
            } else {
                weapon = Translator.formatItem(damage.sourceItem);
            }
        }

        final String result = Main.courier.format("item.format", damager, weapon);
        return ( result != null && weapon != null ? result : damager );
    }

}
