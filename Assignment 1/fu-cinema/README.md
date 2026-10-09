# FUCinema Booking System - MSS301 Assignment 1

Dự án gồm ba Spring Boot service và một API Gateway:

| Thành phần | Cổng | Database |
|---|---:|---|
| Customer Service | 8081 | SQL Server (`cinema_customer`) |
| Movie Service | 8082 | MongoDB (`cinema_movie`) |
| Booking Service | 8083 | MySQL (`cinema_booking`) |
| API Gateway | 9000 | Định tuyến request đến các service |

## Yêu cầu

- JDK 21
- Maven 3.9 trở lên
- Docker Desktop
- Postman Desktop để kiểm thử API

## Chạy hệ thống

Chạy các lệnh dưới đây tại thư mục gốc repository `mss301-lab`.

Khởi động database và kiểm tra trạng thái:

```powershell
docker compose -f "Assignment 1/fu-cinema/docker-compose.yml" up -d
docker compose -f "Assignment 1/fu-cinema/docker-compose.yml" ps -a
```

Khởi động từng ứng dụng trong một terminal riêng:

```powershell
mvn -f "Assignment 1/fu-cinema/customer-service/pom.xml" spring-boot:run
```

```powershell
mvn -f "Assignment 1/fu-cinema/movie-service/pom.xml" spring-boot:run
```

```powershell
mvn -f "Assignment 1/fu-cinema/booking-service/pom.xml" spring-boot:run
```

```powershell
mvn -f "Assignment 1/fu-cinema/api-gateway/pom.xml" spring-boot:run
```

Các request Postman đi qua Gateway tại `http://localhost:9000`. Kiểm tra Gateway sẵn sàng tại <http://localhost:9000/actuator/health>.

## Tài khoản kiểm thử

| Vai trò | Email | Mật khẩu | Trạng thái |
|---|---|---|---|
| Admin | `admin@fucinema.com` | `@@abc123@@` | Tài khoản quản trị |
| Customer | `an@gmail.com` | `123456` | Hoạt động |
| Customer | `binh@gmail.com` | `123456` | Hoạt động |
| Customer | `chi@gmail.com` | `123456` | Không hoạt động; đăng nhập trả về 403 |

## Kiểm thử tích hợp bằng Postman

Import `postman/FUCinemaBookingSystem.postman_collection.json` và `postman/FUCinema-Local.postman_environment.json`. Chọn environment `FUCinema-Local`, sau đó chạy toàn bộ collection theo thứ tự.

Collection tạo customer, movie, showtime và booking kiểm thử trong các database local. Collection cũng cập nhật hồ sơ customer và hủy một số booking kiểm thử.

## Tài liệu bài tập

- [Hướng dẫn chạy kiểm thử Postman](docs/POSTMAN_TEST_GUIDE.md)
- [Báo cáo Assignment 1](docs/TuPhucNguyen-assignment1.docx)
