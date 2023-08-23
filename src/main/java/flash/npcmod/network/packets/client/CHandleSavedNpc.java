package flash.npcmod.network.packets.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import flash.npcmod.core.saves.NpcSaveUtil;
import flash.npcmod.entity.NpcEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.fml.network.NetworkEvent;
import org.json.JSONObject;

import java.util.function.Supplier;

public class CHandleSavedNpc {

  PacketType type;
  String newName;
  String prevName;
  boolean isGlobal;
  JSONObject npcJson;
  BlockPos pos;

  public CHandleSavedNpc(String prevName, String newName, boolean isGlobal) {
    type = PacketType.RENAME;
    this.prevName = prevName;
    this.newName = newName;
    this.isGlobal = isGlobal;
  }

  public CHandleSavedNpc(JSONObject npcJson, BlockPos pos) {
    type = PacketType.PLACE;
    this.npcJson = npcJson;
    this.pos = pos;
  }

  public CHandleSavedNpc(JSONObject npcJson) {
    type = PacketType.GLOBAL_SAVE;
    this.npcJson = npcJson;
  }

  public CHandleSavedNpc(String toDelete, boolean isGlobal) {
    type = PacketType.DELETE;
    this.prevName = toDelete;
    this.isGlobal = isGlobal;
  }

  public static void encode(CHandleSavedNpc msg, PacketBuffer buf) {
    buf.writeInt(msg.type.ordinal());
    switch (msg.type) {
      case PLACE: 
        buf.writeString(msg.npcJson.toString());
        buf.writeBlockPos(msg.pos); 
        break;
      case RENAME: 
        buf.writeString(msg.prevName);
        buf.writeString(msg.newName);
        buf.writeBoolean(msg.isGlobal); 
        break;
      case DELETE: 
        buf.writeString(msg.prevName);
        buf.writeBoolean(msg.isGlobal); 
        break;
      case GLOBAL_SAVE: 
        buf.writeString(msg.npcJson.toString());
        break;
    }
  }

  public static CHandleSavedNpc decode(PacketBuffer buf) {
    PacketType type = PacketType.values()[buf.readInt()];
    switch (type) {
      case PLACE:
        return new CHandleSavedNpc(new JSONObject(buf.readString()), buf.readBlockPos());
      case RENAME:
        return new CHandleSavedNpc(buf.readString(), buf.readString(), buf.readBoolean());
      case DELETE:
        return new CHandleSavedNpc(buf.readString(), buf.readBoolean());
      case GLOBAL_SAVE: 
        return new CHandleSavedNpc(new JSONObject(buf.readString()));
    }

    throw new IllegalArgumentException("Invalid packet type");
  }

  public static void handle(CHandleSavedNpc msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayerEntity sender = ctx.get().getSender();
      if (sender.hasPermissionLevel(4) && sender.isCreative()) {
        switch (msg.type) {
          case PLACE:
            NpcEntity npcEntity = NpcEntity.fromJson(sender.world, msg.npcJson);
            BlockPos pos = msg.pos;
            VoxelShape collisionShape = sender.world.getBlockState(pos).getCollisionShape(sender.world, pos);
            double blockHeight = collisionShape.isEmpty() ? 0 : collisionShape.getBoundingBox().maxY;
            npcEntity.setPosition(pos.getX()+0.5, pos.getY()+blockHeight, pos.getZ()+0.5);
            sender.world.addEntity(npcEntity);
            break;
          case RENAME:
            NpcSaveUtil.rename(sender.getCachedUniqueIdString(), msg.prevName, msg.newName, msg.isGlobal);
            break;
          case DELETE:
            NpcSaveUtil.delete(sender, msg.prevName, msg.isGlobal);
            break;
          case GLOBAL_SAVE: 
            NpcSaveUtil.buildGlobal(msg.npcJson);
            break;
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }

  enum PacketType {
    RENAME,
    PLACE,
    DELETE,
    GLOBAL_SAVE
  }
}
