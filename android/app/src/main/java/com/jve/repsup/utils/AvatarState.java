package com.jve.repsup.utils;

/**
 * Remembers whether the user's avatar has changed during this app session.
 *
 * The locker bumps the version number every time the outfit is saved. Screens
 * that draw the avatar (the Me tab and the top-bar picture) remember the version
 * they last drew and only redraw when it has moved on, so switching tabs does not
 * re-composite the avatar every time.
 */
public final class AvatarState {

    private static int version = 0;

    private AvatarState() {
        // Utility class - no instances.
    }

    /** Call after the avatar's outfit, skin tone, body or hair has been saved. */
    public static synchronized void markChanged() {
        version++;
    }

    /** The current avatar version; compare with the version a screen last drew. */
    public static synchronized int version() {
        return version;
    }
}
