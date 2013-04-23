package edgruberman.bukkit.obituaries;

import org.bukkit.block.BlockState;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/** damage by block */
public class Accident extends Damage {

    protected final BlockState damager;

    public Accident(final Coroner coroner, final EntityDamageEvent damage) {
        this(coroner, damage, ((EntityDamageByBlockEvent) damage).getDamager().getState());
    }

    public Accident(final Coroner coroner, final EntityDamageEvent damage, final BlockState damager) {
        super(coroner, damage);
        this.damager = damager;
    }

    @Override
    public String describeAsKiller() {
        return Translator.describeMaterial(this.damager);
    }

    @Override
    public BlockState getDamager() {
        return this.damager;
    }

}
