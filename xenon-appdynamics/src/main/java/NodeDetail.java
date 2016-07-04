import com.vmware.xenon.common.ServiceDocument;

/**
 * Created by agrawalshubham on 7/4/2016.
 */
public class NodeDetail extends ServiceDocument {

    public static final String FIELD_NAME_APP_NAME = "appName";
    public static final String FIELD_NAME_APP_ID = "appId";
    public static final String FIELD_NAME_TIER_NAME = "tierName";
    public static final String FIELD_NAME_TIER_ID = "tierId";
    public static final String FIELD_NAME_AGENT_TYPE = "agentType";
    public static final String FIELD_NAME_APP_AGENT_PRESENT = "appAgentPresent";
    public static final String FIELD_NAME_APP_AGENT_VERSION = "appAgentVersion";
    public static final String FIELD_NAME_NODE_ID = "nodeId";
    public static final String FIELD_NAME_NODE_IP_ADDRESSES = "nodeIpAddresses";
    public static final String FIELD_NAME_MACHINE_AGENT_PRESENT = "machineAgentPresent";
    public static final String FIELD_NAME_MACHINE_AGENT_VERSION = "machineAgentVersion";
    public static final String FIELD_NAME_MACHINE_ID = "machineId";
    public static final String FIELD_NAME_MACHINE_NAME = "machineName";
    public static final String FIELD_NAME_MACHINE_OS_TYPE = "machineOsType";
    public static final String FIELD_NAME_NODE_NAME = "nodeName";
    public static final String FIELD_NAME_NODE_UNIQUE_LOCAL_ID = "nodeUniqueLocalId";
    public static final String FIELD_NAME_NODE_TYPE = "nodeType";


    public String appName;
    public int appId;
    public String tierName;
    public int tierId;
    public String agentType;
    public boolean appAgentPresent;
    public String appAgentVersion;
    public int nodeId;
    public String nodeIpAddress;
    public boolean machineAgentPresent;
    public String machineAgentVersion;
    public int machineId;
    public String machineName;
    public String machineOsType;
    public String nodeName;
    public int nodeUniqueLocalId;
    public String nodeType;


}