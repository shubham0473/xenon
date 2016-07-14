import com.vmware.xenon.common.FactoryService;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.TaskState;
import com.vmware.xenon.common.Utils;
import com.vmware.xenon.services.common.TaskService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Created by agrawalshubham on 7/9/2016.
 */
public class TierDataCollectionService extends TaskService<Tier> {

    public static final String FACTORY_LINK = ServiceUrls.SERVICE_URI_TIER_DATA;
    public static final String SELF_LINK = ServiceUrls.SERVICE_URI_TIER_DATA;


    public String username = "shubham@customer1";
    public String password = "paradiddle";
    public String credentials = username + ":" + password;
    public String authStr = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());

    /**
     * Simple passthrough to our parent's constructor.
     *
     */
    public TierDataCollectionService() {
        super(Tier.class);
        toggleOption(ServiceOption.PERSISTENCE, true);
        toggleOption(ServiceOption.REPLICATION, true);
        toggleOption(ServiceOption.INSTRUMENTATION, true);
        toggleOption(ServiceOption.OWNER_SELECTION, true);
    }

    public static FactoryService createFactory() {
        return FactoryService.create(TierDataCollectionService.class, ServiceOption.IDEMPOTENT_POST,
                ServiceOption.INSTRUMENTATION);
    }

    @Override
    protected void initializeState(Tier tier, Operation op){
        tier.action = Tier.Action.INITIALIZE;
        super.initializeState(tier, op);
    }

//    public void initAndCreateChildren(Tier tier){
//
//    }

    public void handleAction(Tier tier, Operation op){
        System.out.println("Tier Service: in handle Substage");
        switch(tier.action) {
            case INITIALIZE:
                System.out.println("Tier: Tier Service: in init");
                initAndCreateChildren(tier, op);
                break;
            case REFRESH:
                System.out.println("Tier: In refresh");
                break;
            case SYNC:
                break;
            case LOAD:
                break;
            default:
                break;
        }
    }

    public void signalNodeManager(Tier tier, Operation parentOp){
        JSONObject reqJson = new JSONObject();
        reqJson.put("id", tier.appId);
        reqJson.put("parentSelfLink", this.getSelfLink());
        Operation signal = Operation.createPost(URI.create(ServiceUrls.SERVICE_HOST + ServiceUrls.SERVICE_URI_NODE_MANAGER));
        signal.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        signal.setReferer(this.getUri());
        System.out.println("req json =- " + reqJson.toString());
        signal.setBody(reqJson.toString());
        signal.setCompletion((postOp, failOp) -> {
            if (failOp != null) {
                System.out.println("Tier: error in send fetch signal to node manager");
                Utils.toString(failOp);
                signal.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
                System.out.println(failOp.getMessage());

            } else {
                System.out.println("Tier: success in send fetch signal to node manager");
                String response = postOp.getBody(String.class);
                System.out.println(response.toString());
                signal.complete();
                parentOp.complete();
            }
        });
        this.sendRequest(signal);
        return;
    }

    public void initAndCreateChildren(Tier tier, Operation parentOp){
        System.out.println(ServiceUrls.APP_DYNAMICS_PER_APP_API + ServiceUrls.APP_DYNAMICS_TIER_API + "/" + tier.tierId + "?output=JSON");
        Operation dataRequest = Operation
                .createGet(URI.create(ServiceUrls.APP_DYNAMICS_PER_APP_API + tier.appId + "/" + ServiceUrls.APP_DYNAMICS_TIER_API + "/" + tier.tierId + "?output=JSON"));
        dataRequest.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        dataRequest.setBody(new Object());
        dataRequest.setReferer(this.getUri());
        dataRequest.setCompletion((getOp, failOp) -> {
            if (failOp != null) {

                System.out.println("Tier: fail self info");
                Utils.toString(failOp);
                dataRequest.fail(Operation.STATUS_CODE_BAD_REQUEST);
                System.out.println(failOp.getMessage());
                return;
            } else {
                System.out.println("Tier: success self info");
                String data = getOp.getBody(String.class);
                JSONArray requestJson = new JSONArray(data);
                System.out.println(requestJson.getJSONObject(0).toString());
                sendSelfPatch(tier, TaskState.TaskStage.STARTED, subStageSetter(Tier.Action.REFRESH));
                signalNodeManager(tier, parentOp);
                return;
            }
        });

        this.sendRequest(dataRequest);
    }

    @Override
    public void handlePatch(Operation patch){
        Tier currentTier = getState(patch);
        Tier patchBody = getBody(patch);

        updateState(currentTier, patchBody);

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

    private Consumer<Tier> subStageSetter(Tier.Action action) {
        return tierState -> tierState.action = action;
    }



}
