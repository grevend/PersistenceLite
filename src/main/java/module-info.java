open module grevend.persistencelite {

    requires java.sql;
    requires org.postgresql.jdbc;
    requires jdk.httpserver;
    requires java.compiler;
    requires reflections;
    requires com.google.gson;
    requires org.jetbrains.annotations;

    exports grevend.common.jacoco;
    exports grevend.common;
    exports grevend.persistencelite;
    exports grevend.persistencelite.dao;
    exports grevend.persistencelite.entity;
    exports grevend.persistencelite.service;
    exports grevend.persistencelite.service.sql;
    exports grevend.persistencelite.service.rest;
    exports grevend.persistencelite.internal.entity;
    exports grevend.persistencelite.util;
    exports grevend.sequence.function;
    exports grevend.sequence.iterators;
    exports grevend.sequence;

    //provides System.LoggerFinder with PersistenceLiteLoggerFinder;

}