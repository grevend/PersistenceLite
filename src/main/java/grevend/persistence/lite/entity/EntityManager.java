package grevend.persistence.lite.entity;

import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.util.Option;
import grevend.persistence.lite.util.ThrowingFunction;
import grevend.persistence.lite.util.Triplet;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

public final class EntityManager {

<<<<<<< Updated upstream
    public static final Predicate<Constructor<?>> viableConstructor =
            constructor -> constructor.getParameterCount() == 0
                    && !constructor.isSynthetic()
                    && (Modifier.isPublic(constructor.getModifiers())
                    || Modifier.isProtected(constructor.getModifiers()));

    public static final Predicate<Field> viableFields =
            field -> !field.isSynthetic()
                    && !field.isAnnotationPresent(Ignore.class)
                    && !Modifier.isAbstract(field.getModifiers())
                    && !Modifier.isFinal(field.getModifiers())
                    && !Modifier.isStatic(field.getModifiers())
                    && !Modifier.isTransient(field.getModifiers());

    private Database database;
=======
    private final Database database;
>>>>>>> Stashed changes
    private Map<Class<?>, List<Triplet<Class<?>, String, String>>> entityAttributes;

    public EntityManager(@NotNull Database database) {
        this.database = database;
        this.entityAttributes = new HashMap<>();
    }

<<<<<<< Updated upstream
=======
    public @NotNull Database getDatabase() {
        return database;
    }

>>>>>>> Stashed changes
    private @NotNull List<Triplet<Class<?>, String, String>> getFields(@NotNull Class<?> entity) {
        return Arrays.stream(entity.getDeclaredFields()).filter(this.database.getExtension().isFieldViable())
                .map(field -> new Triplet<Class<?>, String, String>(field.getType(), field.getName(),
                        (field.isAnnotationPresent(Attribute.class) ? field.getAnnotation(Attribute.class).name() :
                                field.getName()))).collect(Collectors.toList());
    }

    private @NotNull Optional<Constructor<?>> getConstructor(@NotNull Class<?> entity) {
        List<Constructor<?>> constructors = Arrays.stream(entity.getDeclaredConstructors())
                .filter(this.database.getExtension().isConstructorViable())
                .collect(Collectors.toList());
        if (constructors.size() != 0) {
            return Optional.ofNullable(constructors.get(0));
        } else {
            return Optional.empty();
        }
    }

    private @NotNull <A> A constructEntity(@NotNull Class<A> entity)
            throws IllegalStateException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        Optional<Constructor<?>> constructor = this.getConstructor(entity);
        if (entity.isAnnotationPresent(Entity.class)) {
            if (constructor.isPresent()) {
                this.entityAttributes.computeIfAbsent(entity, this::getFields);
                constructor.get().setAccessible(true);
                return entity.cast(constructor.get().newInstance());
            } else {
                throw new IllegalArgumentException("Class " + entity.getCanonicalName()
                        + " must declare an empty public or protected constructor");
            }
        } else {
            throw new IllegalArgumentException("Class " + entity.getCanonicalName()
                    + " must be annotated with @" + Entity.class.getCanonicalName());
        }
    }

    public @NotNull <A> A constructEntity(@NotNull Class<A> entity, @NotNull Map<String, Object> values)
            throws IllegalStateException {
        return constructEntity(entity, values::get);
    }

    public @NotNull <A> A constructEntity(@NotNull Class<A> entity, @NotNull ResultSet resultSet)
            throws IllegalStateException {
        return constructEntity(entity, resultSet::getObject);
    }

    private @NotNull <A> A constructEntity(@NotNull Class<A> entity, @NotNull ThrowingFunction<String, ?> values)
            throws IllegalStateException {
        try {
            A obj = constructEntity(entity);
            if (this.entityAttributes.containsKey(entity)) {
                for (Triplet<Class<?>, String, String> attribute : this.entityAttributes.get(entity)) {
                    Field field = entity.getField(attribute.getB());
                    boolean isAccessible = field.canAccess(obj);
                    field.setAccessible(true);
                    if (attribute.getA().equals(Option.class)) {
                        if (values.apply(attribute.getC()) instanceof Serializable) {
                            field.set(obj, Option.of((Serializable) values.apply(attribute.getC())));
                        } else {
                            throw new IllegalStateException("Value of " + attribute.getC() + " does not implement " +
                                    Serializable.class.getCanonicalName() + ".");
                        }
                    }
                    if (attribute.getA().equals(Optional.class) && !Serializable.class.isAssignableFrom(entity)) {
                        field.set(obj,
                                attribute.getA().equals(Optional.class) ?
                                        Optional.ofNullable(values.apply(attribute.getC())) :
                                        values.apply(attribute.getC()));
                    } else {
                        throw new IllegalStateException("Entity " + entity.getCanonicalName() + " implements " +
                                Serializable.class.getCanonicalName() + " but includes attribute " + attribute.getB() +
                                " of unserializable type " + Optional.class.getCanonicalName() + ".");
                    }
                    field.setAccessible(isAccessible);
                }
            } else {
                throw new IllegalStateException("EntityManager does not recognize " + entity.getCanonicalName() + ".");
            }
            return obj;
        } catch (Exception exception) {
            throw new IllegalStateException("Construction of " + entity.getCanonicalName() + " failed.", exception);
        }
    }

}
