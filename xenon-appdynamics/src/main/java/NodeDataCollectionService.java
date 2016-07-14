import com.vmware.xenon.common.FactoryService;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.TaskState;
import com.vmware.xenon.common.Utils;
import com.vmware.xenon.services.common.TaskService;
import org.json.JSONArray;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Created by agrawalshubham on 7/13/2016.
 */
public class NodeDataCollectionService extends TaskService<Node> {

    public static final String FACTORY_LINK = ServiceUrls.SERVICE_URI_NODE_DATA;
    public static final String SELF_LINK = ServiceUrls.SERVICE_URI_NODE_DATA;


    public String username = "shubham@customer1";
    public String password = "paradiddle";
    public String credentials = username + ":" + password;
    public String authStr = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());

    public static FactoryService createFactory() {
        return FactoryService.create(NodeDataCollectionService.class, ServiceOption.IDEMPOTENT_POST,
                ServiceOption.INSTRUMENTATION);
    }

    public NodeDataCollectionService() {
        super(Node.class);
        toggleOption(ServiceOption.PERSISTENCE, true);
        toggleOption(ServiceOption.REPLICATION, true);
        toggleOption(ServiceOption.INSTRUMENTATION, true);
        toggleOption(ServiceOption.OWNER_SELECTION, true);
    }


    @Override
    protected void initializeState(Node node, Operation op){
        node.action = Node.Action.INITIALIZE;
        super.initializeState(node, op);
    }

    @Override
    public void handlePatch(Operation patch){
        Node currentNode = getState(patch);
        Node patchBody = getBody(patch);

        updateState(currentNode, patchBody);

        switch (patchBody.taskInfo.stage){
            case CREATED:
                break;
            case STARTED:
                handleAction(patchBody, patch);
                break;
            case CANCELLED:
                break;
            case FINISHED:
                break;
            case FAILED:
                break;
            default:
                break;
        }
    }

    public void handleAction(Node node, Operation op){
        switch(node.action) {
            case INITIALIZE:
                System.out.println("Node: in init");
                getSelfInformation(node, op);
                break;
            case REFRESH:
                System.out.println("Node: In collecting");
                break;
            case SYNC:
                break;
            case LOAD:
                break;
            default:
                break;
        }
    }

    public void getSelfInformation(Node node, Operation op){
        System.out.println(ServiceUrls.APP_DYNAMICS_PER_APP_API + ServiceUrls.APP_DYNAMICS_TIER_API + "/" + node.tierId + "?output=JSON");
        Operation dataRequest = Operation
                .createGet(URI.create(ServiceUrls.APP_DYNAMICS_PER_APP_API + node.appId + "/" + ServiceUrls.APP_DYNAMICS_TIER_API + "/" + node.nodeId + "?output=JSON"));
        dataRequest.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        dataRequest.setBody(new Object());
        dataRequest.setReferer(this.getUri());
        dataRequest.setCompletion((getOp, failOp) -> {
            if (failOp != null) {

                System.out.println("Node: fail self info");
                Utils.toString(failOp);
                dataRequest.fail(Operation.STATUS_CODE_BAD_REQUEST);
                System.out.println(failOp.getMessage());
                return;
            } else {
                System.out.println("Node: success self info");
                String data = getOp.getBody(String.class);
                JSONArray requestJson = new JSONArray(data);
                System.out.println(requestJson.getJSONObject(0).toString());
                sendSelfPatch(node, TaskState.TaskStage.STARTED, subStageSetter(Node.Action.REFRESH));
                op.complete();
                return;
            }
        });

        this.sendRequest(dataRequest);
    }

    private Consumer<Node> subStageSetter(Node.Action action) {
        return nodeState -> nodeState.action = action;
    }


}
