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

import grevend.persistencelite.service.Configurator;
import grevend.sequence.function.ThrowingConsumer;
import java.io.FileNotFoundException;
import java.util.Properties;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @see Configurator
 * @see PostgresService
 * @since 0.2.0
 */
public final class PostgresConfigurator implements Configurator<PostgresService> {

    private final PostgresService service;

    /**
     * @param service The service that is currently being configured.
     *
     * @since 0.2.0
     */
    @Contract(pure = true)
    PostgresConfigurator(@NotNull PostgresService service) {
        this.service = service;
    }

    /**
     * @param propertiesFile The path and filename of the properties file defining the credentials.
     *
     * @return this
     *
     * @since 0.2.0
     */
    @NotNull
    @Contract("_ -> this")
    public PostgresConfigurator credentials(@NotNull String propertiesFile) {
        Properties props = new Properties();

        try (var stream = this.getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (stream != null) {
                props.load(stream);
            } else {
                throw new FileNotFoundException(
                    "Credentials property file '" + propertiesFile + "' not found.");
            }
        } catch (Exception exception) {
            if (exception instanceof FileNotFoundException fileNotFoundException) {
                throw new IllegalStateException("", fileNotFoundException);
            } else {
                exception.printStackTrace();
            }
        }

        this.service.setProperties(props);
        return this;
    }

    /**
     * @param callback The consumer that should be called if a connection times out.
     *
     * @return this
     *
     * @since 0.6.8
     */
    @NotNull
    @Contract(value = "_ -> this", pure = true)
    public PostgresConfigurator onConnectionFailure(@Nullable ThrowingConsumer<ConnectionStatus> callback) {
        this.service.connectionFailureCallbacks.add(callback);
        return this;
    }


    /**
     * @return The service that is currently being configured.
     *
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public PostgresService service() {
        return this.service;
    }

}
