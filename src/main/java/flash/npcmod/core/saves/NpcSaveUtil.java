package flash.npcmod.core.saves;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import flash.npcmod.Main;
import flash.npcmod.core.FileUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NpcSaveUtil {

  public static final byte MAX_SAVED_NPCS = 20;

  public enum BuildResult {
    SUCCESS,
    TOOMANY,
    FAILED,
    EXISTS
  }

  public static BuildResult build(String uuid, String npcJson) {
    Writer fw = null;
    JsonObject jsonObject = new Gson().fromJson(npcJson, JsonObject.class);
    String name = jsonObject.get("name").getAsString();
    try {
      File folder = FileUtil.readDirectory(FileUtil.getWorldName()+"/"+ Main.MODID+"/saves/"+uuid);
      String path = "saves/"+uuid;
      try {
        if (folder.listFiles().length + 1 > MAX_SAVED_NPCS) {
          return BuildResult.TOOMANY;
        }
      } catch (Exception ignored) {}
      jsonObject.addProperty("internalName", name);

      File jsonFile = FileUtil.getJsonFile(path, name);
      if (jsonFile.exists()) {
        return BuildResult.EXISTS;
      }

      fw = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8);
      fw.write(jsonObject.toString());

      return BuildResult.SUCCESS;
    } catch (Exception e) {
      Main.LOGGER.warn("Could not build Saved NPC file " + name + ".json");
    } finally {
      try {
        if (fw != null) {
          fw.flush();
          fw.close();
        } else {
          Main.LOGGER.debug("Could not close FileWriter for Saved NPC " + name + ".json, fw is null");
        }
      } catch (IOException e) {
        Main.LOGGER.warn("Could not close FileWriter for Saved NPC " + name + ".json");
      }
    }
    return BuildResult.FAILED;
  }

  public static BuildResult buildGlobal(JSONObject jsonObject) {
    if (!jsonObject.has("internalName")) {
      jsonObject.put("internalName", jsonObject.getString("name"));
    }
    String name = jsonObject.getString("internalName");
    Writer fw = null;
    try {

      File jsonFile = FileUtil.getFileFromGlobal("saves", name+".json");
      if (jsonFile.exists()) {
        return BuildResult.EXISTS;
      }

      fw = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8);
      fw.write(jsonObject.toString());

      return BuildResult.SUCCESS;
    } catch (Exception e) {
      Main.LOGGER.warn("Could not build Saved NPC file " + name + ".json");
    } finally {
      try {
        if (fw != null) {
          fw.flush();
          fw.close();
        } else {
          Main.LOGGER.debug("Could not close FileWriter for Saved NPC " + name + ".json, fw is null");
        }
      } catch (IOException e) {
        Main.LOGGER.warn("Could not close FileWriter for Saved NPC " + name + ".json");
      }
    }
    return BuildResult.FAILED;
  }

  public static boolean rename(String uuid, String previousName, String newName, boolean isGlobal) {
    String path = isGlobal ? "saves" : "saves/"+uuid;
    File jsonFile = isGlobal ? FileUtil.getFileFromGlobal(path, previousName+".json") : FileUtil.getJsonFileForWriting(path, previousName);
    Writer fw = null;
    if (jsonFile.exists()) {
      try {
        File newFile = isGlobal ? FileUtil.getFileFromGlobal(path, newName+".json") : FileUtil.getJsonFileForWriting(path, newName);
        boolean success = jsonFile.renameTo(newFile);

        if (success) {
          InputStreamReader is = new InputStreamReader(new FileInputStream(newFile), StandardCharsets.UTF_8);
          JsonObject jsonObject = new Gson().fromJson(is, JsonObject.class);
          jsonObject.addProperty("internalName", newName);
          is.close();

          fw = new OutputStreamWriter(new FileOutputStream(newFile), StandardCharsets.UTF_8);
          fw.write(jsonObject.toString());
        }

        return success;
      } catch (Exception e) {
        Main.LOGGER.warn("Could not rename Saved NPC file " + previousName + ".json");
      } finally {
        try {
          if (fw != null) {
            fw.flush();
            fw.close();
          } else {
            Main.LOGGER.debug("Could not close FileWriter for Saved NPC "+newName+".json, fw is null");
          }
        } catch (IOException e) {
          Main.LOGGER.warn("Could not close FileWriter for Saved NPC "+newName+".json");
        }
      }
    }
    return false;
  }

  public static boolean delete(ServerPlayerEntity sender, String name, boolean isGlobal) {
    File file = isGlobal ? FileUtil.getFileFromGlobal("saves", name+".json") : FileUtil.getJsonFileForWriting("saves/"+sender.getCachedUniqueIdString(), name);
    return file != null && file.exists() && file.delete();
  }

  public static List<String> loadGlobal() {
    List<String> savedNpcs = new ArrayList<>();
    File[] globalFiles = FileUtil.getAllFromGlobal("saves");
    putFileArrayIntoList(globalFiles, savedNpcs);
    return savedNpcs;
  }

  public static List<String> load(String uuid) {
    List<String> savedNpcs = new ArrayList<>();
    File[] worldFiles = FileUtil.getAllFromWorld("saves/"+uuid);
    putFileArrayIntoList(worldFiles, savedNpcs);
    return savedNpcs;
  }

  private static void putFileArrayIntoList(@Nullable File[] files, List<String> list) {
    if (files != null) {
      for (File file : files) {
        try {
          InputStreamReader is = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
          String fileContents = new BufferedReader(is).lines().collect(Collectors.joining("\n"));;
          String jsonString = new JSONObject(fileContents).toString();  // skips if it's not a json file?
          list.add(jsonString);
          is.close();
        } catch (Exception ignored) {}
      }
    }
  }

}
