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
            color = "#199e70",
            icon = "🛒",
            keywords = listOf(
                "edeka", "rewe", "kaufland", "erdimili", "lidl", "aldi",
                "netto", "penny", "Baeckerei", "Backstube", "Gercek Supermarket",
                "picnic", "kaisermarkt", "hepsi", "zurheide"
            )
        ),
        "Drogerie" to Category(
            name = "Drogerie",
            label = "Drogerie / Pflege",
            color = "#19a2b8",
            icon = "🧴",
            keywords = listOf(
                "apotheke", "dm ", " dm", "rossmann", "mueller", "müller",
                "Action Germany GmbH"
            )
        ),
        "Ausgehen" to Category(
            name = "Ausgehen",
            label = "Ausgehen",
            color = "#d95926",
            icon = "🍽️",
            // "eis" alone also matches "Freisinger Bank" – keep it specific
            keywords = listOf(
                "grill", "restaurant", "eisdiele", "eiscafe", "eiscafé", "caffe",
                "café", "cafe", "pizz", "burger", "döner", "doner", "kebab",
                "imbiss", "mcdonald", "kfc", "subway"
            )
        ),
        "Versicherungen" to Category(
            name = "Versicherungen",
            label = "Versicherungen / GEZ / Steuern",
            color = "#9085e9",
            icon = "🛡️",
            keywords = listOf(
                "arag", "adac", "gez", "rundfunk", "beitrag", "finanzamt", "steuer",
                "versicherung", "concordia", "allianz", "huk", "ergo ", "axa",
                "debeka", "gothaer", "provinzial", "signal iduna", "devk", "generali",
                "aok", "barmer", "krankenkasse", "gesundheitskasse", "gesundheits kasse"
            )
        ),
        "Sport" to Category(
            name = "Sport",
            label = "Sport / Freizeit",
            color = "#d55181",
            icon = "🏋️",
            keywords = listOf(
                "rosa", "fitx", "mcfit", "rsg", "fitnessstudio", "sport",
                "abdul hasib", "hasib"
            )
        ),
        "Tanken" to Category(
            name = "Tanken",
            label = "Tanken",
            color = "#e66767",
            icon = "⛽",
            keywords = listOf("jet ", "aral", "total ", "shell", "esso", "tankstelle", "bp ")
        ),
        "Fixkosten" to Category(
            name = "Fixkosten",
            label = "Fix-Kosten",
            color = "#3987e5",
            icon = "🏠",
            keywords = listOf(
                "vodafone", "telekom", "miete", "möbius", "mobius", "congstar",
                "strom", "gas", "wasser", "internet", "lekker energie",
                "drillisch", "rheinenergie"
            )
        ),
        "Einkaufen" to Category(
            name = "Einkaufen",
            label = "Einkaufen",
            color = "#c98500",
            icon = "🛍️",
            keywords = listOf(
                "galeria", "galaeria", "jack & jones", "jack and jones", "reserved",
                "h&m", "saturn", "media markt", "mediamarkt", "zara", "primark", "c&a",
                "h+m", "tkmaxx", "tk maxx", "miniso", "sostrene", "amazon",
                "vero moda", "etsy", "klarna"
            )
        ),
        "Sonstiges" to Category(
            name = "Sonstiges",
            label = "Sonstiges",
            color = "#7b7a74",
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