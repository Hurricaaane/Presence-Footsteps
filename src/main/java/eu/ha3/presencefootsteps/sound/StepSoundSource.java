package eu.ha3.presencefootsteps.sound;

import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.generator.StepSoundGenerator;
import net.minecraft.entity.LivingEntity;

public interface StepSoundSource {
    StepSoundGenerator getStepGenerator(SoundEngine engine);

    final class Container implements StepSoundSource {
        private Locomotion locomotion;
        private StepSoundGenerator stepSoundGenerator;

        private final LivingEntity entity;

        public Container(LivingEntity entity) {
            this.entity = entity;
        }

        @Override
        public StepSoundGenerator getStepGenerator(SoundEngine engine) {
            Locomotion loco = engine.getLocomotion(entity);

            if (stepSoundGenerator == null || loco != locomotion) {
                locomotion = loco;
                stepSoundGenerator = loco.supplyGenerator();
            }
            return stepSoundGenerator;
        }
    }
}
