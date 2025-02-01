package ru.timeconqueror.timecore.molang;

import gg.moonflower.molangcompiler.api.MolangRuntime;
import gg.moonflower.molangcompiler.core.object.MolangVariableStorage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import ru.timeconqueror.timecore.api.molang.TCMolangEnvironment;

import java.util.HashMap;

@Getter
@SuppressWarnings("UnstableApiUsage")
public class TCMolangRuntime extends MolangRuntime implements TCMolangEnvironment {
    private final MolangRuntimeProperties runtimeProperties = new MolangRuntimeProperties();

    public TCMolangRuntime() {
        super(Object2ObjectOpenHashMap::new, HashMap::new, new MolangVariableStorage(true), new MolangVariableStorage(true), new MolangVariableStorage(false));
    }

    @Override
    public void unloadLibrary(String name) {
        objects.remove(name);
    }
}
