package com.jve.repsup.utils;

import android.text.Editable;
import android.text.TextWatcher;

/** A TextWatcher that only reports the new text, for simple search boxes. */
public class SimpleTextWatcher implements TextWatcher {

    public interface OnTextChanged {
        void onTextChanged(String text);
    }

    private final OnTextChanged callback;

    public SimpleTextWatcher(OnTextChanged callback) {
        this.callback = callback;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        callback.onTextChanged(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
