package com.sproutly.app.mealplan.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

enum class MealSlot(val key: String, val label: String) {
    BREAKFAST("breakfast", "Breakfast"),
    LUNCH("lunch", "Lunch"),
    DINNER("dinner", "Dinner"),
    SNACK("snack", "Snack");

    companion object {
        fun fromKey(key: String): MealSlot? = entries.firstOrNull { it.key == key }
    }
}

@Serializable
data class MealPlanDay(
    val date: String,
    val meals: Map<String, String> = emptyMap(),
) {
    fun meal(slot: MealSlot): String = meals[slot.key].orEmpty()
}

@Serializable
data class MealPlan(
    @SerialName("week_start") val weekStartISO: String,
    val days: List<MealPlanDay>,
)

object MealPlanFactory {
    private val starter: Map<MealSlot, List<String>> = mapOf(
        MealSlot.BREAKFAST to listOf(
            "Tofu scramble with greens",
            "Overnight oats with chia",
            "Avocado toast with hemp seeds",
            "Soy yogurt protein bowl",
            "Mushroom breakfast wrap",
            "Peanut butter banana oats",
            "Tempeh hash",
        ),
        MealSlot.LUNCH to listOf(
            "Lentil power bowl",
            "Chickpea herb wrap",
            "Quinoa edamame salad",
            "Black bean burrito bowl",
            "Sesame tofu noodles",
            "Falafel plate",
            "White bean tomato stew",
        ),
        MealSlot.DINNER to listOf(
            "Cashew greens pasta",
            "Miso aubergine rice",
            "Red lentil dal",
            "Mushroom tofu stir-fry",
            "Smoky tempeh tacos",
            "Coconut chickpea curry",
            "Roasted squash risotto",
        ),
        MealSlot.SNACK to listOf(
            "B12 supplement and fruit",
            "Hummus with carrots",
            "Roasted edamame",
            "Protein smoothie",
            "Trail mix",
            "Apple with peanut butter",
            "Dark chocolate soy yogurt",
        ),
    )

    private val isoDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun currentMonday(today: LocalDate = LocalDate.now()): LocalDate =
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun emptyPlan(weekStart: LocalDate = currentMonday()): MealPlan {
        val days = (0..6).map { offset ->
            MealPlanDay(date = weekStart.plusDays(offset.toLong()).format(isoDate))
        }
        return MealPlan(weekStartISO = weekStart.format(isoDate), days = days)
    }

    fun starterPlan(weekStart: LocalDate = currentMonday()): MealPlan {
        val days = (0..6).map { idx ->
            val date = weekStart.plusDays(idx.toLong())
            val meals = buildMap {
                MealSlot.entries.forEach { slot ->
                    put(slot.key, starter.getValue(slot)[idx])
                }
            }
            MealPlanDay(date = date.format(isoDate), meals = meals)
        }
        return MealPlan(weekStartISO = weekStart.format(isoDate), days = days)
    }
}
