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

package grevend.persistencelite.entity.lookup;

import grevend.persistencelite.entity.EntityIdentifier;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.entity.EntityProperty;
import grevend.persistencelite.entity.EntityRelation;
import grevend.persistencelite.entity.EntityRelationType;
import grevend.persistencelite.entity.Id;
import grevend.persistencelite.entity.Property;
import grevend.persistencelite.entity.Relation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The interface that is used for the various component lookup implementations.
 *
 * @param <E> The entity type.
 * @param <C> The type of the annotated member that is used to lookup the metadata required for
 *            creating an {@code EntityProperty}.
 *
 * @author David Greven
 * @see EntityMetadata
 * @see EntityProperty
 * @see MethodHandles
 * @since 0.2.0
 */
public interface ComponentLookup<E, C extends AnnotatedElement> {

    /**
     * Generates a {@code Stream} of annotated member components.
     *
     * @param entityMetadata The metadata of the entity for which this operation is being
     *                       performed.
     *
     * @return The {@code Stream} of components.
     *
     * @see Stream
     * @since 0.2.0
     */
    @NotNull
    Stream<C> components(@NotNull EntityMetadata<E> entityMetadata);

    /**
     * Looks up the type of the annotated member component.
     *
     * @param component The component from which the type is to be looked up.
     *
     * @return The component type in form of a {@code Class}.
     *
     * @see Class
     * @since 0.2.0
     */
    @NotNull
    Class<?> lookupComponentType(@NotNull C component);

    /**
     * Looks up the name of the annotated member component.
     *
     * @param component The component from which the name is to be looked up.
     *
     * @return The component name.
     *
     * @since 0.2.0
     */
    @NotNull
    String lookupComponentName(@NotNull C component);

    /**
     * Creates an {@code EntityProperty} based on the {@code EntityMetadata}, the {@code
     * ComponentLookup} {@code MethodHandle} factory and the provided component.
     *
     * @param entityMetadata The metadata of the entity for which this operation is being
     *                       performed.
     * @param lookup         The factory used to create the getter {@code MethodHandle}.
     * @param component      The component to be converted.
     *
     * @return The created {@code EntityProperty}.
     *
     * @see EntityProperty
     * @see EntityMetadata
     * @see ComponentLookup
     * @see MethodHandle
     * @since 0.2.0
     */
    @NotNull
    private EntityProperty createProperty(@NotNull EntityMetadata<E> entityMetadata, @NotNull MethodHandles.Lookup lookup, @NotNull C component) {
        var getter = this.lookupGetter(entityMetadata, lookup, component);

        var propertyName = component.isAnnotationPresent(Property.class) ?
            component.getAnnotation(Property.class).name() : this.lookupComponentName(component);

        var autoGenerated = component.isAnnotationPresent(Id.class) &&
            component.getAnnotation(Id.class).autoGenerated();

        var identifier = component.isAnnotationPresent(Id.class) ?
            new EntityIdentifier(autoGenerated) : null;

        var relation = component.isAnnotationPresent(Relation.class) ?
            this.createRelation(component) : null;
        
        var copy = component.isAnnotationPresent(Id.class) ||
            (component.isAnnotationPresent(Property.class) &&
                component.getAnnotation(Property.class).copy());

        return new EntityProperty(this.lookupComponentType(component), getter,
            this.lookupComponentName(component), propertyName, identifier, relation, copy);
    }

    /**
     * @param component The component to be converted.
     *
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Contract("_ -> new")
    private EntityRelation createRelation(@NotNull C component) {
        var annotation = component.getAnnotation(Relation.class);

        return new EntityRelation(annotation.selfProperties(), annotation.targetEntity(),
            annotation.targetProperties(), EntityRelationType.UNKNOWN, false);
    }

    /**
     * Creates entity properties based on the components of the components method stream using the
     * createProperty method.
     *
     * @param entityMetadata The metadata of the entity for which this operation is being
     *                       performed.
     *
     * @return Returns a collection of the created entity properties.
     *
     * @apiNote This default implementation returns an unmodifiable collection.
     * @see Collection
     * @see EntityProperty
     * @see EntityMetadata
     * @see #components(EntityMetadata)
     * @see #createProperty(EntityMetadata, MethodHandles.Lookup, AnnotatedElement)
     * @since 0.2.0
     */
    @NotNull
    default Collection<EntityProperty> lookupProperties(@NotNull EntityMetadata<E> entityMetadata) {
        var lookup = MethodHandles.lookup();
        return this.components(entityMetadata)
            .map(component -> this.createProperty(entityMetadata, lookup, component))
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Creates a {@code MethodHandle} for the passed component.
     *
     * @param entityMetadata The metadata of the entity for which this operation is being
     *                       performed.
     * @param lookup         The factory used to create the getter {@code MethodHandle}.
     * @param component      The component for which the getter should be looked up.
     *
     * @return Returns either the getter's MethodHandle or null.
     *
     * @see MethodHandle
     * @see EntityMetadata
     * @see ComponentLookup
     * @since 0.2.0
     */
    @Nullable
    MethodHandle lookupGetter(@NotNull EntityMetadata<E> entityMetadata, @NotNull MethodHandles.Lookup lookup, @NotNull C component);

    /**
     * Create a {@code MethodHandle} for the entity constructor.
     *
     * @param entityMetadata The metadata of the entity for which this operation is being
     *                       performed.
     *
     * @return Returns either the constructor's MethodHandle or null.
     *
     * @see MethodHandle
     * @see EntityMetadata
     * @since 0.2.0
     */
    @Nullable
    MethodHandle lookupConstructor(@NotNull EntityMetadata<E> entityMetadata);

    /**
     * @param entityMetadata The metadata of the entity for which this operation is being
     *                       performed.
     *
     * @return Returns a collection of all the super types.
     *
     * @see Collection
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    Collection<EntityMetadata<?>> lookupSuperTypes(@NotNull EntityMetadata<E> entityMetadata);

}
