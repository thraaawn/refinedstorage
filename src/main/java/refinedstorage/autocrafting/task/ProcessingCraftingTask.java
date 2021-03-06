package refinedstorage.autocrafting.task;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import refinedstorage.RefinedStorageUtils;
import refinedstorage.autocrafting.CraftingPattern;
import refinedstorage.tile.TileCrafter;
import refinedstorage.tile.controller.TileController;

public class ProcessingCraftingTask implements ICraftingTask {
    public static final int ID = 1;

    public static final String NBT_INSERTED = "Inserted";
    public static final String NBT_CHILD_TASKS = "ChildTasks";
    public static final String NBT_SATISFIED = "Satisfied";

    private CraftingPattern pattern;
    private boolean inserted[];
    private boolean childTasks[];
    private boolean satisfied[];
    private boolean updatedOnce;

    public ProcessingCraftingTask(CraftingPattern pattern) {
        this.pattern = pattern;
        this.inserted = new boolean[pattern.getInputs().length];
        this.childTasks = new boolean[pattern.getInputs().length];
        this.satisfied = new boolean[pattern.getOutputs().length];
    }

    public ProcessingCraftingTask(NBTTagCompound tag, CraftingPattern pattern) {
        this.pattern = pattern;
        this.inserted = RefinedStorageUtils.readBooleanArray(tag, NBT_INSERTED);
        this.childTasks = RefinedStorageUtils.readBooleanArray(tag, NBT_CHILD_TASKS);
        this.satisfied = RefinedStorageUtils.readBooleanArray(tag, NBT_SATISFIED);
    }

    @Override
    public CraftingPattern getPattern() {
        return pattern;
    }

    @Override
    public boolean update(TileController controller) {
        this.updatedOnce = true;

        TileCrafter crafter = pattern.getCrafter(controller.getWorld());
        IItemHandler handler = RefinedStorageUtils.getItemHandler(crafter.getFacingTile(), crafter.getDirection().getOpposite());

        if (handler != null) {
            for (int i = 0; i < inserted.length; ++i) {
                if (!inserted[i]) {
                    ItemStack input = pattern.getInputs()[i];
                    ItemStack took = controller.take(input, 1);

                    if (took != null) {
                        if (ItemHandlerHelper.insertItem(handler, took, true) == null) {
                            ItemHandlerHelper.insertItem(handler, took, false);

                            inserted[i] = true;
                        } else {
                            controller.push(took, took.stackSize, false);
                        }
                    } else if (!childTasks[i]) {
                        CraftingPattern pattern = controller.getPatternWithBestScore(input);

                        if (pattern != null) {
                            childTasks[i] = true;

                            controller.addCraftingTask(controller.createCraftingTask(pattern));

                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        } else {
            return true;
        }

        for (int i = 0; i < satisfied.length; ++i) {
            if (!satisfied[i]) {
                return false;
            }
        }

        return true;
    }

    public void onPushed(ItemStack inserted) {
        for (int i = 0; i < pattern.getOutputs().length; ++i) {
            if (!satisfied[i] && RefinedStorageUtils.compareStackNoQuantity(inserted, pattern.getOutputs()[i])) {
                satisfied[i] = true;

                return;
            }
        }
    }

    @Override
    public void onDone(TileController controller) {
        // NO OP
    }

    @Override
    public void onCancelled(TileController controller) {
        // NO OP
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagCompound patternTag = new NBTTagCompound();
        pattern.writeToNBT(patternTag);
        tag.setTag(CraftingPattern.NBT, patternTag);

        RefinedStorageUtils.writeBooleanArray(tag, NBT_INSERTED, inserted);
        RefinedStorageUtils.writeBooleanArray(tag, NBT_CHILD_TASKS, childTasks);
        RefinedStorageUtils.writeBooleanArray(tag, NBT_SATISFIED, satisfied);

        tag.setInteger("Type", ID);
    }

    @Override
    public String getInfo() {
        if (!updatedOnce) {
            return "T=gui.refinedstorage:crafting_monitor.not_started_yet";
        }

        StringBuilder builder = new StringBuilder();

        boolean hasMissingItems = false;

        for (int i = 0; i < pattern.getInputs().length; ++i) {
            ItemStack input = pattern.getInputs()[i];

            if (!inserted[i] && !childTasks[i]) {
                if (!hasMissingItems) {
                    builder.append("I=gui.refinedstorage:crafting_monitor.missing_items\n");

                    hasMissingItems = true;
                }

                builder.append("T=").append(input.getUnlocalizedName()).append(".name\n");
            }
        }

        boolean areItemsCrafting = false;

        for (int i = 0; i < pattern.getInputs().length; ++i) {
            ItemStack input = pattern.getInputs()[i];

            if (!inserted[i] && childTasks[i]) {
                if (!areItemsCrafting) {
                    builder.append("I=gui.refinedstorage:crafting_monitor.items_crafting\n");

                    areItemsCrafting = true;
                }

                builder.append("T=").append(input.getUnlocalizedName()).append(".name\n");
            }
        }

        boolean areItemsProcessing = false;

        for (int i = 0; i < pattern.getInputs().length; ++i) {
            ItemStack input = pattern.getInputs()[i];

            if (inserted[i]) {
                if (!areItemsProcessing) {
                    builder.append("I=gui.refinedstorage:crafting_monitor.items_processing\n");

                    areItemsProcessing = true;
                }

                builder.append("T=").append(input.getUnlocalizedName()).append(".name\n");
            }
        }

        return builder.toString();
    }
}
