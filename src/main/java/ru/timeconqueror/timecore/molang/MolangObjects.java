package ru.timeconqueror.timecore.molang;

import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.object.MolangLibrary;
import ru.timeconqueror.timecore.api.molang.Molang;
import ru.timeconqueror.timecore.api.molang.TCMolangExpressions;

import java.util.Map;

public class MolangObjects {

    public final static MolangExpression ANIM_TIME = TCMolangExpressions.usingRuntimeProperties((env, props) -> props.getAnimationTime() / 1000F);

    public static final MolangLibrary TICKER_QUERY_SET = new MolangLibraryImpl("Ticker Query Set",
            Map.of(Molang.Query.Animation.ANIM_TIME, ANIM_TIME));
}
