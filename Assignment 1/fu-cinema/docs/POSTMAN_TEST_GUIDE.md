# Hướng dẫn kiểm thử tích hợp bằng Postman

## Chuẩn bị

Khởi động database và các service theo [README](../README.md). Đảm bảo Gateway sẵn sàng tại `http://localhost:9000/actuator/health`; trạng thái cần hiển thị là `UP`.

## Import collection và environment

Trong Postman, chọn **Import** và import hai file:

- `FUCinemaBookingSystem.postman_collection.json`
- `FUCinema-Local.postman_environment.json`

Chọn environment **FUCinema-Local**. Biến `gateway` cần trỏ tới `http://localhost:9000`. Collection tự lưu token và ID cần thiết trong các request.

## Chạy kiểm thử

1. Mở collection **FUCinemaBookingSystem** và chọn **Run**.
2. Chọn environment **FUCinema-Local**.
3. Giữ nguyên thứ tự các folder từ `01-Auth` đến `08-Report`, rồi chạy collection.
4. Kiểm tra kết quả; các request và test cần ở trạng thái pass.

Nếu cần lưu minh chứng kiểm thử, chụp màn hình phần tổng kết của Collection Runner.

## Dữ liệu kiểm thử

Collection tạo dữ liệu customer, movie, room, showtime và booking; đồng thời cập nhật hồ sơ một customer mẫu và hủy booking kiểm thử. Hãy chạy trên database dành cho kiểm thử.
