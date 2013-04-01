package edgruberman.bukkit.obituaries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;

/** identify and track source of combustion */
class FireInvestigator implements Listener {

    private final Coroner coroner;
    private final Map<Entity, String> combusters = new HashMap<Entity, String>();

    FireInvestigator(final Coroner coroner) {
        this.coroner = coroner;
        coroner.plugin.getServer().getPluginManager().registerEvents(this, coroner.plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityCombust(final EntityCombustEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        this.recordCombuster(event);
    }

    String getCombuster(final Entity entity) {
        return this.combusters.get(entity);
    }

    void remove(final Entity entity) {
        this.combusters.remove(entity);
    }

    void clear() {
        this.combusters.clear();
    }

    private void recordCombuster(final EntityCombustEvent event) {
        if (event instanceof EntityCombustByEntityEvent) {
            final EntityCombustByEntityEvent byEntity = (EntityCombustByEntityEvent) event;
            this.combusters.put(event.getEntity(), this.coroner.describeKiller(byEntity.getCombuster()));
            return;
        }

        if (event instanceof EntityCombustByBlockEvent) {
            final EntityCombustByBlockEvent byBlock = (EntityCombustByBlockEvent) event;
            if (byBlock.getCombuster() != null)
                this.coroner.plugin.getLogger().warning("Block combust now triggers with combuster! Update Obituaries plugin");

            // this.combusters.put(event.getEntity(), this.translator.formatMaterial(byBlock.getCombuster()));
        }

        // Assume block

        // Check current
        final Location original = event.getEntity().getLocation();
        if (this.identifyCombuster(original.clone(), event.getEntity())) return;

        // Check closest on x
        final Double adjustX = FireInvestigator.closestAdjustOnAxis(original.getX());
        if (adjustX != null)
            if (this.identifyCombuster(original.clone().add(adjustX, 0, 0), event.getEntity()))
                return;

        // Check closest on z
        final Double adjustZ = FireInvestigator.closestAdjustOnAxis(original.getZ());
        if (adjustZ != null)
            if (this.identifyCombuster(original.clone().add(0, 0, adjustZ), event.getEntity()))
                return;

        // Check closest diagonal
        if (adjustX != null && adjustZ != null)
            if (this.identifyCombuster(original.clone().add(adjustX, 0, adjustZ), event.getEntity()))
                return;

        // Nothing identified, assume basic fire caused combustion
        this.combusters.put(event.getEntity(), Translator.formatMaterial(Material.FIRE));
    }

    private static Double closestAdjustOnAxis(final double coord) {
        final double fPart = Math.abs(coord - (long) coord);
        if (fPart == 0.5d) return null;

        return (fPart < 0.5d ? -1 : 1) * Math.signum(coord);
    }

    private boolean identifyCombuster(final Location location, final Entity entity) {
        // Check foot block
        if (FireInvestigator.isCombustible(location)) {
            this.combusters.put(entity, Translator.formatMaterial(location.getBlock()));
            return true;
        }

        // Check head block
        location.add(0, 1, 0);
        if (FireInvestigator.isCombustible(location)) {
            this.combusters.put(entity, Translator.formatMaterial(location.getBlock()));
            return true;
        }

        // Check block above head if high enough
        final double fPartY = location.getY() - (long) location.getY();
        if (fPartY > (2d - 1.62d)) {
            location.add(0, 1, 0);
            if (FireInvestigator.isCombustible(location)) {
                this.combusters.put(entity, Translator.formatMaterial(location.getBlock()));
                return true;
            }
        }

        return false;
    }

    /** materials that can set a player on fire */
    private final static Set<Material> COMBUSTIBLES = new HashSet<Material>(Arrays.asList(new Material[] {
              Material.LAVA
            , Material.STATIONARY_LAVA
            , Material.FIRE
    }));

    private static boolean isCombustible(final Location location) {
        return FireInvestigator.COMBUSTIBLES.contains(location.getBlock().getType());
    }

}
