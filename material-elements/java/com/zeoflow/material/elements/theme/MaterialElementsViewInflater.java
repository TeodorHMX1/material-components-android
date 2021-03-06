/*
 * Copyright 2020 ZeoFlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zeoflow.material.elements.theme;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatViewInflater;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.zeoflow.material.elements.button.MaterialButton;
import com.zeoflow.material.elements.checkbox.MaterialCheckBox;
import com.zeoflow.material.elements.radiobutton.MaterialRadioButton;
import com.zeoflow.material.elements.textfield.MaterialAutoCompleteTextView;
import com.zeoflow.material.elements.textview.MaterialTextView;

/**
 * An extension of {@link AppCompatViewInflater} that replaces some framework widgets with Material
 * elements ones at inflation time, provided a Material Elements theme is in use.
 */
public class MaterialElementsViewInflater extends AppCompatViewInflater
{
  @NonNull
  @Override
  protected AppCompatButton createButton(@NonNull Context context, @NonNull AttributeSet attrs)
  {
    return new MaterialButton(context, attrs);
  }

  @NonNull
  @Override
  protected AppCompatCheckBox createCheckBox(Context context, AttributeSet attrs)
  {
    return new MaterialCheckBox(context, attrs);
  }

  @NonNull
  @Override
  protected AppCompatRadioButton createRadioButton(Context context, AttributeSet attrs)
  {
    return new MaterialRadioButton(context, attrs);
  }

  @NonNull
  @Override
  protected AppCompatTextView createTextView(Context context, AttributeSet attrs)
  {
    return new MaterialTextView(context, attrs);
  }

  @NonNull
  @Override
  protected AppCompatAutoCompleteTextView createAutoCompleteTextView(
      @NonNull Context context, @Nullable AttributeSet attrs)
  {
    return new MaterialAutoCompleteTextView(context, attrs);
  }
}
