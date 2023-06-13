package net.reimaden.arcadiandream.item.custom.tools;





import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.reimaden.arcadiandream.damage.ModDamageSources;
import net.reimaden.arcadiandream.networking.ModMessages;
import net.reimaden.arcadiandream.sound.ModSounds;
import net.reimaden.arcadiandream.util.IEntityDataSaver;
import net.reimaden.arcadiandream.util.StaminaHelper;


public class RoukankenItem extends SwordItem {

boolean isDashing = false;

int dashTimer;

    public RoukankenItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);

    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (selected) {

            //This is fucking retarded. I shouldn't have to do this. Fuck you.
            //Cancels fall damage during dashes. Wanted to have it still be there if you fall a certain amount but my hand was forced.
            if (!entity.isOnGround() && isDashing) {
                entity.fallDistance = 0F;
            } else {
                isDashing = false;
            }

            //Makeshift timer for particles.
            if (isDashing) {
                if (dashTimer > 30) {
                    generateTrail(world, (PlayerEntity) entity);
                } else {
                    dashTimer++;
                }
            }

        }
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

if (!world.isClient && hand == Hand.MAIN_HAND){

    ItemStack stack = user.getMainHandStack();
    NbtCompound nbt;
    if (stack.hasNbt()) {
        nbt = stack.getNbt();
    } else {
        nbt = new NbtCompound();
    }

//Dash Function
    if (StaminaHelper.getStamina((IEntityDataSaver) user) >= 30 && !user.isSneaking() && nbt.getByte("sheathed") == 0) {
        isDashing = true;

        StaminaHelper.changeStamina((IEntityDataSaver) user, -30);
        PacketByteBuf buffer = PacketByteBufs.create();
        ServerPlayNetworking.send((ServerPlayerEntity) user, ModMessages.ROUKANKEN_DASH, buffer);

        if (user.isOnGround()){
            playSound(world, user, ModSounds.ROUKANKEN_DASH_1);
        } else {
            playSound(world, user, ModSounds.ROUKANKEN_DASH_2);
        }
        SliceNDice(world, user, user.getMainHandStack());
    }

    //Shift + RC to sheathe and unsheathe the sword, default is sheathed.



    if (user.isSneaking()) {
        //Sheathe or unsheathe
        if (nbt.getByte("sheathed") == 1) {
            nbt.putByte("sheathed", (byte) 0);
            playSound(world, user, ModSounds.ROUKANKEN_UNSHEATHE);
        } else {
            nbt.putByte("sheathed", (byte) 1);
            playSound(world, user, ModSounds.ROUKANKEN_SHEATHE);
        }
        stack.setNbt(nbt);
        }
}
        return super.use(world, user, hand);
    }

    private void generateTrail(World world, PlayerEntity user){
            boolean hasFireAspect = EnchantmentHelper.getFireAspect(user) > 0;
                    if (hasFireAspect) {
                        world.addParticle(ParticleTypes.LAVA, user.getX(), user.getY() + 1, user.getZ(), 0.0, 0.0, 0.0);
                    } else {
                        world.addParticle(ParticleTypes.ENCHANTED_HIT, user.getX(), user.getY() + 1, user.getZ(), 0.0, 0.0, 0.0);
                    }

    }

    private static void playSound(World world, PlayerEntity user, SoundEvent sound) {
        float pitch = 0.9f + user.getRandom().nextFloat() * 0.2f;
        world.playSound(null, user.getBlockPos(), sound,
                user.getSoundCategory(), 1f, pitch);
    }

    private void SliceNDice(World world, PlayerEntity user, ItemStack item){
        double offset = 2;
        for (MobEntity mob : world.getNonSpectatingEntities(MobEntity.class,
                user.getBoundingBox().expand(2.5, 2, 2.5).offset(new Vec3d(
                        user.getRotationVector().getX() * offset,
                        user.getRotationVector().getY() * offset,
                        user.getRotationVector().getZ() * offset)))) {
                mob.damage(ModDamageSources.danmaku(world, mob, user), 8 + (2 * EnchantmentHelper.getLevel(Enchantments.SHARPNESS, item)));
            }
        }
    }



