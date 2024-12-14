package ru.timeconqueror.timecore;

import gg.moonflower.molangcompiler.api.MolangCompiler;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import ru.timeconqueror.timecore.animation.AnimationRegistry;
import ru.timeconqueror.timecore.api.Markers;
import ru.timeconqueror.timecore.api.TimeCoreAPI;
import ru.timeconqueror.timecore.api.animation.Animation;
import ru.timeconqueror.timecore.api.util.EnvironmentUtils;
import ru.timeconqueror.timecore.common.capability.CapabilityManager;
import ru.timeconqueror.timecore.molang.MolangLoader;

@Mod(TimeCore.MODID)
//TODO add readable exception when there's no animation file, for now it's just nullpointer
//FIXME repackage stuff before first release!
public final class TimeCore {
    public static final String MODID = "timecore";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static TimeCore INSTANCE = null;

    private static final String MARKER_PROPERTY = "timecore.logging.markers";

    @Getter
    private final CapabilityManager capabilityManager;
    @Getter
    private final MolangCompiler molangCompiler;

    public TimeCore() {
        INSTANCE = this;
        checkForMixinBootstrap();

        molangCompiler = MolangLoader.load(ForgeMod.class.getClassLoader());
        capabilityManager = new CapabilityManager();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onConstruct);

        TimeCoreAPI.setup(this);
    }

    /**
     * Creates ResourceLocation with bound mod id.
     */
    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

    private void onConstruct(FMLConstructModEvent event) {
        EnvironmentUtils.handleMarkerVisibility(TimeCore.MODID, MARKER_PROPERTY, Markers.all());
    }

    private void setup(final FMLCommonSetupEvent event) {
//        ReflectionHelper.loadClass(StructureRevealer.class); //FIXME port?
        event.enqueueWork(capabilityManager::addDefaultAttachers);

        AnimationRegistry.registerAnimation(Animation.NULL);
    }

    private static void checkForMixinBootstrap() {
        try {
            if (MixinEnvironment.getCurrentEnvironment().getPhase() != MixinEnvironment.Phase.DEFAULT) {
                throw new IllegalArgumentException("Mixins are not initialized");
            }
        } catch (NoClassDefFoundError e) {
            throw new IllegalStateException("TimeCore requires MixinBootstrap Mod to be loaded.");
        }
    }
}
