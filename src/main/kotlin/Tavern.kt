import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt

private const val TAVERN_MASTER = "Taernyl"
private const val TAVERN_NAME = "$TAVERN_MASTER's Folly"

private val firstNames = setOf("Alex", "Mordoc", "Sophie", "Tariq")
private val lastNames = setOf("Ironfoot", "Fernsworth", "Baggins", "Downstrider")

private val menuData = File("data/tavern-menu-data.txt")
    .readText()
    .split("\n")
    .map { it.split(",") }

private val menuItems = menuData.map { (_, name, _) -> name }

private val menuItemPrices = menuData.associate { (_, name, price) -> name to price.toDouble() }

private val menuItemTypes = menuData.associate { (type, name, _) -> name to type }

fun visitTavern() {
    narrate("$heroName enters $TAVERN_NAME")
    narrate("There are severals items for sale:")
    println(menuItems.joinToString())

    val patrons: MutableSet<String> = firstNames.shuffled()
        .zip(lastNames.shuffled()) { firstName, lastName -> "$firstName $lastName" }
        .toMutableSet()


    val patronGold = mutableMapOf(
        TAVERN_MASTER to 86.00,
        heroName to 4.50,
        *patrons.map { it to 6.00 }.toTypedArray()
    )

    narrate("$heroName sees several patrons in the tavern:")
    narrate(patrons.joinToString())

    val itemOfDay = patrons.flatMap { gatFavoriterMenuItems(it) }.random()
    println("The item of the day is the $itemOfDay")

    repeat(3) {
        placeOrder(patrons.random(), menuItems.random(), patronGold)
    }
    displayPatronBalances(patronGold)

    val departingPatrons: List<String> = patrons.filter { patron ->
        patronGold.getOrDefault(patron, 0.0) < 4
    }
    patrons -= departingPatrons
    patronGold -= departingPatrons
    departingPatrons.forEach { patron ->
        narrate("$heroName sees $patron departing the tavern")
    }
    narrate("There are still some patrons in the tavern")
    narrate(patrons.joinToString())

}


private fun gatFavoriterMenuItems(patron: String): List<String> {
    return when (patron) {
        "Alex Iroonfoot" -> menuItems.filter { menuItem ->
            menuItemTypes[menuItem]?.contains("desert") == true
        }
        else -> menuItems.shuffled().take(Random.nextInt(1..2))
    }

}

private fun placeOrder(
    patronName: String,
    menuItemName: String,
    patronGold: MutableMap<String, Double>
) {
    val itemPrice = menuItemPrices.getValue(menuItemName)
    narrate("$patronName speaks with $TAVERN_MASTER to place an order")
    if (itemPrice <= patronGold.getOrDefault(patronName, 0.0)) {
        val action = when (menuItemTypes[menuItemName]) {
            "shandy", "elixir" -> "pours"
            "meal" -> "serves"
            else -> "hands"
        }
        narrate("$TAVERN_MASTER $action $patronName a $menuItemName")
        narrate("$patronName pays $TAVERN_MASTER $itemPrice gold")
        patronGold[patronName] = patronGold.getValue(patronName) - itemPrice
        patronGold[TAVERN_MASTER] = patronGold.getValue(TAVERN_MASTER) + itemPrice
    } else {
        narrate("$TAVERN_MASTER says , \"you need more coin for a $menuItemName\"")
    }
}

private fun displayPatronBalances(patronGold: MutableMap<String, Double>) {
    narrate("$heroName intuitively know how much money each patron has")
    patronGold.forEach { (patron, balance) -> narrate("$patron has ${"%.2f".format(balance)} gold") }
}
