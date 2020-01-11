package noobanidus.mods.katusleep;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
@Mod(modid = Katusleep.MODID, name = Katusleep.MODNAME, version = Katusleep.VERSION, acceptableRemoteVersions = "*")
@SuppressWarnings("WeakerAccess")
public class Katusleep {
  public static final String MODID = "katusleep";
  public static final String MODNAME = "Katu-sleep";
  public static final String VERSION = "GRADLE:VERSION";

  public static Logger LOG;

  @SuppressWarnings("unused")
  @Mod.Instance(Katusleep.MODID)
  public static Katusleep instance;

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    LOG = event.getModLog();
  }

  @SubscribeEvent
  public static void onPlayerSleep (PlayerSleepInBedEvent event) {
    EntityPlayer player = event.getEntityPlayer();
    World world = player.world;
    if (!world.isDaytime()) {
      if (world.isRainingAt(player.getPosition()) || world.isThundering() || world.isRaining()) {
        return;
      }

      for (PotionEffect effect : player.getActivePotionEffects()) {
        if (KatuConfig.getPotions().contains(effect.getPotion().getRegistryName())) {
          return;
        }
      }

      event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
      if (!world.isRemote) {
        player.sendStatusMessage(new TextComponentTranslation("katusleep.you_may_not_sleep"), true);
      }
    }
  }

  @Config(modid=MODID)
  public static class KatuConfig{
    @Config.Comment("A list of potion registry names that should be looked for when allowing sleep")
    public static String[] SLEEP_POTIONS = new String[]{};

    @Config.Ignore
    private static Set<ResourceLocation> POTION_LIST = null;

    public static Set<ResourceLocation> getPotions () {
      if (POTION_LIST == null) {
        POTION_LIST = Stream.of(SLEEP_POTIONS).map(ResourceLocation::new).collect(Collectors.toCollection(HashSet::new));
      }

      return POTION_LIST;
    }
  }
}
