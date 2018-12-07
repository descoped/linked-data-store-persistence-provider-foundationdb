package no.ssb.lds.core.persistence.foundationdb;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.directory.Directory;
import com.apple.foundationdb.directory.DirectoryLayer;
import com.apple.foundationdb.subspace.Subspace;
import no.ssb.lds.api.persistence.PersistenceInitializer;
import no.ssb.lds.api.persistence.ProviderName;
import no.ssb.lds.api.persistence.flattened.DefaultFlattenedPersistence;
import no.ssb.lds.api.persistence.json.BufferedJsonPersistence;
import no.ssb.lds.api.persistence.json.JsonPersistence;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

@ProviderName("foundationdb")
public class FoundationDBInitializer implements PersistenceInitializer {
    @Override
    public String persistenceProviderId() {
        return "foundationdb";
    }

    @Override
    public Set<String> configurationKeys() {
        return Set.of(
                "foundationdb.directory.node-prefix.hex",
                "foundationdb.directory.content-prefix.hex",
                "persistence.fragment.capacity"
        );
    }

    @Override
    public JsonPersistence initialize(String defaultNamespace, Map<String, String> configuration, Set<String> managedDomains) {
        FDB fdb = FDB.selectAPIVersion(520);
        Database db = fdb.open();
        String nodePrefixHex = configuration.get("foundationdb.directory.node-prefix.hex");
        if (nodePrefixHex == null || nodePrefixHex.isBlank()) {
            nodePrefixHex = "0x23"; // default
        }
        String contentPrefixHex = configuration.get("foundationdb.directory.content-prefix.hex");
        if (contentPrefixHex == null || contentPrefixHex.isBlank()) {
            contentPrefixHex = "0x24";  // default
        }
        int fragmentCapacityBytes = Integer.parseInt(ofNullable(configuration.get("persistence.fragment.capacity")).orElse("8192"));
        byte[] nodePrefix = hexToBytes(nodePrefixHex);
        byte[] contentPrefix = hexToBytes(contentPrefixHex);
        Directory directory = new DirectoryLayer(new Subspace(nodePrefix), new Subspace(contentPrefix));
        FoundationDBPersistence persistence = new FoundationDBPersistence(new FoundationDBTransactionFactory(db), new DefaultFoundationDBDirectory(db, directory));
        return new BufferedJsonPersistence(new DefaultFlattenedPersistence(persistence, fragmentCapacityBytes), fragmentCapacityBytes);
    }

    static byte[] hexToBytes(String hexStr) {
        Pattern hexBytesPattern = Pattern.compile("(?:0[xX])?((?:[0-9A-Fa-f]{2})*)");
        if (!hexBytesPattern.matcher(hexStr).matches()) {
            throw new IllegalArgumentException("Not a hex string: \"" + hexStr + "\"");
        }
        byte[] buf = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            buf[i / 2] = Byte.parseByte(str, 16);
        }

        return buf;
    }
}
