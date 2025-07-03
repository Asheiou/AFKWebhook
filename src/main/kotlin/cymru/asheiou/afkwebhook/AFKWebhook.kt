package cymru.asheiou.afkwebhook

import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class AFKWebhook : JavaPlugin() {
  lateinit var metrics: Metrics

  override fun onEnable() {
    if (!File(dataFolder, "config.yml").exists()) {
      saveDefaultConfig()
      logger.info("Config not found! Creating one.")
      logger.warning(
        "*****\nThis plugin requires configuration! Please open the config and add a webhook URL " +
                "- instructions are in there if you don't know how to create one!\n*****"
      )
    }
    config.options().copyDefaults(true)
    reloadConfig()

    Bukkit.getPluginManager().registerEvents(EventListener(this), this)
    getCommand("reloadhook")?.setExecutor(ReloadCommand(this))

    val pluginId = 26352
    metrics = Metrics(this, pluginId)

    logger.info("Enabled successfully!")
  }

  override fun onDisable() {
    metrics.shutdown()
    logger.info("ttyl")
  }
}
