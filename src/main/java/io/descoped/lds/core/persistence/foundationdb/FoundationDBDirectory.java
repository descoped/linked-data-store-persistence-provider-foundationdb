package io.descoped.lds.core.persistence.foundationdb;

import com.apple.foundationdb.subspace.Subspace;
import com.apple.foundationdb.tuple.Tuple;
import io.descoped.lds.api.persistence.PersistenceException;

import java.util.concurrent.CompletableFuture;

public interface FoundationDBDirectory {

    CompletableFuture<? extends Subspace> createOrOpen(Tuple key) throws PersistenceException;
}
