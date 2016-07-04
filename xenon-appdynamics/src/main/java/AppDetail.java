import com.vmware.xenon.common.ServiceDocument;

/**
 * Created by agrawalshubham on 7/4/2016.
 */
// A custom service document to represent the application information
public class AppDetail extends ServiceDocument {

    public static final String FIELD_NAME_APP_NAME = "name";
    public static final String FIELD_NAME_APP_ID = "id";
    public static final String FIELD_NAME_APP_DESC = "description";

    public String name;
    public int id;
    public String description;

}