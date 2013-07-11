package net.minecraft.src;

import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.Isolator;

/*
            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2004 Sam Hocevar <sam@hocevar.net> 

 Everyone is permitted to copy and distribute verbatim or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

  0. You just DO WHAT THE FUCK YOU WANT TO. 
*/

public class PFReaderMLP extends PFReader4P
{
	public PFReaderMLP(Isolator isolator)
	{
		super(isolator);
	}
	
	@Override
	public void generateFootsteps(EntityPlayer ply)
	{
		//if (true)
		//	throw new Minecraft161NotYetFixedRuntimeException();
		
		// recomment on fix
		//Pony pony = Pony.getPonyFromRegistry(ply, this.mod.manager().getMinecraft().renderEngine);
		//this.isPegasus = pony != null ? pony.isPegasus() : false;
		
		super.generateFootsteps(ply);
	}
}
