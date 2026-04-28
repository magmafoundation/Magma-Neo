package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.craftbukkit.util.CraftChatMessage;

public class CraftMerchantCustom implements CraftMerchant {
    private MinecraftMerchant merchant;

    @Deprecated // Paper - Adventure
    public CraftMerchantCustom(String title) {
        this.merchant = new MinecraftMerchant(title);
        getMerchant().craftMerchant = this;
    }

    // Paper start
    public CraftMerchantCustom(net.kyori.adventure.text.Component title) {
        this.merchant = new MinecraftMerchant(title);
        getMerchant().craftMerchant = this;
    }
    // Paper end

    @Override
    public String toString() {
        return "CraftMerchantCustom";
    }

    @Override
    public MinecraftMerchant getMerchant() {
        return this.merchant;
    }

    public static class MinecraftMerchant implements Merchant {
        private final Component title;
        private final MerchantOffers trades = new MerchantOffers();
        private Player tradingPlayer;
        protected CraftMerchant craftMerchant;

        @Deprecated // Paper - Adventure
        public MinecraftMerchant(String title) {
            Preconditions.checkArgument(title != null, "Title cannot be null");
            this.title = CraftChatMessage.fromString(title)[0];
        }

        // Paper start
        public MinecraftMerchant(net.kyori.adventure.text.Component title) {
            Preconditions.checkArgument(title != null, "Title cannot be null");
            this.title = io.papermc.paper.adventure.PaperAdventure.asVanilla(title);
        }
        // Paper end

        @Override
        public CraftMerchant getCraftMerchant() {
            return craftMerchant;
        }

        @Override
        public void setTradingPlayer(Player entityhuman) {
            this.tradingPlayer = entityhuman;
        }

        @Override
        public Player getTradingPlayer() {
            return this.tradingPlayer;
        }

        @Override
        public MerchantOffers getOffers() {
            return this.trades;
        }

        @Override
        public void notifyTrade(MerchantOffer merchantrecipe) {
            // increase recipe's uses
            merchantrecipe.increaseUses();
        }

        @Override
        public void notifyTradeUpdated(ItemStack itemstack) {}

        public Component getScoreboardDisplayName() {
            return title;
        }

        @Override
        public int getVillagerXp() {
            return 0; // xp
        }

        @Override
        public void overrideXp(int i) {}

        @Override
        public boolean showProgressBar() {
            return false; // is-regular-villager flag (hides some gui elements: xp bar, name suffix)
        }

        @Override
        public SoundEvent getNotifyTradeSound() {
            return SoundEvents.VILLAGER_YES;
        }

        @Override
        public void overrideOffers(MerchantOffers merchantrecipelist) {}

        @Override
        public boolean isClientSide() {
            return false;
        }
    }
}
