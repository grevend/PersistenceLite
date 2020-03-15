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

package grevend.persistence.lite.database;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.entity.EntityImplementationException;
import grevend.persistence.lite.util.PrimaryKey;
import grevend.persistence.lite.util.Triplet;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class Database implements AutoCloseable {

  private final String name;
  private final int version;

  public Database(@NotNull String name, int version) {
    this.name = name;
    this.version = version;
    this.onStart();
  }

  public String getName() {
    return this.name;
  }

  public int getVersion() {
    return this.version;
  }

  public abstract @NotNull URI getURI() throws URISyntaxException;

  public abstract @NotNull <A> Dao<A> createDao(@NotNull EntityClass<A> entity,
      @NotNull List<Triplet<Class<?>, String, String>> keys);

  public void onStart() {
  }

  public void onStop() {
  }

  private @NotNull <A> Dao<A> getDao(@NotNull EntityClass<A> entity) {
    var keys = entity.getPrimaryKeys();
    if (keys.size() <= 0) {
      throw new EntityImplementationException(
          "Every entity must possess a primary key annotated with %s.",
          PrimaryKey.class.getCanonicalName());
    } else {
      return this.createDao(entity, keys);
    }
  }

  public @NotNull <A> Dao<A> getDao(@NotNull Class<A> clazz) {
    return this.getDao(EntityClass.of(clazz));
  }

  @Override
  public void close() {
    this.onStop();
  }

}
