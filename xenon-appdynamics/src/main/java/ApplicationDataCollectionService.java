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
 * Created by agrawalshubham on 7/6/2016.
 */
public class ApplicationDataCollectionService extends TaskService<Application> {

    public static final String FACTORY_LINK = ServiceUrls.SERVICE_URI_APP_DATA;
    public static final String SELF_LINK = ServiceUrls.SERVICE_URI_APP_DATA;


    public String username = "shubham@customer1";
    public String password = "paradiddle";
    public String credentials = username + ":" + password;
    public String authStr = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());

    /**
     * Simple passthrough to our parent's constructor.
     *
     *
     */
    public ApplicationDataCollectionService() {
        super(Application.class);
        toggleOption(ServiceOption.PERSISTENCE, true);
        toggleOption(ServiceOption.REPLICATION, true);
        toggleOption(ServiceOption.INSTRUMENTATION, true);
        toggleOption(ServiceOption.OWNER_SELECTION, true);
    }

    /**
     * These substages helps in creating a FSM which triggers different routine on different states i.e substages
     */

    public static FactoryService createFactory() {
        return FactoryService.create(ApplicationDataCollectionService.class, ServiceOption.IDEMPOTENT_POST,
                ServiceOption.INSTRUMENTATION);
    }
//
//    protected Application validateStartPost(Operation taskOperation){
//        Application app = super.validateStartPost(taskOperation);
//        if(app == null){
//            return null;
//        }
//        if(ServiceHost.isServiceCreate(taskOperation)){
//            if(app.subStage != null){
//                taskOperation.fail(new IllegalArgumentException("Do not specify subStage: internal use only"));
//            }
//        }
//
//    @Override
//    public void handleCreate(Operation op){
//
//
//    }\

//    @Override
//    public void handleGet(Operation get){
//        System.out.println("in gett");
//        super.handleGet(get);
//        get.complete();
//    }


    //
    public void handleSubstage(Application app, Operation parentOp){
        System.out.println("App =" + app.toString());
        switch (app.action) {
            case INITIALIZE:
                System.out.println("App: In init");
                initAndCreateChildren(app, parentOp);
                break;
            case REFRESH:
                refresh(app, parentOp);
                System.out.println("App: In collected");
                break;
            case SYNC:
                syncSelfAndChildren(app, parentOp);
                break;
            default:
                System.out.println("App: Unexpected sub stage");
                break;
        }
    }

    public void syncSelfAndChildren(Application app, Operation parentOp){

    }

    public void refresh(Application app, Operation parentOp){
        Operation dataRequest = Operation
                .createGet(URI.create(ServiceUrls.APP_DYNAMICS_PER_APP_API + app.id + "?output=JSON"));
        dataRequest.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        dataRequest.setBody(new Object());
        dataRequest.setReferer(this.getUri());
        dataRequest.setCompletion((getOp, failOp) -> {
            if (failOp != null) {
                Utils.toString(failOp);
                dataRequest.fail(Operation.STATUS_CODE_BAD_REQUEST);
                System.out.println("App: failure in getselfinfo");
                System.out.println(failOp.getMessage());
                return;
            } else {
                String data = getOp.getBody(String.class);
                JSONArray requestJson = new JSONArray(data);
                System.out.println(requestJson.getJSONObject(0).toString());
                System.out.println("App: success in getselfinfo");
                sendSelfPatch(app, TaskState.TaskStage.STARTED, subStageSetter(Application.Action.REFRESH));
                signalTierManager(app, parentOp);
                return;
            }
        });

        this.sendRequest(dataRequest);
    }



    //Send signal to TierManager with app id and self link
    public void signalTierManager(Application app, Operation parentOp){
        System.out.println("App: in signal tier manager");
        JSONObject reqJson = new JSONObject();
        reqJson.put("id", app.id);
        reqJson.put("parentSelfLink", this.getSelfLink());
        Operation signal = Operation.createPost(URI.create(ServiceUrls.SERVICE_HOST + ServiceUrls.SERVICE_URI_TIER_MANAGER));
        signal.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        signal.setReferer(this.getUri());
        System.out.println("req json =- " + reqJson.toString());
        signal.setBody(reqJson.toString());
        signal.setCompletion((postOp, failOp) -> {
            if (failOp != null) {
                System.out.println("App: error in send signal to TM");
                Utils.toString(failOp);
                signal.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
                System.out.println(failOp.getMessage());

            } else {
                System.out.println("App : succes in send fetch signal to TM");
                String response = postOp.getBody(String.class);
                System.out.println(response.toString());
                signal.complete();
                parentOp.complete();
            }
        });
        this.sendRequest(signal);
        return;
    }


    @Override
    protected void initializeState(Application app, Operation op){
        app.action = Application.Action.INITIALIZE;
        logSevere("In init state");

        super.initializeState(app, op);
    }

    //Get the data about itself from the appdynamics api
    public void initAndCreateChildren(Application app, Operation parentOp){
        Operation dataRequest = Operation
                .createGet(URI.create(ServiceUrls.APP_DYNAMICS_PER_APP_API + app.id + "?output=JSON"));
        dataRequest.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        dataRequest.setBody(new Object());
        dataRequest.setReferer(this.getUri());
        dataRequest.setCompletion((getOp, failOp) -> {
            if (failOp != null) {
                Utils.toString(failOp);
                dataRequest.fail(Operation.STATUS_CODE_BAD_REQUEST);
                System.out.println("App: failure in getselfinfo");
                System.out.println(failOp.getMessage());
                return;
            } else {
                String data = getOp.getBody(String.class);
                JSONArray requestJson = new JSONArray(data);
                System.out.println(requestJson.getJSONObject(0).toString());
                System.out.println("App: success in getselfinfo");
                sendSelfPatch(app, TaskState.TaskStage.STARTED, subStageSetter(Application.Action.REFRESH));
                signalTierManager(app, parentOp);
                return;
            }
        });

        this.sendRequest(dataRequest);
    }


    @Override
    public void handlePatch(Operation patch){
        Application currentApp = getState(patch);
        Application patchBody = getBody(patch);

        //TODO: check if the transition is valid

        updateState(currentApp, patchBody);

        switch(patchBody.taskInfo.stage){
            case CREATED:
                break;
            case STARTED:
                handleSubstage(patchBody, patch);
                break;
            case CANCELLED:
                logInfo("Request cancelled");
                break;
            case FINISHED:
                logInfo("Finished successfully");
                break;
            case FAILED:
                logWarning("Task failed: %s", (patchBody.failureMessage == null ? "No reason given"
                        : patchBody.failureMessage));
                break;
            default:
                logWarning("Unexpected stage: %s", patchBody.taskInfo.stage);
                break;
        }
    }



    private Consumer<Application> subStageSetter(Application.Action action) {
        return appState -> appState.action = action;
    }



}
