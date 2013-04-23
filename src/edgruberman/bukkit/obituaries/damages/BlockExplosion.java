package edgruberman.bukkit.obituaries.damages;

import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageEvent;

import edgruberman.bukkit.obituaries.Attack;
import edgruberman.bukkit.obituaries.Coroner;
import edgruberman.bukkit.obituaries.Main;
import edgruberman.bukkit.obituaries.Translator;

public class BlockExplosion extends Attack {

    private static final String DEFAULT_MATERIAL = "(default)";

    public BlockExplosion(final Coroner coroner, final EntityDamageEvent damage) {
        super(coroner, damage);
    }

    @Override
    public String describeAsKiller() {
        final String material;
        if (this.damager == null) {
            material = Translator.describeMaterial(Material.BED_BLOCK);
        } else {
            material = Translator.describeEntity(this.damager);
        }

        if (this.damager != null & this.damager instanceof TNTPrimed) {
            final TNTPrimed tnt = (TNTPrimed) this.damager;
            if (tnt.getSource() != null) {
                return Main.courier.format("explosion." + this.damager.getType().name(), material, Translator.describeEntity(tnt.getSource()));
            }
        }

        return Main.courier.format("explosion." + BlockExplosion.DEFAULT_MATERIAL, material);
    }

}
