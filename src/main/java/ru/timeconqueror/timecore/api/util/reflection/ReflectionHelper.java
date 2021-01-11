package ru.timeconqueror.timecore.api.util.reflection;

import com.google.common.base.Joiner;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;
import ru.timeconqueror.timecore.TimeCore;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

public class ReflectionHelper {
    private static final UnlockedField<Field> F_MODIFIERS = findField(Field.class, "modifiers");

    public static boolean isFinal(Field f) {
        return Modifier.isFinal(f.getModifiers());
    }

    public static boolean isStatic(Field f) {
        return Modifier.isStatic(f.getModifiers());
    }

    public static boolean isStatic(Method m) {
        return Modifier.isStatic(m.getModifiers());
    }

    /**
     * Removes {@code final} modifier from field.
     */
    public static void unfinalize(Field f) throws IllegalAccessException {
        if (isFinal(f)) {
            if (isStatic(f)) {
                Field modifiersField = F_MODIFIERS.getField();
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            } else {
                f.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            }
        }
    }

    public static void setAccessible(Field f) {
        if (!f.isAccessible()) {
            f.setAccessible(true);
        }
    }

    public static void setAccessible(Method m) {
        if (!m.isAccessible()) {
            m.setAccessible(true);
        }
    }

    public static void setAccessible(Constructor<?> c) {
        if (!c.isAccessible()) {
            c.setAccessible(true);
        }
    }

    /**
     * Creates class from given location.
     *
     * @param name the fully qualified location of the desired class.
     * @return class from given location or null if class isn't found.
     */
    @Nullable
    public static Class<?> createClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Finds a field with the specified location in the given class and makes it accessible.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Returns null if the field is not found and prints error stacktrace.
     *
     * @param clazz     The class to find the field on.
     * @param fieldName The location of the field to find.
     * @return The field with the specified location in the given class or null if the field is not found.
     * @see #findField(Class, String)
     */
    public static <T> Optional<UnlockedField<T>> findFieldSoftly(Class<?> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            return Optional.of(new UnlockedField<>(f));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Finds a field with the specified location in the given class and makes it accessible.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws {@link RuntimeException} if the field is not found.
     *
     * @param clazz     The class to find the field on.
     * @param fieldName The location of the field to find.
     * @return The field with the specified location in the given class or throws an exception.
     * @see #findFieldSoftly(Class, String)
     */
    public static <T> UnlockedField<T> findField(Class<?> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            return new UnlockedField<>(f);
        } catch (Throwable e) {
            throw new RuntimeException("Can't retrieve field " + fieldName + " from class " + clazz, e);
        }
    }

    /**
     * Only for fields, which come from vanilla minecraft!
     * <p>
     * Finds a field with the specified location in the given class and makes it accessible.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Returns null if the field is not found and prints error stacktrace.
     *
     * @param clazz   The class to find the field on.
     * @param srgName The searge obfuscated location of the field to find.
     * @return The field with the specified location in the given class or null if the field is not found.
     */
    public static <T, C> Optional<UnlockedField<T>> findObfFieldSoftly(Class<C> clazz, String srgName) {
        try {
            Field f = ObfuscationReflectionHelper.findField(clazz, srgName);
            return Optional.of(new UnlockedField<>(f));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Finds a method with the specified location and params in the given class and makes it accessible.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Returns null if the method is not found and prints error stacktrace.
     *
     * @param clazz      The class to find the field on.
     * @param methodName The location of the method to find.
     * @param params     The parameter classes of the method to find.
     * @return The method with the specified location in the given class or null if the method is not found.
     * @see #findMethod(Class, String, Class[])
     */
    public static <T> Optional<UnlockedMethod<T>> findMethodSoftly(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, params);
            return Optional.of(new UnlockedMethod<>(method));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Finds a method with the specified location and params in the given class and makes it accessible.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws {@link RuntimeException} if the field is not found.
     *
     * @param clazz      The class to find the field on.
     * @param methodName The location of the method to find.
     * @param params     The parameter classes of the method to find.
     * @return The field with the specified location in the given class or throws an exception.
     * @see #findMethodSoftly(Class, String, Class[])
     */
    public static <T> UnlockedMethod<T> findMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, params);
            return new UnlockedMethod<>(method);
        } catch (Throwable e) {
            throw new RuntimeException("Can't retrieve method " + clazz.getName() + "#" + methodName + "(" + Joiner.on(",").join(params) + ")", e);
        }
    }

    /**
     * Only for methods, which come from vanilla minecraft!
     * <p>
     * Finds a method with the specified location and params in the given class and makes it accessible.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Returns null if the method is not found and prints error stacktrace.
     *
     * @param clazz   The class to find the field on.
     * @param srgName The searge obfuscated location of the method to find.
     * @param params  The parameter classes of the method to find.
     * @return The method with the specified location in the given class or null if the method is not found.
     */
    public static <T> Optional<UnlockedMethod<T>> findObfMethodSoftly(Class<?> clazz, String srgName, Class<?>... params) {
        try {
            Method method = ObfuscationReflectionHelper.findMethod(clazz, srgName, params);
            return Optional.of(new UnlockedMethod<>(method));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Finds a constructor in the specified class that has matching parameter types.
     *
     * @param clazz  The class to find the constructor in
     * @param params The parameter types of the constructor.
     * @param <T>    The type of constructor.
     * @return The constructor with specified params or null if the constructor is not found.
     */
    public static <T> Optional<UnlockedConstructor<T>> findConstructorSoftly(Class<T> clazz, Class<?>... params) {
        try {
            Constructor<T> constructor = ObfuscationReflectionHelper.findConstructor(clazz, params);
            return Optional.of(new UnlockedConstructor<>(constructor));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Loads provided class. (so it calls static initializer)
     *
     * @param clazz class to be loaded.
     * @throws RuntimeException if class isn't found
     */
    public static void initClass(Class<?> clazz) {
        initClass(clazz.getName());
    }

    /**
     * Loads class with provided location. (so it calls static initializer)
     *
     * @param className full location of class to be loaded
     * @throws RuntimeException if class isn't found
     */
    public static void initClass(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            TimeCore.LOGGER.error("Can't load class" + className + ", because it isn't found");
            throw new RuntimeException();
        }
    }

    public static <E extends Enum<E>> E[] getEnumValues(Class<E> enumClass) {
        return enumClass.getEnumConstants();
    }
}
