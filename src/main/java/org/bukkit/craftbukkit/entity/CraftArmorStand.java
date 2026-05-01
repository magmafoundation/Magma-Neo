package org.bukkit.craftbukkit.entity;

import net.minecraft.core.Rotations;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class CraftArmorStand extends CraftLivingEntity implements ArmorStand {
    public CraftArmorStand(CraftServer server, net.minecraft.world.entity.decoration.ArmorStand entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftArmorStand";
    }

    @Override
    public net.minecraft.world.entity.decoration.ArmorStand getHandle() {
        return (net.minecraft.world.entity.decoration.ArmorStand) super.getHandle();
    }

    @Override
    public ItemStack getItemInHand() {
        return getEquipment().getItemInHand();
    }

    @Override
    public void setItemInHand(ItemStack item) {
        getEquipment().setItemInHand(item);
    }

    @Override
    public ItemStack getBoots() {
        return getEquipment().getBoots();
    }

    @Override
    public void setBoots(ItemStack item) {
        getEquipment().setBoots(item);
    }

    @Override
    public ItemStack getLeggings() {
        return getEquipment().getLeggings();
    }

    @Override
    public void setLeggings(ItemStack item) {
        getEquipment().setLeggings(item);
    }

    @Override
    public ItemStack getChestplate() {
        return getEquipment().getChestplate();
    }

    @Override
    public void setChestplate(ItemStack item) {
        getEquipment().setChestplate(item);
    }

    @Override
    public ItemStack getHelmet() {
        return getEquipment().getHelmet();
    }

    @Override
    public void setHelmet(ItemStack item) {
        getEquipment().setHelmet(item);
    }

    @Override
    public EulerAngle getBodyPose() {
        return fromNMS(getHandle().bodyPose);
    }

    @Override
    public void setBodyPose(EulerAngle pose) {
        getHandle().setBodyPose(toNMS(pose));
    }

    @Override
    public EulerAngle getLeftArmPose() {
        return fromNMS(getHandle().leftArmPose);
    }

    @Override
    public void setLeftArmPose(EulerAngle pose) {
        getHandle().setLeftArmPose(toNMS(pose));
    }

    @Override
    public EulerAngle getRightArmPose() {
        return fromNMS(getHandle().rightArmPose);
    }

    @Override
    public void setRightArmPose(EulerAngle pose) {
        getHandle().setRightArmPose(toNMS(pose));
    }

    @Override
    public EulerAngle getLeftLegPose() {
        return fromNMS(getHandle().leftLegPose);
    }

    @Override
    public void setLeftLegPose(EulerAngle pose) {
        getHandle().setLeftLegPose(toNMS(pose));
    }

    @Override
    public EulerAngle getRightLegPose() {
        return fromNMS(getHandle().rightLegPose);
    }

    @Override
    public void setRightLegPose(EulerAngle pose) {
        getHandle().setRightLegPose(toNMS(pose));
    }

    @Override
    public EulerAngle getHeadPose() {
        return fromNMS(getHandle().headPose);
    }

    @Override
    public void setHeadPose(EulerAngle pose) {
        getHandle().setHeadPose(toNMS(pose));
    }

    @Override
    public boolean hasBasePlate() {
        return !getHandle().isNoBasePlate();
    }

    @Override
    public void setBasePlate(boolean basePlate) {
        getHandle().setNoBasePlate(!basePlate);
    }

    @Override
    public void setGravity(boolean gravity) {
        super.setGravity(gravity);
        // Armor stands are special
        getHandle().noPhysics = !gravity;
    }

    @Override
    public boolean isVisible() {
        return !getHandle().isInvisible();
    }

    @Override
    public void setVisible(boolean visible) {
        getHandle().setInvisible(!visible);
    }

    @Override
    public boolean hasArms() {
        return getHandle().isShowArms();
    }

    @Override
    public void setArms(boolean arms) {
        getHandle().setShowArms(arms);
    }

    @Override
    public boolean isSmall() {
        return getHandle().isSmall();
    }

    @Override
    public void setSmall(boolean small) {
        getHandle().setSmall(small);
    }

    private static EulerAngle fromNMS(Rotations old) {
        return new EulerAngle(
                Math.toRadians(old.getX()),
                Math.toRadians(old.getY()),
                Math.toRadians(old.getZ()));
    }

    private static Rotations toNMS(EulerAngle old) {
        return new Rotations(
                (float) Math.toDegrees(old.getX()),
                (float) Math.toDegrees(old.getY()),
                (float) Math.toDegrees(old.getZ()));
    }

    @Override
    public boolean isMarker() {
        return getHandle().isMarker();
    }

    @Override
    public void setMarker(boolean marker) {
        getHandle().setMarker(marker);
    }

    @Override
    public void addEquipmentLock(EquipmentSlot equipmentSlot, LockType lockType) {
        getHandle().disabledSlots |= (1 << CraftEquipmentSlot.getNMS(equipmentSlot).getFilterFlag() + lockType.ordinal() * 8);
    }

    @Override
    public void removeEquipmentLock(EquipmentSlot equipmentSlot, LockType lockType) {
        getHandle().disabledSlots &= ~(1 << CraftEquipmentSlot.getNMS(equipmentSlot).getFilterFlag() + lockType.ordinal() * 8);
    }

    @Override
    public boolean hasEquipmentLock(EquipmentSlot equipmentSlot, LockType lockType) {
        return (getHandle().disabledSlots & (1 << CraftEquipmentSlot.getNMS(equipmentSlot).getFilterFlag() + lockType.ordinal() * 8)) != 0;
    }

    // Paper start
    @Override
    public boolean canMove() {
        return getHandle().canMove;
    }

    @Override
    public void setCanMove(boolean move) {
        getHandle().canMove = move;
    }

    @Override
    public ItemStack getItem(org.bukkit.inventory.EquipmentSlot slot) {
        com.google.common.base.Preconditions.checkArgument(slot != null, "slot");
        com.google.common.base.Preconditions.checkArgument(slot != EquipmentSlot.BODY, "Cannot get body item");
        return getHandle().getItemBySlot(org.bukkit.craftbukkit.CraftEquipmentSlot.getNMS(slot)).asBukkitMirror();
    }

    @Override
    public void setItem(org.bukkit.inventory.EquipmentSlot slot, ItemStack item) {
        com.google.common.base.Preconditions.checkArgument(slot != null, "slot");
        com.google.common.base.Preconditions.checkArgument(slot != EquipmentSlot.BODY, "Cannot set body item");
        switch (slot) {
            case HAND:
                getEquipment().setItemInMainHand(item);
                return;
            case OFF_HAND:
                getEquipment().setItemInOffHand(item);
                return;
            case FEET:
                setBoots(item);
                return;
            case LEGS:
                setLeggings(item);
                return;
            case CHEST:
                setChestplate(item);
                return;
            case HEAD:
                setHelmet(item);
                return;
        }
        throw new UnsupportedOperationException(slot.name());
    }

    @Override
    public java.util.Set<org.bukkit.inventory.EquipmentSlot> getDisabledSlots() {
        java.util.Set<org.bukkit.inventory.EquipmentSlot> disabled = new java.util.HashSet<>();
        for (org.bukkit.inventory.EquipmentSlot slot : org.bukkit.inventory.EquipmentSlot.values()) {
            if (this.isSlotDisabled(slot)) {
                disabled.add(slot);
            }
        }
        return disabled;
    }

    @Override
    public void setDisabledSlots(org.bukkit.inventory.EquipmentSlot... slots) {
        int disabled = 0;
        for (org.bukkit.inventory.EquipmentSlot slot : slots) {
            if (slot == org.bukkit.inventory.EquipmentSlot.OFF_HAND) continue;
            net.minecraft.world.entity.EquipmentSlot nmsSlot = org.bukkit.craftbukkit.CraftEquipmentSlot.getNMS(slot);
            disabled += (1 << nmsSlot.getFilterFlag()) + (1 << (nmsSlot.getFilterFlag() + 8)) + (1 << (nmsSlot.getFilterFlag() + 16));
        }
        getHandle().disabledSlots = disabled;
    }

    @Override
    public void addDisabledSlots(org.bukkit.inventory.EquipmentSlot... slots) {
        java.util.Set<org.bukkit.inventory.EquipmentSlot> disabled = getDisabledSlots();
        java.util.Collections.addAll(disabled, slots);
        setDisabledSlots(disabled.toArray(new org.bukkit.inventory.EquipmentSlot[0]));
    }

    @Override
    public void removeDisabledSlots(org.bukkit.inventory.EquipmentSlot... slots) {
        java.util.Set<org.bukkit.inventory.EquipmentSlot> disabled = getDisabledSlots();
        for (final org.bukkit.inventory.EquipmentSlot slot : slots) disabled.remove(slot);
        setDisabledSlots(disabled.toArray(new org.bukkit.inventory.EquipmentSlot[0]));
    }

    @Override
    public boolean isSlotDisabled(org.bukkit.inventory.EquipmentSlot slot) {
        return getHandle().isDisabled(org.bukkit.craftbukkit.CraftEquipmentSlot.getNMS(slot));
    }

    @Override
    public io.papermc.paper.math.Rotations getBodyRotations() {
        return fromNMSRotations(getHandle().bodyPose);
    }

    @Override
    public void setBodyRotations(io.papermc.paper.math.Rotations rotations) {
        getHandle().setBodyPose(toNMSRotations(rotations));
    }

    @Override
    public io.papermc.paper.math.Rotations getLeftArmRotations() {
        return fromNMSRotations(getHandle().leftArmPose);
    }

    @Override
    public void setLeftArmRotations(io.papermc.paper.math.Rotations rotations) {
        getHandle().setLeftArmPose(toNMSRotations(rotations));
    }

    @Override
    public io.papermc.paper.math.Rotations getRightArmRotations() {
        return fromNMSRotations(getHandle().rightArmPose);
    }

    @Override
    public void setRightArmRotations(io.papermc.paper.math.Rotations rotations) {
        getHandle().setRightArmPose(toNMSRotations(rotations));
    }

    @Override
    public io.papermc.paper.math.Rotations getLeftLegRotations() {
        return fromNMSRotations(getHandle().leftLegPose);
    }

    @Override
    public void setLeftLegRotations(io.papermc.paper.math.Rotations rotations) {
        getHandle().setLeftLegPose(toNMSRotations(rotations));
    }

    @Override
    public io.papermc.paper.math.Rotations getRightLegRotations() {
        return fromNMSRotations(getHandle().rightLegPose);
    }

    @Override
    public void setRightLegRotations(io.papermc.paper.math.Rotations rotations) {
        getHandle().setRightLegPose(toNMSRotations(rotations));
    }

    @Override
    public io.papermc.paper.math.Rotations getHeadRotations() {
        return fromNMSRotations(getHandle().headPose);
    }

    @Override
    public void setHeadRotations(io.papermc.paper.math.Rotations rotations) {
        getHandle().setHeadPose(toNMSRotations(rotations));
    }

    private static io.papermc.paper.math.Rotations fromNMSRotations(Rotations old) {
        return io.papermc.paper.math.Rotations.ofDegrees(old.getX(), old.getY(), old.getZ());
    }

    private static Rotations toNMSRotations(io.papermc.paper.math.Rotations old) {
        return new Rotations((float) old.x(), (float) old.y(), (float) old.z());
    }

    @Override
    public boolean canTick() {
        return this.getHandle().canTick;
    }

    @Override
    public void setCanTick(final boolean tick) {
        this.getHandle().canTick = tick;
        this.getHandle().canTickSetByAPI = true;
    }
    // Paper end
}
