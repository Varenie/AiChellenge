package ru.varenie.aichellenge.domain.models

data class Meal(
    val name: String,
    val calories: Int = 0,      // если 0 — будем высчитывать
    val protein: Int = 0,
    val fat: Int = 0,
    val carbs: Int = 0,
    val weightGrams: Int? = null // вес блюда в граммах
) {
    fun withEstimatedValues(): Meal {
        if (calories != 0) return this

        // Простая оценка по средним значениям на 100 г
        val estimatedWeight = weightGrams ?: 150 // если не указан, считаем 150 г
        val avgCaloriesPer100g = 150
        val avgProteinPer100g = 5
        val avgFatPer100g = 7
        val avgCarbsPer100g = 20

        val factor = estimatedWeight / 100f

        return copy(
            calories = (avgCaloriesPer100g * factor).toInt(),
            protein = (avgProteinPer100g * factor).toInt(),
            fat = (avgFatPer100g * factor).toInt(),
            carbs = (avgCarbsPer100g * factor).toInt()
        )
    }
}

data class MealResponse(
    val totalCalories: Int = 0,
    val meals: List<Meal> = emptyList()
) {
    companion object {
        fun fromMeals(meals: List<Meal>): MealResponse {
            val estimatedMeals = meals.map { it.withEstimatedValues() }
            val total = estimatedMeals.sumOf { it.calories }
            return MealResponse(totalCalories = total, meals = estimatedMeals)
        }
    }
}

