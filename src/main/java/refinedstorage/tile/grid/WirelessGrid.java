package refinedstorage.tile.grid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import refinedstorage.RefinedStorage;
import refinedstorage.RefinedStorageUtils;
import refinedstorage.block.EnumGridType;
import refinedstorage.item.ItemWirelessGrid;
import refinedstorage.network.MessageWirelessGridCraftingStart;
import refinedstorage.network.MessageWirelessGridHeldItemPush;
import refinedstorage.network.MessageWirelessGridSettingsUpdate;
import refinedstorage.network.MessageWirelessGridStoragePull;
import refinedstorage.tile.ClientItem;
import refinedstorage.tile.config.IRedstoneModeConfig;
import refinedstorage.tile.controller.TileController;

import java.util.ArrayList;
import java.util.List;

public class WirelessGrid implements IGrid {
    private EnumHand hand;
    private World world;
    private BlockPos controllerPos;
    private int sortingType;
    private int sortingDirection;
    private int searchBoxMode;
    private List<ClientItem> items = new ArrayList<ClientItem>();
    private long lastUpdate;

    public WirelessGrid(World world, ItemStack stack, EnumHand hand) {
        this.hand = hand;
        this.world = world;
        this.controllerPos = new BlockPos(ItemWirelessGrid.getX(stack), ItemWirelessGrid.getY(stack), ItemWirelessGrid.getZ(stack));
        this.sortingType = ItemWirelessGrid.getSortingType(stack);
        this.sortingDirection = ItemWirelessGrid.getSortingDirection(stack);
        this.searchBoxMode = ItemWirelessGrid.getSearchBoxMode(stack);
    }

    @Override
    public EnumGridType getType() {
        return EnumGridType.NORMAL;
    }

    @Override
    public List<ClientItem> getItems() {
        return items;
    }

    @Override
    public void setItems(List<ClientItem> items) {
        this.items = items;
        this.lastUpdate = System.currentTimeMillis();
    }

    @Override
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    public ItemStack onItemPush(EntityPlayer player, ItemStack stack) {
        TileEntity tile = world.getTileEntity(controllerPos);

        if (tile instanceof TileController) {
            TileController controller = (TileController) tile;

            if (controller.canRun()) {
                controller.getWirelessGridHandler().drainEnergy(player, ItemWirelessGrid.USAGE_PUSH);

                return controller.push(stack, stack.stackSize, false);
            }
        }

        return stack;
    }

    @Override
    public void onHeldItemPush(boolean one) {
        RefinedStorage.NETWORK.sendToServer(new MessageWirelessGridHeldItemPush(controllerPos.getX(), controllerPos.getY(), controllerPos.getZ(), one));
    }

    @Override
    public void onItemPull(int id, int flags) {
        RefinedStorage.NETWORK.sendToServer(new MessageWirelessGridStoragePull(controllerPos.getX(), controllerPos.getY(), controllerPos.getZ(), id, flags));
    }

    public void onClose(EntityPlayer player) {
        TileEntity tile = player.worldObj.getTileEntity(controllerPos);

        if (tile instanceof TileController) {
            ((TileController) tile).getWirelessGridHandler().handleClose(player);
        }
    }

    @Override
    public int getSortingType() {
        return sortingType;
    }

    @Override
    public int getSortingDirection() {
        return sortingDirection;
    }

    @Override
    public int getSearchBoxMode() {
        return searchBoxMode;
    }

    @Override
    public void onSortingTypeChanged(int type) {
        RefinedStorage.NETWORK.sendToServer(new MessageWirelessGridSettingsUpdate(RefinedStorageUtils.getIdFromHand(hand), getSortingDirection(), type, getSearchBoxMode()));

        this.sortingType = type;
    }

    @Override
    public void onSortingDirectionChanged(int direction) {
        RefinedStorage.NETWORK.sendToServer(new MessageWirelessGridSettingsUpdate(RefinedStorageUtils.getIdFromHand(hand), direction, getSortingType(), getSearchBoxMode()));

        this.sortingDirection = direction;
    }

    @Override
    public void onSearchBoxModeChanged(int searchBoxMode) {
        RefinedStorage.NETWORK.sendToServer(new MessageWirelessGridSettingsUpdate(RefinedStorageUtils.getIdFromHand(hand), getSortingDirection(), getSortingType(), searchBoxMode));

        this.searchBoxMode = searchBoxMode;
    }

    @Override
    public void onCraftingRequested(int id, int quantity) {
        RefinedStorage.NETWORK.sendToServer(new MessageWirelessGridCraftingStart(controllerPos.getX(), controllerPos.getY(), controllerPos.getZ(), id, quantity));
    }

    @Override
    public IRedstoneModeConfig getRedstoneModeSetting() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return System.currentTimeMillis() - lastUpdate < 1000;
    }
}
