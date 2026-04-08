# Tổng Hợp Lệnh Terminal Thường Dùng Khi Build AlphaMini App

Dưới đây là một số dòng lệnh quan trọng mà chúng ta đã sử dụng và bạn (hoặc lập trình viên khác) có thể sẽ cần để can thiệp vào mã nguồn, biên dịch và nạp app AlphaMini lên robot thông qua môi trường ADB.

## 1. Biên Dịch (Build) và Nạp App Tự Động
Chúng tôi đã gói gọn tất cả mọi thao tác phức tạp vào một shell script là `build_and_install.sh`. Bất cứ khi nào bạn sửa code hoặc đổi nhạc, bạn chỉ cần gõ duy nhất lệnh này:

```bash
cd /home/ubt_alphamini_app_demo
./build_and_install.sh
```

*(Script này tích hợp Java 11, gọi gradle assembleDebug, cấp quyền và ADB push app cho bạn tự động hoàn toàn).*

---

## 2. Các Lệnh Đơn Lẻ (Nếu Cần Kiểm Soát Sâu)

### Thiết Lập Môi Trường
Nếu máy tính của bạn sử dụng phiên bản Java quá cao (như Java 21) bạn **BUỘC PHẢI** hạ xuống Java 11 trước khi gõ các lệnh của bộ `gradlew` AlphaMini.
```bash
# Trỏ đường dẫn tới bản JDK 11 local
export JAVA_HOME=/home/ubt_alphamini_app_demo/jdk-11.0.11+9
export PATH=$JAVA_HOME/bin:$PATH
```

### Build APK
Biên dịch tất cả source code Java/XML thành tệp cài đặt APK chứa Debug Key. File xuất ra sẽ nằm ở: `app/build/outputs/apk/debug/app-debug.apk`.
```bash
./gradlew assembleDebug
```
Hoặc dọn dẹp bộ nhớ đệm trước khi build:
```bash
./gradlew clean assembleDebug
```

### Gỡ và Cài App bằng ADB
Xóa ứng dụng bản cũ để tránh lỗi xung đột Token chữ ký số. Cực kì quan trọng trong quá trình Development test.
```bash
adb uninstall com.ubtrobot.mini.sdkdemo
```
Nạp gói APK mới bạn vừa biên dịch lên AlphaMini:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Cấp Quyền Phụ Trợ (Cho App Gốc)
Trong một số tính năng, robot cần được cấp phép truy cập thư mục hệ thống (Thẻ SD), bạn ép robot trao quyền bằng lệnh:
```bash
adb shell pm grant com.ubtrobot.mini.sdkdemo android.permission.READ_EXTERNAL_STORAGE
adb shell pm grant com.ubtrobot.mini.sdkdemo android.permission.WRITE_EXTERNAL_STORAGE
```

### Tải JDK 11 Di Động (Portable)
Trong trường hợp mang sang máy tính lạ không có quyền `sudo` cài Java, hãy dùng lệnh sau để tải Java "chạy liền" (Portable):
```bash
curl -L "https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9/OpenJDK11U-jdk_x64_linux_hotspot_11.0.11_9.tar.gz" -o jdk11.tar.gz
tar -xzf jdk11.tar.gz
rm jdk11.tar.gz
```
