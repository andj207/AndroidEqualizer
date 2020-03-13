package com.bullhead.equalizer;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class EqualizerFragment extends Fragment {

    private static final String ARG_AUDIO_SESSION_ID = "audio_session_id";

    private LineSet dataset;
    private LineChartView chart;
    private float[] points;

    private int y = 0;

    private SeekBar[] seekBarFinal = new SeekBar[5];
    private Spinner presetSpinner;
    private Context ctx;

    public EqualizerFragment() {
        // Required empty public constructor
    }

    private Equalizer equalizer;
    private BassBoost bassBoost;
    private PresetReverb presetReverb;
    private Virtualizer virtualizer;

    private int audioSessionId = 0;

    static int themeColor = Color.parseColor("#B24242");
    private static int backgroundColor = Color.DKGRAY;
    private static boolean showBackButton = true;

    public static EqualizerFragment newInstance(int audioSessionId) {

        Bundle args = new Bundle();
        args.putInt(ARG_AUDIO_SESSION_ID, audioSessionId);

        EqualizerFragment fragment = new EqualizerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Settings.isEditing = true;

            if (getArguments() != null && getArguments().containsKey(ARG_AUDIO_SESSION_ID)){
                audioSessionId = getArguments().getInt(ARG_AUDIO_SESSION_ID);
            }

            if (Settings.equalizerModel == null){
                Settings.equalizerModel = new EqualizerModel();
                Settings.equalizerModel.setReverbPreset(PresetReverb.PRESET_NONE);
                Settings.equalizerModel.setBassStrength((short) (1000 / 19));
            }

            if (Settings.equalizer == null){
                Settings.equalizer = new Equalizer(0, audioSessionId);
            }
            equalizer = Settings.equalizer;

            if (Settings.bassBoost == null){
                Settings.bassBoost = new BassBoost(0, audioSessionId);
            }
            bassBoost = Settings.bassBoost;

            if (Settings.virtualizer == null){
                Settings.virtualizer = new Virtualizer(0, audioSessionId);
            }
            virtualizer = Settings.virtualizer;

            if (Settings.presetReverb == null){
                Settings.presetReverb = new PresetReverb(0, audioSessionId);
            }
            presetReverb = Settings.presetReverb;
        }
        catch (Throwable e){
            Log.e("Equalizer Fragment", e.getMessage(), e);
            if (getActivity() != null){
                Toast.makeText(getActivity(), "Could not setup Equalizer. Please reopen to try again!", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            View backgroundView = view.findViewById(R.id.equalizer_content);
            backgroundView.setBackgroundColor(backgroundColor);

            ImageView backBtn = view.findViewById(R.id.equalizer_back_btn);
            backBtn.setVisibility(showBackButton ? View.VISIBLE : View.GONE);
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
            });

            SwitchCompat equalizerSwitch = view.findViewById(R.id.equalizer_switch);
            equalizerSwitch.setChecked(Settings.isEqualizerEnabled);

            int[][] states = new int[][] {
                    new int[] { android.R.attr.state_activated },
                    new int[] { android.R.attr.state_checked },
                    new int[] { android.R.attr.state_selected },
                    new int[] { -android.R.attr.state_selected },
                    new int[] { -android.R.attr.state_checked },
                    new int[]{}
            };

            int[] colors = new int[] {
                    themeColor,
                    themeColor,
                    themeColor,
                    Color.WHITE,
                    Color.WHITE,
                    Color.WHITE
            };

            ColorStateList myTintList = new ColorStateList(states, colors);
            equalizerSwitch.setThumbTintList(myTintList);
            equalizerSwitch.setTrackTintList(myTintList);

            equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    equalizer.setEnabled(isChecked);
                    bassBoost.setEnabled(isChecked);
                    virtualizer.setEnabled(isChecked);
                    presetReverb.setEnabled(isChecked);
                    Settings.isEqualizerEnabled = isChecked;
                    Settings.equalizerModel.setEqualizerEnabled(isChecked);
                }
            });

            ImageView spinnerDropDownIcon = view.findViewById(R.id.spinner_dropdown_icon);
            spinnerDropDownIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presetSpinner.performClick();
                }
            });

            presetSpinner = view.findViewById(R.id.equalizer_preset_spinner);

            chart = view.findViewById(R.id.lineChart);
            chart.getBackground().setTint(ColorUtils.blendARGB(backgroundColor, Color.BLACK, 0.2f));
            Paint paint = new Paint();
            dataset = new LineSet();

            AnalogController bassController = view.findViewById(R.id.controllerBass);
            AnalogController virtualizerController = view.findViewById(R.id.controller3D);
            TextView tvReverb = view.findViewById(R.id.tvReverb);
            AppCompatSpinner reverbSpinner = view.findViewById(R.id.spinnerReverb);

            bassController.setLabel("Bass Boost");
            virtualizerController.setLabel("3D");
            tvReverb.setTextColor(themeColor);


            String[] reverbs = getResources().getStringArray(R.array.reverb_arrays);

            int reverbPos = 0;

            if (Settings.reverbPreset >= 0 && Settings.reverbPreset < reverbs.length){
                reverbPos = Settings.reverbPreset;
            }

            reverbSpinner.setSelection(reverbPos);

            bassController.circlePaint.setColor(ColorUtils.setAlphaComponent(0x000, 0x33));
            bassController.circlePaint2.setColor(themeColor);
            bassController.linePaint.setColor(themeColor);
            bassController.invalidate();

            virtualizerController.circlePaint.setColor(ColorUtils.setAlphaComponent(0x000, 0x33));
            virtualizerController.circlePaint2.setColor(themeColor);
            virtualizerController.linePaint.setColor(themeColor);
            virtualizerController.invalidate();

            if (!Settings.isEqualizerReloaded) {
                int x = 0;
                if (bassBoost != null) {
                    try {
                        x = (int) Math.ceil(bassBoost.getRoundedStrength() * 19f / 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (virtualizer != null) {
                    try {
                        y = (int) Math.ceil(virtualizer.getRoundedStrength() * 19f / 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (x == 0) {
                    bassController.setProgress(1);
                } else {
                    bassController.setProgress(x);
                }

                if (y == 0) {
                    virtualizerController.setProgress(1);
                } else {
                    virtualizerController.setProgress(y);
                }
            } else {
                int x = (int) Math.ceil(Settings.bassStrength * 19f / 1000);
                y = (int) Math.ceil(Settings.virtualizerStrength * 19f / 1000);
                if (x == 0) {
                    bassController.setProgress(1);
                } else {
                    bassController.setProgress(x);
                }

                if (y == 0) {
                    virtualizerController.setProgress(1);
                } else {
                    virtualizerController.setProgress(y);
                }
            }

            reverbSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Settings.reverbPreset = (short) position;
                    Settings.equalizerModel.setReverbPreset(Settings.reverbPreset);
                    try {
                        presetReverb.setPreset((short) Settings.reverbPreset);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            bassController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
                @Override
                public void onProgressChanged(int progress) {
                    Settings.bassStrength = (short) (((float) 1000 / 19) * (progress));
                    Settings.equalizerModel.setBassStrength(Settings.bassStrength);
                    try {
                        if (bassBoost.getStrengthSupported()){
                            bassBoost.setStrength(Settings.bassStrength);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            virtualizerController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
                @Override
                public void onProgressChanged(int progress) {
                    Settings.virtualizerStrength = (short) (((float) 1000 / 19) * (progress));
                    Settings.equalizerModel.setVirtualizerStrength(Settings.virtualizerStrength);
                    try {
                        if (virtualizer.getStrengthSupported()){
                            virtualizer.setStrength(Settings.virtualizerStrength);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    y = progress;
                }
            });

            TextView equalizerHeading = new TextView(getContext());
            equalizerHeading.setText(R.string.eq);
            equalizerHeading.setTextSize(20);
            equalizerHeading.setGravity(Gravity.CENTER_HORIZONTAL);

            short numberOfFrequencyBands = 5;

            points = new float[numberOfFrequencyBands];

            final short lowerEqualizerBandLevel = equalizer.getBandLevelRange()[0];
            final short upperEqualizerBandLevel = equalizer.getBandLevelRange()[1];

            for (short i = 0; i < numberOfFrequencyBands; i++) {
                final short equalizerBandIndex = i;
                final TextView frequencyHeaderTextView = new TextView(getContext());
                frequencyHeaderTextView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                frequencyHeaderTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                frequencyHeaderTextView.setTextColor(Color.parseColor("#FFFFFF"));
                frequencyHeaderTextView.setText((equalizer.getCenterFreq(equalizerBandIndex) / 1000) + "Hz");

                LinearLayout seekBarRowLayout = new LinearLayout(getContext());
                seekBarRowLayout.setOrientation(LinearLayout.VERTICAL);

                TextView lowerEqualizerBandLevelTextView = new TextView(getContext());
                lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                lowerEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"));
                lowerEqualizerBandLevelTextView.setText((lowerEqualizerBandLevel / 100) + "dB");

                TextView upperEqualizerBandLevelTextView = new TextView(getContext());
                lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                upperEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"));
                upperEqualizerBandLevelTextView.setText((upperEqualizerBandLevel / 100) + "dB");

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                layoutParams.weight = 1;

                SeekBar seekBar = new SeekBar(getContext());
                TextView textView = new TextView(getContext());
                switch (i) {
                    case 0:
                        seekBar = view.findViewById(R.id.seekBar1);
                        textView = view.findViewById(R.id.textView1);
                        break;
                    case 1:
                        seekBar = view.findViewById(R.id.seekBar2);
                        textView = view.findViewById(R.id.textView2);
                        break;
                    case 2:
                        seekBar = view.findViewById(R.id.seekBar3);
                        textView = view.findViewById(R.id.textView3);
                        break;
                    case 3:
                        seekBar = view.findViewById(R.id.seekBar4);
                        textView = view.findViewById(R.id.textView4);
                        break;
                    case 4:
                        seekBar = view.findViewById(R.id.seekBar5);
                        textView = view.findViewById(R.id.textView5);
                        break;
                }
                seekBarFinal[i] = seekBar;
                seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN));
                seekBar.getThumb().setColorFilter(new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN));
                seekBar.setId(i);
                seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);

                textView.setText(frequencyHeaderTextView.getText());
                textView.setTextColor(Color.WHITE);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                if (Settings.isEqualizerReloaded) {
                    points[i] = Settings.seekbarpos[i] - lowerEqualizerBandLevel;
                    dataset.addPoint(frequencyHeaderTextView.getText().toString(), points[i]);
                    seekBar.setProgress(Settings.seekbarpos[i] - lowerEqualizerBandLevel);
                } else {
                    points[i] = equalizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel;
                    dataset.addPoint(frequencyHeaderTextView.getText().toString(), points[i]);
                    seekBar.setProgress(equalizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel);
                    Settings.seekbarpos[i] = equalizer.getBandLevel(equalizerBandIndex);
                    Settings.isEqualizerReloaded = true;
                }

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        equalizer.setBandLevel(equalizerBandIndex, (short) (progress + lowerEqualizerBandLevel));
                        points[seekBar.getId()] = equalizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel;
                        Settings.seekbarpos[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                        Settings.equalizerModel.getSeekbarpos()[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                        dataset.updateValues(points);
                        chart.notifyDataUpdate();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        presetSpinner.setSelection(0);
                        Settings.presetPos = 0;
                        Settings.equalizerModel.setPresetPos(0);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }

            equalizeSound();

            paint.setColor(Color.parseColor("#555555"));
            paint.setStrokeWidth((float) (1.10 * Settings.ratio));

            dataset.setColor(themeColor);
            dataset.setSmooth(true);
            dataset.setThickness(5);

            chart.setXAxis(false);
            chart.setYAxis(false);

            chart.setYLabels(AxisController.LabelPosition.NONE);
            chart.setXLabels(AxisController.LabelPosition.NONE);
            chart.setGrid(ChartView.GridType.NONE, 7, 10, paint);

            chart.setAxisBorderValues(-300, 3300);

            chart.addData(dataset);
            chart.show();

            Button mEndButton = new Button(getContext());
            mEndButton.setBackgroundColor(themeColor);
            mEndButton.setTextColor(Color.WHITE);
        } catch (Throwable e){
            Log.e("Equalizer Fragment", e.getMessage(), e);
            if (getActivity() != null){
                Toast.makeText(getActivity(), "Could not setup Equalizer. Please reopen to try again!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void equalizeSound() {
        ArrayList<String> equalizerPresetNames = new ArrayList<>();
        ArrayAdapter<String> equalizerPresetSpinnerAdapter = new ArrayAdapter<>(ctx,
                R.layout.spinner_item,
                equalizerPresetNames);
        equalizerPresetSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        equalizerPresetNames.add("Custom");

        for (short i = 0; i < equalizer.getNumberOfPresets(); i++) {
            equalizerPresetNames.add(equalizer.getPresetName(i));
        }

        presetSpinner.setAdapter(equalizerPresetSpinnerAdapter);
        //presetSpinner.setDropDownWidth((Settings.screen_width * 3) / 4);
        if (Settings.isEqualizerReloaded && Settings.presetPos != 0) {
//            correctPosition = false;
            presetSpinner.setSelection(Settings.presetPos);
        }

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (position != 0) {
                        equalizer.usePreset((short) (position - 1));
                        Settings.presetPos = position;
                        short numberOfFreqBands = 5;

                        final short lowerEqualizerBandLevel = equalizer.getBandLevelRange()[0];

                        for (short i = 0; i < numberOfFreqBands; i++) {
                            seekBarFinal[i].setProgress(equalizer.getBandLevel(i) - lowerEqualizerBandLevel);
                            points[i] = equalizer.getBandLevel(i) - lowerEqualizerBandLevel;
                            Settings.seekbarpos[i] = equalizer.getBandLevel(i);
                            Settings.equalizerModel.getSeekbarpos()[i] = equalizer.getBandLevel(i);
                        }
                        dataset.updateValues(points);
                        chart.notifyDataUpdate();
                    }
                } catch (Exception e) {
                    Toast.makeText(ctx, "Error while updating Equalizer", Toast.LENGTH_SHORT).show();
                }
                Settings.equalizerModel.setPresetPos(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ctx = null;
        Settings.isEditing = false;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int id = 0;

        public Builder setAudioSessionId(int id) {
            this.id = id;
            return this;
        }

        public Builder setAccentColor(int color) {
            themeColor = color;
            return this;
        }

        public Builder setBackgroundColor(int color){
            backgroundColor = color;
            return this;
        }

        public Builder setShowBackButton(boolean show){
            showBackButton = show;
            return this;
        }

        public EqualizerFragment build() {
            return EqualizerFragment.newInstance(id);
        }
    }


}
