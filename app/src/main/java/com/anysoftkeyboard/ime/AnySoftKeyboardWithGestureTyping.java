package com.anysoftkeyboard.ime;

import android.content.SharedPreferences;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.gesturetyping.GestureTypingDebugUtils;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.gesturetyping.Point;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public abstract class AnySoftKeyboardWithGestureTyping extends AnySoftKeyboardWithQuickText {

    private boolean mGestureTypingEnabled;

    @Override
    protected void onLoadSettingsRequired(SharedPreferences sharedPreferences) {
        super.onLoadSettingsRequired(sharedPreferences);
        mGestureTypingEnabled = sharedPreferences.getBoolean(getString(R.string.settings_key_gesture_typing),
                getResources().getBoolean(R.bool.settings_default_gesture_typing));
    }

    public abstract void pickSuggestionManually(int index, CharSequence suggestion);

    public abstract void setSuggestions(List<? extends CharSequence> suggestions,
                                        boolean completions, boolean typedWordValid,
                                        boolean haveMinimalSuggestion);

    @Override
    public void onGestureTypingInput(final List<Point> gestureInput, final int[] keyCodesInPath, final int keyCodesInPathLength) {
        InputConnection ic = getCurrentInputConnection();

        if (mGestureTypingEnabled && ic != null) {
            if (gestureInput.size() > 1) {

                if (GestureTypingDebugUtils.DEBUG) {
                    GestureTypingDebugUtils.DEBUG_INPUT.clear();
                    // Avoid introducing referencing bugs
                    for (Point p : gestureInput) GestureTypingDebugUtils.DEBUG_INPUT.add(new Point(p.x, p.y));
                }

                final boolean isShifted = mShiftKeyState.isActive();
                final boolean isCapsLocked = mShiftKeyState.isLocked();

                List<CharSequence> wordsInPath = mSuggest.getWordsForPath(isShifted, isCapsLocked, keyCodesInPath, keyCodesInPathLength);
                List<? extends CharSequence> gestureTypingPossibilities = GestureTypingDetector.getGestureWords(gestureInput, wordsInPath, getCurrentAlphabetKeyboard().getKeys());
                if (gestureTypingPossibilities.size() > 0) {
                    mWord.setTypedWord(gestureTypingPossibilities.get(0));
                    mWord.setPreferredWord(gestureTypingPossibilities.get(0));
                    ic.setComposingText(mWord.getPreferredWord(), 1);
                    if (gestureTypingPossibilities.size() > 1) {
                        setCandidatesViewShown(true);
                        setSuggestions(gestureTypingPossibilities, false, true, true);
                    }
                }

                if (GestureTypingDebugUtils.DEBUG) {
                    if (!gestureTypingPossibilities.isEmpty())
                        GestureTypingDebugUtils.DEBUG_WORD = gestureTypingPossibilities.get(0);
                    else
                        GestureTypingDebugUtils.DEBUG_WORD = "";

                    GestureTypingDebugUtils.DEBUG_KEYS = getCurrentAlphabetKeyboard().getKeys();
                }
            }
        }
    }
}