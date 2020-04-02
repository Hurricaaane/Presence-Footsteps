package eu.ha3.presencefootsteps.sound.acoustics;

import com.google.gson.JsonObject;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.util.Period;

class DelayedAcoustic extends VaryingAcoustic implements Options {

    private final Period delay = new Period(0);

    public DelayedAcoustic(JsonObject json, AcousticsJsonParser context) {
        super(json, context);

        if (json.has("delay")) {
            delay.set(json.get("delay").getAsLong());
        } else {
            delay.set(json.get("delay_min").getAsLong(), json.get("delay_max").getAsLong());
        }
    }

    @Override
    protected Options getOptions() {
        return this;
    }

    @Override
    public boolean containsKey(Object option) {
        return "delay_min".equals(option)
            || "delay_max".equals(option);
    }

    @Override
    public long get(String option) {
        return "delay_min".equals(option) ? delay.min
             : "delay_max".equals(option) ? delay.max
             : 0;
    }
}
