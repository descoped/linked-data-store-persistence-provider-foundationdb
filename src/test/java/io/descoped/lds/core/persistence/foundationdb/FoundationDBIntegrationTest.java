package io.descoped.lds.core.persistence.foundationdb;

import io.descoped.lds.core.persistence.test.PersistenceIntegrationTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.Map;
import java.util.Set;

public class FoundationDBIntegrationTest extends PersistenceIntegrationTest {

    public FoundationDBIntegrationTest() {
        super("lds-provider-fdb-testng-ns");
    }

    @BeforeClass
    public void setup() {
        this.persistence = new FoundationDBInitializer().initialize(
                namespace,
                Map.of("foundationdb.directory.node-prefix.hex", "3A",
                        "foundationdb.directory.content-prefix.hex", "3B",
                        "persistence.fragment.capacity", String.valueOf(8192)),
                Set.of("Person", "Address", "FunkyLongAddress"));
    }

    @AfterClass
    public void teardown() {
        if (persistence != null) {
            persistence.close();
        }
    }
}
