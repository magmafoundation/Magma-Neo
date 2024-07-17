package org.bukkit.craftbukkit.block;

import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.TileState;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftBlockEntityState<T extends BlockEntity> extends CraftBlockState implements TileState {
    private final T tileEntity;
    private final T snapshot;

    public CraftBlockEntityState(World world, T tileEntity) {
        super(world, tileEntity.getBlockPos(), tileEntity.getBlockState());

        this.tileEntity = tileEntity;

        // copy tile entity data:
        this.snapshot = this.createSnapshot(tileEntity);
        this.load(snapshot);
    }

    protected CraftBlockEntityState(CraftBlockEntityState<T> state, Location location) {
        super(state, location);
        this.tileEntity = createSnapshot(state.snapshot);
        this.snapshot = tileEntity;
        loadData(state.getSnapshotNBT());
    }

    public void refreshSnapshot() {
        this.load(tileEntity);
    }

    private RegistryAccess getRegistryAccess() {
        LevelAccessor worldHandle = getWorldHandle();
        return (worldHandle != null) ? worldHandle.registryAccess() : MinecraftServer.getDefaultRegistryAccess();
    }

    private T createSnapshot(T tileEntity) {
        if (tileEntity == null) {
            return null;
        }

        CompoundTag nbtTagCompound = tileEntity.saveWithFullMetadata(getRegistryAccess());
        T snapshot = (T) BlockEntity.loadStatic(getPosition(), getHandle(), nbtTagCompound, getRegistryAccess());

        return snapshot;
    }

    public Set<DataComponentType<?>> applyComponents(DataComponentMap datacomponentmap, DataComponentPatch datacomponentpatch) {
        Set<DataComponentType<?>> result = snapshot.applyComponentsSet(datacomponentmap, datacomponentpatch);
        load(snapshot);
        return result;
    }

    public DataComponentMap collectComponents() {
        return snapshot.collectComponents();
    }

    // Loads the specified data into the snapshot BlockEntity.
    public void loadData(CompoundTag nbtTagCompound) {
        snapshot.loadWithComponents(nbtTagCompound, getRegistryAccess());
        load(snapshot);
    }

    // copies the BlockEntity-specific data, retains the position
    private void copyData(T from, T to) {
        CompoundTag nbtTagCompound = from.saveWithFullMetadata(getRegistryAccess());
        to.loadWithComponents(nbtTagCompound, getRegistryAccess());
    }

    // gets the wrapped BlockEntity
    protected T getTileEntity() {
        return tileEntity;
    }

    // gets the cloned BlockEntity which is used to store the captured data
    protected T getSnapshot() {
        return snapshot;
    }

    // gets the current BlockEntity from the world at this position
    protected BlockEntity getTileEntityFromWorld() {
        requirePlaced();

        return getWorldHandle().getBlockEntity(this.getPosition());
    }

    // gets the NBT data of the BlockEntity represented by this block state
    public CompoundTag getSnapshotNBT() {
        // update snapshot
        applyTo(snapshot);

        return snapshot.saveWithFullMetadata(getRegistryAccess());
    }

    public CompoundTag getSnapshotNBTWithoutComponents() {
        CompoundTag nbt = getSnapshotNBT();
        snapshot.removeComponentsFromTag(nbt);
        return nbt;
    }

    // copies the data of the given tile entity to this block state
    protected void load(T tileEntity) {
        if (tileEntity != null && tileEntity != snapshot) {
            copyData(tileEntity, snapshot);
        }
    }

    // applies the BlockEntity data of this block state to the given BlockEntity
    protected void applyTo(T tileEntity) {
        if (tileEntity != null && tileEntity != snapshot) {
            copyData(snapshot, tileEntity);
        }
    }

    protected boolean isApplicable(BlockEntity tileEntity) {
        return tileEntity != null && this.tileEntity.getClass() == tileEntity.getClass();
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);

        if (result && this.isPlaced()) {
            BlockEntity tile = getTileEntityFromWorld();

            if (isApplicable(tile)) {
                applyTo((T) tile);
                tile.setChanged();
            }
        }

        return result;
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return this.getSnapshot().persistentDataContainer;
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket(@NotNull Location location) {
        T vanillaTileEntitiy = (T) BlockEntity.loadStatic(CraftLocation.toBlockPosition(location), getHandle(), getSnapshotNBT(), getRegistryAccess());
        return ClientboundBlockEntityDataPacket.create(vanillaTileEntitiy);
    }

    @Override
    public CraftBlockEntityState<T> copy() {
        return new CraftBlockEntityState<>(this, null);
    }

    @Override
    public CraftBlockEntityState<T> copy(Location location) {
        return new CraftBlockEntityState<>(this, location);
    }
}
