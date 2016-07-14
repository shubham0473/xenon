import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.ServiceDocumentDescription;
import com.vmware.xenon.services.common.TaskService;

/**
 * Created by agrawalshubham on 7/4/2016.
 */
public class Tier extends TaskService.TaskServiceState {

    public enum Action {
        INITIALIZE, REFRESH, SYNC, LOAD
    }


    public static final String FIELD_NAME_APP_NAME = "appName";
    public static final String FIELD_NAME_APP_ID = "appId";
    public static final String FIELD_NAME_TIER_NAME = "tierName";
    public static final String FIELD_NAME_TIER_ID = "tierID";
    public static final String FIELD_NAME_TIER_DESCRIPTION = "tierDescription";
    public static final String FIELD_NAME_AGENT_TYPE = "tierAgentType";
    public static final String FIELD_NAME_TYPE = "tierType";
    public static final String FIELD_NAME_NODE_COUNT = "tierNodeCount";
    public static final String FIELD_NAME_PARENT_SELF_LINK = "parentSelfLink";


    public String appName;
    public int appId;
    public String tierName;
    public int tierId;
    public String tierDescription;
    public String tierAgentType;
    public String tierType;
    public int tierNodeCount;
    public String parentSelfLink;

    @ServiceDocument.UsageOption(option = ServiceDocumentDescription.PropertyUsageOption.AUTO_MERGE_IF_NOT_NULL)
    public Action action;
}