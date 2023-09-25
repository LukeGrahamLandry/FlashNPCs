package flash.npcmod.core;

import flash.npcmod.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class FileUtil {
  private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

  public static String getGlobalDirectoryName() {
    return Main.MODID + "/global";
  }

  @Nullable
  public static File getFileFromGlobal(String path, String name) {
    File directory = getOrCreateDirectory(getGlobalDirectoryName() + SEPARATOR + path);
    try {
      return new File(directory.getCanonicalPath(), name);
    } catch (IOException e) {
      Main.LOGGER.warn("Could not get file " + path + SEPARATOR + name);
    }
    return null;
  }


  public static File getOrCreateDirectory(String path) {
    File directory = new File(".", path);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    return directory;
  }

  /**
   * Gets the path to the current world. If for some reason we get an IOException,
   * this'll grab the world name instead of the world directory.
   *
   * @return The path from "." to the current world
   */
  public static String getWorldDirectory() {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

    try {
      String rootPath = server.getDataDirectory().getCanonicalPath();
      String worldPath = server.func_240776_a_(FolderName.DOT).toFile().getCanonicalPath();
      return worldPath.replace(rootPath + SEPARATOR, "");
    } catch (IOException e) {
      Main.LOGGER.warn("Error while getting world directory, falling back to the old method");
      e.printStackTrace();
      String worldName = server.getServerConfiguration().getWorldName();
      if (server.isDedicatedServer()) {
        return worldName;
      }
      return "saves" + SEPARATOR + worldName;
    }
  }


  public static String getWorldName() {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    String worldName = server.getServerConfiguration().getWorldName();
    if (server.isDedicatedServer()) {
      return worldName;
    }
    return "saves" + SEPARATOR+worldName;
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
      File dir = FileUtil.getOrCreateDirectory(FileUtil.getGlobalDirectoryName()+"/"+path);
      return dir.listFiles();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return new File[0];
  }

  public static File[] getAllFromWorld(String path) {
    try {
      File dir = FileUtil.getOrCreateDirectory((shouldGetFromWorld() ? FileUtil.getWorldDirectory() + "/" : "") + Main.MODID + "/" + path);
      return dir.listFiles();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return new File[0];
  }

  public static boolean shouldGetFromWorld() {
    return Main.PROXY.shouldSaveInWorld();
  }

  @Nullable
  public static File getFileFromPath(String path, String name) {
    if (shouldGetFromWorld()) {
      path = getWorldDirectory() + SEPARATOR + path;
    }
    File directory = getOrCreateDirectory(path);
    try {
      return new File(directory.getCanonicalPath(), name);
    } catch (IOException e) {
      Main.LOGGER.warn("Could not get file " + path + SEPARATOR + name);
    }
    return null;
  }


  private static File getFileForWriting(String path, String name, String extension) {
    if (!name.endsWith(extension)) {
      name = name + extension;
    }
    path = Main.MODID + SEPARATOR + path;
    return FileUtil.getFileFromPath(path, name);
  }

  public static File getJsonFileForWriting(String path, String name) {
    return getFileForWriting(path, name, ".json");
  }

}
