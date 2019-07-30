package com.brohoof.minelittlepony;

/**
 * Duck class - https://github.com/MineLittlePony/MineLittlePony
 *
 */
public abstract class MineLittlePony {
    public static MineLittlePony getInstance() {
        return null;
    }

    public abstract PonyManager getManager();
    
    public abstract PonyConfig getConfig();
}