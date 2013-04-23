package edgruberman.bukkit.obituaries.damages;

import org.bukkit.event.entity.EntityDamageEvent;

import edgruberman.bukkit.obituaries.Coroner;
import edgruberman.bukkit.obituaries.Damage;

public class FireTick extends Damage {

    public FireTick(final Coroner coroner, final EntityDamageEvent event) {
        super(coroner, event);
    }

    @Override
    public String describeAsKiller() {
        return this.coroner.getCombusterAsKiller();
    }

    @Override
    public Object getDamager() {
        return this.coroner.getCombuster();
    }

}
