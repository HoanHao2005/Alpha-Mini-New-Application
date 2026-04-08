#!/bin/bash

# 1. Đảm bảo dùng đúng phiên bản Java 11 đã tải để tương thích với project cũ
export JAVA_HOME=/home/hoanhao/ubt_alphamini_app_demo/jdk-11.0.11+9
export PATH=$JAVA_HOME/bin:$PATH

# 2. Chuyển vào thư mục gốc của project
cd /home/hoanhao/ubt_alphamini_app_demo

echo "Đang Build APK..."
# 3. Chạy lệnh Build của Gradle
./gradlew assembleDebug

# Kiểm tra xem build có thành công không
if [ $? -eq 0 ]; then
    echo "Build thành công! Đang tiến hành gỡ cài đặt app cũ (nếu có) và nạp app mới..."
    
    # Gỡ bản cũ để tránh lỗi xung đột chữ ký
    adb uninstall com.ubtrobot.mini.sdkdemo
    
    # Cài bản mới
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    
    if [ $? -eq 0 ]; then
        echo " Thao tác nạp app lên robot AlphaMini thành công!"
    else
        echo " Lỗi khi nạp app lên robot. Hãy kiểm tra kết nối ADB (Vysor)."
    fi
else
    echo " Build thất bại. Hãy kiểm tra lại tên file nhạc (.mp3) trong res/raw xem có bị viết hoa/khoảng trắng không."
fi
