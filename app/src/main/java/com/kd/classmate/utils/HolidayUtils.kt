package com.kd.classmate.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters

object HolidayUtils {

    fun getHolidayName(date: LocalDate): String? {
        val year = date.year
        val holidays = mutableMapOf<LocalDate, String>()

        // 1. FIXED DATE HOLIDAYS
        holidays[LocalDate.of(year, Month.JANUARY, 1)] = "New Year's Day"
        holidays[LocalDate.of(year, Month.FEBRUARY, 25)] = "EDSA People Power Revolution"
        holidays[LocalDate.of(year, Month.APRIL, 9)] = "Araw ng Kagitingan"
        holidays[LocalDate.of(year, Month.MAY, 1)] = "Labor Day"
        holidays[LocalDate.of(year, Month.JUNE, 12)] = "Independence Day"
        holidays[LocalDate.of(year, Month.AUGUST, 21)] = "Ninoy Aquino Day"
        holidays[LocalDate.of(year, Month.NOVEMBER, 1)] = "All Saints' Day"
        holidays[LocalDate.of(year, Month.NOVEMBER, 30)] = "Bonifacio Day"
        holidays[LocalDate.of(year, Month.DECEMBER, 8)] = "Feast of the Immaculate Conception"
        holidays[LocalDate.of(year, Month.DECEMBER, 25)] = "Christmas Day"
        holidays[LocalDate.of(year, Month.DECEMBER, 30)] = "Rizal Day"
        holidays[LocalDate.of(year, Month.DECEMBER, 31)] = "New Year's Eve"

        // 2. MOVEABLE: National Heroes Day (Last Monday of August)
        val heroesDay = LocalDate.of(year, Month.AUGUST, 31)
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        holidays[heroesDay] = "National Heroes Day"

        // 3. MOVEABLE: Holy Week (Calculated via Computus Algorithm)
        val easter = calculateEaster(year)
        holidays[easter.minusDays(3)] = "Maundy Thursday"
        holidays[easter.minusDays(2)] = "Good Friday"
        holidays[easter.minusDays(1)] = "Black Saturday"

        return holidays[date]
    }


     // Meeus/Jones/Butcher Algorithm for Gregorian Easter
    private fun calculateEaster(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }

    fun isHoliday(date: LocalDate): Boolean = getHolidayName(date) != null
}