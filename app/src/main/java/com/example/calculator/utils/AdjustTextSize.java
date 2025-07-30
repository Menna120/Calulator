package com.example.calculator.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

public class AdjustTextSize {
    public void setupTextSizeAdjustment(final TextView textView, final HorizontalScrollView scrollView, final float maxSize, final float minSize) {
        textView.getViewTreeObserver().addOnGlobalLayoutListener(() -> adjustTextSize(textView, scrollView, maxSize, minSize));
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adjustTextSize(textView, scrollView, maxSize, minSize);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void adjustTextSize(TextView textView, HorizontalScrollView scrollView, float initialSize, float minSize) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, initialSize);
        float currentSize = initialSize;

        int availableWidth = scrollView.getWidth() - scrollView.getPaddingLeft() - scrollView.getPaddingRight();

        if (availableWidth <= 0) {
            return;
        }

        float textWidth = textView.getPaint().measureText(textView.getText().toString());

        while (textWidth > availableWidth && currentSize > minSize) {
            currentSize -= 1f;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize);
            textWidth = textView.getPaint().measureText(textView.getText().toString());
        }

        if (textWidth <= availableWidth && currentSize < initialSize) {
            float nextSize = currentSize + 1f;
            if (nextSize <= initialSize) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, nextSize);
                textWidth = textView.getPaint().measureText(textView.getText().toString());
                if (textWidth > availableWidth) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize);
                }
            }
        }
    }
}
