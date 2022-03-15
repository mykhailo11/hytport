package org.hyt.hytport.visual.util

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible

class HYTAnimationUtil {

    companion object{

        fun getAnimator(update: (ValueAnimator) -> Unit): ValueAnimator {
            val animator: ValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            animator.duration = 400;
            animator.interpolator = AccelerateDecelerateInterpolator();
            animator.addUpdateListener(update);
            return animator;
        }

        fun animateVisibility(view: View): Unit{
            if (view.isVisible){
                view.alpha = 1.0f;
                val animator: ValueAnimator = getAnimator {
                    val value: Float = it.animatedValue as Float;
                    view.alpha = 1.0f - value;
                }
                animator.doOnEnd {
                    view.visibility = View.GONE
                }
                animator.start();
            }else{
                view.alpha = 0.0f;
                view.visibility = View.VISIBLE;
                val animator: ValueAnimator = getAnimator {
                    val value: Float = it.animatedValue as Float;
                    view.alpha = value;
                }
                animator.start();
            }
        }

    }

}