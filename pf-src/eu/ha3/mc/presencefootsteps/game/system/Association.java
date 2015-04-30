package eu.ha3.mc.presencefootsteps.game.system;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class Association {
	
	private IBlockState blockState;
	private String data;
	
	private boolean isPrimative = false;
	private boolean noAssociation = false;
	
	public int x;
	public int y;
	public int z;
	
	public Association() {
	}
	
	public Association(String raw) {
		data = raw;
	}
	
	public Association(IBlockState state, int xx, int yy, int zz) {
		init(state, xx, yy, zz);
	}
	
	public Association init(IBlockState state, int xx, int yy, int zz) {
		blockState = state;
		x = xx;
		y = yy;
		z = zz;
		return this;
	}
	
	public String getData() {
		return data;
	}
	
	public Association setAssociation(String association) {
		data = association;
		return this;
	}
	
	public Association setNoAssociation() {
		noAssociation = true;
		return this;
	}
	
	public boolean getNoAssociation() {
		return noAssociation;
	}
	
	public Association setPrimative(String primative) {
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
	
	public int getMeta() {
		return getBlock().getMetaFromState(blockState);
	}
	
	public boolean isNotEmitter() {
		return data.contentEquals("NOT_EMITTER");
	}
}
