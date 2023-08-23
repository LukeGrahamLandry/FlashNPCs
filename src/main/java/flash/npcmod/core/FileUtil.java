package flash.npcmod.core;

import flash.npcmod.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public class FileUtil {

  public static String getWorldName() {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    String worldName = server.getServerConfiguration().getWorldName();
    if (server.isDedicatedServer()) {
      return worldName;
    }
    return "saves/"+worldName;
  }

  @Nullable
  public static File readFileFrom(String path, String name) {
    if (shouldReadFromWorld()) {
      path = getWorldName() + "/" + path;
    }
    File directory = readDirectory(path);
    try {
      File file = new File(directory.getCanonicalPath(), name);
      return file;
    } catch (IOException e) {
      Main.LOGGER.warn("Could not read file " + path + "/" + name);
    }
    return null;
  }

  @Nullable
  public static File readDirectory(String path) {
    File directory = new File(".", path);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    return directory;
  }

  public static File getJsonFile(String path, String name) {
    path = Main.MODID+"/"+path;
    File jsonFile = FileUtil.readFileFrom(path, name+".json");
    return jsonFile;
  }

  public static boolean shouldReadFromWorld() {
    return Main.PROXY.shouldSaveInWorld();
  }

  public static File[] getAllFiles(String path) {
    File[] globalDirectory = getAllFromGlobal(path);
    File[] worldDirectory = getAllFromWorld(path);

    File[] out = new File[globalDirectory.length + worldDirectory.length];
    System.arraycopy(globalDirectory, 0, out, 0, globalDirectory.length);
    System.arraycopy(worldDirectory, 0, out, globalDirectory.length, worldDirectory.length);

    return out;
  }

  public static File[] getAllFromGlobal(String path) {
    try {
      return FileUtil.getOrCreateDirectory(FileUtil.getGlobalDirectoryName()+"/"+path).listFiles();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return new File[0];
  }

  public static File[] getAllFromWorld(String path) {
    try {
      return FileUtil.getOrCreateDirectory((shouldGetFromWorld() ? FileUtil.getWorldDirectory() + "/" : "") + Main.MODID + "/" + path).listFiles();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return new File[0];
  }

}
