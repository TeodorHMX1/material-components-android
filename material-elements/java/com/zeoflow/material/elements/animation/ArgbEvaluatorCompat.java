
package com.zeoflow.material.elements.animation;

import android.animation.TypeEvaluator;
import androidx.annotation.NonNull;


public class ArgbEvaluatorCompat implements TypeEvaluator<Integer> {
  private static final ArgbEvaluatorCompat instance = new ArgbEvaluatorCompat();

  
  @NonNull
  public static ArgbEvaluatorCompat getInstance() {
    return instance;
  }

  
  @NonNull
  @Override
  public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
    int startInt = startValue;
    float startA = ((startInt >> 24) & 0xff) / 255.0f;
    float startR = ((startInt >> 16) & 0xff) / 255.0f;
    float startG = ((startInt >> 8) & 0xff) / 255.0f;
    float startB = (startInt & 0xff) / 255.0f;

    int endInt = endValue;
    float endA = ((endInt >> 24) & 0xff) / 255.0f;
    float endR = ((endInt >> 16) & 0xff) / 255.0f;
    float endG = ((endInt >> 8) & 0xff) / 255.0f;
    float endB = (endInt & 0xff) / 255.0f;

    
    startR = (float) Math.pow(startR, 2.2);
    startG = (float) Math.pow(startG, 2.2);
    startB = (float) Math.pow(startB, 2.2);

    endR = (float) Math.pow(endR, 2.2);
    endG = (float) Math.pow(endG, 2.2);
    endB = (float) Math.pow(endB, 2.2);

    
    float a = startA + fraction * (endA - startA);
    float r = startR + fraction * (endR - startR);
    float g = startG + fraction * (endG - startG);
    float b = startB + fraction * (endB - startB);

    
    a = a * 255.0f;
    r = (float) Math.pow(r, 1.0 / 2.2) * 255.0f;
    g = (float) Math.pow(g, 1.0 / 2.2) * 255.0f;
    b = (float) Math.pow(b, 1.0 / 2.2) * 255.0f;

    return Math.round(a) << 24 | Math.round(r) << 16 | Math.round(g) << 8 | Math.round(b);
  }
}
