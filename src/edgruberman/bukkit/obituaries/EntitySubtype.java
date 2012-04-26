package edgruberman.bukkit.obituaries;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.Slime;
import org.bukkit.entity.StorageMinecart;

enum EntitySubtype {

      SLIME_BIG(EntityType.SLIME)
    , SLIME_SMALL(EntityType.SLIME)
    , SLIME_TINY(EntityType.SLIME)
    , MAGMA_CUBE_BIG(EntityType.MAGMA_CUBE)
    , MAGMA_CUBE_SMALL(EntityType.MAGMA_CUBE)
    , MAGMA_CUBE_TINY(EntityType.MAGMA_CUBE)
    , CREEPER_POWERED(EntityType.CREEPER)
    , MINECART_STORAGE(EntityType.MINECART)
    , MINECART_POWERED(EntityType.MINECART)
    ;

    static EntitySubtype of(final Entity entity) {
        for (final EntitySubtype subtype : EntitySubtype.values())
            if (subtype.matches(entity)) return subtype;

        return null;
    }

    final EntityType type;

    private EntitySubtype(final EntityType type) {
        this.type = type;
    }

    boolean matches(final Entity entity) {
        if (entity.getType() != this.type) return false;

        switch (this) {

        case CREEPER_POWERED:
            return ((Creeper) entity).isPowered();

        case MINECART_STORAGE:
            return entity instanceof StorageMinecart;

        case MINECART_POWERED:
            return entity instanceof PoweredMinecart;

        case SLIME_TINY:
            return ((Slime) entity).getSize() == 1;

        case SLIME_SMALL:
            return ((Slime) entity).getSize() == 2;

        case SLIME_BIG:
            return ((Slime) entity).getSize() == 4;

        case MAGMA_CUBE_TINY:
            return ((MagmaCube) entity).getSize() == 1;

        case MAGMA_CUBE_SMALL:
            return ((MagmaCube) entity).getSize() == 2;

        case MAGMA_CUBE_BIG:
            return ((MagmaCube) entity).getSize() == 4;

        }

        return false;
    }
}
