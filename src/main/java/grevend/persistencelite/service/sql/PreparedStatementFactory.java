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

package grevend.persistencelite.service.sql;

import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.entity.EntityProperty;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

final class PreparedStatementFactory {

    @NotNull
    PreparedStatement prepare(@NotNull StatementType statementType, @NotNull Connection connection, @NotNull EntityMetadata<?> entityMetadata) throws SQLException {
        var cache = StatementCache.getInstance().getStatementMap();
        if (!cache.containsKey(entityMetadata)) {
            cache.put(entityMetadata, new HashMap<>());
        }
        if (!cache.get(entityMetadata).containsKey(statementType)) {
            cache.get(entityMetadata).put(statementType, switch (statementType) {
                case INSERT -> this.prepareInsert(entityMetadata);
                case SELECT -> this.prepareSelect(entityMetadata);
                case SELECT_ALL -> this.prepareSelectAll(entityMetadata);
                case UPDATE -> null;
                case DELETE -> this.prepareDelete(entityMetadata);
            });
        }
        return connection.prepareStatement(
            StatementCache.getInstance().getStatementMap().get(entityMetadata).get(statementType));
    }

    @NotNull
    private String prepareInsert(@NotNull EntityMetadata<?> entityMetadata) {
        return "insert into " + entityMetadata.getName() + " ("
            + entityMetadata.getUniqueProperties().stream().map(EntityProperty::propertyName)
            .distinct().collect(Collectors.joining(", "))
            + ") values ("
            + String
            .join(", ", Collections.nCopies(entityMetadata.getUniqueProperties().size(), "?"))
            + ")";
    }

    @NotNull
    private String prepareSelect(@NotNull EntityMetadata<?> entityMetadata) {
        return this.prepareSelectAll(entityMetadata) + " where " + entityMetadata.getIdentifiers()
            .stream().map(prop -> entityMetadata.getName() + "." + prop.propertyName() + " = ?")
            .collect(Collectors.joining(" and ")) + " limit 1";
    }

    @NotNull
    private String prepareSelectAll(@NotNull EntityMetadata<?> entityMetadata) {
        var builder = new StringBuilder();
        builder.append("select distinct * from ").append(entityMetadata.getName());
        entityMetadata.getDeclaredSuperTypes()
            .forEach(superType -> this.prepareSelectAll(builder, entityMetadata, superType));
        return builder.toString();
    }

    private void prepareSelectAll(@NotNull StringBuilder builder, @NotNull EntityMetadata<?> parent, @NotNull EntityMetadata<?> child) {
        builder.append(this.prepareInnerJoin(parent, child));
        child.getDeclaredSuperTypes()
            .forEach(superType -> this.prepareSelectAll(builder, child, superType));
    }

    @NotNull
    private String prepareInnerJoin(@NotNull EntityMetadata<?> parent, @NotNull EntityMetadata<?> child) {
        return " inner join " + child.getName() + " on " + child.getIdentifiers().stream().map(
            prop -> parent.getName() + "." + prop.propertyName() + " = " + child.getName() + "."
                + prop.propertyName()).collect(Collectors.joining(" and "));
    }

    @NotNull
    private String prepareDelete(@NotNull EntityMetadata<?> entityMetadata) {
        return "delete from " /*+ entityMetadata.getName() + " where " + properties.keySet().stream()
            .map(attribute -> attribute + "=?").collect(Collectors.joining(", "))*/;
    }

    enum StatementType {
        INSERT, SELECT, SELECT_ALL, UPDATE, DELETE
    }

    static final class StatementCache {

        private static final Object MUTEX = new Object();
        private static volatile StatementCache INSTANCE;
        private final Map<EntityMetadata<?>, Map<StatementType, String>> preparedStatementMap;

        @Contract(pure = true)
        private StatementCache() {
            this.preparedStatementMap = new HashMap<>();
        }

        @NotNull
        private static StatementCache getInstance() {
            var result = INSTANCE;
            if (result == null) {
                synchronized (MUTEX) {
                    result = INSTANCE;
                    if (result == null) {
                        INSTANCE = result = new StatementCache();
                    }
                }
            }
            return result;
        }

        @NotNull
        @Contract(pure = true)
        private Map<EntityMetadata<?>, Map<StatementType, String>> getStatementMap() {
            return this.preparedStatementMap;
        }

        @SuppressWarnings("unused")
        void clearCache() {
            this.preparedStatementMap.clear();
        }

    }

}
