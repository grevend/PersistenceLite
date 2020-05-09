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

package grevend.persistencelite.service.rest;

import static grevend.persistencelite.service.rest.RestMode.SERVER;

import com.sun.net.httpserver.HttpServer;
import grevend.persistencelite.PersistenceLite;
import grevend.persistencelite.dao.DaoFactory;
import grevend.persistencelite.dao.TransactionFactory;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.service.Service;
import grevend.persistencelite.service.sql.PostgresService;
import grevend.persistencelite.util.TypeMarshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @see RestConfigurator
 * @since 0.3.0
 */
public final class RestService implements Service<RestConfigurator> {

    private RestMode mode;
    private Service<?> service;
    private int version, poolSize;
    private String scope;

    /**
     * @since 0.3.0
     */
    @Contract(pure = true)
    public RestService() {
        this.mode = RestMode.REQUESTER;
        this.service = null;
    }

    public static void main(String[] args) throws IOException {
        var postgres = PersistenceLite.configure(PostgresService.class)
            .credentials("credentials.properties").service();

        PersistenceLite.configure(RestService.class)
            .mode(SERVER)
            .version(2)
            .scope("grevend.main")
            .threadPool(10)
            .uses(postgres)
            .service()
            .start();
    }

    /**
     * @param mode
     *
     * @since 0.3.0
     */
    void setMode(@NotNull RestMode mode) {
        this.mode = mode;
    }

    /**
     * @param version
     *
     * @since 0.3.3
     */
    void setVersion(int version) { this.version = version; }

    /**
     * @param scope
     *
     * @since 0.3.3
     */
    void setScope(@NotNull String scope) { this.scope = scope; }

    /**
     * @param poolSize
     *
     * @since 0.3.3
     */
    void setPoolSize(int poolSize) { this.poolSize = poolSize; }

    /**
     * @param service
     *
     * @since 0.3.0
     */
    void setService(@Nullable Service<?> service) {
        this.service = service;
    }

    /**
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public RestConfigurator configurator() {
        return new RestConfigurator(this);
    }

    /**
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    @Override
    public DaoFactory daoFactory() {
        return this.service.daoFactory();
    }

    /**
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    @Override
    public TransactionFactory transactionFactory() {
        return this.service.transactionFactory();
    }

    /**
     * @param entity
     * @param from
     * @param to
     * @param marshaller
     *
     * @since 0.3.0
     */
    @Override
    public <A, B, E> void registerTypeMarshaller(@Nullable Class<E> entity, @NotNull Class<A> from, @NotNull Class<B> to, @NotNull TypeMarshaller<A, B> marshaller) {
        this.service.registerTypeMarshaller(entity, from, to, marshaller);
    }

    /**
     * @since 0.3.3
     */
    @NotNull
    public HttpServer start() throws IOException {
        if (this.mode != RestMode.SERVER && this.scope != null) {
            throw new IllegalStateException();
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        EntityMetadata.entities(this.scope).forEach(entity -> {
            server.createContext("/api/v" + this.version + "/" + entity.name().toLowerCase(),
                exchange -> {
                    System.out.println("Request...");
                    String response =
                        "{\"api\": \"" + this.version + "\", \"persistencelite\": \""
                            + PersistenceLite.VERSION
                            + "\", \"entity\": \"" + entity.name()
                            + "\", \"message\": \"Hello World!\"}";
                    exchange.getResponseHeaders()
                        .put("Content-Type", List.of("application/json; utf-8"));
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                });
        });
        server.setExecutor(this.poolSize == -1 ? null
            : Executors.newFixedThreadPool(this.poolSize));
        server.start();
        System.out.println(server.getAddress());
        return server;
    }

}