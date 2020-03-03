package global;

import java.util.ArrayList;
import java.util.List;

import bwktrum.BwktrumPeerData;

/**
 * Created by kaali on 7/2/17.
 */

public class BwktrumGlobalData {

    public static final String KAALI_TESTNET_SERVER = "164.68.122.247";

    public static final String[] TRUSTED_NODES = new String[]{"164.68.122.247"};

    public static final List<BwktrumPeerData> listTrustedHosts(){
        List<BwktrumPeerData> list = new ArrayList<>();
        list.add(new BwktrumPeerData(KAALI_TESTNET_SERVER,8765,55552));
        for (String trustedNode : TRUSTED_NODES) {
            list.add(new BwktrumPeerData(trustedNode,8765,55552));
        }
        return list;
    }

}
