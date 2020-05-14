package stanic.stfakeplayers.utils

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import org.bukkit.Bukkit
import stanic.stfakeplayers.Main
import java.util.*

fun createFakePlayer(name: String) {
    val uuid = UUID.randomUUID()

    val fake = WrapperPlayServerPlayerInfo()
    fake.action = PlayerInfoAction.ADD_PLAYER
    val profile = WrappedGameProfile(uuid, name)
    val data = PlayerInfoData(profile, 1, NativeGameMode.CREATIVE, WrappedChatComponent.fromText(name))
    val dataList: MutableList<PlayerInfoData> = ArrayList<PlayerInfoData>()
    dataList.add(data)
    fake.data = dataList

    Bukkit.getOnlinePlayers().forEach {
        fake.sendPacket(it)
    }

    Main.instance.fakes[name] = fake
}