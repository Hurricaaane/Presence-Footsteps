package eu.ha3.presencefootsteps.world;

import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LegacyBlockLookup extends StateLookup {

    private static final Logger logger = LogManager.getLogger("PFLegacy");

    @Override
    public void add(String key, String value) {
        try {
            int endOfNumber = key.indexOf('^');
            if (endOfNumber == -1) {
                endOfNumber = key.indexOf('.');
            }
            if (endOfNumber == -1) {
                endOfNumber = key.length();
            }
            String number = key.substring(0, endOfNumber);
            int id = Integer.parseInt(number);

            Block o = Registry.BLOCK.get(id);

            String fullKeyRebuild = Registry.BLOCK.getId(o)
                    + (endOfNumber == key.length() ? "" : key.substring(endOfNumber));

            super.add(fullKeyRebuild, value);

            logger.debug("Adding legacy key: " + fullKeyRebuild + " for " + key);
        } catch (NumberFormatException e) {
            super.add(key, value);
        }
    }
}
