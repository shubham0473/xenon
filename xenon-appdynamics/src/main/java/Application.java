import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.ServiceDocumentDescription;
import com.vmware.xenon.services.common.TaskService;

/**
 * Created by agrawalshubham on 7/4/2016.
 */
// A custom service document to represent the application information
public class Application extends TaskService.TaskServiceState {

    public enum Action {
        INITIALIZE, REFRESH, SYNC, LOAD
    }


    public static final String FIELD_NAME_APP_NAME = "name";
    public static final String FIELD_NAME_APP_ID = "id";
    public static final String FIELD_NAME_APP_DESC = "description";
    public static final String FIELD_NAME_NUMBER_OF_TIERS = "numberOfTiers";

    public String name;
    public int id;
    public String description;
    public int numberOfTiers;

    @ServiceDocument.UsageOption(option = ServiceDocumentDescription.PropertyUsageOption.AUTO_MERGE_IF_NOT_NULL)
    public Action action;

}