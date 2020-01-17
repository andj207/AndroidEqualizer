package com.bullhead.equalizer;

import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;

public class Settings {
    public static boolean isEqualizerEnabled = true;
    public static boolean isEqualizerReloaded = true;
    public static int[] seekbarpos = new int[5];
    public static int presetPos;
    public static short reverbPreset = -1, bassStrength = -1;
    public static EqualizerModel equalizerModel;
    public static double ratio = 1.0;
    public static boolean isEditing = false;

    public static Equalizer equalizer;
    public static BassBoost bassBoost;
    public static PresetReverb presetReverb;
}
