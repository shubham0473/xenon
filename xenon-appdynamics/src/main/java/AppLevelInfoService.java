import com.vmware.xenon.common.*;
import com.vmware.xenon.services.common.QueryTask;
import com.vmware.xenon.services.common.QueryTask.Query;
import com.vmware.xenon.services.common.ServiceUriPaths;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AppLevelInfoService extends StatefulService {
	public static final String SELF_LINK = ServiceUriPaths.CORE + "/info/app";
	public static final String FACTORY_LINK = ServiceUriPaths.CORE + "/info/app";

	public String username = "shubham@customer1";
	public String password = "paradiddle";
	public String credentials = username + ":" + password;
	public String authStr = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());

	public static FactoryService createFactory() {
		return FactoryService.createIdempotent(AppLevelInfoService.class, AppDetail.class);
	}

	public AppLevelInfoService() {
		super(AppDetail.class);
		super.toggleOption(ServiceOption.PERSISTENCE, true);
		super.toggleOption(ServiceOption.REPLICATION, true);
		super.toggleOption(ServiceOption.INSTRUMENTATION, true);
		super.toggleOption(ServiceOption.OWNER_SELECTION, true);

		// TODO Auto-generated constructor stub
	}



	@Override
	public void handleStart(Operation startPost) {

		super.handleStart(startPost);
//		try {
//			getApplicationData();
//		} catch (JSONException | URISyntaxException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		startPost.complete();
	}

	@Override
	public void handlePatch(Operation patch) {
		AppDetail updateState = updateState(patch);

		if(updateState == null){
			return;
		}
//		super.handlePatch(patch);
		patch.complete();
	}
//	@Override
//	public void handlePost(Operation post){
//	System.out.println("entered post");
//	super.handlePost(post);
//	post.complete();
//	}

//	@Override
//	public void handleGet(Operation get) {
//		super.handleGet(get);
//		get.complete();
//	}

	// Create document instances for each entry in the Application JSON
	public void createDocument(JSONObject app) {

		Query appQuery = Query.Builder.create()
				.addFieldClause(ServiceDocument.FIELD_NAME_KIND, Utils.buildKind(AppDetail.class))
				.addFieldClause(AppDetail.FIELD_NAME_APP_NAME, app.getString("name")).build();

		QueryTask queryTask = QueryTask.Builder.createDirectTask().setQuery(appQuery).build();

		URI queryTaskUri = UriUtils.buildUri(this.getHost(), ServiceUriPaths.CORE_QUERY_TASKS);

		Operation postQuery = Operation.createPost(queryTaskUri).setBody(queryTask).setCompletion((op, ex) -> {
			if (ex != null) {
				return;
			}

			QueryTask queryResponse = op.getBody(QueryTask.class);
			if (queryResponse.results.documentLinks.isEmpty()) {
				postNewApp(app);
			} else {
				// PATCH request to update the existing document state
				try {
					patchExistingApp(app, queryResponse.results.documentLinks.get(0));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return;
		});

		this.sendRequest(postQuery);

	}

	public void postNewApp(JSONObject app) {
		
		Operation op = Operation.createPost(URI.create("http://localhost:8001/core/info/app"));
		op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
		op.setBody(app.toString());
		op.setCompletion((postOp, failOp) -> {
			if (failOp != null) {
				Utils.toString(failOp);
				op.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
			} else {
				String response = postOp.getBody(String.class);
				op.complete();
			}
		});
		this.sendRequest(op);
		return;
	}

	public void patchExistingApp(JSONObject app, String link) throws URISyntaxException {
		
		if(link == null) return;

		URI uri = new URI("http://localhost:8001" + link);

		Operation op = Operation.createPatch(uri);
		op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
		op.setBody(app.toString());
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

	// This function queries the AppDynamics API to fetch the information about
	// all the app available
	// Returns an array of JSON
	public void getApplicationData() throws URISyntaxException, JSONException, IOException {

		Operation op = Operation
				.createGet(URI.create("http://blr-agrawalshubham:8090/controller/rest/applications?output=JSON"));
		op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
		op.setBody(new Object());
		op.setCompletion((getOp, failOp) -> {
			if (failOp != null) {
				Utils.toString(failOp);
				op.fail(Operation.STATUS_CODE_BAD_REQUEST);
				return;
			} else {
				String response = getOp.getBody(String.class);
				if(response.toString().compareTo("{}") == 0) {
					op.complete();
					return;
				}
				JSONArray data = new JSONArray(response.toString());

				for (int i = 0; i < data.length(); i++) {
					createDocument(data.getJSONObject(i));
				}
				op.complete();
				return;
			}
		});

		this.sendRequest(op);
//		System.out.println(op.isForwarded());

		return;
	}

	private AppDetail updateState(Operation update){
		AppDetail body = getBody(update);
		AppDetail currentState = getState(update);

		boolean hasStateChanged = Utils.mergeWithState(getStateDescription(), currentState, body);

		if(body.name != null){
			currentState.name = body.name;
		}

		if(body.description!= null){
			currentState.description = body.description;
		}

		if (body.documentExpirationTimeMicros != 0) {
			currentState.documentExpirationTimeMicros = body.documentExpirationTimeMicros;
		}

		update.setBody(currentState);
		return currentState;
	}

}
