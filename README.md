# ĐỒ ÁN JAVA OOP — QUẢN LÝ DỰ ÁN VÀ NHÂN SỰ

Ứng dụng Java Console xây dựng theo hướng đối tượng, bám **slide hướng dẫn 37 trang môn Lập trình hướng đối tượng — GV Nguyễn Dũng** (đề tài gợi ý số 9). **Không dùng Spring/Hibernate**. Dữ liệu mẫu giả lập, chỉ phục vụ học tập.

> **Lưu ý trước khi nộp:** Đây là bộ mã nguồn/tài liệu tham khảo được tạo sẵn. Nhóm phải tự rà soát, hiểu và điều chỉnh, tự phân công, bổ sung tên và chứng minh quá trình đóng góp thực tế trên Git. File `PHAN-CONG.xlsx` và trang bìa báo cáo còn chỗ trống cần điền. Chưa có lịch sử commit. Tài liệu gốc đầy đủ `DO-AN-NHOM.md` được slide dẫn chiếu nhưng chưa được cung cấp; nên đối chiếu thêm khi nhận từ giảng viên.

## 1. Có gì trong ZIP?

- `src/main/java/` — Java 17 đầy đủ, Console tương tác.
- `src/test/java/` — các bài test JUnit 5 (cần Maven/cấu hình JUnit để chạy).
- `src/verification/java/` — 25 bước tự kiểm tra, không cần Maven/JUnit.
- `data/*.csv` — 5 nhân viên mẫu, 2 dự án, 6 lượt tham gia, 5 công việc.
- `uml/class-diagram.png` — UML lớp bắt buộc.
- `BAO-CAO.pdf` — báo cáo theo 5 chương, có bảng đối chiếu 18 tiêu chí.
- `PHAN-CONG.xlsx` — mẫu phân công, cần điền thật.
- `pom.xml` — cấu hình Maven để IntelliJ nhận diện dự án Java 17 và tải JUnit 5.

## 2. Mở trên IntelliJ IDEA (khuyến nghị)

1. Giải nén ZIP, vào IntelliJ: **File > Open**, chọn thư mục `OOP_Nhom6_QuanLyDuAnVaNhanSu` (có `pom.xml`); mở dưới dạng Maven project nếu được hỏi.
2. Chọn **Project SDK Java 17 trở lên**; để IntelliJ tải Maven dependencies khi kết nối được internet.
3. Mở `src/main/java/vn/pxu/oop/pm/Main.java`, bấm Run (▶).
4. Trong **Run > Edit Configurations**, đặt **Working directory** là **thư mục gốc dự án** để đọc `data/*.csv`.
5. Khi thêm/sửa/xóa thành công, chương trình tự lưu xuống 4 file CSV trong `data/`.

Trong **Settings > Editor > File Encodings**, chọn UTF-8 để hiển thị đúng tiếng Việt. Sau khi sửa source, chạy lại `Main.java` trong IntelliJ để biên dịch thay đổi.

Gói nộp không kèm cấu hình IntelliJ và file biên dịch sẵn. IntelliJ sẽ tạo lại `.idea/` và thư mục kết quả build khi mở/chạy dự án.

## 3. Các nhóm chức năng

| Nhóm | Chức năng |
| --- | --- |
| Nhân sự | Liệt kê, thêm, sửa, xóa, tìm theo ID/tên/vai trò; 3 vai trò Developer, Tester, Manager |
| Dự án | Liệt kê, tạo, sửa, xóa, tìm, đổi trạng thái |
| Công việc | Liệt kê theo dự án, thêm, sửa, xóa, tìm, giao/bỏ giao, cập nhật trạng thái |
| Phân công | Xem, thêm, gỡ, sửa ngày tham gia, tìm kiếm và thống kê số thành viên dự án |
| Thống kê | Nhân viên theo vai trò, tiến độ dự án, ước tính chi phí theo 2 cách, workload theo nhân viên |

## 4. Quy tắc nghiệp vụ chính

- Mã nhân viên/dự án/task phải duy nhất (không phân biệt chữ hoa/thường); email nhân sự không được trùng.
- Tên, email, ngày, mức lương, số giờ dự kiến có validation; nhập menu không đúng sẽ hỏi lại.
- Công việc chỉ được giao cho nhân viên đã thuộc dự án.
- Công việc cần người phụ trách mới được chuyển **DONE**.
- Dự án chỉ được chuyển **COMPLETED** khi có ít nhất một task và tất cả task **DONE**.
- Dự án **COMPLETED** cần được mở lại trạng thái trước khi chỉnh task/nhân sự.
- Không xóa nhân viên đang thuộc dự án hoặc đang có task được giao.
- Xóa dự án xóa luôn các task và lượt phân công thuộc dự án (composition), **không xóa hồ sơ nhân viên** (aggregation).
- Chi phí chiến lược `MonthlyTeamCostStrategy` dựa trên toàn bộ thành viên trong một tháng; `TaskEffortCostStrategy` chỉ tính task đã được giao theo giả định 160h/tháng. Đây là số liệu **minh họa**, không phải bảng lương thực tế.

## 5. Kỹ thuật OOP

Cấu trúc dự án:

```text
vn.pxu.oop.pm
├── model        # Employee (abstract), Developer/Tester/Manager, Project/Task/ProjectMember
├── enums        # EmployeeType, TaskStatus, ProjectStatus, TaskPriority
├── service      # EmployeeService, ProjectService, TaskService, ReportService, InMemoryDatabase
├── factory      # EmployeeFactory — Factory
├── strategy     # ProjectCostStrategy & 2 concrete strategies — Strategy
├── storage      # FileDataStore (CSV), CsvCodec
├── exception    # ValidationException, EntityNotFoundException, BusinessRuleException, StorageException
├── util         # Check
├── cli          # ConsoleMenu, ConsoleInput
└── Main.java
```

Chi tiết đối chiếu 18 yêu cầu: Chương 3 trong `BAO-CAO.pdf`.

## 6. Kiểm thử

Đã kiểm tra trên bản sao tạm bằng JDK 21, biên dịch với cấu hình Java 17: **26 bài test JUnit 5** đạt, **25 smoke checks** đạt và Maven build thành công. Toàn bộ source kiểm thử được giữ trong `src/`.

Trong IntelliJ, bấm chuột phải vào `src/test/java` và chọn **Run 'All Tests'**, hoặc dùng cửa sổ **Maven > Lifecycle > test**. Chờ IntelliJ tải xong dependencies trước khi chạy.

Nếu muốn thử với bộ dữ liệu riêng, đặt **Program arguments** của `Main` thành `--data-dir <folder>` trong **Run > Edit Configurations**.

## 7. Trước khi nộp cho giảng viên

- [ ] Kiểm tra số nhóm 6; điền tên, mã sinh viên, thông tin giảng viên, % đóng góp thực tế.
- [ ] Nhóm xem và tự giải thích **từng lớp / đoạn code đã nhận**, không nộp khi chưa hiểu.
- [ ] Tự tạo GitHub repository; mọi thành viên commit code thực tế, tổng cộng **>=25 commit trải đều**.
- [ ] Kiểm tra đầy đủ M0–M5; bổ sung bản mô tả M1 và UML v1/v2 nếu mốc chưa nộp.
- [ ] Run JUnit 5 nếu muốn xét điểm thưởng, chụp bằng chứng kiểm thử.
- [ ] Chạy trên máy giảng viên và thử trường hợp nhập sai; hoàn thiện bảo vệ 20 phút.
- [ ] Không cần demo.mp4 theo quyết định nhóm, vì slide xác định video là tùy chọn.

**Không có web UI hoặc cơ sở dữ liệu SQL**: chúng không bắt buộc theo đề bài. Tập trung vào OOP, UML, validation, thiết kế pattern và giải thích mã.
