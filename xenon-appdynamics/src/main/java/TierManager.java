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
 * Created by agrawalshubham on 7/9/2016.
 */
public class TierManager extends StatelessService {
    public static final String SELF_LINK = ServiceUrls.SERVICE_URI_TIER_MANAGER;
//    public static final String FACTORY_LINK = ServiceUriPaths.CORE + ServiceUrls.SERVICE_URI_MANAGER;

    public static final String ACTION_INITIALIZE = "INITIALIZE";
    public static final String ACTION_LOAD_APPS = "LOAD-APPS";
    public static final String ACTION_SYNC = "SYNC";

    public String username = "shubham@customer1";
    public String password = "paradiddle";
    public String credentials = username + ":" + password;
    public String authStr = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());



    @Override
    public void handlePost(Operation post){
        System.out.println("TM: in handlePost");
        String request = post.getBody(String.class);
        JSONObject requestJson = new JSONObject(request);

        switch(requestJson.getString("action")){
            case ACTION_INITIALIZE:
                loadTiers(post, requestJson);
                break;
            case ACTION_SYNC:
                break;
            default:
                post.fail(Operation.STATUS_CODE_BAD_METHOD);
                break;
        }


    }

    public void loadTiers(Operation op, JSONObject requestJson){
        List<Operation> postTierOps = new ArrayList<>();
        System.out.println(ServiceUrls.APP_DYNAMICS_PER_APP_API + requestJson.getInt("id") + "/" + ServiceUrls.APP_DYNAMICS_TIER_API + ServiceUrls.APP_DYNAMICS_JSON);
        Operation getTierData = Operation
                .createGet(URI.create(ServiceUrls.APP_DYNAMICS_PER_APP_API + requestJson.getInt("id") + "/" + ServiceUrls.APP_DYNAMICS_TIER_API + ServiceUrls.APP_DYNAMICS_JSON));
        getTierData.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        getTierData.setBody(new Object());
        getTierData.setReferer(this.getUri());
        getTierData.setCompletion((getOp, failOp) -> {
            if (failOp != null) {
                Utils.toString(failOp);
                getTierData.fail(Operation.STATUS_CODE_BAD_REQUEST);
                System.out.println(failOp.getMessage());
                System.out.println("TM: failure in Appdynamics api call");
                return;
            } else {
                System.out.println("TM: success in Appdynamics api call");
                String response = getOp.getBody(String.class);
                if (response.toString().compareTo("{}") == 0) {
                    getTierData.complete();
                    return;
                }
                JSONArray data = new JSONArray(response.toString());

                for (int i = 0; i < data.length(); i++) {
                    if (data.getJSONObject(i) != null) {
                        System.out.println(data.getJSONObject(i).toString());
                        Operation postTier = Operation.createPost(URI.create(ServiceUrls.SERVICE_HOST + ServiceUrls.SERVICE_URI_TIER_DATA));
                        postTier.setReferer(this.getUri());
//                        System.out.println(makeTierJson(tier, appId, parentSelfLink).toString());
                        postTier.setBody(makeTierJson(data.getJSONObject(i), requestJson.getInt("id"), requestJson.getString("parentSelfLink")).toString());
                        postTierOps.add(postTier);
//                        this.sendRequest(postTier);

//                        postTier(data.getJSONObject(i), json.getInt("id"), json.getString("parentSelfLink"));
                    }
                }

                OperationJoin.JoinedCompletionHandler jh = (ops, failures) -> {
                    if(failures != null){
                        op.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
                        System.out.println("TM: -----------" + failures.toString());
                        return;
                    }
                    System.out.println("TM: Successfully created Tiers");
                    op.complete();
                };
                OperationJoin.create(postTierOps).setCompletion(jh).sendWith(this);
                return;
            }
        });

        this.sendRequest(getTierData);
    }

    private JSONObject makeTierJson(JSONObject tier, int appId, String parentSelfLink){
        JSONObject requestJson = new JSONObject();

        requestJson.put("appName", "safdasdfasf");
        requestJson.put("appId", appId);
        requestJson.put("tierName", tier.getString("name"));
        requestJson.put("tierId", tier.getInt("id"));
        requestJson.put("tierDescription", tier.getString("description"));
        requestJson.put("tierAgentType", tier.getString("agentType"));
        requestJson.put("tierType", tier.getString("type"));
        requestJson.put("tierNodeCount", tier.getInt("numberOfNodes"));
        requestJson.put("parentSelfLink", parentSelfLink);
        return requestJson;
    }

//    public void postTier(JSONObject tier, int appId, String parentSelfLink) {
//        Operation op = Operation.createPost(URI.create(ServiceUrls.SERVICE_HOST + ServiceUrls.SERVICE_URI_TIER_DATA));
////        op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
//        op.setReferer(this.getUri());
//        System.out.println(makeTierJson(tier, appId, parentSelfLink).toString());
//        op.setBody(makeTierJson(tier, appId, parentSelfLink).toString());
//        op.setCompletion((postOp, failOp) -> {
//            if (failOp != null) {
//                Utils.toString(failOp);
//                op.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
//                System.out.println(failOp.getMessage());
//            } else {
//                String response = postOp.getBody(String.class);
//                System.out.println(response.toString());
//                op.complete();
//            }
//        });
//        this.sendRequest(op);
//        return;
//    }

}
