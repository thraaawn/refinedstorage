package refinedstorage.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import refinedstorage.block.EnumGridType;
import refinedstorage.container.slot.*;
import refinedstorage.tile.grid.IGrid;
import refinedstorage.tile.grid.TileGrid;
import refinedstorage.tile.grid.WirelessGrid;

public class ContainerGrid extends ContainerBase {
    private IGrid grid;

    private SlotGridCraftingResult craftingResultSlot;
    private SlotDisabled patternResultSlot;

    public ContainerGrid(EntityPlayer player, IGrid grid) {
        super(player);

        this.grid = grid;

        addPlayerInventory(8, (grid.getType() == EnumGridType.CRAFTING || grid.getType() == EnumGridType.PATTERN) ? 174 : 126);

        if (grid.getType() == EnumGridType.CRAFTING) {
            int x = 25;
            int y = 106;

            for (int i = 0; i < 9; ++i) {
                addSlotToContainer(new SlotGridCrafting(((TileGrid) grid).getMatrix(), i, x, y));

                x += 18;

                if ((i + 1) % 3 == 0) {
                    y += 18;
                    x = 25;
                }
            }

            addSlotToContainer(craftingResultSlot = new SlotGridCraftingResult(this, player, (TileGrid) grid, 0, 133 + 4, 120 + 4));
        } else if (grid.getType() == EnumGridType.PATTERN) {
            int x = 8;
            int y = 106;

            for (int i = 0; i < 9; ++i) {
                addSlotToContainer(new SlotSpecimenLegacy(((TileGrid) grid).getMatrix(), i, x, y, false));

                x += 18;

                if ((i + 1) % 3 == 0) {
                    y += 18;
                    x = 8;
                }
            }

            addSlotToContainer(patternResultSlot = new SlotDisabled(((TileGrid) grid).getResult(), 0, 116 + 4, 120 + 4));

            addSlotToContainer(new SlotItemHandler(((TileGrid) grid).getPatterns(), 0, 152, 104));
            addSlotToContainer(new SlotOutput(((TileGrid) grid).getPatterns(), 1, 152, 144));
        }
    }

    public IGrid getGrid() {
        return grid;
    }

    public void sendCraftingSlots() {
        for (int i = 0; i < inventorySlots.size(); ++i) {
            Slot slot = inventorySlots.get(i);

            if (slot instanceof SlotGridCrafting || slot == craftingResultSlot) {
                for (int j = 0; j < listeners.size(); ++j) {
                    listeners.get(j).sendSlotContents(this, i, slot.getStack());
                }
            }
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);

        if (!player.worldObj.isRemote && grid instanceof WirelessGrid) {
            ((WirelessGrid) grid).onClose(player);
        }
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return (slot == craftingResultSlot || slot == patternResultSlot) ? false : super.canMergeSlot(stack, slot);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        if (!player.worldObj.isRemote) {
            Slot slot = inventorySlots.get(slotIndex);

            if (slot == craftingResultSlot) {
                ((TileGrid) grid).onCraftedShift(this, player);
            } else if (slot != patternResultSlot && !(slot instanceof SlotSpecimenLegacy)) {
                ItemStack stack = inventorySlots.get(slotIndex).getStack();

                if (stack != null) {
                    inventorySlots.get(slotIndex).putStack(grid.onItemPush(player, stack));

                    detectAndSendChanges();
                }
            }
        }

        return null;
    }
}
