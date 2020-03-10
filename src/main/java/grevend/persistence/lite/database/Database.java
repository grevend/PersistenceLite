package grevend.persistence.lite.database;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.entity.Attribute;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.util.Ignore;
import grevend.persistence.lite.util.PrimaryKey;
import grevend.persistence.lite.util.Triplet;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Database implements AutoCloseable {

    private final String name, user, password;
    private final int version;

    public Database(@NotNull String name, int version, @NotNull String user, @NotNull String password) {
        this.name = name;
        this.version = version;
        this.user = user;
        this.password = password;
        this.onCreate();
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public abstract @NotNull URI getURI() throws URISyntaxException;

    public abstract @NotNull <A> Dao<A> createDao(@NotNull EntityClass<A> entity,
                                                  @NotNull List<Triplet<Class<?>, String, String>> keys);

    public void onStart() {
    }

    public void onStop() {
    }

    public @NotNull Predicate<Constructor<?>> isConstructorViable() {
        return constructor -> constructor.getParameterCount() == 0 && !constructor.isSynthetic();
    }

    public @NotNull Predicate<Field> isFieldViable() {
        return field -> !field.isSynthetic()
                && !field.isAnnotationPresent(Ignore.class)
                && !Modifier.isAbstract(field.getModifiers())
                && !Modifier.isFinal(field.getModifiers())
                && !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers());
    }

    private @NotNull <A> List<Triplet<Class<?>, String, String>> getPrimaryKeys(@NotNull EntityClass<A> entity) {
        return Arrays.stream(entity.getEntityClass().getDeclaredFields())
                .filter(this.isFieldViable())
                .filter(field -> field.isAnnotationPresent(PrimaryKey.class))
                .map(field -> new Triplet<Class<?>, String, String>(field.getType(), field.getName(),
                        (field.isAnnotationPresent(Attribute.class) ? field.getAnnotation(Attribute.class).name() :
                                field.getName()))).collect(Collectors.toList());
    }

    private @NotNull <A> Dao<A> getDao(@NotNull EntityClass<A> entity)
            throws IllegalArgumentException {
        var keys = getPrimaryKeys(entity);
        if (keys.size() <= 0) {
            throw new IllegalArgumentException(
                    "Every entity must possess a primary key annotated with " +
                            PrimaryKey.class.getCanonicalName() + ".");
        } else {
            return createDao(entity, keys);
        }
    }

    public @NotNull <A> Dao<A> getDao(@NotNull Class<A> clazz) throws IllegalArgumentException {
        return this.getDao(EntityClass.of(clazz));
    }

    public void onCreate() {
        this.onStart();
    }

    @Override
    public void close() throws Exception {
        this.onStop();
    }

}
