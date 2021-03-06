package refinedstorage.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import refinedstorage.RefinedStorageItems;
import refinedstorage.RefinedStorageUtils;
import refinedstorage.item.ItemWirelessGrid;
import refinedstorage.tile.grid.TileGrid;

public class MessageWirelessGridSettingsUpdate extends MessageHandlerPlayerToServer<MessageWirelessGridSettingsUpdate> implements IMessage {
    private int hand;
    private int sortingDirection;
    private int sortingType;
    private int searchBoxMode;

    public MessageWirelessGridSettingsUpdate() {
    }

    public MessageWirelessGridSettingsUpdate(int hand, int sortingDirection, int sortingType, int searchBoxMode) {
        this.hand = hand;
        this.sortingDirection = sortingDirection;
        this.sortingType = sortingType;
        this.searchBoxMode = searchBoxMode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        hand = buf.readInt();
        sortingDirection = buf.readInt();
        sortingType = buf.readInt();
        searchBoxMode = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(hand);
        buf.writeInt(sortingDirection);
        buf.writeInt(sortingType);
        buf.writeInt(searchBoxMode);
    }

    @Override
    public void handle(MessageWirelessGridSettingsUpdate message, EntityPlayerMP player) {
        ItemStack held = player.getHeldItem(RefinedStorageUtils.getHandById(message.hand));

        if (held != null && held.getItem() == RefinedStorageItems.WIRELESS_GRID && held.getTagCompound() != null) {
            if (TileGrid.isValidSortingDirection(message.sortingDirection)) {
                held.getTagCompound().setInteger(ItemWirelessGrid.NBT_SORTING_DIRECTION, message.sortingDirection);
            }

            if (TileGrid.isValidSortingType(message.sortingType)) {
                held.getTagCompound().setInteger(ItemWirelessGrid.NBT_SORTING_TYPE, message.sortingType);
            }

            if (TileGrid.isValidSearchBoxMode(message.searchBoxMode)) {
                held.getTagCompound().setInteger(ItemWirelessGrid.NBT_SEARCH_BOX_MODE, message.searchBoxMode);
            }
        }
    }
}
