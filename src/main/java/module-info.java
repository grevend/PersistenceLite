import grevend.persistencelite.util.logging.PersistenceLiteLoggerFinder;

module PersistenceLite {
    requires org.jetbrains.annotations;
    requires java.sql;

    exports grevend.jacoco;
    exports grevend.persistencelite;
    exports grevend.persistencelite.dao;
    exports grevend.persistencelite.entity;
    exports grevend.persistencelite.service;
    exports grevend.persistencelite.service.memory;
    exports grevend.persistencelite.service.sql;
    exports grevend.persistencelite.util;
    exports grevend.sequence.function;
    exports grevend.sequence.iterators;
    exports grevend.sequence;

    opens grevend.jacoco;
    opens grevend.persistencelite;
    opens grevend.persistencelite.dao;
    opens grevend.persistencelite.entity;
    opens grevend.persistencelite.service;
    opens grevend.persistencelite.service.memory;
    opens grevend.persistencelite.service.sql;
    opens grevend.persistencelite.util;
    opens grevend.sequence.function;
    opens grevend.sequence.iterators;
    opens grevend.sequence;

    provides System.LoggerFinder with PersistenceLiteLoggerFinder;
}