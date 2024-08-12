package com.uycode.wit

data class PhoneInfo(
    val id: Int,
    val number: String,
    val name: String,
    val isp: String,
    val province: String,
    val city: String,
    val zip: String,
    val areaCode: String
) {
    override fun toString(): String {
        return "PhoneInfo(number='$number', name='$name', isp='$isp', province='$province', city='$city', zip='$zip', areaCode='$areaCode')"
    }

    companion object {
        // getter and setter methods
        fun PhoneInfo(): PhoneInfo {
            return PhoneInfo(0,"", "", "", "", "", "", "")
        }
    }

}
