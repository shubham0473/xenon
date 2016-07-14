import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.common.Utils;

import java.net.URI;
import java.util.logging.Level;

public class Host extends ServiceHost{

	public String username = "shubham@customer1";
	public String password = "paradiddle";
	public String credentials = username + ":" + password;
	public String authStr = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());

	 public static void main(String[] args) throws Throwable {
         String[] arg = {

                 "--port=8001",
         };
	        Host h = new Host();
	        h.initialize(arg);
	        h.toggleDebuggingMode(true);
	        h.start();
	        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	            h.log(Level.WARNING, "Host stopping ...");
	            h.stop();
	            h.log(Level.WARNING, "Host is stopped");
	        }));
	    }

	    @Override
	    public ServiceHost start() throws Throwable {


	        super.start();

	        startDefaultCoreServicesSynchronously();
	        super.log(Level.SEVERE, "Starting service");
			super.startService(new ApplicationManager());
			super.startFactory(ApplicationDataCollectionService.class, ApplicationDataCollectionService::createFactory);
			super.startService(new TierManager());
			super.startFactory(TierDataCollectionService.class, TierDataCollectionService::createFactory);
			super.startService(new NodeManager());
			super.startFactory(NodeDataCollectionService.class, NodeDataCollectionService::createFactory);
			//Sending signal to Application Manager
//			sendFetchSignal();

	        return this;
	    }

	private void sendFetchSignal(){
		System.out.println("in send fetch signal" + "URL" + ServiceUrls.SERVICE_URI_APP_MANAGER);
		Operation op = Operation.createPost(URI.create(getPublicUri() + ServiceUrls.SERVICE_URI_APP_MANAGER));
		op.addRequestHeader(Operation.AUTHORIZATION_HEADER, authStr);
		op.setReferer(this.getUri());
		op.setBody(new Object());
		op.setCompletion((postOp, failOp) -> {
			if (failOp != null) {
				System.out.println("in error: send fetch signal");
				Utils.toString(failOp);
				op.fail(Operation.STATUS_CODE_INTERNAL_ERROR);
				System.out.println(failOp.getMessage());

			} else {
				System.out.println("in success: send fetch signal");
				String response = postOp.getBody(String.class);
				System.out.println(response.toString());
				op.complete();
			}
		});
		this.sendRequest(op);
		return;
	}

}
