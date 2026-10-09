# Postman Integration Test Guide

Hướng dẫn này chỉ mô tả cách chạy integration test của FUCinema bằng Postman.

## 1. Khởi động hệ thống

Làm theo phần **Run the application** trong [README](../README.md) để bật ba database và bốn ứng dụng. Chờ các service khởi động xong; riêng `cinema-sqlserver-init` hiện `Exited (0)` là bình thường vì script khởi tạo database đã chạy xong.

Kiểm tra Gateway:

```powershell
curl.exe http://localhost:9000/actuator/health
```

Kết quả mong đợi có `"status":"UP"`. Mọi request trong collection đều gọi API qua Gateway ở `http://localhost:9000`.

## 2. Import collection và environment

Trong Postman, chọn **Import** và import hai file trong thư mục `postman/`:

- `FUCinemaBookingSystem.postman_collection.json`
- `FUCinema-Local.postman_environment.json`

Chọn environment **FUCinema-Local**. Biến `gateway` phải có giá trị `http://localhost:9000`; token và ID còn lại được các script tự điền khi collection chạy.

## 3. Chạy integration test

1. Mở collection **FUCinemaBookingSystem** và chọn **Run**.
2. Chọn environment **FUCinema-Local**.
3. Giữ nguyên thứ tự request và folder từ `01-Auth` đến `08-Report`, rồi bấm **Run FUCinemaBookingSystem**.
4. Xác nhận toàn bộ request và test đều pass. Lần kiểm tra gần nhất chạy 85 request, 202 assertion, không có lỗi.

Chụp màn hình trang kết quả Runner có tổng số request/test và trạng thái pass để đưa vào báo cáo.

## 4. Dữ liệu do collection tạo

Collection ghi dữ liệu thử vào các database local: đăng ký customer, tạo movie/room/showtime/booking; cập nhật hồ sơ customer An và hủy một số booking thử. Chạy trên môi trường local của assignment, không dùng database có dữ liệu cần giữ.

## TODO 11.3

TODO 11.3 là thêm hướng dẫn chạy hệ thống và test accounts vào README (`docs: add run guide and test accounts to README`). Ảnh kết quả Collection Runner là bằng chứng kiểm thử để đưa vào báo cáo; không cần export lại collection/environment nếu đang dùng đúng hai file đã có trong `postman/`.
