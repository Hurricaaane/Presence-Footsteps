package eu.ha3.presencefootsteps.sound.acoustics;

import com.google.gson.JsonObject;

import eu.ha3.presencefootsteps.config.Options;
import eu.ha3.presencefootsteps.resources.AcousticsJsonReader;
import eu.ha3.presencefootsteps.sound.Period;

public class DelayedAcoustic extends VaryingAcoustic implements Options {

    protected final Period delay = new Period(0);

    public DelayedAcoustic(JsonObject json, AcousticsJsonReader context) {
        super(json, context);
        outputOptions = this;

        if (json.has("delay")) {
            getDelayRange().set(json.get("delay").getAsLong());
        } else {
            getDelayRange().set(json.get("delay_min").getAsLong(), json.get("delay_max").getAsLong());
        }
    }

    public Period getDelayRange() {
        return delay;
    }

    @Override
    public boolean containsKey(String option) {
        return option.equals("delay_min") || option.equals("delay_max");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long get(String option) {
        return option.equals("delay_min") ? delay.min : option.equals("delay_max") ? delay.max : null;
    }

    @Override
    public DelayedAcoustic withOption(String option, Object value) {
        if (option.equals("delay_min")) {
            delay.min = (Long) value;
        }
        if (option.equals("delay_max")) {
            delay.max = (Long) value;
        }
        return this;
    }
}
