import com.vmware.xenon.common.*;
import com.vmware.xenon.services.common.QueryTask;
import com.vmware.xenon.services.common.ServiceUriPaths;
import org.json.JSONArray;
import org.json.JSONObject;

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

//    public String appName = "MyApp";

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

    public void queryAppLinks(){

        QueryTask.Query appQuery = QueryTask.Query.Builder.create()
                .addFieldClause(ServiceDocument.FIELD_NAME_KIND, Utils.buildKind(AppDetail.class)).build();

        QueryTask queryTask = QueryTask.Builder.createDirectTask().setQuery(appQuery).build();

        URI queryTaskUri = UriUtils.buildUri(this.getHost(), ServiceUriPaths.CORE_QUERY_TASKS);

        Operation postQuery = Operation.createPost(queryTaskUri).setBody(queryTask).setCompletion((op, ex) -> {
            if (ex != null) {
                return;
            }

            QueryTask queryResponse = op.getBody(QueryTask.class);
            if (queryResponse.results.documentLinks.isEmpty()) {
                System.out.println("no result");
                return;
            } else {
                // get the self-link list of all the apps
                for(int i = 0; i < queryResponse.results.documentLinks.size(); i++){
                    try {
                        System.out.println("doc link : " + queryResponse.results.documentLinks.get(i).toString());
                        getAppInfo(queryResponse.results.documentLinks.get(i).toString());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
        });

        this.sendRequest(postQuery);
        return;
    }

    public void getAppInfo(String link) throws InterruptedException {

        Operation op = Operation.createGet(URI.create("http://localhost:8001" +link));
        op.setBody(new Object());
        op.setReferer(this.getUri());
        op.setCompletion((getOp, failOp) -> {
            if (failOp != null) {
                System.out.println("error in get app info");
                Utils.toString(failOp);
                op.fail(Operation.STATUS_CODE_BAD_REQUEST);
                System.out.println(failOp.getMessage());
                return;
            } else {
                System.out.println("success in fetching app info");
                String response = getOp.getBody(String.class);
                System.out.println(response.toString());
                if(response.toString().compareTo("{}") == 0) {
                    op.complete();
                    return;
                }
                JSONObject data = new JSONObject(response.toString());
                System.out.println("appname: " + data.getString("name"));
                getTierData(data.getString("name"));
                op.complete();
                return;
            }
        });
        this.sendRequest(op);
        return;
    }

    @Override
    public void handlePatch(Operation patch) {
        TierDetail updateState = updateState(patch);

        if(updateState == null){
            return;
        }
        patch.complete();
    }

    @Override
    public void handleStart(Operation startPost) {

        super.handleStart(startPost);
        startPost.complete();
    }


    public void getTierData(String appName){
        System.out.println("getTierData");
        System.out.println(appName);
        Operation op = Operation.createGet(URI.create("http://blr-agrawalshubham:8090/controller/rest/applications/"+ appName + "/tiers?output=JSON"));
        op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        op.setBody(new Object());
        op.setReferer(this.getUri());
        op.setCompletion((getOp, failOp) -> {
            if (failOp != null) {
                System.out.println("error in appdynamics api");
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

                for(int i = 0; i < data.length(); i++){
                    createDocument(data.getJSONObject(i));
                }
                op.complete();
                return;
            }
        });

        this.getHost().sendRequest(op);
        return;
    }

    private JSONObject createRequestJson(JSONObject json){

        JSONObject requestJson = new JSONObject();

        requestJson.put("appName", "safdasdfasf");
        requestJson.put("appId", 55);
        requestJson.put("tierName", json.getString("name"));
        requestJson.put("tierId", json.getInt("id"));
        requestJson.put("tierDescription", json.getString("description"));
        requestJson.put("tierAgentType", json.getString("agentType"));
        requestJson.put("tierType", json.getString("type"));
        requestJson.put("tierNodeCount", json.getInt("numberOfNodes"));
        return requestJson;
    }

    public void createDocument(JSONObject tier){
        QueryTask.Query appQuery = QueryTask.Query.Builder.create()
                .addFieldClause(ServiceDocument.FIELD_NAME_KIND, Utils.buildKind(TierDetail.class))
                .addFieldClause(TierDetail.FIELD_NAME_TIER_NAME, tier.getString("name")).build();

        QueryTask queryTask = QueryTask.Builder.createDirectTask().setQuery(appQuery).build();

        URI queryTaskUri = UriUtils.buildUri(this.getHost(), ServiceUriPaths.CORE_QUERY_TASKS);

        Operation postQuery = Operation.createPost(queryTaskUri).setBody(queryTask).setCompletion((op, ex) -> {
            if (ex != null) {
                return;
            }

            QueryTask queryResponse = op.getBody(QueryTask.class);
            if (queryResponse.results.documentLinks.isEmpty()) {
                postNewTier(tier);
            } else {
                // PATCH request to update the existing document state
                try {
                    patchExistingTier(tier, queryResponse.results.documentLinks.get(0));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return;
        });

        this.sendRequest(postQuery);
        return;
    }

    public void postNewTier(JSONObject tier){
        System.out.println("post new tier");
        Operation op = Operation.createPost(URI.create("http://localhost:8001/core/info/tier"));
        op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        op.setBody(createRequestJson(tier).toString());
        op.setCompletion((postOp, failOp) -> {
            if (failOp != null) {
                Utils.toString(failOp);
                op.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
                System.out.println(failOp.getMessage());
            } else {
                System.out.println("success in poost new tier");
                String response = postOp.getBody(String.class);
                op.complete();
            }
        });
        this.sendRequest(op);
        return;
    }

    public void patchExistingTier(JSONObject tier, String link){

        if(link == null) return;

        URI uri = URI.create("http://localhost:8001" + link);

        System.out.println("in patch tier uri = " + uri.toString());

        Operation op = Operation.createPatch(uri);
        op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        op.setBody(createRequestJson(tier).toString());
        op.setCompletion((patchOp, failOp) -> {
            if (failOp != null) {
                Utils.toString(failOp);
                op.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
                System.out.println(failOp.getMessage());
            } else {
                String response = patchOp.getBody(String.class);
                System.out.println(response.toString());
                op.complete();
            }
        });
        this.sendRequest(op);
        return;
    }

    private TierDetail updateState(Operation update){
        TierDetail body = getBody(update);
        TierDetail currentState = getState(update);

        boolean hasStateChanged = Utils.mergeWithState(getStateDescription(), currentState, body);

        if(body.tierName != null){
            currentState.tierName = body.tierName;
        }

        if(body.tierDescription != null){
            currentState.tierDescription = body.tierDescription;
        }

        if(body.tierNodeCount != -1){
            currentState.tierNodeCount = body.tierNodeCount;
        }

        if(body.tierAgentType != null){
            currentState.tierAgentType = body.tierAgentType;
        }
        if(body.tierType != null){
            currentState.tierType = body.tierType;
        }

        if (body.documentExpirationTimeMicros != 0) {
            currentState.documentExpirationTimeMicros = body.documentExpirationTimeMicros;
        }

        update.setBody(currentState);
        return currentState;
    }
}
