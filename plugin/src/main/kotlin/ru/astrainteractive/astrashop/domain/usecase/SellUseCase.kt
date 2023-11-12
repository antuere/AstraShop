package ru.astrainteractive.astrashop.domain.usecase

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.astrainteractive.astralibs.economy.EconomyProvider
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.astrashop.api.calculator.PriceCalculator
import ru.astrainteractive.astrashop.api.model.ShopConfig
import ru.astrainteractive.astrashop.api.model.SpigotShopItem
import ru.astrainteractive.astrashop.domain.usecase.SellUseCase.Param
import ru.astrainteractive.astrashop.domain.usecase.SellUseCase.Result
import ru.astrainteractive.astrashop.util.copy
import ru.astrainteractive.klibs.mikro.core.domain.UseCase

interface SellUseCase : UseCase.Parametrized<Param, Result> {
    class Param(
        val amount: Int,
        val shopItem: ShopConfig.ShopItem<SpigotShopItem>,
        val player: Player
    )

    sealed interface Result {
        object Failure : Result
        class Success(val soldAmount: Int) : Result
    }
}

class SellUseCaseImpl(
    private val economy: EconomyProvider,
    private val logger: Logger,
) : SellUseCase {

    override suspend operator fun invoke(input: Param): Result {
        val item = input.shopItem
        val player = input.player
        val amount = input.amount
        if (PriceCalculator.calculateSellPrice(item, amount) <= 0) {
            player.sendMessage("Предмет не закупается")
            return Result.Failure
        }
        val itemStack = when (val shopItem = item.shopItem) {
            is SpigotShopItem.ItemStack -> {
                if (!player.inventory.contains(shopItem.itemStack)) {
                    player.sendMessage("У вас нет такого предмета")
                    return Result.Failure
                }
                shopItem.itemStack
            }

            is SpigotShopItem.Material -> {
                if (!player.inventory.contains(shopItem.material)) {
                    player.sendMessage("У вас нет такого предмета")
                    return Result.Failure
                }
                ItemStack(shopItem.material)
            }

            else -> error("Not spigot item")
        }
        val couldNotRemoveAmount = player.inventory.removeItem(itemStack.copy(amount)).map { it.value.amount }.sum()
        val sellAmount = amount - couldNotRemoveAmount

        val money = PriceCalculator.calculateSellPrice(item, sellAmount)
        economy.addMoney(player.uniqueId, money)
        player.sendMessage("Вы получили $money\$")
        logger.info(
            "BuyUseCase",
            "${player.name} sold $sellAmount of ${itemStack.type.name} for $money",
            logInFile = false
        )
        return Result.Success(sellAmount)
    }
}