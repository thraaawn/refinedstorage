package refinedstorage.autocrafting.task;

import net.minecraft.nbt.NBTTagCompound;
import refinedstorage.autocrafting.CraftingPattern;
import refinedstorage.tile.controller.TileController;

public interface ICraftingTask {
    CraftingPattern getPattern();

    boolean update(TileController controller);

    void onDone(TileController controller);

    void onCancelled(TileController controller);

    void writeToNBT(NBTTagCompound tag);

    String getInfo();
}
