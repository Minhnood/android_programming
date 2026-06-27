# JDM Legends Garage 🏎️

Ứng dụng Android (Java) mô phỏng **cửa hàng bán xe JDM** — duyệt xe, xem chi tiết & đánh giá, thêm vào giỏ, đặt hàng và theo dõi đơn, đăng ký lái thử, đọc tin tức ô tô. Hỗ trợ 3 ngôn ngữ (Tiếng Việt / English / 日本語).

> Đồ án môn Lập trình Android. Package: `com.example.bai1`.

---

## 👤 Tài khoản demo

Đăng nhập sẵn để chấm/thử nhanh (mật khẩu trên server được **băm bcrypt**, đây là các tài khoản mẫu đặt mật khẩu sẵn):

| Vai trò | Email | Mật khẩu |
|--------|-------|----------|
| **Admin** (xem trang quản trị) | `admin@jdm.com` | `Admin@123` |
| **Khách hàng** | `khach@jdm.com` | `Khach@123` |

> 🔒 *Vì mật khẩu được mã hoá bcrypt (một chiều) ở server, không thể xem lại mật khẩu của tài khoản người dùng đã đăng ký — đây là các tài khoản demo được tạo riêng để minh hoạ. Bạn có thể tự đăng ký tài khoản mới ngay trong app.*

---

## ✨ Tính năng

| Nhóm | Chức năng |
|------|-----------|
| Tài khoản | Đăng nhập, đăng ký, quên mật khẩu (có server + fallback ngoại tuyến) |
| Hồ sơ | Xem/sửa hồ sơ, đổi avatar (lưu nội bộ), đăng xuất |
| Mua sắm | Trang chủ, danh mục theo vùng/hãng, tìm kiếm (lịch sử + lọc theo hãng) |
| Chi tiết xe | Thông số, chọn màu (đổi ảnh theo màu), đánh giá & trả lời, chia sẻ, yêu thích |
| **Lái thử** | Đăng ký lái thử: chọn ngày–giờ, để lại tên & SĐT, ghi chú |
| Giỏ & đặt hàng | Giỏ hàng, thanh toán (gợi ý địa chỉ qua OpenStreetMap + bản đồ), chọn hình thức thanh toán |
| Đơn hàng | Lịch sử đơn, chi tiết & theo dõi trạng thái (đồng bộ từ server) |
| Khác | Wishlist, tin tức (RSS VnExpress), thông báo, cài đặt (ngôn ngữ, thông báo), WebView |

---

## 🛠 Công nghệ

- **Ngôn ngữ:** Java 17
- **SDK:** `minSdk 24`, `compileSdk/targetSdk 35`
- **Thư viện:** [Glide](https://github.com/bumptech/glide) (ảnh), [Gson](https://github.com/google/gson) (JSON), [Lottie](https://airbnb.io/lottie/) (animation), Material Components, ConstraintLayout, SwipeRefreshLayout
- **Mạng:** `HttpURLConnection` (không dùng Retrofit)
- **Lưu trữ:** `SharedPreferences` + Gson (không dùng database)
- **Kiểm thử:** JUnit 4 (unit test chạy trên JVM)

---

## 🧱 Kiến trúc

App theo hướng **Activity-centric**: mỗi màn hình là một `Activity` (đều kế thừa `BaseActivity` để áp ngôn ngữ đã chọn), gọi trực tiếp các lớp quản lý dữ liệu.

```
app/src/main/java/com/example/bai1/
├── *Activity.java        # Các màn hình (Login, Home, DetailProduct, Cart, Checkout, TestDrive, …)
├── *Adapter.java         # Adapter cho RecyclerView (Car, Cart, Review, News, OrderHistory)
├── *Model.java / *.java  # Model: CarModel, OrderModel, ReviewModel, NewsModel,
│                         #        NotificationModel, ColorVariant, TestDriveBooking
├── DataManager.java      # Wishlist / Cart / Orders / Reviews / Test drives (SharedPreferences + Gson)
├── AccountManager.java   # Phiên đăng nhập, hồ sơ, token (SharedPreferences)
├── JsonUtils.java        # Gọi API + cache danh sách xe (+ fallback assets/cars.json)
├── JsonSafe.java         # Parse JSON an toàn (không crash khi dữ liệu hỏng)
├── Validators.java       # Kiểm tra email / mật khẩu / số điện thoại (dùng chung, test được)
├── LocaleHelper.java     # Đa ngôn ngữ vi/en/ja
└── NotificationHelper.java, UiUtils.java, CarImages.java
```

- **Tầng dữ liệu:** `DataManager` (dữ liệu người dùng) + `AccountManager` (tài khoản) + `JsonUtils` (danh mục xe). Đọc JSON đi qua `JsonSafe.parseOr(...)` để dữ liệu hỏng/đổi schema trả về danh sách rỗng thay vì làm văng app.
- **Đa ngôn ngữ:** mọi chuỗi nằm trong `res/values*/strings.xml` (vi/en/ja), đổi ngôn ngữ trong **Cài đặt** qua `LocaleHelper`.

---

## 🌐 Backend

- Base URL: `https://jdm-shop.onrender.com`
- Các API chính: `/api/cars`, `/api/users/login`, `/api/users/register`, `/api/orders`, `/api/reviews/{carId}`, `/api/order/{id}`
- Xác thực bằng JWT (lưu trong `AccountManager`, đính kèm qua `JsonUtils`).
- **Ngoại tuyến:** khi mất kết nối, đăng nhập/đăng ký dùng dữ liệu đã lưu cục bộ và danh mục xe đọc từ `assets/cars.json`.

---

## ▶️ Chạy & Build

**Yêu cầu:** Android Studio (Giraffe trở lên) + JDK 17.

```bash
# Build APK debug
./gradlew assembleDebug

# Chạy unit test (JVM)
./gradlew test
# Báo cáo: app/build/reports/tests/testDebugUnitTest/index.html
```

Hoặc mở project bằng Android Studio rồi bấm **Run** trên emulator/thiết bị (Android 7.0+).

---

## ✅ Unit test

Test thuần JVM (không cần emulator), tại `app/src/test/java/com/example/bai1/`:

- `ValidatorsTest` — email / mật khẩu / số điện thoại hợp lệ & không hợp lệ.
- `JsonSafeTest` — JSON hợp lệ parse đúng; null/rỗng/JSON hỏng trả về fallback (không văng).
- `DataModelTest` — round-trip Gson cho `CarModel`, `OrderModel`, `TestDriveBooking`.

---

## 📝 Ghi chú khi đưa lên production

- Đang bật `android:usesCleartextTraffic="true"` để tiện gọi server demo — nên tắt và dùng HTTPS thuần khi phát hành.
- Token đang lưu dạng `SharedPreferences` thường — cân nhắc `EncryptedSharedPreferences`.
- Có thể chuyển sang Retrofit/Room/MVVM nếu cần mở rộng quy mô.
