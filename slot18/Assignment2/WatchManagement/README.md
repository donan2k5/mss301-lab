# Assignment 2 – Watch Management (`de190060`)

Đã thực hiện Bước 0–5 của `Assignment_Readme.md`: scaffold bốn project,
datasource riêng, MSAccount JWT, thiết kế Saga và MSBrand participant.
MSWatch mới là project scaffold; API Gateway chưa có route/JWT filter vì thuộc
Bước 6–7.

## Chạy local

Yêu cầu Java 21, Maven 3.9+, SQL Server 2019/2022 và tài khoản `sa`.
Chạy [database/init.sql](database/init.sql) trên SQL Server trước. Sao chép
`.env.example` thành `.env`, đặt mật khẩu DB và JWT secret ngẫu nhiên. Xuất
các biến `DB_HOST`, `DB_PORT`, `DB_PASSWORD`, `JWT_SECRET` vào môi trường shell;
Spring Boot không tự nạp file `.env`.

Chạy `mvn spring-boot:run` trong `MSAccount_de190060` (cổng 8081) và
`MSBrand_de190060` (cổng 8082). API cần kiểm tra trực tiếp:

- `POST http://localhost:8081/api/auth/login` với
  `{"username":"adminsys","password":"@1"}`.
- `GET http://localhost:8082/api/brands`.
- `PUT http://localhost:8082/internal/brands/1/watches/99`.
- `DELETE http://localhost:8082/internal/brands/watches/99`.

`APIGateway_de190060` khởi động ở cổng 8080, chưa định tuyến ở phạm vi này.
Chi tiết bước Saga tiếp theo nằm trong [docs/SAGA_DESIGN.md](docs/SAGA_DESIGN.md).
