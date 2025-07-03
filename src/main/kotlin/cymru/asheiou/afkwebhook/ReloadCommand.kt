package cymru.asheiou.afkwebhook

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.net.URI
import java.net.URISyntaxException

class ReloadCommand(val plugin: JavaPlugin) : CommandExecutor {
  override fun onCommand(
    sender: CommandSender,
    command: Command,
    label: String,
    args: Array<out String?>?
  ): Boolean {
    MessageSender.sendMessage(sender, "Reloading config...")
    plugin.reloadConfig()
    MessageSender.sendMessage(sender, "Reloaded! Sending test webhook...")
    val webhook = plugin.config.getString("webhook")
    if (webhook == null) {
      MessageSender.sendMessage(
        sender, "No webhook was found in your config! Please check it and try " +
                "again."
      )
      return true
    }
    val uri: URI
    try {
      uri = URI(webhook)
    } catch (_: URISyntaxException) {
      MessageSender.sendMessage(
        sender, "Your webhook URI isn't in a valid link format. Please check " +
                "your config and try again."
      )
      return true
    }
    val response = WebhookSender.postWebhook(uri, "Ping!") ?: run {
      MessageSender.sendMessage(sender,"The request failed to send! This is most often the result of a " +
              "malformed webhook URL. Please check your config and try again.")
      return true
    }
    val validationCheck = WebhookSender.validateResponse(response)
    if (validationCheck) {
      MessageSender.sendMessage(sender, "Webhook sent successfully!")
      return true
    }
    MessageSender.sendMessage(
      sender, "Webhook send failed! Code: " + response.statusCode()
              + ". Message:" + response.body() + ". Please check your config and try again."
    )
    return true
  }
}