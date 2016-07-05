import com.vmware.xenon.common.*;
import com.vmware.xenon.services.common.QueryTask;
import com.vmware.xenon.services.common.ServiceUriPaths;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by agrawalshubham on 7/4/2016.
 */
public class NodeLevelInfoService extends StatefulService {


    public static final String SELF_LINK = ServiceUriPaths.CORE + "/info/node";
    public static final String FACTORY_LINK = ServiceUriPaths.CORE + "/info/node";

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


    public void queryAppLinks(){
        List appList = new ArrayList();

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
                    getAppInfo(queryResponse.results.documentLinks.get(i).toString());
                }
                return;
            }
        });

        this.sendRequest(postQuery);
        return;
    }

    public void getAppInfo(String link){
        System.out.println(link);

        Operation op = Operation.createGet(URI.create("http://localhost:8001/core/info/app/ff912de9-d4a4-450a-99e3-e3b14fb72f12/"));
//        op.addResponseHeader(Operation.CONTENT_TYPE_HEADER, Operation.MEDIA_TYPE_APPLICATION_JSON);
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
                System.out.println("success in fetching app info");
                String response = getOp.getBody(String.class);
                System.out.println(response.toString());
                if(response.toString().compareTo("{}") == 0) {
                    op.complete();
                    return;
                }
                JSONObject data = new JSONObject(response.toString());
//                getTierData(data.getString("name"));
                op.complete();
                return;
            }
        });
        this.getHost().sendRequest(op);
    }

    public void createDocument(JSONObject node){
        QueryTask.Query appQuery = QueryTask.Query.Builder.create()
                .addFieldClause(ServiceDocument.FIELD_NAME_KIND, Utils.buildKind(NodeDetail.class))
                .addFieldClause(TierDetail.FIELD_NAME_TIER_NAME, node.getString("name")).build();

        QueryTask queryTask = QueryTask.Builder.createDirectTask().setQuery(appQuery).build();

        URI queryTaskUri = UriUtils.buildUri(this.getHost(), ServiceUriPaths.CORE_QUERY_TASKS);

        Operation postQuery = Operation.createPost(queryTaskUri).setBody(queryTask).setCompletion((op, ex) -> {
            if (ex != null) {
                return;
            }

            QueryTask queryResponse = op.getBody(QueryTask.class);
            if (queryResponse.results.documentLinks.isEmpty()) {
                postNewNode(node);
            } else {
                // PATCH request to update the existing document state
                try {
                    patchExistingNode(node, queryResponse.results.documentLinks.get(0));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return;
        });

        this.sendRequest(postQuery);
    }

    public void postNewNode(JSONObject tier){
        System.out.println("post new node");
        Operation op = Operation.createPost(URI.create("http://localhost:8001/core/info/tier"));
        op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        op.setBody(tier.toString());
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

    public void patchExistingNode(JSONObject node, String link){
        System.out.println("patch node");

        if(link == null) return;

        URI uri = URI.create("http://localhost:8001" + link);

        Operation op = Operation.createPatch(uri);
        op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        op.setBody(node.toString());
        op.setCompletion((patchOp, failOp) -> {
            if (failOp != null) {
                Utils.toString(failOp);
                op.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
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


    @Override
    public void handlePatch(Operation patch) {
        TierDetail updateState = updateState(patch);

        if(updateState == null){
            return;
        }
        patch.complete();
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

}
