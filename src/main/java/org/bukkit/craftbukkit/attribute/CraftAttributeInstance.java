package org.bukkit.craftbukkit.attribute;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.EquipmentSlot;

public class CraftAttributeInstance implements AttributeInstance {
    private final net.minecraft.world.entity.ai.attributes.AttributeInstance handle;
    private final Attribute attribute;

    public CraftAttributeInstance(net.minecraft.world.entity.ai.attributes.AttributeInstance handle, Attribute attribute) {
        this.handle = handle;
        this.attribute = attribute;
    }

    @Override
    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public double getBaseValue() {
        return handle.getBaseValue();
    }

    @Override
    public void setBaseValue(double d) {
        handle.setBaseValue(d);
    }

    @Override
    public Collection<AttributeModifier> getModifiers() {
        List<AttributeModifier> result = new ArrayList<AttributeModifier>();
        for (net.minecraft.world.entity.ai.attributes.AttributeModifier nms : handle.getModifiers()) {
            result.add(convert(nms));
        }

        return result;
    }

    @Override
    public void addModifier(AttributeModifier modifier) {
        Preconditions.checkArgument(modifier != null, "modifier");
        handle.addPermanentModifier(convert(modifier));
    }

    @Override
    public void removeModifier(AttributeModifier modifier) {
        Preconditions.checkArgument(modifier != null, "modifier");
        handle.removeModifier(convert(modifier));
    }

    @Override
    public double getValue() {
        return handle.getValue();
    }

    @Override
    public double getDefaultValue() {
        return handle.getAttribute().value().getDefaultValue();
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeModifier convert(AttributeModifier bukkit) {
        return new net.minecraft.world.entity.ai.attributes.AttributeModifier(CraftNamespacedKey.toMinecraft(bukkit.getKey()), bukkit.getAmount(), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.values()[bukkit.getOperation().ordinal()]);
    }

    public static AttributeModifier convert(net.minecraft.world.entity.ai.attributes.AttributeModifier nms) {
        return new AttributeModifier(CraftNamespacedKey.fromMinecraft(nms.id()), nms.amount(), AttributeModifier.Operation.values()[nms.operation().ordinal()], org.bukkit.inventory.EquipmentSlotGroup.ANY);
    }

    public static AttributeModifier convert(net.minecraft.world.entity.ai.attributes.AttributeModifier nms, EquipmentSlot slot) {
        return new AttributeModifier(CraftNamespacedKey.fromMinecraft(nms.id()), nms.amount(), AttributeModifier.Operation.values()[nms.operation().ordinal()], slot.getGroup());
    }
}
