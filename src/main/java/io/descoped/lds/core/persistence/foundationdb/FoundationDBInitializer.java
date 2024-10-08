package io.descoped.lds.core.persistence.foundationdb;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.directory.Directory;
import com.apple.foundationdb.directory.DirectoryLayer;
import com.apple.foundationdb.subspace.Subspace;
import io.descoped.lds.api.persistence.PersistenceInitializer;
import io.descoped.lds.api.persistence.ProviderName;
import io.descoped.lds.api.persistence.reactivex.RxJsonPersistence;
import io.descoped.lds.api.persistence.reactivex.RxJsonPersistenceBridge;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
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
    public RxJsonPersistence initialize(String defaultNamespace, Map<String, String> configuration, Set<String> managedDomains) {
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
        FoundationDBRxPersistence persistence = new FoundationDBRxPersistence(new FoundationDBTransactionFactory(db), new DefaultFoundationDBDirectory(db, directory));
        return new RxJsonPersistenceBridge(persistence, fragmentCapacityBytes);
    }

    static byte[] hexToBytes(String hexStr) {
        Pattern hexBytesPattern = Pattern.compile("(?:0[xX])?((?:[0-9A-Fa-f]{2})*)");
        Matcher matcher = hexBytesPattern.matcher(hexStr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a hex string: \"" + hexStr + "\"");
        }
        String hex = matcher.group(1);
        byte[] buf = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            buf[i / 2] = Byte.parseByte(str, 16);
        }

        return buf;
    }
}
