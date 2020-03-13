package com.bullhead.equalizer;

import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;

import androidx.annotation.Nullable;

public class Settings {
    public static boolean isEqualizerEnabled = false;
    public static boolean isEqualizerReloaded = true;
    public static int[] seekbarpos = new int[5];
    public static int presetPos;
    public static short reverbPreset = 0, bassStrength = 0, virtualizerStrength = 0;
    public static double ratio = 1.0;
    public static boolean isEditing = false;

    public static EqualizerModel equalizerModel;

    public static Equalizer equalizer;

    public static BassBoost bassBoost;

    public static PresetReverb presetReverb;

    public static Virtualizer virtualizer;
}
