package com.uycode.wit.util

import com.uycode.wit.Constants


fun reformatPhoneNumber(s: String): String {
    return s.replace(Constants.PURE_NUMBER_REGEX.toRegex(), "")
}