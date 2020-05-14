package stanic.stfakeplayers.commands

import com.comphenix.protocol.wrappers.EnumWrappers
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import stanic.stfakeplayers.Main
import stanic.stfakeplayers.Main.Companion.settings
import stanic.stfakeplayers.utils.createFakePlayer
import stanic.stutils.bukkit.command.command
import stanic.stutils.bukkit.message.replace
import stanic.stutils.bukkit.message.replaceColor
import stanic.stutils.bukkit.message.send

fun Main.registerFakeCommand() = command("fake") { sender, args ->
    if (sender !is Player) sender.send(settings.getString("Messages.onlyInGame").replaceColor())
    else {
        if (!sender.hasPermission(settings.getString("Config.permission"))) {
            sender.send(settings.getString("Messages.notHasPermission").replaceColor())
            return@command
        }
        if (args.size < 2 && args.getOrNull(0) != "list") {
            sender.send(settings.getStringList("Messages.argsEmpty").replaceColor())
            return@command
        }

        when (args[0]) {
            "list", "lista", "ver", "show" -> {
                if (Main.instance.fakes.isEmpty()) sender.send(settings.getString("Messages.notHasFakes").replaceColor())
                else {
                    var fakes = ""
                    if (Main.instance.fakes.keys.toList().size == 1) fakes = Main.instance.fakes.keys.toList()[0]
                    else for (fake in Main.instance.fakes.keys.toList()) fakes = "$fakes, $fake"

                    sender.send(
                        settings.getStringList("Messages.fakeList").replaceColor()
                            .replace("{amount}" to Main.instance.fakes.size.toString()).replace("{fakes}" to fakes)
                    )
                }
            }
            "create", "criar" -> {
                if (Main.instance.fakes.containsKey(args[1])) {
                    sender.send(settings.getString("Messages.alreadyExists").replaceColor())
                    return@command
                }

                createFakePlayer(args[1])
                sender.send(settings.getStringList("Messages.fakeCreated").replaceColor().replace("{fake}" to args[1]))
            }
            "remove", "remover", "delete" -> {
                if (!Main.instance.fakes.containsKey(args[1])) {
                    sender.send(settings.getString("Messages.notFound").replaceColor())
                    return@command
                }

                val destroy = Main.instance.fakes[args[1]]!!
                destroy.action = EnumWrappers.PlayerInfoAction.REMOVE_PLAYER

                Bukkit.getOnlinePlayers().forEach {
                    destroy.sendPacket(it)
                }

                sender.send(settings.getStringList("Messages.fakeRemoved").replaceColor().replace("{fake}" to args[1]))
                Main.instance.fakes.remove(args[1])
            }
            else -> sender.send(settings.getStringList("Messages.argsEmpty").replaceColor())
        }
    }
}