package cymru.asheiou.afkwebhook

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

class MessageSender {
  companion object {
    fun sendMessage(recipient: CommandSender, msg: String) {
      val prefix = ChatColor.GOLD.toString() + "[AFKWebhook] " + ChatColor.YELLOW
      recipient.sendMessage(prefix + msg)
    }
  }
}