import com.vmware.xenon.services.common.ServiceUriPaths;

/**
 * Created by agrawalshubham on 7/6/2016.
 */
public class ServiceUrls  {
    public static final String SERVICE_HOST = "http://localhost:8001";
    public static final String SERVICE_URI_APP_MANAGER = ServiceUriPaths.CORE + "/app-manager";
    public static final String SERVICE_URI_TIER_MANAGER = ServiceUriPaths.CORE + "/tier-manager";
    public static final String SERVICE_URI_NODE_MANAGER = ServiceUriPaths.CORE + "/node-manager";
    public static final String SERVICE_URI_APP_DATA = ServiceUriPaths.CORE + "/apps/";
    public static final String SERVICE_URI_TIER_DATA = ServiceUriPaths.CORE + "/tiers/";
    public static final String SERVICE_URI_NODE_DATA = ServiceUriPaths.CORE + "/nodes/";

    public static final String APP_DYNAMICS_APP_API = "http://blr-agrawalshubham:8090/controller/rest/applications?output=JSON";
    public static final String APP_DYNAMICS_PER_APP_API = "http://blr-agrawalshubham:8090/controller/rest/applications/";

    public static final String APP_DYNAMICS_TIER_API = "tiers";
    public static final String APP_DYNAMICS_JSON = "?output=JSON";
    public static final String APP_DYNAMICS_NODE_API = "nodes";

}
