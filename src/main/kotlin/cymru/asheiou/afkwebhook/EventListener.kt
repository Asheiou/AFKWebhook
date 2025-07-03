package cymru.asheiou.afkwebhook

import com.earth2me.essentials.Essentials
import net.ess3.api.events.AfkStatusChangeEvent
import net.ess3.api.events.VanishStatusChangeEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.net.URI
import java.net.URISyntaxException

class EventListener(val plugin: JavaPlugin) : Listener {
  val ess = Bukkit.getPluginManager().getPlugin("Essentials") as Essentials

  @EventHandler(priority = EventPriority.LOWEST)
  fun onAfkStatusChange(event: AfkStatusChangeEvent) {
    val player = event.affected.base
    if (!player.hasPermission("afkwebhook.send")) {
      plugin.logger.info("Player ${player.name} does not have permission to send an afk webhook.")
      return
    }
    if (player.uniqueId in VanishHandler.vanishedUsers) {
      plugin.logger.info("Player ${player.name} is vanished and won't send an afk webhook.")
      return
    }
    val webhook = plugin.config.getString("webhook") ?: run {
      plugin.logger.warning("Webhook supplied does not exist!")
      notifyAdminsOfError()
      return
    }
    if (webhook == "WEBHOOK") {
      plugin.logger.warning("Setup not complete! Please define the webhook in the config.yml.")
      notifyAdminsOfError()
      return
    }
    val message = plugin.config.getString("messages." + if (event.value) "afk" else "not-afk")
    if (message == null) {
      plugin.logger.warning(
        "You appear to have deleted the messages in the config.yml. You can either put them back " +
                "or delete the config and restart your server to get the default back (although you'll have to" +
                " reconfigure the webhook)."
      )
      notifyAdminsOfError()
      return
    }
    val messageSubstituted = message.replace("{PLAYER}", player.name)
    val uri: URI
    try {
      uri = URI(webhook)
    } catch (_: URISyntaxException) {
      plugin.logger.warning("Your webhook is not in valid URL format. Please check it and reload the plugin!")
      notifyAdminsOfError()
      return
    }
    val response = WebhookSender.postWebhook(uri, messageSubstituted) ?: run {
      plugin.logger.warning("The request failed to send. This most often is a result of a malformed webhook URL." +
              " Check your config and reload the plugin!")
      notifyAdminsOfError()
      return
    }
    val validationCheck = WebhookSender.validateResponse(response)
    if (validationCheck) {
      plugin.logger.info("AFK webhook posted successfully!")
      return
    }
    plugin.logger.warning("AFK webhook failed. Code:" + response.statusCode() + ". Message:" + response.body())
    notifyAdminsOfError()

  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onVanishStatusChange(event: VanishStatusChangeEvent) {
    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
      if (event.value) VanishHandler.vanishedUsers.add(event.affected.uuid)
      else VanishHandler.vanishedUsers.remove(event.affected.uuid)
    }, 1L)
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onPlayerJoin(event: PlayerJoinEvent) {
    val user = ess.getUser(event.player)
    if (user.isVanished) VanishHandler.vanishedUsers.add(event.player.uniqueId)
  }

  fun notifyAdminsOfError() {
    for (player in Bukkit.getOnlinePlayers()) {
      if (player.hasPermission("afkwebhook.reload")) MessageSender.sendMessage(
        player,
        "An error occurred trying to send an AFK webhook. Please check the console for more details."
      )
    }
  }
}