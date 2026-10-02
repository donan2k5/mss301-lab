# MSS301 Slot 7 — Part 3 và Part 4

Project nằm trong `ShoppingServices/`, gồm Product (8080), Order (8081), Inventory (8082) và API Gateway (9000). Mã dựa trên các service của slot trước. Part 3 thêm OpenFeign, WireMock và Gateway; Part 4 dùng Keycloak cấp JWT để bảo vệ Gateway.

## Chuẩn bị và chạy

Cần JDK 21, Maven 3.9+ và Docker Desktop. Chạy các lệnh sau ở đúng thư mục:

```powershell
cd ShoppingServices/order-service
docker compose up -d mysql

cd ../product-service
docker compose up -d mongodb

cd ../api-gateway
docker compose up -d
```

Gateway Compose chạy Keycloak tại `localhost:8181` và MySQL riêng. Lần đầu khởi động, Keycloak import `docker/keycloak/realms/spring-microservices-realm.json`. Chờ `http://localhost:8181/realms/spring-microservices-realm/.well-known/openid-configuration` trả JSON trước khi chạy Gateway. Tài khoản quản trị dùng cho bài lab là `admin/admin`; client `spring-microservices-client` dùng secret phát triển `mss301-dev-secret-change-me`.

Mở bốn terminal và chạy `mvn spring-boot:run` lần lượt trong `inventory-service`, `product-service`, `order-service`, `api-gateway`. Inventory và Order dùng hai database MySQL riêng; Flyway nạp `iphone_15` với tồn kho 100. Order gọi Inventory bằng OpenFeign trước khi lưu, trả `409` nếu hết hàng.

Gateway chuyển tiếp `/api/products/**`, `/api/order/**`, `/api/inventory/**` và yêu cầu Bearer JWT cho mọi API; `/actuator/health` vẫn công khai. Để lấy token:

```powershell
$tokenResponse = Invoke-RestMethod -Method Post `
  -Uri 'http://localhost:8181/realms/spring-microservices-realm/protocol/openid-connect/token' `
  -Body @{grant_type='client_credentials';client_id='spring-microservices-client';client_secret='mss301-dev-secret-change-me'}
$token = $tokenResponse.access_token
Invoke-RestMethod -Uri 'http://localhost:9000/api/products' -Headers @{Authorization="Bearer $token"}
```

Nếu cổng 9000 đang được ứng dụng khác sử dụng, chạy Gateway với `--server.port=9004` (ví dụ `java -jar target/api-gateway-0.0.1-SNAPSHOT.jar --server.port=9004` sau `mvn package`) và đổi `gatewayUrl` trong Postman environment thành `http://localhost:9004`.

## Kiểm thử

Chạy `mvn test` trong từng service. Order dùng MySQL Testcontainers + WireMock; Product và Inventory dùng Testcontainers; Gateway dùng MockMvc/WireMock và HTTP server giả lập, không cần Keycloak cho test tự động.

Hai bộ Postman được tạo cục bộ trong `ShoppingServices/api-gateway/postman/` và được Git ignore theo `CLAUDE.md`. Import cả collection và environment tương ứng, chọn environment ở góc trên phải, sau đó bấm Run:

- `slot7.postman_collection.json` + `slot7.postman_environment.json`: Part 3, chạy ở commit trước khi thêm bảo mật Gateway.
- `slot7-part4.postman_collection.json` + `slot7-part4.postman_environment.json`: Part 4, lấy token trước rồi kiểm tra API có và không có JWT.

Chạy Part 4 bằng Newman:

```powershell
npx --yes newman run .\ShoppingServices\api-gateway\postman\slot7-part4.postman_collection.json -e .\ShoppingServices\api-gateway\postman\slot7-part4.postman_environment.json
```

Kết quả kiểm tra tại máy: Order 3 test, Product 2 test, Inventory 1 test, Gateway 10 test đều pass. Newman Part 3 pass 10/10 assertion và Part 4 pass 12/12 assertion. Gateway được chạy thử trên cổng 9004 vì cổng 9000 đang có dịch vụ khác sử dụng.
