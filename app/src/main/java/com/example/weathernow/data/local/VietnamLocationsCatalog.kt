package com.example.weathernow.data.local

import com.example.weathernow.domain.model.WeatherLocation
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Data item representing a curated Vietnamese location.
 */
data class VietnamLocationEntry(
    val id: String,
    val name: String,
    val adminArea: String,
    val latitude: Double,
    val longitude: Double,
    val aliases: List<String> = emptyList()
) {
    fun toWeatherLocation(): WeatherLocation = WeatherLocation(
        id = id,
        name = name,
        country = "Việt Nam",
        adminArea = adminArea,
        latitude = latitude,
        longitude = longitude,
        timezone = "Asia/Ho_Chi_Minh",
        isFavorite = false
    )
}

/**
 * Curated catalog of Vietnamese administrative units and key urban centers,
 * reflecting the 34 administrative units (Resolution 202/2025/QH15)
 * as well as major historical urban centers.
 */
object VietnamLocationsCatalog {

    val entries: List<VietnamLocationEntry> = listOf(
        // === 1. THÀNH PHỐ TRỰC THUỘC TRUNG ƯƠNG ===
        VietnamLocationEntry(
            id = "vn_hanoi",
            name = "Hà Nội",
            adminArea = "Thủ đô Hà Nội",
            latitude = 21.0285,
            longitude = 105.8542,
            aliases = listOf("hanoi", "ha noi", "hn", "thu do ha noi", "thu do")
        ),
        VietnamLocationEntry(
            id = "vn_hcm",
            name = "TP. Hồ Chí Minh",
            adminArea = "Thành phố Hồ Chí Minh",
            latitude = 10.8231,
            longitude = 106.6297,
            aliases = listOf("ho chi minh", "tphcm", "tp hcm", "sai gon", "saigon", "sg", "hcm")
        ),
        VietnamLocationEntry(
            id = "vn_haiphong",
            name = "Hải Phòng",
            adminArea = "TP. Hải Phòng",
            latitude = 20.8449,
            longitude = 106.6881,
            aliases = listOf("hai phong", "hp", "thanh pho hoa phuong do")
        ),
        VietnamLocationEntry(
            id = "vn_haiduong",
            name = "Hải Dương",
            adminArea = "TP. Hải Phòng",
            latitude = 20.9374,
            longitude = 106.3145,
            aliases = listOf("hai duong", "hd", "tp hai duong")
        ),
        VietnamLocationEntry(
            id = "vn_danang",
            name = "Đà Nẵng",
            adminArea = "TP. Đà Nẵng",
            latitude = 16.0544,
            longitude = 108.2022,
            aliases = listOf("da nang", "dn", "thanh pho da nang")
        ),
        VietnamLocationEntry(
            id = "vn_hoian",
            name = "Hội An",
            adminArea = "TP. Đà Nẵng",
            latitude = 15.8801,
            longitude = 108.3380,
            aliases = listOf("hoi an", "pho co hoi an", "quang nam")
        ),
        VietnamLocationEntry(
            id = "vn_tamky",
            name = "Tam Kỳ",
            adminArea = "TP. Đà Nẵng",
            latitude = 15.5707,
            longitude = 108.4735,
            aliases = listOf("tam ky", "quang nam")
        ),
        VietnamLocationEntry(
            id = "vn_cantho",
            name = "Cần Thơ",
            adminArea = "TP. Cần Thơ",
            latitude = 10.0452,
            longitude = 105.7469,
            aliases = listOf("can tho", "ct", "tay do")
        ),
        VietnamLocationEntry(
            id = "vn_hue",
            name = "Huế",
            adminArea = "TP. Huế",
            latitude = 16.4637,
            longitude = 107.5909,
            aliases = listOf("hue", "thua thien hue", "tt hue", "co do hue")
        ),
        VietnamLocationEntry(
            id = "vn_dongnai",
            name = "Đồng Nai",
            adminArea = "TP. Đồng Nai",
            latitude = 10.9574,
            longitude = 106.8427,
            aliases = listOf("dong nai", "bien hoa", "dn")
        ),

        // === 2. CÁC TỈNH ĐỒNG BẰNG SÔNG HỒNG & MIỀN BẮC (SAU SÁP NHẬP) ===
        VietnamLocationEntry(
            id = "vn_hungyen",
            name = "Hưng Yên",
            adminArea = "Tỉnh Hưng Yên",
            latitude = 20.6464,
            longitude = 106.0511,
            aliases = listOf("hung yen", "hy", "pho hien", "tp hung yen")
        ),
        VietnamLocationEntry(
            id = "vn_thaibinh",
            name = "Thái Bình",
            adminArea = "Tỉnh Hưng Yên",
            latitude = 20.4500,
            longitude = 106.3400,
            aliases = listOf("thai binh", "tb", "tp thai binh", "tinh thai binh")
        ),
        VietnamLocationEntry(
            id = "vn_ninhbinh",
            name = "Ninh Bình",
            adminArea = "Tỉnh Ninh Bình",
            latitude = 20.2506,
            longitude = 105.9745,
            aliases = listOf("ninh binh", "nb", "hoa lu", "tp ninh binh")
        ),
        VietnamLocationEntry(
            id = "vn_namdinh",
            name = "Nam Định",
            adminArea = "Tỉnh Ninh Bình",
            latitude = 20.4344,
            longitude = 106.1772,
            aliases = listOf("nam dinh", "nd", "tp nam dinh")
        ),
        VietnamLocationEntry(
            id = "vn_phuly",
            name = "Phủ Lý",
            adminArea = "Tỉnh Ninh Bình",
            latitude = 20.5413,
            longitude = 105.9139,
            aliases = listOf("phu ly", "ha nam", "tp phu ly")
        ),
        VietnamLocationEntry(
            id = "vn_bacninh",
            name = "Bắc Ninh",
            adminArea = "Tỉnh Bắc Ninh",
            latitude = 21.1861,
            longitude = 106.0763,
            aliases = listOf("bac ninh", "bn", "kinh bac", "tp bac ninh")
        ),
        VietnamLocationEntry(
            id = "vn_bacgiang",
            name = "Bắc Giang",
            adminArea = "Tỉnh Bắc Ninh",
            latitude = 21.2731,
            longitude = 106.1946,
            aliases = listOf("bac giang", "bg", "tp bac giang")
        ),
        VietnamLocationEntry(
            id = "vn_phutho",
            name = "Phú Thọ",
            adminArea = "Tỉnh Phú Thọ",
            latitude = 21.3228,
            longitude = 105.2280,
            aliases = listOf("phu tho", "pt", "viet tri", "dat to")
        ),
        VietnamLocationEntry(
            id = "vn_vinhyen",
            name = "Vĩnh Yên",
            adminArea = "Tỉnh Phú Thọ",
            latitude = 21.3090,
            longitude = 105.6049,
            aliases = listOf("vinh yen", "vinh phuc", "vp", "tp vinh yen")
        ),
        VietnamLocationEntry(
            id = "vn_hoabinh",
            name = "Hòa Bình",
            adminArea = "Tỉnh Phú Thọ",
            latitude = 20.8172,
            longitude = 105.3376,
            aliases = listOf("hoa binh", "hb", "tp hoa binh")
        ),
        VietnamLocationEntry(
            id = "vn_quangninh",
            name = "Quảng Ninh",
            adminArea = "Tỉnh Quảng Ninh",
            latitude = 20.9500,
            longitude = 107.0833,
            aliases = listOf("quang ninh", "qn", "ha long", "cam pha", "uong bi")
        ),
        VietnamLocationEntry(
            id = "vn_tuyenquang",
            name = "Tuyên Quang",
            adminArea = "Tỉnh Tuyên Quang",
            latitude = 21.8234,
            longitude = 105.2155,
            aliases = listOf("tuyen quang", "tq")
        ),
        VietnamLocationEntry(
            id = "vn_hagiang",
            name = "Hà Giang",
            adminArea = "Tỉnh Tuyên Quang",
            latitude = 22.8233,
            longitude = 104.9839,
            aliases = listOf("ha giang", "hg", "cao nguyen da dong van")
        ),
        VietnamLocationEntry(
            id = "vn_laocai",
            name = "Lào Cai",
            adminArea = "Tỉnh Lào Cai",
            latitude = 22.4856,
            longitude = 103.9707,
            aliases = listOf("lao cai", "lc", "tp lao cai")
        ),
        VietnamLocationEntry(
            id = "vn_sapa",
            name = "Sa Pa",
            adminArea = "Tỉnh Lào Cai",
            latitude = 22.3364,
            longitude = 103.8438,
            aliases = listOf("sa pa", "sapa", "fansipan")
        ),
        VietnamLocationEntry(
            id = "vn_yenbai",
            name = "Yên Bái",
            adminArea = "Tỉnh Lào Cai",
            latitude = 21.7167,
            longitude = 104.8667,
            aliases = listOf("yen bai", "yb")
        ),
        VietnamLocationEntry(
            id = "vn_thainguyen",
            name = "Thái Nguyên",
            adminArea = "Tỉnh Thái Nguyên",
            latitude = 21.5928,
            longitude = 105.8442,
            aliases = listOf("thai nguyen", "tn", "tp thai nguyen")
        ),
        VietnamLocationEntry(
            id = "vn_backan",
            name = "Bắc Kạn",
            adminArea = "Tỉnh Thái Nguyên",
            latitude = 22.1472,
            longitude = 105.8347,
            aliases = listOf("bac kan", "bk")
        ),
        VietnamLocationEntry(
            id = "vn_caobang",
            name = "Cao Bằng",
            adminArea = "Tỉnh Cao Bằng",
            latitude = 22.6667,
            longitude = 106.2500,
            aliases = listOf("cao bang", "cb", "thac ban gioc")
        ),
        VietnamLocationEntry(
            id = "vn_langson",
            name = "Lạng Sơn",
            adminArea = "Tỉnh Lạng Sơn",
            latitude = 21.8500,
            longitude = 106.7500,
            aliases = listOf("lang son", "ls", "xu lang")
        ),
        VietnamLocationEntry(
            id = "vn_laichau",
            name = "Lai Châu",
            adminArea = "Tỉnh Lai Châu",
            latitude = 22.3833,
            longitude = 103.4667,
            aliases = listOf("lai chau", "lc")
        ),
        VietnamLocationEntry(
            id = "vn_dienbien",
            name = "Điện Biên",
            adminArea = "Tỉnh Điện Biên",
            latitude = 21.3833,
            longitude = 103.0167,
            aliases = listOf("dien bien", "dien bien phu", "db")
        ),
        VietnamLocationEntry(
            id = "vn_sonla",
            name = "Sơn La",
            adminArea = "Tỉnh Sơn La",
            latitude = 21.3167,
            longitude = 103.9000,
            aliases = listOf("son la", "sl", "moc chau")
        ),

        // === 3. BẮC TRUNG BỘ & DUYÊN HẢI MIỀN TRUNG ===
        VietnamLocationEntry(
            id = "vn_thanhhoa",
            name = "Thanh Hóa",
            adminArea = "Tỉnh Thanh Hóa",
            latitude = 19.8000,
            longitude = 105.7667,
            aliases = listOf("thanh hoa", "th", "sam son", "tp thanh hoa")
        ),
        VietnamLocationEntry(
            id = "vn_nghean",
            name = "Nghệ An",
            adminArea = "Tỉnh Nghệ An",
            latitude = 18.6667,
            longitude = 105.6667,
            aliases = listOf("nghe an", "na", "vinh", "cua lo", "tp vinh")
        ),
        VietnamLocationEntry(
            id = "vn_hatinh",
            name = "Hà Tĩnh",
            adminArea = "Tỉnh Hà Tĩnh",
            latitude = 18.3428,
            longitude = 105.9058,
            aliases = listOf("ha tinh", "ht", "tp ha tinh")
        ),
        VietnamLocationEntry(
            id = "vn_quangtri",
            name = "Quảng Trị",
            adminArea = "Tỉnh Quảng Trị",
            latitude = 16.8167,
            longitude = 107.1000,
            aliases = listOf("quang tri", "qt", "dong ha")
        ),
        VietnamLocationEntry(
            id = "vn_donghoi",
            name = "Đồng Hới",
            adminArea = "Tỉnh Quảng Trị",
            latitude = 17.4833,
            longitude = 106.6000,
            aliases = listOf("dong hoi", "quang binh", "qb", "phong nha")
        ),
        VietnamLocationEntry(
            id = "vn_quangngai",
            name = "Quảng Ngãi",
            adminArea = "Tỉnh Quảng Ngãi",
            latitude = 15.1205,
            longitude = 108.7923,
            aliases = listOf("quang ngai", "qn", "tp quang ngai", "ly son")
        ),
        VietnamLocationEntry(
            id = "vn_kontum",
            name = "Kon Tum",
            adminArea = "Tỉnh Quảng Ngãi",
            latitude = 14.3500,
            longitude = 108.0000,
            aliases = listOf("kon tum", "kt", "mang den")
        ),
        VietnamLocationEntry(
            id = "vn_gialai",
            name = "Gia Lai",
            adminArea = "Tỉnh Gia Lai",
            latitude = 13.9833,
            longitude = 108.0000,
            aliases = listOf("gia lai", "gl", "pleiku")
        ),
        VietnamLocationEntry(
            id = "vn_quynhon",
            name = "Quy Nhơn",
            adminArea = "Tỉnh Gia Lai",
            latitude = 13.7820,
            longitude = 109.2194,
            aliases = listOf("quy nhon", "binh dinh", "tp quy nhon")
        ),
        VietnamLocationEntry(
            id = "vn_khanhhoa",
            name = "Khánh Hòa",
            adminArea = "Tỉnh Khánh Hòa",
            latitude = 12.2388,
            longitude = 109.1967,
            aliases = listOf("khanh hoa", "kh", "nha trang", "cam ranh")
        ),
        VietnamLocationEntry(
            id = "vn_phanrang",
            name = "Phan Rang",
            adminArea = "Tỉnh Khánh Hòa",
            latitude = 11.5681,
            longitude = 108.9864,
            aliases = listOf("phan rang", "ninh thuan", "phan rang thap cham")
        ),

        // === 4. TÂY NGUYÊN & ĐÔNG NAM BỘ ===
        VietnamLocationEntry(
            id = "vn_lamdong",
            name = "Lâm Đồng",
            adminArea = "Tỉnh Lâm Đồng",
            latitude = 11.9404,
            longitude = 108.4583,
            aliases = listOf("lam dong", "ld", "da lat", "dalat", "bao loc")
        ),
        VietnamLocationEntry(
            id = "vn_phanthiet",
            name = "Phan Thiết",
            adminArea = "Tỉnh Lâm Đồng",
            latitude = 10.9273,
            longitude = 108.1022,
            aliases = listOf("phan thiet", "binh thuan", "mui ne")
        ),
        VietnamLocationEntry(
            id = "vn_gianghia",
            name = "Gia Nghĩa",
            adminArea = "Tỉnh Lâm Đồng",
            latitude = 11.9936,
            longitude = 107.6983,
            aliases = listOf("gia nghia", "dak nong")
        ),
        VietnamLocationEntry(
            id = "vn_daklak",
            name = "Đắk Lắk",
            adminArea = "Tỉnh Đắk Lắk",
            latitude = 12.6667,
            longitude = 108.0500,
            aliases = listOf("dak lak", "dac lac", "buon ma thuot", "bmt")
        ),
        VietnamLocationEntry(
            id = "vn_tayninh",
            name = "Tây Ninh",
            adminArea = "Tỉnh Tây Ninh",
            latitude = 11.3100,
            longitude = 106.0983,
            aliases = listOf("tay ninh", "tn", "nui ba den")
        ),
        VietnamLocationEntry(
            id = "vn_binhduong",
            name = "Bình Dương",
            adminArea = "Tỉnh Bình Dương",
            latitude = 10.9804,
            longitude = 106.6519,
            aliases = listOf("binh duong", "bd", "thu dau mot")
        ),
        VietnamLocationEntry(
            id = "vn_vungtau",
            name = "Bà Rịa - Vũng Tàu",
            adminArea = "Tỉnh Bà Rịa - Vũng Tàu",
            latitude = 10.4114,
            longitude = 107.1362,
            aliases = listOf("vung tau", "ba ria", "brvt", "vt")
        ),

        // === 5. TÂY NAM BỘ (ĐỒNG BẰNG SÔNG CỬU LONG) ===
        VietnamLocationEntry(
            id = "vn_longan",
            name = "Long An",
            adminArea = "Tỉnh Long An",
            latitude = 10.5333,
            longitude = 106.4000,
            aliases = listOf("long an", "tan an", "la")
        ),
        VietnamLocationEntry(
            id = "vn_tiengiang",
            name = "Tiền Giang",
            adminArea = "Tỉnh Tiền Giang",
            latitude = 10.3667,
            longitude = 106.3667,
            aliases = listOf("tien giang", "my tho", "tg")
        ),
        VietnamLocationEntry(
            id = "vn_bentre",
            name = "Bến Tre",
            adminArea = "Tỉnh Bến Tre",
            latitude = 10.2333,
            longitude = 106.3833,
            aliases = listOf("ben tre", "bt", "xu dua")
        ),
        VietnamLocationEntry(
            id = "vn_travinh",
            name = "Trà Vinh",
            adminArea = "Tỉnh Trà Vinh",
            latitude = 9.9333,
            longitude = 106.3333,
            aliases = listOf("tra vinh", "tv")
        ),
        VietnamLocationEntry(
            id = "vn_vinhlong",
            name = "Vĩnh Long",
            adminArea = "Tỉnh Vĩnh Long",
            latitude = 10.2500,
            longitude = 105.9667,
            aliases = listOf("vinh long", "vl")
        ),
        VietnamLocationEntry(
            id = "vn_dongthap",
            name = "Đồng Tháp",
            adminArea = "Tỉnh Đồng Tháp",
            latitude = 10.4500,
            longitude = 105.6333,
            aliases = listOf("dong thap", "cao lanh", "sa dec", "dt")
        ),
        VietnamLocationEntry(
            id = "vn_angiang",
            name = "An Giang",
            adminArea = "Tỉnh An Giang",
            latitude = 10.3833,
            longitude = 105.4333,
            aliases = listOf("an giang", "long xuyen", "chau doc", "ag")
        ),
        VietnamLocationEntry(
            id = "vn_kiengiang",
            name = "Kiên Giang",
            adminArea = "Tỉnh Kiên Giang",
            latitude = 10.0167,
            longitude = 105.0833,
            aliases = listOf("kien giang", "rach gia", "ha tien", "kg")
        ),
        VietnamLocationEntry(
            id = "vn_phuquoc",
            name = "Phú Quốc",
            adminArea = "Tỉnh Kiên Giang",
            latitude = 10.2899,
            longitude = 103.9840,
            aliases = listOf("phu quoc", "dao phu quoc", "pq", "dao ngoc")
        ),
        VietnamLocationEntry(
            id = "vn_haugiang",
            name = "Hậu Giang",
            adminArea = "Tỉnh Hậu Giang",
            latitude = 9.7833,
            longitude = 105.4667,
            aliases = listOf("hau giang", "vi thanh", "hg")
        ),
        VietnamLocationEntry(
            id = "vn_soctrang",
            name = "Sóc Trăng",
            adminArea = "Tỉnh Sóc Trăng",
            latitude = 9.6000,
            longitude = 105.9667,
            aliases = listOf("soc trang", "st")
        ),
        VietnamLocationEntry(
            id = "vn_baclieu",
            name = "Bạc Liêu",
            adminArea = "Tỉnh Bạc Liêu",
            latitude = 9.2833,
            longitude = 105.7167,
            aliases = listOf("bac lieu", "bl", "cong tu bac lieu")
        ),
        VietnamLocationEntry(
            id = "vn_camau",
            name = "Cà Mau",
            adminArea = "Tỉnh Cà Mau",
            latitude = 9.1833,
            longitude = 105.1500,
            aliases = listOf("ca mau", "cm", "dat mui ca mau")
        )
    )

    /**
     * Strips Vietnamese diacritics / accents and non-alphanumeric chars for resilient fuzzy searching.
     */
    fun normalize(input: String): String {
        val normalized = Normalizer.normalize(input.lowercase().trim(), Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(normalized).replaceAll("")
            .replace('đ', 'd')
            .replace('Đ', 'd')
            .replace(Regex("[^a-z0-9 ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Search within curated Vietnamese locations with exact and prefix fuzzy matching.
     */
    fun search(query: String): List<WeatherLocation> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()

        return entries.filter { entry ->
            val normalizedName = normalize(entry.name)
            val normalizedAdmin = normalize(entry.adminArea)
            val matchesName = normalizedName.contains(normalizedQuery) || normalizedQuery.contains(normalizedName)
            val matchesAdmin = normalizedAdmin.contains(normalizedQuery)
            val matchesAlias = entry.aliases.any { alias ->
                val normAlias = normalize(alias)
                normAlias.contains(normalizedQuery) || normalizedQuery == normAlias
            }
            matchesName || matchesAdmin || matchesAlias
        }.sortedByDescending { entry ->
            val normName = normalize(entry.name)
            when {
                normName == normalizedQuery -> 100
                normName.startsWith(normalizedQuery) -> 50
                entry.aliases.any { normalize(it) == normalizedQuery } -> 80
                else -> 10
            }
        }.map { it.toWeatherLocation() }
    }
}
