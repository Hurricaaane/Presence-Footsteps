package eu.ha3.presencefootsteps.gui;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import eu.ha3.presencefootsteps.main.PFHaddon;
import eu.ha3.presencefootsteps.util.PFHelper;
import eu.ha3.util.property.simple.ConfigProperty;

public class BlockReport {
	private final PFHaddon mod;
	private ConfigProperty results;
	
	public BlockReport(PFHaddon haddon) {
		mod = haddon;
	}
	
	public BlockReport generateReport() {
		results = new ConfigProperty();
		for (Object o : Block.REGISTRY) {
			Block block = (Block) o;
			results.setProperty(PFHelper.nameOf(block), getSoundData(block) + getClassData(block));
		}
		results.commit();
		return this;
	}
	
	public BlockReport generateUnknownReport() {
		results = new ConfigProperty();
		for (Object o : Block.REGISTRY) {
			Block block = (Block) o;
			if (!mod.getIsolator().getBlockMap().hasEntryForBlock(block)) {
				results.setProperty(PFHelper.nameOf(block), getSoundData(block) + getClassData(block));
			}
		}
		results.commit();
		return this;
	}
	
	private String getSoundData(Block block) {
		if (block.getSoundType() == null) {
			return "NULL";
		}
		if (block.getSoundType().getStepSound() == null) {
			return "NO_SOUND";
		}
		return block.getSoundType().getStepSound().getSoundName().getResourcePath();
	}
	
	private String getClassData(Block block) {
		String soundName = "";
		if (block instanceof BlockLiquid) soundName += "," + "EXTENDS_LIQUID";
		if (block instanceof BlockBush) soundName += "," + "EXTENDS_BUSH";
		if (block instanceof BlockDoublePlant) soundName += "," + "EXTENDS_DOUBLE_PLANT";
		if (block instanceof BlockCrops) soundName += "," + "EXTENDS_CROPS";
		if (block instanceof BlockContainer) soundName += "," + "EXTENDS_CONTAINER";
		if (block instanceof BlockLeaves) soundName += "," + "EXTENDS_LEAVES";
		if (block instanceof BlockRailBase) soundName += "," + "EXTENDS_RAIL";
		if (block instanceof BlockSlab) soundName += "," + "EXTENDS_SLAB";
		if (block instanceof BlockBasePressurePlate) soundName += "," + "EXTENDS_PRESSURE_PLATE";
		if (block instanceof BlockStairs) soundName += "," + "EXTENDS_STAIRS";
		if (block instanceof BlockBreakable) soundName += "," + "EXTENDS_BREAKABLE";
		if (block instanceof BlockFalling) soundName += "," + "EXTENDS_PHYSICALLY_FALLING";
		if (block instanceof BlockPane) soundName += "," + "EXTENDS_PANE";
		if (block instanceof BlockRotatedPillar) soundName += "," + "EXTENDS_PILLAR";
		if (block instanceof BlockTorch) soundName += "," + "EXTENDS_TORCH";
		if (block instanceof BlockCarpet) soundName += "," + "EXTENDS_CARPET";
		if (!block.getDefaultState().isFullyOpaque()) soundName += "," + "HITBOX";
		return soundName;
	}
	
	public void printResults(String location, String ext) {
		File loc = null;
		int counter = 0;
		while (loc == null || loc.exists()) {
			loc = new File(mod.getUtility().getMcFolder(), location + (counter == 0 ? "" : "_" + counter) + ext);
			counter++;
		}
		results.setSource(loc.getAbsolutePath());
		results.save();
		
		TextComponentString link = new TextComponentString(loc.getName());
		link.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, loc.getAbsolutePath()));
        link.getStyle().setUnderlined(Boolean.valueOf(true));
		mod.getChatter().printChat(TextFormatting.GREEN, new TextComponentTranslation("pf.report.save", link));
	}
}
