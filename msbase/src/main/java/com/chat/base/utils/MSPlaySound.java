package com.chat.base.utils;

import android.media.AudioManager;
import android.media.SoundPool;

import com.chat.base.MSBaseApplication;

public class MSPlaySound {
    private int soundIn;
    private int soundOut;
    private int soundRecord;
    private boolean soundInLoaded;
    private boolean soundOutLoaded;
    private boolean soundRecordLoaded;
    private SoundPool soundPool;

    private MSPlaySound() {
    }

    private static class PlaySoundBinder {
        static final MSPlaySound play = new MSPlaySound();
    }

    public static MSPlaySound getInstance() {
        return PlaySoundBinder.play;
    }

    public void playRecordMsg(int playID) {
        try {
            if (soundPool == null) {
                soundPool = new SoundPool(3, AudioManager.STREAM_SYSTEM, 0);
                soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                    if (status == 0) {
                        try {
                            soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                        } catch (Exception ignored) {
                        }
                    }
                });
            }
            if (soundRecord == 0 && !soundRecordLoaded) {
                soundRecordLoaded = true;
                soundRecord = soundPool.load(MSBaseApplication.getInstance().getContext(), playID, 1);
            }
            if (soundRecord != 0) {
                try {
                    soundPool.play(soundRecord, 1.0f, 1.0f, 1, 0, 1.0f);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void playOutMsg(int playID) {
        try {
            if (soundPool == null) {
                soundPool = new SoundPool(3, AudioManager.STREAM_SYSTEM, 0);
                soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                    if (status == 0) {
                        try {
                            soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                        } catch (Exception ignored) {
                        }
                    }
                });
            }
            if (soundOut == 0 && !soundOutLoaded) {
                soundOutLoaded = true;
                soundOut = soundPool.load(MSBaseApplication.getInstance().getContext(), playID, 1);
            }
            if (soundOut != 0) {
                try {
                    soundPool.play(soundOut, 1.0f, 1.0f, 1, 0, 1.0f);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void playInMsg(int playID) {
        try {
            if (soundPool == null) {
                soundPool = new SoundPool(3, AudioManager.STREAM_SYSTEM, 0);
                soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                    if (status == 0) {
                        try {
                            soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                        } catch (Exception ignored) {
                        }
                    }
                });
            }
            if (soundIn == 0 && !soundInLoaded) {
                soundInLoaded = true;
                soundIn = soundPool.load(MSBaseApplication.getInstance().getContext(), playID, 1);
            }
            if (soundIn != 0) {
                try {
                    soundPool.play(soundIn, 1.0f, 1.0f, 1, 0, 1.0f);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

}
