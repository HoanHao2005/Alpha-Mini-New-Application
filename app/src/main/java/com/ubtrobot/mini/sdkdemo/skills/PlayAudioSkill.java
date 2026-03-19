package com.ubtrobot.mini.sdkdemo.skills;

import android.media.MediaPlayer;
import android.util.Log;

import com.ubtechinc.skill.ProxySkill;
import com.ubtechinc.skill.SkillType;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtechinc.skill.SkillHelper;
import com.ubtrobot.mini.sdkdemo.R;
import com.ubtrobot.mini.sysevent.event.base.KeyEvent;
import com.ubtrobot.speech.protos.Speech;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * PlayAudioSkill - Skill phát nhạc MP3 nội bộ từ thư mục res/raw
 *
 * Mặc định phát bài hát đầu tiên tìm thấy trong thư mục raw để biểu diễn.
 */
public class PlayAudioSkill extends ProxySkill {

    private static final String TAG = "PlayAudioSkill";

    private MediaPlayer mediaPlayer;
    
    @Override
    protected void onSkillStart() {
        super.onSkillStart();
        Log.d(TAG, "PlayAudioSkill khởi động - phát bài hát nội bộ đầu tiên");
        playFirstRawAudio();
    }

    @Override
    protected void onSkillStop(SkillStopCause skillStopCause) {
        super.onSkillStop(skillStopCause);
        Log.d(TAG, "PlayAudioSkill dừng, nguyên nhân: " + skillStopCause);
        releaseMediaPlayer();
    }

    /**
     * Dùng reflection đọc class R.raw để lấy danh sách nhạc nhúng sẵn
     */
    private void playFirstRawAudio() {
        List<Integer> rawAudioIds = new ArrayList<>();
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                rawAudioIds.add(field.getInt(field));
            } catch (Exception e) {
                Log.e(TAG, "Lỗi đọc raw resource trong Skill", e);
            }
        }

        if (rawAudioIds.isEmpty()) {
            Log.w(TAG, "Không có file nhạc nào trong thư mục res/raw/");
            return;
        }

        // Phát file đầu tiên
        int rawId = rawAudioIds.get(0);
        playAudioFromRaw(rawId);
    }
    
    /**
     * Phát nhạc từ Resource ID
     */
    private void playAudioFromRaw(int rawId) {
        releaseMediaPlayer();
        
        try {
            Log.i(TAG, "Bắt đầu phát nhạc qua Skill từ resource id: " + rawId);
            mediaPlayer = MediaPlayer.create(com.ubtech.utilcode.utils.Utils.getContext(), rawId);
            
            if (mediaPlayer == null) {
                throw new Exception("MediaPlayer.create trả về null cho resource id: " + rawId);
            }

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.i(TAG, "Phát xong bài hát qua Skill");
                releaseMediaPlayer();
            });
            
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi phát nhạc trong Skill: " + rawId, e);
            releaseMediaPlayer();
        }
    }

    /**
     * Dừng và giải phóng MediaPlayer.
     */
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi giải phóng MediaPlayer", e);
            } finally {
                mediaPlayer = null;
            }
        }
    }

    @Call(path = "/play_audio/startSkill")
    public void onStartSkill(Request request, final Responder responder) {
        Log.d(TAG, "onStartSkill được gọi");
        responder.respondSuccess();
    }

    @Override
    protected String getSkillName() {
        return "play_audio_skill";
    }

    @Override
    protected SkillType getSkillType() {
        return SkillType.Interruptible;
    }

    @Override
    protected SkillType getSubSkillType() {
        return null;
    }

    @Override
    protected boolean onHeadTapEvent(KeyEvent keyEvent) {
        // Chạm đầu robot = dừng nhạc
        releaseMediaPlayer();
        stopSkill();
        return true;
    }

    @Override
    protected void onWakeUpEvent(Speech.WakeupParam wakeupParam) {
        // Wake-up event = dừng nhạc
        releaseMediaPlayer();
        stopSkill();
    }

    @Override
    protected boolean isNeedWakeUpEvent() {
        return true;
    }

    @Override
    protected boolean isNeedHeadTapEvent() {
        return true;
    }
}
