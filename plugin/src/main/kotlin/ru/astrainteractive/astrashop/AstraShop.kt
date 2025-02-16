@file:OptIn(UnsafeApi::class)

package ru.astrainteractive.astrashop

import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.kotlin.tooling.core.UnsafeApi
import ru.astrainteractive.astralibs.async.PluginScope
import ru.astrainteractive.astralibs.menu.event.GlobalInventoryClickEvent
import ru.astrainteractive.astrashop.command.CommandManager
import ru.astrainteractive.astrashop.di.RootModule
import ru.astrainteractive.astrashop.di.impl.CommandModuleImpl
import ru.astrainteractive.astrashop.di.impl.RootModuleImpl
import ru.astrainteractive.klibs.kdi.getValue

/**
 * Initial class for your plugin
 */
class AstraShop : JavaPlugin() {
    private val rootModule: RootModule by RootModuleImpl
    init {
        rootModule.plugin.initialize(this)
    }

    /**
     * This method called when server starts or PlugMan load plugin.
     */
    override fun onEnable() {
        CommandManager(this, CommandModuleImpl).create()
        GlobalInventoryClickEvent.onEnable(this)
    }

    /**
     * This method called when server is shutting down or when PlugMan disable plugin.
     */
    override fun onDisable() {
        HandlerList.unregisterAll(this)
        PluginScope.close()
        GlobalInventoryClickEvent.onDisable()
        Bukkit.getOnlinePlayers().forEach {
            it.closeInventory()
        }
    }

    /**
     * As it says, function for plugin reload
     */
    fun reloadPlugin() {
        rootModule.translation.reload()
        Bukkit.getOnlinePlayers().forEach {
            it.closeInventory()
        }
    }
}
