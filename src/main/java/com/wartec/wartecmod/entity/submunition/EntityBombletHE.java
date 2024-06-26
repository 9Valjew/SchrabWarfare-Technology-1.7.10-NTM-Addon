package com.wartec.wartecmod.entity.submunition;

import com.hbm.entity.grenade.EntityGrenadeBase;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.items.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityBombletHE extends EntityGrenadeBase {

    public EntityBombletHE(World p_i1773_1_)
    {
        super(p_i1773_1_);
    }

    public EntityBombletHE(World p_i1774_1_, EntityLivingBase p_i1774_2_)
    {
        super(p_i1774_1_, p_i1774_2_);
    }

    public EntityBombletHE(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_)
    {
        super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
    }

    @Override
    public void explode() {

        if (!this.worldObj.isRemote)
        {
            this.setDead();

            if(this.ticksExisted > 10)
                ExplosionLarge.explode(worldObj, posX, posY, posZ, 2.5F, true, true, true);
        }
    }
}