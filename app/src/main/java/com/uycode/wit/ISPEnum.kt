package com.uycode.wit

enum class ISPEnum(val nameCn: String, val icon: Int) {
    CHINA_TELECOM("中国电信", R.drawable.china_telecom),
    CHINA_UNICOM("中国联通", R.drawable.china_unicom),
    CHINA_MOBILE("中国移动", R.drawable.china_mobile),
    UNKNOWN("未知", R.drawable.unknown_isp);


    companion object {
        fun getByNameCn(nameCn: String): ISPEnum {
            for (isp in entries) {
                if (isp.nameCn == nameCn) {
                    return isp
                }
            }
            return UNKNOWN
        }
    }
}