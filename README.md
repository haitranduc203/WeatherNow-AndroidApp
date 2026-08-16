# ☀️ WeatherNow — Modern Android Weather Application

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-9.0.1-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com/build)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.03.01-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Latest-7986CB.svg?logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-FF6F00.svg)](https://developer.android.com/topic/architecture)

**WeatherNow** là ứng dụng dự báo thời tiết Android Native được phát triển bằng **Kotlin** và **Jetpack Compose**, tuân thủ nghiêm ngặt mô hình **Clean Architecture & MVVM**, tích hợp chiến lược **Offline-First**, cơ chế đồng bộ nền với **WorkManager**, và hệ thống giao diện **Atmospheric Glassmorphism** hiện đại được thiết kế từ **Google Stitch**.

---

## 🌟 Tính năng nổi bật (Key Features)

- 📍 **Thời tiết thời gian thực (Live Current Weather):** Nhiệt độ, cảm giác thực tế (feels-like), độ ẩm, tốc độ gió, chỉ số UV, áp suất khí quyển và điểm sương.
- ⏱️ **Dự báo 24 giờ (Hourly Forecast):** Dải thông tin thời tiết từng giờ trực quan kèm biểu đồ đường cong nhiệt độ và xác suất mưa.
- 📅 **Dự báo 7 ngày (7-Day Forecast):** Xem tổng quan tuần với thanh dải nhiệt độ (min/max range slider) và điều kiện thời tiết theo ngày.
- 🔍 **Tìm kiếm thành phố thông minh (Smart Search):** Tự động debounce truy vấn, lưu lịch sử tìm kiếm, hiển thị tọa độ và cho phép thêm nhanh vào danh sách yêu thích.
- 💖 **Quản lý địa điểm yêu thích (Favorite Locations):** Theo dõi thời tiết của nhiều thành phố trên toàn thế giới với thông tin giờ địa phương.
- ⚡ **Chiến lược Offline-First (Stale-While-Revalidate):** Dữ liệu được lưu vào Room Cache, cho phép mở app và xem thời tiết ngay lập tức kể cả khi mất kết nối mạng.
- 🔄 **Làm mới dữ liệu nền (Background WorkManager):** Cập nhật dữ liệu thời tiết định kỳ tự động và thông báo tóm tắt buổi sáng.
- 🎨 **Tùy biến giao diện & Đơn vị đo (Customization):** Hỗ trợ Dark Mode / Light Mode / Theo hệ thống, chuyển đổi đơn vị nhiệt độ (°C / °F) và vận tốc gió (km/h, mph, m/s).

---

## 🏗️ Kiến trúc ứng dụng (Architecture & Design Pattern)

Ứng dụng được tổ chức theo chuẩn **Clean Architecture** kết hợp **MVVM (Model-View-ViewModel)** và **Repository Pattern**:

```text
com.example.weathernow
├── core                     # Các tiện ích cốt lõi dùng chung
│   ├── common               # Resource<T>, Constants
│   ├── network              # NetworkError mapper
│   └── location             # Trình quản lý vị trí GPS thiết bị
├── data                     # Phân lớp dữ liệu (Remote, Local, Preferences)
│   ├── remote               # Retrofit API Services, DTOs, DataSource
│   ├── local                # Room Database, DAOs, Entities
│   ├── datastore            # DataStore Preferences (Theme, Units, Settings)
│   ├── mapper               # Chuyển đổi DTO <-> Domain <-> Entity
│   └── repository           # Repository Implementations (Offline-first logic)
├── domain                   # Phân lớp nghiệp vụ độc lập (Pure Kotlin)
│   ├── model                # Domain Entities (CurrentWeather, Forecast, Location)
│   └── repository           # Repository Interfaces
└── presentation             # Phân lớp hiển thị (Jetpack Compose UI)
    ├── navigation           # Navigation3 Type-Safe Routes & NavHost
    ├── home                 # Màn hình Dashboard thời tiết chính
    ├── search               # Màn hình tìm kiếm địa điểm
    ├── favorites            # Màn hình quản lý danh sách yêu thích
    ├── settings             # Màn hình cài đặt & tùy chỉnh
    ├── onboarding           # Màn hình giới thiệu & xin quyền vị trí
    └── forecast             # Màn hình phân tích dự báo chi tiết
```

### 🔁 Luồng dữ liệu Offline-First (Stale-While-Revalidate)
1. UI quan sát dữ liệu thông qua Kotlin `StateFlow`.
2. Khi mở màn hình hoặc chọn thành phố: Repository lập tức phát dữ liệu từ **Room Database** lên UI.
3. Song song, Repository thực hiện gọi **Open-Meteo REST API** để cập nhật dữ liệu mới nhất.
4. Khi có kết quả từ API $\rightarrow$ Lưu vào Room Database $\rightarrow$ Flow tự động cập nhật UI một cách mượt mà.
5. Nếu mất mạng $\rightarrow$ Tiếp tục hiển thị dữ liệu cache với trạng thái Offline và nút Retry.

---

## 🛠️ Công nghệ & Thư viện (Tech Stack)

| Hạng mục | Công nghệ / Thư viện | Mô tả |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.x | Ngôn ngữ phát triển chính |
| **UI Framework** | Jetpack Compose + Material 3 | Giao diện khai báo hiện đại với Design System tùy chỉnh |
| **Design System** | Google Stitch | Thiết kế giao diện Atmospheric Glassmorphism (Project ID: `5798590191045381534`) |
| **Navigation** | Navigation3 (`androidx.navigation3`) | Điều hướng type-safe hiện đại |
| **Asynchronous** | Coroutines & Flow / StateFlow | Xử lý bất đồng bộ và luồng dữ liệu phản ứng |
| **Networking** | Retrofit 2 + OkHttp 3 Logging | Giao tiếp REST API với Open-Meteo |
| **Serialization** | Kotlinx Serialization | Parse dữ liệu JSON an toàn kiểu |
| **Local Database** | Room Database (SQLite) | Lưu trữ cache thời tiết và địa điểm yêu thích |
| **Preferences** | Jetpack DataStore | Lưu trữ cấu hình người dùng (Theme, Đơn vị) |
| **Background Work**| WorkManager KTX | Lập lịch tác vụ chạy nền định kỳ và thông báo |
| **Location** | Google Play Services Location | Định vị GPS theo yêu cầu của người dùng |
| **Testing** | JUnit, Kotlinx Coroutines Test, Turbine, MockK | Kiểm thử đơn vị luồng dữ liệu, ViewModel, Mapper và Repository |

---

## 🌐 Nguồn dữ liệu thời tiết (Weather API)

Ứng dụng sử dụng API miễn phí mã nguồn mở từ **[Open-Meteo](https://open-meteo.com/)**:
- **Forecast API:** `https://api.open-meteo.com/v1/forecast`
- **Geocoding API:** `https://geocoding-api.open-meteo.com/v1/search`
- *Ghi chú:* Tuân thủ điều khoản cấp phép của Open-Meteo cho mục đích phi thương mại.

---

## 🚀 Hướng dẫn cài đặt & Chạy ứng dụng (Build & Run)

### Yêu cầu hệ thống:
- Android Studio Ladybug / Quail hoặc mới hơn.
- JDK 17+ (hoặc JDK 22).
- Android SDK Platform 35 / 36 (Hỗ trợ thiết bị từ Android 8.0 Oreo - API 26 trở lên).

### Các bước biên dịch:
1. Clone mã nguồn về máy:
   ```bash
   git clone <repository-url>
   cd WeatherApp_Antigravity_Project_Plan
   ```
2. Chạy kiểm thử Unit Tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```
3. Đóng gói bản Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
4. Cài đặt trực tiếp lên thiết bị/máy ảo:
   ```bash
   ./gradlew installDebug
   ```

---

## 📄 Giấy phép (License)
Dự án được xây dựng phục vụ mục đích học tập, portfolio kỹ thuật và trình diễn kỹ năng phát triển ứng dụng Android Native.
