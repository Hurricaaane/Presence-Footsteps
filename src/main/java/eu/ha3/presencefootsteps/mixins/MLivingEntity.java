package eu.ha3.presencefootsteps.mixins;

import org.spongepowered.asm.mixin.Mixin;

import eu.ha3.presencefootsteps.sound.SoundEngine;
import eu.ha3.presencefootsteps.sound.StepSoundSource;
import eu.ha3.presencefootsteps.sound.generator.StepSoundGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntity.class)
abstract class MLivingEntity extends Entity implements StepSoundSource {
    MLivingEntity() {super(null, null);}
    private final StepSoundSource stepSoundSource = new StepSoundSource.Container((LivingEntity)(Object)this);
    @Override
    public StepSoundGenerator getStepGenerator(SoundEngine engine) {
        return stepSoundSource.getStepGenerator(engine);
    }
}
