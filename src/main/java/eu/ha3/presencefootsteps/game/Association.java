package eu.ha3.presencefootsteps.game;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class Association {
	
	private IBlockState blockState;
	
	private int x;
	private int y;
	private int z;
	
	private String data = null;
	
	private boolean noAssociation = false;
	private boolean isPrimative = false;
	
	public Association(String raw) {
		setAssociation(raw);
	}
	
	public Association(IBlockState state, int xx, int yy, int zz) {
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
		noAssociation = false;
		return this;
	}
	
	public Association setNoAssociation() {
		noAssociation = true;
		return this;
	}
	
	public boolean getNoAssociation() {
		return noAssociation;
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
	
	public boolean isNotEmitter() {
		return data != null && data.contentEquals("NOT_EMITTER");
	}
	
	public BlockPos pos(int offX, int offY, int offZ) {
		return new BlockPos(x + offX, y + offY, z + offZ);
	}
}
