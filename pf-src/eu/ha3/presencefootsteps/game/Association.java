package eu.ha3.presencefootsteps.game;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class Association {
	
	private IBlockState blockState;
	
	public int x;
	public int y;
	public int z;
	
	private String data = null;
	
	private boolean noAssociation = false;
	private boolean isPrimative = false;
	
	public Association() {
	}
	
	public Association(String raw) {
		setAssociation(raw);
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
	
	public int getMeta() {
		return getBlock().getMetaFromState(blockState);
	}
	
	public boolean isNotEmitter() {
		return data != null && data.contentEquals("NOT_EMITTER");
	}
}
