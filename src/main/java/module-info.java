import io.descoped.lds.api.persistence.PersistenceInitializer;

module io.descoped.lds.persistence.foundationdb {
    requires io.descoped.lds.persistence.api;
    requires java.logging;
    requires jul_to_slf4j;
    requires fdb.java;
    requires io.reactivex.rxjava2;
    requires org.reactivestreams;

    exports io.descoped.lds.core.persistence.foundationdb;

    provides PersistenceInitializer with io.descoped.lds.core.persistence.foundationdb.FoundationDBInitializer;
}
