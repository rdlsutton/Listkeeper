package com.radlsuttonedmn.listkeeper;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Custom EditText to support OnTouchListener which overrides performClick to support accessibility
 */

public class OnTouchEditText extends androidx.appcompat.widget.AppCompatEditText {

    // Constructor with only the context as a parameter
    public OnTouchEditText(Context context) {
        super(context);
    }

    // Constructor with an attribute set as a parameter
    public OnTouchEditText(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    // Constructor with an attribute set and style attributes as parameters
    public OnTouchEditText(Context context, AttributeSet attributes, int definedStyleAttribute) {
        super(context, attributes, definedStyleAttribute);
    }

    // Override the parent class's onClick method
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
