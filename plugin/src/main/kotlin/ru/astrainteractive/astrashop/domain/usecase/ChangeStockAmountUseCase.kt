package ru.astrainteractive.astrashop.domain.usecase

import ru.astrainteractive.astrashop.api.ShopApi
import ru.astrainteractive.astrashop.api.model.ShopConfig
import ru.astrainteractive.astrashop.api.model.SpigotShopItem
import ru.astrainteractive.astrashop.api.model.SpigotTitleItem
import ru.astrainteractive.astrashop.domain.usecase.ChangeStockAmountUseCase.Param
import ru.astrainteractive.klibs.mikro.core.domain.UseCase

interface ChangeStockAmountUseCase : UseCase.Parametrized<Param, Unit> {
    class Param(
        val shopItem: ShopConfig.ShopItem<SpigotShopItem>,
        val increaseAmount: Int,
        val shopConfig: ShopConfig<SpigotTitleItem, SpigotShopItem>
    )
}

class ChangeStockAmountUseCaseImpl(
    private val dataSource: ShopApi<SpigotTitleItem, SpigotShopItem>
) : ChangeStockAmountUseCase {

    override suspend operator fun invoke(input: Param) {
        val item = input.shopItem
        val i = input.increaseAmount
        val shopConfig = input.shopConfig
        if (item.stock != -1) {
            val newAmount = (item.stock + i).coerceAtLeast(1)
            item.stock = newAmount
        }
        dataSource.updateShop(shopConfig)
    }
}