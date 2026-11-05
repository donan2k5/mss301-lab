# Thiết kế Saga – Watch Management

## Phạm vi và quyền sở hữu

MSWatch điều phối Saga và sở hữu `DBWatch.Watches`, `SagaStates`, `SagaLogs`.
MSBrand là participant, chỉ ghi `DBBrand.BrandWatches`. Mỗi endpoint nội bộ của
MSBrand thực hiện một giao dịch local, không dùng transaction xuyên service.

| Saga | Bước | Giao dịch local | Compensation | Idempotent |
|---|---:|---|---|---|
| CREATE | 1 | MSWatch: tạo `Watches` | Xóa watch nếu còn | Có: `deleteIfExists` |
| CREATE | 2 | MSBrand: gán `WatchID` vào brand | Gỡ gán watch | Có: xóa khi tồn tại, vắng mặt vẫn thành công |
| UPDATE | 1 | MSWatch: lưu snapshot và sửa watch | Khôi phục snapshot `before` | Có: ghi lại cùng snapshot |
| UPDATE | 2 | MSBrand: upsert brand mới cho watch | Upsert brand cũ | Có: `WatchID` unique, ghi cùng cặp không đổi kết quả |
| DELETE | 1 | MSBrand: gỡ gán watch | Upsert lại brand cũ | Có: upsert cùng cặp |
| DELETE | 2 | MSWatch: xóa cứng watch | Không có – **pivot** | Không áp dụng |

`DELETE_WATCH` là pivot vì khóa chính `IDENTITY` không thể khôi phục an toàn
sau khi xóa cứng. Nó đứng cuối để lỗi mạng ở bước gỡ brand vẫn bù trừ được.
Các bước trước pivot có đúng một compensation. Compensation chạy theo thứ
tự LIFO, retry tối đa ba lần và ghi log từng lần. Nếu bù trừ thất bại, Saga
dừng ở `COMPENSATING` để vận hành phục hồi, không ghi `COMPENSATED` sai.

## Trạng thái

```text
PENDING -> EXECUTING -> COMPLETED
              |
              v
           FAILED -> COMPENSATING -> COMPENSATED
                         |
                         +-> COMPENSATING (cần phục hồi nếu retry hết)
```

Mỗi bước thành công được ghi vào stack compensation trước khi chạy bước kế.
Với lời gọi HTTP timeout sau khi MSBrand đã commit, cần kiểm tra lại trạng
thái phía MSBrand hoặc đăng ký compensation trước lời gọi vì thao tác bù trừ
idempotent. Không được giả định timeout đồng nghĩa với chưa ghi dữ liệu.

## Luồng lỗi CREATE với brand 999

1. Kiểm tra dữ liệu đầu vào và category trước khi bắt đầu Saga.
2. Tạo watch trong DBWatch; đăng ký `DELETE_WATCH` để bù trừ.
3. Gọi `PUT /internal/brands/999/watches/{watchId}`. MSBrand trả 404, không
   tạo dòng `BrandWatches`.
4. Ghi `FAILED`, chạy `DELETE_WATCH` cho watch vừa tạo, ghi `COMPENSATED`.
5. Trả 400 cho yêu cầu brand không tồn tại. DBWatch và DBBrand không còn
   dữ liệu của watch này.

## Vì sao dùng Saga

`@Transactional` chỉ kiểm soát DBWatch; rollback DBWatch không rollback được
lời gọi HTTP đã commit ở DBBrand. 2PC cần XA và giữ khóa trong lúc chờ cả hai
service; endpoint REST của MSBrand không tham gia XA, còn coordinator tạo
điểm lỗi tập trung. Saga dùng giao dịch local và bù trừ có thể retry.
