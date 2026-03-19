package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.motion.protos.Motion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * PlayAudioActivity - Giao diện điều khiển phát nhạc MP3 nội bộ của ứng dụng.
 * Kèm thêm chức năng nhảy múa đồng bộ bằng ActionApi.
 */
public class PlayAudioActivity extends Activity {

    private static final String TAG = DemoApp.DEBUG_TAG;

    private MediaPlayer mediaPlayer;
    private TextView tvStatus;
    private TextView tvFileList;
    private Switch switchDance;

    // API nhảy múa
    private ActionApi actionApi;
    private String currentConfiguredActionId = null;

    // Danh sách lưu trữ id các resource nhạc trong thư mục raw
    private List<Integer> rawAudioIds = new ArrayList<>();
    private List<String> rawAudioNames = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_audio);

        tvStatus = findViewById(R.id.tv_status);
        tvFileList = findViewById(R.id.tv_file_list);
        switchDance = findViewById(R.id.switch_dance);

        // Khởi tạo ActionApi
        actionApi = ActionApi.get();

        // Tự động liệt kê các file trong res/raw
        loadRawAudioFiles();
    }

    /**
     * Tự động lấy danh sách tất cả file nhạc trong thư mục res/raw
     */
    private void loadRawAudioFiles() {
        rawAudioIds.clear();
        rawAudioNames.clear();

        Field[] fields = R.raw.class.getFields();
        for (int count = 0; count < fields.length; count++) {
            try {
                int resourceId = fields[count].getInt(fields[count]);
                String resourceName = fields[count].getName();
                
                // Lưu vào danh sách
                rawAudioIds.add(resourceId);
                rawAudioNames.add(resourceName);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi đọc raw resource", e);
            }
        }

        if (rawAudioNames.isEmpty()) {
            tvFileList.setText("Không có bài hát nào được nhúng sẵn.\nHãy thêm file MP3 vào thư mục res/raw/");
            tvStatus.setText("⚠ Chưa có nhạc");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rawAudioNames.size(); i++) {
                sb.append((i + 1)).append(". ").append(rawAudioNames.get(i)).append("\n\n");
            }
            tvFileList.setText(sb.toString());
            tvStatus.setText("✓ Khả dụng: " + rawAudioNames.size() + " bài hát");
        }
    }

    private int currentSongIndex = 0;

    public void playCurrentSong(View view) {
        if (rawAudioIds.isEmpty()) {
            Toast.makeText(this, "Không có bài hát nội bộ nào!", Toast.LENGTH_LONG).show();
            return;
        }
        playAudioFromRaw(rawAudioIds.get(currentSongIndex), rawAudioNames.get(currentSongIndex));
    }

    public void playNextSong(View view) {
        if (rawAudioIds.isEmpty()) return;
        currentSongIndex++;
        if (currentSongIndex >= rawAudioIds.size()) {
            currentSongIndex = 0; // Quay lại bài đầu tiên
        }
        playCurrentSong(view);
    }

    public void playPrevSong(View view) {
        if (rawAudioIds.isEmpty()) return;
        currentSongIndex--;
        if (currentSongIndex < 0) {
            currentSongIndex = rawAudioIds.size() - 1; // Đi tới bài cuối
        }
        playCurrentSong(view);
    }

    /**
     * Phát nhạc từ Resource ID và kích hoạt nhảy (nếu bật)
     */
    private void playAudioFromRaw(int rawId, String songName) {
        stopAudio();

        Log.i(TAG, "Bắt đầu phát: " + songName);
        tvStatus.setText("▶ Đang phát: " + songName);

        try {
            // Load từ res/raw không cần prepareAsync
            mediaPlayer = MediaPlayer.create(this, rawId);
            if (mediaPlayer == null) {
                throw new Exception("MediaPlayer.create trả về null");
            }

            // Kích hoạt nhảy múa nếu Switch được bật
            if (switchDance.isChecked()) {
                startRobotDance();
            }

            mediaPlayer.setOnCompletionListener(mp -> {
                tvStatus.setText("✓ Phát xong: " + songName);
                Log.i(TAG, "Phát xong: " + songName);
                stopRobotDance(); // Hết nhạc thì ngừng nhảy
                releaseMediaPlayer();
            });

            mediaPlayer.start();
            Toast.makeText(this, "Đang phát: " + songName, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            tvStatus.setText("✗ Lỗi phát nhạc: " + songName);
            Log.e(TAG, "Lỗi khi tạo MediaPlayer cho resource id: " + rawId, e);
            Toast.makeText(this, "Không thể phát bài hát này!", Toast.LENGTH_SHORT).show();
            stopRobotDance();
            releaseMediaPlayer();
        }
    }

    /**
     * Bắt đầu một điệu nhảy ngẫu nhiên hoặc cố định
     */
    private void startRobotDance() {
        if (actionApi == null) return;
        
        List<Motion.Action> actionList = actionApi.getActionList();
        if (actionList != null && !actionList.isEmpty()) {
            // Tìm điệu nhảy hoặc lấy ngẫu nhiên 1 điệu có chữ "dance"
            Motion.Action selectedDance = actionList.get(0);
            for (Motion.Action action : actionList) {
                if (action.getId().toLowerCase().contains("dance")) {
                    selectedDance = action;
                    break;
                }
            }
            
            currentConfiguredActionId = selectedDance.getId();
            Log.i(TAG, "Robot bắt đầu quẩy điệu: " + currentConfiguredActionId);
            
            actionApi.playAction(currentConfiguredActionId, new ResponseListener<Void>() {
                @Override
                public void onResponseSuccess(Void aVoid) {
                    Log.i(TAG, "Nhảy thành công");
                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                    Log.i(TAG, "Lỗi khi nhảy: " + i + " - " + s);
                }
            });
        }
    }

    /**
     * Dừng ngay lập tức việc nhảy múa
     */
    private void stopRobotDance() {
        if (actionApi == null || currentConfiguredActionId == null) return;

        Log.i(TAG, "Robot dừng nhảy múa");
        actionApi.stopAction(new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void aVoid) {
                Log.i(TAG, "Đã stop action thành công");
                currentConfiguredActionId = null;
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "Lỗi stopAction: " + errorCode + " - " + errorMsg);
            }
        });
    }

    /**
     * Dừng phát nhạc từ phím UI
     */
    public void stopAudio(View view) {
        stopAudio();
    }

    private void stopAudio() {
        stopRobotDance(); // Bấm tắt nhạc thì cũng ngừng nhảy

        if (mediaPlayer != null) {
            releaseMediaPlayer();
            tvStatus.setText("⏹ Đã dừng");
            Log.i(TAG, "Đã dừng nhạc");
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Lỗi giải phóng MediaPlayer", e);
            } finally {
                mediaPlayer = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio(); // Rời khỏi app dừng luôn nhac + nhảy
    }
}
