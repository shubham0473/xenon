import com.vmware.xenon.common.ServiceDocument;

/**
 * Created by agrawalshubham on 7/4/2016.
 */
public class TierDetail extends ServiceDocument {

    public static final String FIELD_NAME_APP_NAME = "appName";
    public static final String FIELD_NAME_APP_ID = "appId";
    public static final String FIELD_NAME_TIER_NAME = "tierName";
    public static final String FIELD_NAME_TIER_ID = "tierID";
    public static final String FIELD_NAME_TIER_DESCRIPTION = "tierDesc";
    public static final String FIELD_NAME_AGENT_TYPE = "tierAgentType";
    public static final String FIELD_NAME_TYPE = "tierType";
    public static final String FIELD_NAME_NODE_COUNT = "tierNodeCount";


    public String appName;
    public int appId;
    public String tierName;
    public int tierId;
    public String tierDesc;
    public String tierAgentType;
    public String tierType;
    public int tierNodeCount;

}