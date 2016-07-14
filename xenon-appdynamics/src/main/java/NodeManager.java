import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.OperationJoin;
import com.vmware.xenon.common.StatelessService;
import com.vmware.xenon.common.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by agrawalshubham on 7/13/2016.
 */
public class NodeManager extends StatelessService {

    public static final String SELF_LINK = ServiceUrls.SERVICE_URI_NODE_MANAGER;

    public static final String ACTION_INITIALIZE = "INITIALIZE";
    public static final String ACTION_LOAD_APPS = "LOAD-APPS";
    public static final String ACTION_SYNC = "SYNC";

    public String username = "shubham@customer1";
    public String password = "paradiddle";
    public String credentials = username + ":" + password;
    public String authStr = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());


    @Override
    public void handlePost(Operation post){
        logSevere("NM: In handle post");

//        String requestString = post.getBody(String.class);
//        JSONObject requestJson = new JSONObject(requestString);

//        switch(requestJson.getString("action")){
//            case ACTION_INITIALIZE:
////                initialize(op);
//                break;
//            case ACTION_LOAD_APPS:
////                if(authStr == null){
//                    post.fail(Operation.STATUS_CODE_BAD_METHOD);
////                    return;
////                }
////                loadApps(op);
//                break;
//            case ACTION_SYNC:
//                break;
//            default:
//                logSevere("ACTION NOT SUPPORTED");
//                post.fail(Operation.STATUS_CODE_BAD_METHOD);
//                break;
//        }




        List<Operation> postNodeOps = new ArrayList<>();
        String appJson = post.getBody(String.class);
        JSONObject json = new JSONObject(appJson);


//        System.out.println(ServiceUrls.APP_DYNAMICS_PER_APP_API + json.getInt("id") + "/" + ServiceUrls.APP_DYNAMICS_NODE_API + ServiceUrls.APP_DYNAMICS_JSON);
        Operation getTierData = Operation
                .createGet(URI.create(ServiceUrls.APP_DYNAMICS_PER_APP_API + json.getInt("id") + "/" + ServiceUrls.APP_DYNAMICS_NODE_API + ServiceUrls.APP_DYNAMICS_JSON));
        getTierData.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        getTierData.setBody(new Object());
        getTierData.setReferer(this.getUri());
        getTierData.setCompletion((getOp, failOp) -> {
            if (failOp != null) {
                Utils.toString(failOp);
                getTierData.fail(Operation.STATUS_CODE_BAD_REQUEST);
                System.out.println(failOp.getMessage());
                System.out.println("NM: failure in Appdynamics api call");

                return;
            } else {
                System.out.println("NM: success in Appdynamics api call");

                String response = getOp.getBody(String.class);
                if (response.toString().compareTo("{}") == 0) {
                    getTierData.complete();
                    return;
                }
                JSONArray data = new JSONArray(response.toString());

                for (int i = 0; i < data.length(); i++) {
                    if (data.getJSONObject(i) != null) {
                        System.out.println(data.getJSONObject(i).toString());
                        Operation postTier = Operation.createPost(URI.create(ServiceUrls.SERVICE_HOST + ServiceUrls.SERVICE_URI_NODE_DATA));
                        postTier.setReferer(this.getUri());
//                        System.out.println(makeTierJson(tier, appId, parentSelfLink).toString());
                        postTier.setBody(makeNodeJson(data.getJSONObject(i), json.getInt("id"), json.getString("parentSelfLink")));
                        postNodeOps.add(postTier);
//                        this.sendRequest(postTier);

//                        postTier(data.getJSONObject(i), json.getInt("id"), json.getString("parentSelfLink"));
                    }
                }

                OperationJoin.JoinedCompletionHandler jh = (ops, failures) -> {
                    if(failures != null){
                        post.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
                        System.out.println("NM: -----------------" + failures.toString());
                        return;
                    }
                    System.out.println("NM: Successfully created Nodes");
                    post.complete();
                };
                OperationJoin.create(postNodeOps).setCompletion(jh).sendWith(this);
                return;
            }
        });

        this.sendRequest(getTierData);

        post.complete();
    }

    private JSONObject makeNodeJson(JSONObject node, int appId, String parentSelfLink){
        JSONObject requestJson = new JSONObject();

        requestJson.put("appName", "safdasdfasf");
        requestJson.put("appId", appId);
        requestJson.put("nodeId", node.getInt("id"));
//        requestJson.put("tierId", tier.getInt("id"));
//        requestJson.put("tierDescription", tier.getString("description"));
//        requestJson.put("tierAgentType", tier.getString("agentType"));
//        requestJson.put("tierType", tier.getString("type"));
//        requestJson.put("tierNodeCount", tier.getInt("numberOfNodes"));
//        requestJson.put("parentSelfLink", parentSelfLink);
        return requestJson;
    }

}
