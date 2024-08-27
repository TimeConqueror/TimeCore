package ru.timeconqueror.timecore.internal.loading;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import ru.timeconqueror.timecore.TimeCore;
import ru.timeconqueror.timecore.api.client.resource.GlobalResourceStorage;
import ru.timeconqueror.timecore.api.reflection.ReflectionHelper;
import ru.timeconqueror.timecore.api.reflection.UnlockedField;
import ru.timeconqueror.timecore.api.reflection.UnlockedMethod;
import ru.timeconqueror.timecore.api.reflection.provider.ClassHandler;
import ru.timeconqueror.timecore.api.reflection.provider.ClassHandlers;
import ru.timeconqueror.timecore.api.registry.TimeRegister;
import ru.timeconqueror.timecore.api.registry.VanillaRegister;
import ru.timeconqueror.timecore.api.registry.util.AutoRegistrable;
import ru.timeconqueror.timecore.api.registry.util.AutoRegistrable.Entries;
import ru.timeconqueror.timecore.api.registry.util.AutoRegistrable.Init;
import ru.timeconqueror.timecore.common.KotlinAutomaticEventSubscriber;
import ru.timeconqueror.timecore.molang.MolangLoader;
import ru.timeconqueror.timecore.util.AnnoScanningHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.minecraftforge.fml.Logging.LOADING;

public class ModInitializer {
    private static final Type TIME_AUTO_REG_TYPE = Type.getType(AutoRegistrable.class);
    private static final Type TIME_AUTO_REG_INIT_TYPE = Type.getType(Init.class);
    private static final Type TIME_AUTO_ENTRIES_TYPE = Type.getType(Entries.class);

    public static synchronized void run(ModContainer modContainer, ModFileScanData scanResults, Object mod) {
        TimeCore.LOGGER.debug("Setting up TimeCore components for {}", modContainer.getModId());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(EventPriority.HIGHEST, (FMLConstructModEvent event) -> {
            if (!modContainer.matches(mod)) {
                throw new IllegalArgumentException(String.format("Object being provided as mod (%s) doesn't match the one (%s) in the container.", mod.getClass(), modContainer.getMod().getClass()));
            }
        });

        String modId = modContainer.getModId();

        runKotlinAutomaticEventSubscriber(modId, modContainer, scanResults, mod.getClass());

        setupAutoRegistries(scanResults, modContainer, modEventBus);

        GlobalResourceStorage.INSTANCE.setup(modId);
        MolangLoader.handleQueryDomainAnnotations(scanResults);
    }

    private static void runKotlinAutomaticEventSubscriber(String modId, ModContainer modContainer, ModFileScanData scanResults, Class<?> modClass) {
        TimeCore.LOGGER.debug(LOADING, "Injecting Automatic event subscribers for {}", modId);
        KotlinAutomaticEventSubscriber.inject(modContainer, scanResults, modClass.getClassLoader());
        TimeCore.LOGGER.debug(LOADING, "Completed Automatic event subscribers for {}", modId);
    }

    private static void setupAutoRegistries(ModFileScanData scanResults, ModContainer mod, IEventBus eventBus) {
        Multimap<ResourceKey<?>, Stream<ParentableField>> holderFillers = ArrayListMultimap.create();
        List<TimeRegister> registers = new ArrayList<>();
        List<Runnable> initMethods = new ArrayList<>();

        scanResults.getAnnotations().stream()
                .filter(annotationData -> annotationData.annotationType().equals(TIME_AUTO_REG_TYPE)
                        || annotationData.annotationType().equals(TIME_AUTO_REG_INIT_TYPE)
                        || annotationData.annotationType().equals(TIME_AUTO_ENTRIES_TYPE))
                .forEach(annotationData -> {
                    try {
                        Class<?> containerClass = AnnoScanningHelper.getClass(annotationData);
                        Type type = annotationData.annotationType();

                        if (type.equals(TIME_AUTO_REG_TYPE)) {
                            processAutoRegistrable(containerClass, annotationData, registers::add);
                        } else if (type.equals(TIME_AUTO_REG_INIT_TYPE)) {
                            processTimeAutoRegInitMethod(containerClass, annotationData, initMethods::add);
                        } else {
                            processEntries(containerClass, annotationData, holderFillers::put);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });

        FMLJavaModLoadingContext.get().getModEventBus().register(new EntryFiller(mod.getModId(), holderFillers));
        RegisterSubscriber.regToBus(registers, eventBus);
        processInitMethods(initMethods);
    }

    private static void processEntries(Class<?> containerClass, ModFileScanData.AnnotationData annotationData, BiConsumer<ResourceKey<?>, Stream<ParentableField>> holderFillerAdder) {
        String registryKeyName = "value";
        String registryKeyStr = AnnoScanningHelper.getData(annotationData, registryKeyName);

        if (!ResourceLocation.isValidResourceLocation(registryKeyStr)) {
            throw new IllegalArgumentException(String.format("Class %s is annotated with invalid %s: '%s'", containerClass.getSimpleName(), registryKeyName, registryKeyStr));
        }

        ResourceLocation regKeyLoc = new ResourceLocation(registryKeyStr);
        ResourceKey<?> regKey = ResourceKey.createRegistryKey(regKeyLoc);

        Stream<ParentableField> fields;

        if (KotlinModInitializerModule.INSTANCE.handlesEntriesAnno(containerClass)) {
            fields = KotlinModInitializerModule.INSTANCE.processEntriesAnno(containerClass);
        } else {
            fields = Arrays.stream(containerClass.getDeclaredFields())
                    .filter(ReflectionHelper::isStatic)
                    .filter(ModInitializer::validateFieldForEntriesAnno)
                    .map(ParentableField::orphan);
        }

        holderFillerAdder.accept(regKey, fields);
    }

    static boolean validateFieldForEntriesAnno(Field field) {
        if (TimeRegister.class.isAssignableFrom(field.getType())) {
            return false;
        }

        if (ReflectionHelper.isFinal(field)) {
            throw new IllegalArgumentException(String.format("%s can only be applied to static non-final fields. Cause: %s", AutoRegistrable.class.getSimpleName() + "." + Entries.class.getSimpleName(), ReflectionHelper.getFieldQualifiedName(field)));
        }

        AutoRegistrable.Ignore ignored = field.getDeclaredAnnotation(AutoRegistrable.Ignore.class);
        return ignored == null;
    }

    private static void processAutoRegistrable(Class<?> containerClass, ModFileScanData.AnnotationData annotationData, Consumer<TimeRegister> registerSubscriber) throws ClassNotFoundException {
        String fieldName = annotationData.memberName();
        UnlockedField<?, Object> field = ReflectionHelper.findField(containerClass, fieldName);

        processAutoRegistrableOnField(containerClass, field, registerSubscriber);
    }

    private static void processAutoRegistrableOnField(Class<?> containerClass, UnlockedField<?, Object> field, Consumer<TimeRegister> registerSubscriber) {
        if (field.isStatic()) {
            if (TimeRegister.class.isAssignableFrom(field.unboxed().getType())) {
                TimeRegister register = (TimeRegister) field.get(null);
                register.setOwner(containerClass);

                registerSubscriber.accept(register);
            } else {
                throw new UnsupportedOperationException(AutoRegistrable.class.getSimpleName() + " can be used only on fields that have " + VanillaRegister.class.getSimpleName() + " type. Error is in: " + field);
            }
        } else {
            throw new UnsupportedOperationException(AutoRegistrable.class.getSimpleName() + " can be used only on static fields. Errored: " + field);
        }
    }

    private static void processTimeAutoRegInitMethod(Class<?> containerClass, ModFileScanData.AnnotationData annotationData, Consumer<Runnable> preConstructMethodRegistrator) throws ClassNotFoundException {
        String methodSignature = annotationData.memberName();

        ClassHandler handler = ClassHandlers.findHandler(containerClass);
        if (handler == null) {
            throw new IllegalArgumentException("Can't handle class " + containerClass.getName() + ", because there's no " + ClassHandler.class.getName() + " found for it.");
        }

        UnlockedMethod<?, Object> initMethod = handler.findMethod(containerClass, methodSignature);
        if (initMethod == null)
            throw new NoSuchMethodError("Not found method " + methodSignature + " from class " + containerClass.getName());
        handler.requireStatic(initMethod);

        Method nativeMethod = initMethod.unboxed();
        if (nativeMethod.getParameterCount() == 0) {
            preConstructMethodRegistrator.accept(() -> handler.invokeStaticMethod(initMethod));
        } else if (nativeMethod.getParameterCount() == 1 && FMLConstructModEvent.class.isAssignableFrom(nativeMethod.getParameterTypes()[0])) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.HIGHEST, (FMLConstructModEvent event) -> handler.invokeStaticMethod(initMethod, event));
        } else {
            throw new UnsupportedOperationException(Init.class.getSimpleName() + " can be used only on methods with " + FMLConstructModEvent.class.getName() + " parameter or without any parameters. Error is in: " + initMethod);
        }
    }

    private static void processInitMethods(List<Runnable> initMethods) {
        initMethods.forEach(Runnable::run);
    }

    private static class EntryFiller {
        private final Multimap<ResourceKey<?>, Stream<ParentableField>> holderFillers;
        private final String modId;

        public EntryFiller(String modId, Multimap<ResourceKey<?>, Stream<ParentableField>> holderFillers) {
            this.modId = modId;
            this.holderFillers = holderFillers;
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onSetup(RegisterEvent e) {
            holderFillers.get(e.getRegistryKey())
                    .stream()
                    .flatMap(Function.identity())
                    .forEach(parentableField -> {
                        Field field = parentableField.self();
                        String name = field.getName().toLowerCase();
                        ResourceLocation registryName = new ResourceLocation(modId, name);

                        Object value = null;
                        boolean error = false;
                        IForgeRegistry<Object> forgeRegistry = e.getForgeRegistry();
                        Registry<Object> vanillaRegistry = e.getVanillaRegistry();
                        if (forgeRegistry != null) {
                            value = forgeRegistry.getValue(registryName);

                            if (value == forgeRegistry.getValue(forgeRegistry.getDefaultKey())) {
                                error = true;
                            }
                        } else if (vanillaRegistry != null) {
                            value = vanillaRegistry.get(registryName);

                            if (value == null) {
                                error = true;
                            }
                        }

                        if (error) {
                            throw new IllegalStateException(String.format("Can't find value with registry name '%s' to set field %s", registryName, ReflectionHelper.getFieldQualifiedName(field)));
                        }

                        try {
                            field.setAccessible(true);
                            field.set(parentableField.getParent(), value);
                        } catch (IllegalAccessException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
    }

    public static class ParentableField {
        private final Field field;
        @Nullable
        private final Object parent;

        private ParentableField(Field field, @Nullable Object parent) {
            this.field = field;
            this.parent = parent;
        }

        public static ParentableField orphan(Field field) {
            return new ParentableField(field, null);
        }

        public static ParentableField withParent(Field field, Object parent) {
            return new ParentableField(field, parent);
        }

        Field self() {
            return field;
        }

        @Nullable
        Object getParent() {
            return parent;
        }
    }
}
