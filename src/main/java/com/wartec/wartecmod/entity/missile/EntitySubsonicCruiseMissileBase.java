package com.wartec.wartecmod.entity.missile;

import api.hbm.entity.IRadarDetectable;
import com.hbm.entity.grenade.EntityGrenadeSmart;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.main.MainRegistry;
import com.wartec.wartecmod.tileentity.vls.TileEntityVlsExhaust;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

import java.util.ArrayList;
import java.util.List;

public abstract class EntitySubsonicCruiseMissileBase extends Entity implements IChunkLoader, IRadarDetectable {
	
	int startX;
	int startY;
	int startZ;
	int targetX;
	int targetY;
	int targetZ;
	int velocity;
	double positionvectorCruise;
    double transformationpointvector;
    double startsonicspeed;
    double Range;
	double decelY;
	double accelXZ;
	boolean isSubsonic = false;
	boolean isCluster = false;
    private Ticket loaderTicket;
    public int health = 10;
    protected TileEntityVlsExhaust exhaust = null;
	boolean hasReachedMaxHeight = false;


	public EntitySubsonicCruiseMissileBase(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
		startX = (int) posX;
		startY = (int) posY;
		startZ = (int) posZ;
		targetX = (int) posX;
		targetY = (int) posY;
		targetZ = (int) posZ;
	}
	
    public boolean canBeCollidedWith()
    {
        return true;
    }
    
    public boolean attackEntityFrom(DamageSource src, float dmg)
    {
        if (this.isEntityInvulnerable())
        {
            return false;
        }
        else
        {
            if (!this.isDead && !this.worldObj.isRemote)
            {
            	health -= dmg;
            	
                if (this.health <= 0)
                {
                    this.setDead();
                    this.killMissile();
                }
            }

            return true;
        }
    }
    
    
    private void killMissile() {
        ExplosionLarge.explode(worldObj, posX, posY, posZ, 5, true, false, true);
        ExplosionLarge.spawnShrapnelShower(worldObj, posX, posY, posZ, motionX, motionY, motionZ, 15, 0.075);
        ExplosionLarge.spawnMissileDebris(worldObj, posX, posY, posZ, motionX, motionY, motionZ, 0.25, getDebris(), getDebrisRareDrop());
    }

	public EntitySubsonicCruiseMissileBase(World world, float x, float y, float z, int a, int b, int c, TileEntityVlsExhaust exh) {
		super(world);
		this.ignoreFrustumCheck = true;
		/*this.posX = x;
		this.posY = y;
		this.posZ = z;*/
		this.setLocationAndAngles(x, y, z, 0, 0);
		startX = (int) x;
		startY = (int) y;
		startZ = (int) z;
		targetX = a;
		targetY = b;
		targetZ = c;
		this.exhaust = exh;
		

		
	    Range = (Math.sqrt(((targetX - startX)*(targetX - startX)) + ((targetY - startY)*(targetY - startY)) + ((targetZ - startZ)*(targetZ - startZ))));
		
		transformationpointvector = (Math.sqrt(((targetX - startX)*(targetX - startX)) + ((targetY - startY)*(targetY - startY)) + ((targetZ - startZ)*(targetZ - startZ))))*0.15;
		
		startsonicspeed = transformationpointvector*1.34;
		
		
		this.motionY = 0.25;
		
        Vec3 vector = Vec3.createVectorHelper(targetX - startX, targetY - startY, targetZ - startZ);
		accelXZ = decelY = 1/vector.lengthVector();
		decelY *= 0.25;
			
		velocity = 1;

        this.setSize(1.5F, 1.5F);
	}

	@Override
	protected void entityInit() {
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, worldObj, Type.ENTITY));
		this.dataWatcher.addObject(8, Integer.valueOf(this.health));
		this.dataWatcher.addObject(9, 1);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		motionX = nbt.getDouble("moX");
		motionY = nbt.getDouble("moY");
		motionZ = nbt.getDouble("moZ");
		posX = nbt.getDouble("poX");
		posY = nbt.getDouble("poY");
		posZ = nbt.getDouble("poZ");
		decelY = nbt.getDouble("decel");
		accelXZ = nbt.getDouble("accel");
		targetX = nbt.getInteger("tX");
		targetY = nbt.getInteger("tY");
		targetZ = nbt.getInteger("tZ");
		startX = nbt.getInteger("sX");
		startY = nbt.getInteger("sY");
		startZ = nbt.getInteger("sZ");
		velocity = nbt.getInteger("veloc");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setDouble("moX", motionX);
		nbt.setDouble("moY", motionY);
		nbt.setDouble("moZ", motionZ);
		nbt.setDouble("poX", posX);
		nbt.setDouble("poY", posY);
		nbt.setDouble("poZ", posZ);
		nbt.setDouble("decel", decelY);
		nbt.setDouble("accel", accelXZ);
		nbt.setInteger("tX", targetX);
		nbt.setInteger("tY", targetY);
		nbt.setInteger("tZ", targetZ);
		nbt.setInteger("sX", startX);
		nbt.setInteger("sY", startY);
		nbt.setInteger("sZ", startZ);
		nbt.setInteger("veloc", velocity);
	}
	
	protected void rotation() {
        float f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.rotationPitch = (float)(Math.atan2(this.motionY, f2) * 180.0D / Math.PI) -90; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }
	}
	private void spawnExhaust(double x, double y, double z) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "exhaust");
		data.setString("mode", "soyuz");
		if(exhaust == null) {
			data.setInteger("count", 5);
			data.setDouble("width", worldObj.rand.nextDouble() * 0.25 - 0.5);
		} else {
			data.setInteger("count", 2);
			data.setDouble("width", worldObj.rand.nextDouble() * 0.25 - 0.8);

			NBTTagCompound vep = new NBTTagCompound();
			vep.setString("type", "exhaust");
			vep.setString("mode", "soyuz");
			vep.setInteger("count", 1);
			vep.setDouble("width", worldObj.rand.nextDouble() * 0.25 - 0.8);
			vep.setDouble("posX", exhaust.xCoord);
			vep.setDouble("posY", exhaust.yCoord+11);
			vep.setDouble("posZ", exhaust.zCoord);
			MainRegistry.proxy.effectNT(vep);
		}
		data.setDouble("posX", x);
		data.setDouble("posY", y);
		data.setDouble("posZ", z);
		
		MainRegistry.proxy.effectNT(data);
	}

	public double getTargetHeight() {
		return this.targetY;
	}

	public double[] getTarget() {
		return new double[] { this.targetX, this.targetY, this.targetZ };
	}

	public void setTarget(int a, int b, int c) {
		this.targetX = a;
		this.targetY = b;
		this.targetZ = c;
	}


	@Override
	public void onUpdate() {
		// Calculate the distance to the target
		double distanceToTarget = Math.sqrt(Math.pow(targetX - posX, 2) + Math.pow(targetZ - posZ, 2));

		// 1. Climb to Y = 270 fast
		if (posY < 270 && !hasReachedMaxHeight) {
			motionY = 3;
			if (posY >= 270) {
				hasReachedMaxHeight = true;
			}
		}
		// 2. Move to the target coordinates (U, 270, O) at a velocity of 1
		else if (distanceToTarget > 50) {
			motionY = 0;
			posY = 270;
			Vec3 vector = Vec3.createVectorHelper(targetX - posX, 0, targetZ - posZ);
			vector = vector.normalize();
			vector.xCoord *= 1;
			vector.zCoord *= 1;
			motionX = vector.xCoord;
			motionZ = vector.zCoord;
		}
		// 3. Slowly descend to the ground level until Y=60
		else if (posY > 60) {
			motionY = -0.5;
		} else {
			motionY = 0;
		}

		// Update the position of the missile
		posX += motionX;
		posY += motionY;
		posZ += motionZ;

		// Check if the missile has hit the target or a block
		if (this.worldObj.getBlock((int) this.posX, (int) this.posY, (int) this.posZ) != Blocks.air &&
				this.worldObj.getBlock((int) this.posX, (int) this.posY, (int) this.posZ) != Blocks.water &&
				this.worldObj.getBlock((int) this.posX, (int) this.posY, (int) this.posZ) != Blocks.flowing_water) {

			if (!this.worldObj.isRemote) {
				onImpact();
			}
			this.setDead();
		}
	}

	public void BombletSplit() {
		if (motionY <= 0) {

			if (worldObj.isRemote)
				return;

			this.setDead();

				for (int i = 0; i < 50; i++) {

					EntityGrenadeSmart grenade = new EntityGrenadeSmart(worldObj);
					grenade.posX = posX;
					grenade.posY = posY;
					grenade.posZ = posZ;
					grenade.motionX = motionX + rand.nextGaussian() * 0.25D;
					grenade.motionY = motionY + rand.nextGaussian() * 0.25D;
					grenade.motionZ = motionZ + rand.nextGaussian() * 0.25D;
					grenade.ticksExisted = 10;

					worldObj.spawnEntityInWorld(grenade);
				}
			}
		}
	
	
    
	
	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 500000;
    }
    
	public abstract void onImpact();


	public abstract List<ItemStack> getDebris();
	
	public abstract ItemStack getDebrisRareDrop();
	
	public void cluster() { }
	
	public void init(Ticket ticket) {
		if(!worldObj.isRemote) {
			
            if(ticket != null) {
            	
                if(loaderTicket == null) {
                	
                	loaderTicket = ticket;
                	loaderTicket.bindEntity(this);
                	loaderTicket.getModData();
                }

                ForgeChunkManager.forceChunk(loaderTicket, new ChunkCoordIntPair(chunkCoordX, chunkCoordZ));
            }
        }
	}

	List<ChunkCoordIntPair> loadedChunks = new ArrayList<ChunkCoordIntPair>();

	public void loadNeighboringChunks(int newChunkX, int newChunkZ) {

	}
    

    private void MissileToCruiseMissile() {
		ExplosionLarge.spawnParticles(worldObj, posX, posY, posZ, 7);
		this.dataWatcher.updateObject(9, 2);
    }

}