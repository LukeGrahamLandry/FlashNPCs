package flash.npcmod;

import flash.npcmod.entity.NpcEntity;
import flash.npcmod.init.EntityInit;
import flash.npcmod.init.ItemInit;
import flash.npcmod.network.PacketDispatcher;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Main.MODID)
public class Main {

  // The mod's id (namespace). The value here should match an entry in the META-INF/mods.toml file
  public static final String MODID = "flashnpcs";

  // Directly reference a log4j logger.
  public static final Logger LOGGER = LogManager.getLogger();

  // Proxies
  public static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

  public static final ItemGroup NPC_ITEMGROUP = new ItemGroup(Main.MODID) {
    @Override
    public ItemStack createIcon() {
      return new ItemStack(ItemInit.NPC_EDITOR.get());
    }
  };

  public Main() {
    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

    // Register the setup method for modloading
    modEventBus.addListener(this::setup);

    // Register all the DeferredRegisters
    ItemInit.ITEMS.register(modEventBus);
    EntityInit.ENTITIES.register(modEventBus);

    MinecraftForge.EVENT_BUS.register(this);
  }

  private void setup(final FMLCommonSetupEvent event) {
    // Entity Attributes
    event.enqueueWork(() -> {
      GlobalEntityTypeAttributes.put(EntityInit.NPC_ENTITY.get(), NpcEntity.setCustomAttributes().create());
    });

    // Register packets
    PacketDispatcher.registerMessages();
  }
}
