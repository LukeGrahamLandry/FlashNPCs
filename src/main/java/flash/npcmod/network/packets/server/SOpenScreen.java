package flash.npcmod.network.packets.server;

import flash.npcmod.Main;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SOpenScreen {

  private EScreens screen;
  private String data;
  private int entityid;

  public SOpenScreen(EScreens screen, String data, int entityid) {
    this.screen = screen;
    this.data = data;
    this.entityid = entityid;
  }

  public static void encode(SOpenScreen msg, PacketBuffer buf) {
    buf.writeInt(msg.screen.ordinal());
    buf.writeString(msg.data);
    buf.writeInt(msg.entityid);
  }

  public static SOpenScreen decode(PacketBuffer buf) {
    EScreens screen = EScreens.values()[buf.readInt()];
    String data = buf.readString();
    int entityid = buf.readInt();
    return new SOpenScreen(screen, data, entityid);
  }

  public static void handle(SOpenScreen msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      Main.PROXY.openScreen(msg.screen, msg.data, msg.entityid);
    });
    ctx.get().setPacketHandled(true);
  }

  public enum EScreens {
    DIALOGUE,
    EDITDIALOGUE,
    FUNCTIONBUILDER,
    EDITNPC,
    QUESTEDITOR
  }
}