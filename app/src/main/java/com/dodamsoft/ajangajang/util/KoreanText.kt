package com.dodamsoft.ajangajang.util

/**
 * Returns the Korean possessive particle appropriate for [name].
 *
 * - 지민 (받침 O) → 지민이의
 * - 지우 (받침 X) → 지우의
 * - 비한글 → `의`
 *
 * The `이` suffix is informal and attaches only when the final syllable has a 받침 (jongseong).
 */
fun koreanPossessive(name: String): String {
    val last = name.trim().lastOrNull() ?: return "의"
    val code = last.code
    val isHangulSyllable = code in 0xAC00..0xD7A3
    if (!isHangulSyllable) return "의"
    val hasJongseong = (code - 0xAC00) % 28 != 0
    return if (hasJongseong) "이의" else "의"
}
