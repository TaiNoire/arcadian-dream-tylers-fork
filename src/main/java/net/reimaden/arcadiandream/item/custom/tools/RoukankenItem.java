package net.reimaden.arcadiandream.item.custom.tools;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
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
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.reimaden.arcadiandream.ArcadianDream;
import net.reimaden.arcadiandream.damage.ModDamageSources;
import net.reimaden.arcadiandream.networking.ModMessages;
import net.reimaden.arcadiandream.sound.ModSounds;
import net.reimaden.arcadiandream.util.IEntityDataSaver;
import net.reimaden.arcadiandream.util.StaminaHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RoukankenItem extends SwordItem {

boolean isDashing = false;
int GroundDashActivation = 0;
boolean isGroundDashing = false;
int dashTimer;


    public RoukankenItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (selected && !world.isClient) {

            /*This mess is required to make dashing from the ground activate isDashing because there are a few
            ticks where the player is still on the ground before achieving lift off which cancels the dash.
            Furthermore, GroundDashActivation is used to make sure that the dash is cancelled when using
            the dash on the ground but not actually moving anywhere.*/
            if (isGroundDashing) {
                if (entity.isOnGround() || entity.isInLava()){
                    if (GroundDashActivation >= 2){
                        isGroundDashing = false;
                    } else {
                     isDashing = true;
                    }
                } else {
                    isDashing = true;
                    isGroundDashing = false;
                    }
                }

            //Cancel Fall damage when dashing and create particle
            if (isDashing) {
                if (!entity.isOnGround()) {
                    entity.fallDistance = 0F;
                    if (dashTimer > 7) {
                        //TODO: Fix Roukanken Particles
                        //generateTrail(world, (PlayerEntity) entity);
                    } else {
                        dashTimer++;
                    }
                } else {
                    isDashing = false;
                }
                GroundDashActivation++;
            }
        }
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack stack = user.getMainHandStack();
        NbtCompound nbt;
        if (stack.hasNbt()) {
            nbt = stack.getNbt();
        } else {
            nbt = new NbtCompound();
        }

if (!world.isClient && hand == Hand.MAIN_HAND && nbt != null){

//Dash Function
    if (StaminaHelper.getStamina((IEntityDataSaver) user) >= 30 && !user.isSneaking() && nbt.getByte("sheathed") == 0) {

        if (!user.isCreative()){
            StaminaHelper.changeStamina((IEntityDataSaver) user, -30);
        }

        PacketByteBuf buffer = PacketByteBufs.create();
        ServerPlayNetworking.send((ServerPlayerEntity) user, ModMessages.ROUKANKEN_DASH, buffer);
        SliceNDice(world, user, user.getMainHandStack());
        if (user.isOnGround() || user.isInLava()){
            playSound(world, user, ModSounds.ROUKANKEN_DASH_1);
            isGroundDashing = true;
            GroundDashActivation = 0;
        } else {
            playSound(world, user, ModSounds.ROUKANKEN_DASH_2);
            isDashing = true;
        }

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

    @Override //Change atk stats depending on state
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        final Multimap<EntityAttribute, EntityAttributeModifier> multimap = HashMultimap.create();
        NbtCompound nbt;
        if (stack.hasNbt()) {
            nbt = stack.getNbt();
        } else {
            nbt = new NbtCompound();
        }
        if (nbt != null)
        {
            if (slot == EquipmentSlot.MAINHAND && nbt.getByte("sheathed") == 0){
                multimap.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier",
                                10,
                                EntityAttributeModifier.Operation.ADDITION));
            } else if (slot == EquipmentSlot.MAINHAND && nbt.getByte("sheathed") == 1) {
                multimap.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier",
                                0,
                                EntityAttributeModifier.Operation.ADDITION));
            }
        }

        return multimap;
    }


    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item." + ArcadianDream.MOD_ID + ".roukanken.tooltip"));
            tooltip.add(Text.translatable("item." + ArcadianDream.MOD_ID + ".roukanken.tooltip_2"));
        } else {
            tooltip.add(Text.translatable("item." + ArcadianDream.MOD_ID + ".shot.tooltip"));
        }

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



