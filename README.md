# AlphaMini - Music & Dance Feature

Dự án này là module nâng cấp dành cho robot UBTech AlphaMini, cho phép robot tự động phát nhạc MP3 từ bộ nhớ cục bộ (bên trong file APK) và đồng bộ với các điệu nhảy lập trình sẵn.

##  Tính Năng Chính
- **Phát nhạc nội bộ:** MP3 được nhúng thẳng vào file APK (thư mục `res/raw`), thân thiện với Người Dùng Cuối (không cần biết copy nhạc vào SD card).
- **Đồng bộ hóa:** Đồng bộ cử động nhảy (`ActionApi.playAction`) ngay lúc âm thanh (`MediaPlayer`) hoạt động. Tự động kết thúc nhảy khi hết bài.
- **Tuỳ chọn:** Cho phép người dùng bật/tắt hành động nhảy thông qua Switch UI.
- **Hoạt động ngầm:** Sử dụng kiến trúc `ProxySkill` (Background Service) và Android Interactivity để đảm bảo robot tuân thủ hiệu lệnh.

---

##  Hướng Dẫn Dành Cho Developer

### 1. Kiến Trúc Hoạt Động
- **Giao diện (UI):** `PlayAudioActivity.java` và `activity_play_audio.xml`.
- **Logic nhạc:** `MediaPlayer.create(context, R.raw.xyz)` gọi các ID bằng reflection từ class `R.raw.*`.
- **Logic nhảy:** Sử dụng `ActionApi.get().getActionList()` để lấy danh sách hành động và `actionApi.playAction(id, listener)` để ra lệnh cho robot.
- **Skill Service:** `PlayAudioSkill.java` dùng để kích hoạt nhạc bằng giọng nói hoặc phím cứng.
- **Quyền hạn (Manifest):** Đăng ký service `skills.PlayAudioSkill` kèm skill path `xml/play_audio_skill`.

### 2. Cách Nạp (Thêm) Nhạc Mới 
Để ứng dụng có bài hát của bạn thay vì bài mẫu:
1. Chuẩn bị file nhạc định dạng `.mp3`.
2. Đổi tên file sao cho **Chỉ có chữ cái in thường (a-z), số (0-9) và dấu gạch dưới (_)**. Tuyệt đối không có khoảng trắng.
   - *Ví dụ đúng: `nhac_tre_1.mp3`, `glory_man_united.mp3`*
   - *Ví dụ sai: `NhacTre 2024.mp3`, `Bai-Hat.MP3`*
3. Chép (copy) file nhạc đó vào thư mục gốc của Source Code theo đường dẫn: `app/src/main/res/raw/`.
4. Mở Terminal lên để tự động biên dịch lại mọi thứ bằng lệnh:
   ```bash
   ./build_and_install.sh
   ```

### 3. Lưu Ý Trình Biên Dịch (Gradle Build)
AlphaMini sử dụng Android API cũ, do đó yêu cầu công cụ biên dịch (Gradle) tương thích.
Nếu bạn biên dịch trên máy cá nhân, hãy dùng **JDK 11** để tránh lỗi thuật toán HmacPBESHA256 trên keystore của Java mới (như Java 17/21).

---

##  Hướng Dẫn Sử Dụng (Dành Cho Khách Hàng)

Sau khi lập trình viên (Dev) đã cài ứng dụng (file APK) có chứa nhạc lên robot của bạn, bạn sử dụng cực kỳ dễ dàng theo 3 bước:
1. **Bật Robot:** Đảm bảo kết nối Vysor để bấm thao tác màn hình AlphaMini.
2. **Khởi động tính năng:** Trong ứng dụng chính, cuộn xuống và nhấn nút **" Play Audio Test"**.
3. **Thưởng thức:**
   - Chọn bài nhạc bằng nút **"Tiếp "** và **" Lui"**.
   - Có thể chạm công tắc **" Kèm hành động nhảy múa"** để tắt điệu nhảy nếu chỉ muốn robot phát loa ngoài đứng yên.
   - Bấm **" Phát"** và đợi robot biểu diễn. Bấm **" Dừng"** bất cứ khi nào bạn muốn robot nghỉ.
