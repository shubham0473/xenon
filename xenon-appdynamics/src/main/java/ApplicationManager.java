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
 * Created by agrawalshubham on 7/6/2016.
 */
public class ApplicationManager extends StatelessService {

    public static final String SELF_LINK = ServiceUrls.SERVICE_URI_APP_MANAGER;
//    public static final String FACTORY_LINK = ServiceUriPaths.CORE + ServiceUrls.SERVICE_URI_MANAGER;

    public static final String ACTION_INITIALIZE = "INITIALIZE";
    public static final String ACTION_LOAD_APPS = "LOAD-APPS";
    public static final String ACTION_SYNC = "SYNC";

//    private volatile String authStr = null;

    public String username = "shubham@customer1";
    public String password = "paradiddle";
    public String credentials = username + ":" + password;
    public String authStr = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());


    //Fetches data from app dynamics api and for each app,
    //it posts to ApplicationDataCollectionService
    @Override
    public void handlePost(Operation op){
        System.out.println("AM: In handle post");
        String requestString = op.getBody(String.class);
        JSONObject requestJson = new JSONObject(requestString);

        switch(requestJson.getString("action")){
            case ACTION_INITIALIZE:
                System.out.println("AM: in init");
//                initialize(op);
                loadApps(op);
                break;
            case ACTION_LOAD_APPS:
                if(authStr == null){
                    op.fail(Operation.STATUS_CODE_BAD_METHOD);
                    return;
                }
                System.out.println("AM: in load apps");
                loadApps(op);
                break;
            case ACTION_SYNC:
                sync(op);
                break;
            default:
                logSevere("ACTION NOT SUPPORTED");
                op.fail(Operation.STATUS_CODE_BAD_METHOD);
                break;
        }


        op.complete();
    }

    public void sync(Operation parentOp){

    }

    private void initialize(Operation op) {
        String requestString = op.getBody(String.class);
        JSONObject requestJson  = new JSONObject(requestString);

        JSONObject credentialsJson = requestJson.getJSONObject("credentials");
        String credentials = credentialsJson.getString("username") + ":" + credentialsJson.getString("password");

        authStr = "Basic " + javax.xml.bind.DatatypeConverter.
                printBase64Binary(credentials.getBytes());
        op.complete();
        return;
    }


    public void loadApps(Operation op){
        List<Operation> postAppOps = new ArrayList<>();
        Operation getAppData = Operation
                .createGet(URI.create(ServiceUrls.APP_DYNAMICS_APP_API));
        getAppData.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
        getAppData.setBody(new Object());
        getAppData.setCompletion((getOp, failOp) -> {
            if (failOp != null) {
                Utils.toString(failOp);
                getAppData.fail(Operation.STATUS_CODE_BAD_REQUEST);
                return;
            } else {
                String response = getOp.getBody(String.class);
                if (response.compareTo("{}") == 0) {
                    getAppData.complete();
                    return;
                }
                JSONArray data = new JSONArray(response);


                for (int i = 0; i < data.length(); i++) {
                    if (data.getJSONObject(i) != null) {
                        Operation post = Operation.createPost(URI.create("http://localhost:8001" + ServiceUrls.SERVICE_URI_APP_DATA));
                        post.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
                        post.setBody(data.getJSONObject(i).toString());
                        postAppOps.add(post);

                    }
                }
                OperationJoin.JoinedCompletionHandler jh = (ops, failures) -> {
                    if(failures != null){
                        op.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
                        return;
                    }
                    System.out.println("AM: Successfully created Apps");
                    op.complete();
                };
                OperationJoin.create(postAppOps).setCompletion(jh).sendWith(this);
                return;
            }
        });

        this.sendRequest(getAppData);
    }


}
