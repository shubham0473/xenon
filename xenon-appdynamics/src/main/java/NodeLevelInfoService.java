import com.vmware.xenon.common.*;
import com.vmware.xenon.services.common.ServiceUriPaths;
import org.json.JSONArray;

import java.net.URI;

/**
 * Created by agrawalshubham on 7/4/2016.
 */
public class NodeLevelInfoService extends StatefulService {


    public static final String SELF_LINK = ServiceUriPaths.CORE + "/info/tier";
    public static final String FACTORY_LINK = ServiceUriPaths.CORE + "/info/tier";

    public String username = "shubham@customer1";
    public String password = "paradiddle";
    public String credentials = username + ":" + password;
    public String authStr = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());

    public String appName = "MyApp";

    public NodeLevelInfoService() {
        super(NodeDetail.class);
        super.toggleOption(Service.ServiceOption.PERSISTENCE, true);
        super.toggleOption(Service.ServiceOption.REPLICATION, true);
        super.toggleOption(Service.ServiceOption.INSTRUMENTATION, true);
        super.toggleOption(Service.ServiceOption.OWNER_SELECTION, true);
    }

    public static FactoryService createFactory() {
        return FactoryService.createIdempotent(NodeLevelInfoService.class, NodeDetail.class);
    }
    // A custom service document to represent the application information


    public void getNodeData(){
        System.out.println("getNodeData");
        Operation op = Operation.createGet(URI.create("http://blr-agrawalshubham:8090/controller/rest/applications/MyApp/nodes?output=JSON"));
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
