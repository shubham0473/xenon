import com.vmware.xenon.common.ServiceHost;

import java.util.logging.Level;

public class Host extends ServiceHost{

	 public static void main(String[] args) throws Throwable {
         String[] arg = {

                 "--port=8001",
         };
	        Host h = new Host();
	        h.initialize(arg);
	        h.toggleDebuggingMode(true);
	        h.start();
	        h.log(Level.SEVERE, "entered main");
	        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	            h.log(Level.WARNING, "Host stopping ...");
	            h.stop();
	            h.log(Level.WARNING, "Host is stopped");
	        }));
	    }

	    @Override
	    public ServiceHost start() throws Throwable {

			AppLevelInfoService app = new AppLevelInfoService();
			TierLevelInfoService tier = new TierLevelInfoService();
			NodeLevelInfoService node = new NodeLevelInfoService();
	        super.start();

	        startDefaultCoreServicesSynchronously();
	        super.log(Level.SEVERE, "Starting service");
	        // start the example factory
			super.startFactory(AppLevelInfoService.class, AppLevelInfoService::createFactory);
	        super.startService(app);

			System.out.println("host calling getappdata");
			app.getApplicationData();

			super.startFactory(TierLevelInfoService.class, TierLevelInfoService::createFactory);
			super.startService(tier);
			tier.getTierData();
//
//			super.startFactory(NodeLevelInfoService.class, NodeLevelInfoService::createFactory);
//			super.startService(node);
//			node.getNodeData();
	        return this;
	    }

}
