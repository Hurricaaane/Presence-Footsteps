package eu.ha3.presencefootsteps.world;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;

public class Association {

    public static final Association NOT_EMITTER = new Association();

    private final BlockState blockState;

    private final BlockPos pos;

    private String data = Emitter.NOT_EMITTER;

    private Entity source;

    public Association() {
        this(Blocks.AIR.getDefaultState(), BlockPos.ORIGIN);
    }

    public Association(BlockState state, BlockPos pos) {
        blockState = state;
        this.pos = pos;
    }

    public Association at(Entity source) {

        if (!isNull()) {
            this.source = source;
        }

        return this;
    }

    public Association with(@Nullable String data) {

        if (!isNull() && data != null) {
            this.data = data;
        }

        return this;
    }

    public boolean isNull() {
        return this == NOT_EMITTER;
    }

    public boolean isNotEmitter() {
        return isNull() || Emitter.isNonEmitter(data);
    }

    public boolean hasAssociation() {
        return !isNotEmitter() && Emitter.isResult(data);
    }

    public String getAcousticName() {
        return data;
    }

    public Entity getSource() {
        return source;
    }

    public Material getMaterial() {
        return blockState.getMaterial();
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockSoundGroup getSoundGroup() {
        return blockState.getSoundGroup();
    }
}
