package edgruberman.bukkit.obituaries.damages;

import org.bukkit.event.entity.EntityDamageEvent;

import edgruberman.bukkit.obituaries.Attack;
import edgruberman.bukkit.obituaries.Coroner;

public class Thorns extends Attack {

    public Thorns(final Coroner coroner, final EntityDamageEvent event) {
        super(coroner, event);
    }

    @Override
    public String describeAsKiller() {
        return this.describeAsAccomplice();
    }

}
