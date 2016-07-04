import com.vmware.xenon.common.*;
import com.vmware.xenon.services.common.ServiceUriPaths;
import org.json.JSONArray;

import java.net.URI;

/**
 * Created by agrawalshubham on 7/4/2016.
 */
public class TierLevelInfoService extends StatefulService{

    public static final String SELF_LINK = ServiceUriPaths.CORE + "/info/tier";
    public static final String FACTORY_LINK = ServiceUriPaths.CORE + "/info/tier";

    public String username = "shubham@customer1";
    public String password = "paradiddle";
    public String credentials = username + ":" + password;
    public String authStr = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());

    public String appName = "MyApp";

    public static FactoryService createFactory() {
        return FactoryService.createIdempotent(TierLevelInfoService.class, TierDetail.class);
    }


    public TierLevelInfoService() {
        super(TierDetail.class);
        super.toggleOption(Service.ServiceOption.PERSISTENCE, true);
        super.toggleOption(Service.ServiceOption.REPLICATION, true);
        super.toggleOption(Service.ServiceOption.INSTRUMENTATION, true);
        super.toggleOption(Service.ServiceOption.OWNER_SELECTION, true);

        // TODO Auto-generated constructor stub
    }


    // A custom service document to represent the application information
    public static class TierDetail extends ServiceDocument {

        public static final String FIELD_NAME_APP_NAME = "appName";
        public static final String FIELD_NAME_APP_ID = "appId";
        public static final String FIELD_NAME_TIER_NAME = "tierName";

        public String appName;
        public int appId;
        public String tierName;

    }
//
//    private JSONArray getAllApps(){
//
//    }

    public void getTierData(){
        System.out.println("getTierData");
        Operation op = Operation.createGet(URI.create("http://blr-agrawalshubham:8090/controller/rest/applications/MyApp/tiers?output=JSON"));
        op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        op.setBody(new Object());
        op.setReferer(this.getUri());
        op.setCompletion((getOp, failOp) -> {
            if (failOp != null) {
                System.out.println("error");
                Utils.toString(failOp);
                op.fail(Operation.STATUS_CODE_BAD_REQUEST);
                System.out.println(failOp.getMessage());
                return;
            } else {
                System.out.println("success");
                String response = getOp.getBody(String.class);
                System.out.println(response.toString());
                if(response.toString().compareTo("{}") == 0) {
                    op.complete();
                    return;
                }
                JSONArray data = new JSONArray(response.toString());
                op.complete();
                return;
            }
        });

        this.getHost().sendRequest(op);
    }
}
