/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.persistencelite.internal.entity.factory;

import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.entity.EntityProperty;
import grevend.persistencelite.internal.entity.EntityType;
import grevend.persistencelite.internal.util.Utils;
import grevend.persistencelite.util.TypeMarshaller;
import grevend.sequence.function.ThrowingFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @see EntityMetadata
 * @see EntityProperty
 * @see EntityType
 * @since 0.2.0
 */
public final class EntityFactory {

    /**
     * @param entityMetadata
     * @param properties
     * @param props
     * @param <E>
     *
     * @return
     *
     * @throws Throwable
     * @see EntityMetadata
     * @see Map
     * @since 0.2.3
     */
    @NotNull
    public static <E> E construct(@NotNull EntityMetadata<E> entityMetadata, @NotNull final Map<String, Object> properties, boolean props, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap) throws Throwable {
        return switch (entityMetadata.entityType()) {
            case CLASS, INTERFACE -> throw new UnsupportedOperationException();
            case RECORD -> constructRecord(entityMetadata, properties.keySet(), props,
                key -> Utils.extract(key, properties, List.of()), marshallerMap);
        };
    }

    /**
     * @param entityMetadata
     * @param properties
     * @param props
     * @param values
     * @param <E>
     *
     * @return
     *
     * @throws Throwable
     * @see EntityMetadata
     * @see Collection
     * @see ThrowingFunction
     * @since 0.2.0
     */
    @NotNull
    @SuppressWarnings("unchecked")
    private static <E> E constructRecord(@NotNull EntityMetadata<E> entityMetadata, @NotNull Collection<String> properties, boolean props, @NotNull ThrowingFunction<String, Object> values, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap) throws Throwable {
        if (entityMetadata.constructor() == null) {
            throw new IllegalArgumentException("Missing constructor.");
        }
        final var propertyNames = entityMetadata.declaredProperties().stream()
            .map(prop -> props ? prop.propertyName() : prop.fieldName())
            .collect(Collectors.toUnmodifiableList());
        if (!properties.containsAll(propertyNames)) {
            final var missingProperties = new ArrayList<>(propertyNames);
            missingProperties.removeAll(properties);
            throw new IllegalArgumentException(
                "Missing properties: " + missingProperties.toString());
        }
        final var propsMeta = entityMetadata.declaredProperties().stream().filter(
            prop -> props ? propertyNames.contains(prop.propertyName())
                : propertyNames.contains(prop.fieldName()))
            .collect(Collectors.toUnmodifiableList());

        final List<Object> propertyValues = new ArrayList<>();
        for (var prop : propsMeta) {
            var name = props ? prop.propertyName() : prop.fieldName();
            propertyValues
                .add(marshall(entityMetadata, values.apply(name), prop.type(), marshallerMap));
        }
        return (E) Objects.requireNonNull(entityMetadata.constructor())
            .invokeWithArguments(propertyValues);
    }

    /**
     * @param value
     * @param marshallerMap
     *
     * @return
     *
     * @since 0.5.3
     */
    @Nullable
    private static Object marshall(@NotNull EntityMetadata<?> entityMetadata, @Nullable Object value, @NotNull Class<?> type, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap) {
        if (type.isEnum() && value instanceof String) {
            try {
                var method = type.getMethod("valueOf", String.class);
                method.setAccessible(true);
                return method.invoke(null, ((String) value).toUpperCase());
            } catch (Exception e) {
                e.printStackTrace();
                return value;
            }
        } else if (marshallerMap.containsKey(entityMetadata.entityClass())) {
            if (marshallerMap.get(entityMetadata.entityClass()).containsKey(type)) {
                return marshallerMap.get(entityMetadata.entityClass()).get(type).marshall(value);
            }
        } else if (marshallerMap.containsKey(null)) {
            if (marshallerMap.get(null).containsKey(type)) {
                return marshallerMap.get(null).get(type).marshall(value);
            }
        }
        return value;
    }

    /**
     * @param entityMetadata
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    public static <E> Collection<Map<String, Object>> deconstruct(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        return switch (entityMetadata.entityType()) {
            case CLASS, INTERFACE -> throw new UnsupportedOperationException();
            case RECORD -> deconstructRecord(entityMetadata, entity, unmarshallerMap);
        };
    }

    /**
     * @param entityMetadata
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    private static <E> Collection<Map<String, Object>> deconstructRecord(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        Collection<Map<String, Object>> components = new ArrayList<>(
            deconstructRecordSuperTypes(entityMetadata, entity, unmarshallerMap));
        components.add(deconstructRecordComponents(entityMetadata, entity, unmarshallerMap));
        return components;
    }

    /**
     * @param entityMetadata
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    private static <E> Collection<Map<String, Object>> deconstructRecordSuperTypes(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        return entityMetadata.superTypes().stream()
            .map(superType -> deconstructRecordSuperType(superType, entity, unmarshallerMap))
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * @param superTypeMetadata
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    private static <E> Map<String, Object> deconstructRecordSuperType(@NotNull EntityMetadata<?> superTypeMetadata, @NotNull E entity, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        Map<String, Object> properties = new HashMap<>();
        superTypeMetadata.declaredProperties().forEach(property -> {
            try {
                properties.put(property.propertyName(), unmarshall(superTypeMetadata,
                    Objects.requireNonNull(property.getter()).invoke(entity), property.type(),
                    unmarshallerMap));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return properties;
    }

    /**
     * @param entityMetadata
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    private static <E> Map<String, Object> deconstructRecordComponents(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        Map<String, Object> properties = new HashMap<>();
        var superPropNames = new HashSet<String>();
        entityMetadata.superTypes().forEach(metadata -> metadata.declaredProperties()
            .forEach(prop -> superPropNames.add(prop.fieldName())));
        var props = entityMetadata.declaredProperties().stream()
            .filter(prop -> !superPropNames.contains(prop.fieldName()) || prop.identifier() != null
                || prop.copy()).collect(Collectors.toCollection(ArrayList::new));
        props.forEach(property -> {
            try {
                properties.put(property.propertyName(), unmarshall(entityMetadata,
                    Objects.requireNonNull(property.getter()).invoke(entity), property.type(),
                    unmarshallerMap));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return properties;
    }

    /**
     * @param value
     *
     * @return
     *
     * @since 0.5.3
     */
    @Nullable
    private static Object unmarshall(@NotNull EntityMetadata<?> entityMetadata, @Nullable Object value, @NotNull Class<?> type, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        if (value != null && Objects.requireNonNull(value).getClass().isEnum()) {
            return value.toString().toLowerCase();
        } else if (unmarshallerMap.containsKey(entityMetadata.entityClass())) {
            if (unmarshallerMap.get(entityMetadata.entityClass()).containsKey(type)) {
                return unmarshallerMap.get(entityMetadata.entityClass()).get(type).marshall(value);
            }
        } else if (unmarshallerMap.containsKey(null)) {
            if (unmarshallerMap.get(null).containsKey(type)) {
                return unmarshallerMap.get(null).get(type).marshall(value);
            }
        }
        return value;
    }

}
