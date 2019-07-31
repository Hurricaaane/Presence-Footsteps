package eu.ha3.presencefootsteps.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;

public class Association {

    private final BlockState blockState;

    private final int x;
    private final int y;
    private final int z;

    private String data = null;

    private boolean hasAssociation = false;
    private boolean isPrimative = false;

    public Association(String raw) {
        this(Blocks.AIR.getDefaultState(), 0, 0, 0);
        setAssociation(raw);
    }

    public Association(BlockState state, int xx, int yy, int zz) {
        blockState = state;
        x = xx;
        y = yy;
        z = zz;
    }

    public String getData() {
        return data;
    }

    public Association setAssociation(String association) {
        data = association;
        hasAssociation = true;
        return this;
    }

    public Association setNoAssociation() {
        hasAssociation = false;
        return this;
    }

    public boolean hasAssociation() {
        return hasAssociation;
    }

    public Association setPrimitive(String primative) {
        data = primative;
        isPrimative = true;
        return this;
    }

    public boolean isPrimative() {
        return isPrimative;
    }

    public Block getBlock() {
        return blockState.getBlock();
    }

    public Material getMaterial() {
        return blockState.getMaterial();
    }

    public BlockState getState() {
        return blockState;
    }

    public BlockSoundGroup getSoundGroup() {
        return blockState.getSoundGroup();
    }

    public boolean isNotEmitter() {
        return data != null && data.contentEquals("NOT_EMITTER");
    }

    public BlockPos pos(int offX, int offY, int offZ) {
        return new BlockPos(x + offX, y + offY, z + offZ);
    }
}
