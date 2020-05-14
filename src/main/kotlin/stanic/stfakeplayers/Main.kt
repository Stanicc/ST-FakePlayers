package stanic.stfakeplayers

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerOptions
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedServerPing
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import stanic.stfakeplayers.commands.registerFakeCommand
import stanic.stutils.bukkit.event.event
import java.util.*
import java.io.File

class Main : JavaPlugin() {

    val fakes = HashMap<String, WrapperPlayServerPlayerInfo>()

    override fun onEnable() {
        instance = this
        loadSettings()

        registerFakeCommand()
        event<PlayerJoinEvent> { event ->
            fakes.values.toList().forEach {
                it.sendPacket(event.player)
            }
        }

        ProtocolLibrary.getProtocolManager().addPacketListener(
            object : PacketAdapter(
                this, ListenerPriority.NORMAL,
                listOf(PacketType.Status.Server.OUT_SERVER_INFO), ListenerOptions.ASYNC
            ) {
                override fun onPacketSending(event: PacketEvent) {
                    handlePing(event.packet.serverPings.read(0))
                }
            })

        /**
        command("spawnfake") { sender, args ->
            if (sender !is Player) return@command

            val craftServer = server as CraftServer
            val craftWorld = (sender.location.world as CraftWorld).handle

            val entity = EntityPlayer(craftServer.server, craftWorld, GameProfile(UUID.randomUUID(), args[0]), PlayerInteractManager(craftWorld))

            entity.setLocation(sender.location.x, sender.location.y, sender.location.z, sender.location.yaw, sender.location.pitch)

            Bukkit.getOnlinePlayers().forEach {
                val connection = (it as CraftPlayer).handle.playerConnection
                connection.sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity))
                connection.sendPacket(PacketPlayOutNamedEntitySpawn(entity))
            }

            GlobalScope.launch {
                delay(2000)
                sender.sendMessage("Disconnected")
                Bukkit.getOnlinePlayers().forEach {
                    val connection = (it as CraftPlayer).handle.playerConnection
                    connection.sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entity))
                    connection.sendPacket(PacketPlayOutEntityDestroy(entity.id))
                    entity.bukkitEntity.remove()
                }
            }
        }
        **/
    }

    fun handlePing(ping: WrappedServerPing) {
        ping.playersOnline = ping.playersOnline + fakes.size
    }

    fun loadSettings() {
        sett = File(dataFolder, "settings.yml")
        if (!sett.exists()) {
            sett.parentFile.mkdirs()
            saveResource("settings.yml", false)
        }
        settings = YamlConfiguration()
        try {
            settings.load(sett)
        } catch (e: InvalidConfigurationException) {
            e.printStackTrace()
        }
    }

    companion object {
        lateinit var instance: Main
            private set

        lateinit var settings: FileConfiguration
            internal set
        lateinit var sett: File
    }

}