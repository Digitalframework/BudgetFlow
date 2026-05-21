package com.banking.shared.data

/**
 * Category definitions with keywords for transaction categorization.
 * Ported from the original JavaScript banking app.
 */
class CategoryMapper private constructor() {

    companion object {
        val shared: CategoryMapper = CategoryMapper()
        
        /**
         * Get all categories as a list (static accessor).
         */
        fun getAllCategories(): List<Category> = shared.getAllCategories()
    }

    val CATEGORIES = mapOf(
        "Lebensmittel" to Category(
            name = "Lebensmittel",
            label = "Lebensmittel",
            color = "#52c41a",
            icon = "🛒",
            keywords = listOf(
                "edeka", "rewe", "kaufland", "erdimili", "lidl", "aldi",
                "netto", "penny", "Baeckerei", "Backstube", "Gercek Supermarket"
            )
        ),
        "Drogerie" to Category(
            name = "Drogerie",
            label = "Drogerie / Pflege",
            color = "#13c2c2",
            icon = "🧴",
            keywords = listOf(
                "apotheke", "dm ", " dm", "rossmann", "mueller", "müller",
                "Action Germany GmbH"
            )
        ),
        "Ausgehen" to Category(
            name = "Ausgehen",
            label = "Ausgehen",
            color = "#fa8c16",
            icon = "🍽️",
            keywords = listOf(
                "grill", "restaurant", "eis", "caffe", "café", "cafe", "cafe",
                "pizz", "burger", "döner"
            )
        ),
        "Versicherungen" to Category(
            name = "Versicherungen",
            label = "Versicherungen / GEZ / Steuern",
            color = "#722ed1",
            icon = "🛡️",
            keywords = listOf(
                "arag", "adac", "gez", "rundfunk", "beitrag", "finanzamt", "steuer"
            )
        ),
        "Sport" to Category(
            name = "Sport",
            label = "Sport / Freizeit",
            color = "#eb2f96",
            icon = "🏋️",
            keywords = listOf(
                "rosa", "fitx", "mcfit", "rsg", "fitnessstudio", "sport",
                "abdul hasib", "hasib"
            )
        ),
        "Tanken" to Category(
            name = "Tanken",
            label = "Tanken",
            color = "#fa541c",
            icon = "⛽",
            keywords = listOf("jet ", "aral", "total ", "shell", "esso", "tankstelle", "bp ")
        ),
        "Fixkosten" to Category(
            name = "Fixkosten",
            label = "Fix-Kosten",
            color = "#1677ff",
            icon = "🏠",
            keywords = listOf(
                "vodafone", "telekom", "miete", "möbius", "mobius", "congstar",
                "strom", "gas", "wasser", "internet", "lekker energie"
            )
        ),
        "Einkaufen" to Category(
            name = "Einkaufen",
            label = "Einkaufen",
            color = "#d4b106",
            icon = "🛍️",
            keywords = listOf(
                "galeria", "galaeria", "jack & jones", "jack and jones", "reserved",
                "h&m", "saturn", "media markt", "mediamarkt", "zara", "primark", "c&a"
            )
        ),
        "Sonstiges" to Category(
            name = "Sonstiges",
            label = "Sonstiges",
            color = "#8c8c8c",
            icon = "❓",
            keywords = emptyList()
        )
    )

    /**
     * Categorize a transaction description based on keywords.
     * Returns the category name or "Sonstiges" if no match.
     */
    fun categorizeTransaction(description: String?): String {
        if (description.isNullOrBlank()) return "Sonstiges"
        val lower = description.lowercase()
        
        for ((key, category) in CATEGORIES) {
            if (key == "Sonstiges") continue
            for (keyword in category.keywords) {
                if (lower.contains(keyword.lowercase())) {
                    return key
                }
            }
        }
        return "Sonstiges"
    }

    /**
     * Get a category by name.
     */
    fun getCategory(name: String): Category? = CATEGORIES[name]

    /**
     * Get all categories as a list.
     */
    fun getAllCategories(): List<Category> = CATEGORIES.values.toList()
}