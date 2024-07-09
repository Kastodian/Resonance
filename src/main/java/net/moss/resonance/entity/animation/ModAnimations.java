package net.moss.resonance.entity.animation;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;

public class ModAnimations {
    public static final Animation LOST_SOUL = Animation.Builder.create(6f).looping()
    .addBoneAnimation("outer",
	    new Transformation(Transformation.Targets.ROTATE,
		    new Keyframe(0f, AnimationHelper.createRotationalVector(-45f, 0f, -45f), Transformation.Interpolations.CUBIC),
	    	new Keyframe(2f, AnimationHelper.createRotationalVector(0f, -360f, 0f),	Transformation.Interpolations.CUBIC),
    		new Keyframe(4f, AnimationHelper.createRotationalVector(0f, 0f, 0f), Transformation.Interpolations.CUBIC),
		    new Keyframe(6f, AnimationHelper.createRotationalVector(-45f, 0f, -45f), Transformation.Interpolations.CUBIC)))
    .addBoneAnimation("inner",
	    new Transformation(Transformation.Targets.ROTATE,
		    new Keyframe(0f, AnimationHelper.createRotationalVector(90f, 0f, -90f), Transformation.Interpolations.CUBIC),
		    new Keyframe(2f, AnimationHelper.createRotationalVector(0f, 0f, 0f), Transformation.Interpolations.CUBIC),
    		new Keyframe(4f, AnimationHelper.createRotationalVector(0f, -180f, -90f), Transformation.Interpolations.CUBIC),
		    new Keyframe(6f, AnimationHelper.createRotationalVector(90f, 0f, -90f), Transformation.Interpolations.CUBIC)))
    .addBoneAnimation("core",
	    new Transformation(Transformation.Targets.ROTATE,
		    new Keyframe(0f, AnimationHelper.createRotationalVector(0f, 0f, 0f), Transformation.Interpolations.CUBIC),
    		new Keyframe(2f, AnimationHelper.createRotationalVector(0f, -360f, -180f), Transformation.Interpolations.CUBIC),
		    new Keyframe(4f, AnimationHelper.createRotationalVector(180f, 0f, -180f), Transformation.Interpolations.CUBIC),
    		new Keyframe(6f, AnimationHelper.createRotationalVector(0f, -360f, 0f), Transformation.Interpolations.CUBIC)
    )).build();
}
