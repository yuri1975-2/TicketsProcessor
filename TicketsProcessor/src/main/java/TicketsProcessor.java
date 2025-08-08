import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class TicketsProcessor {
    /**
     *
     * @param args
     *
     * entry point
     */
    public static void main (String[] args)
    {

        // ensure filename has been provided
        if (1 != args.length)
        {
            System.out.println("please, specify path to ticket information");
            return;
        }

        try
        {
            // parse json-file ignoring unnecessary attributes
            File file = new File(args[0]);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            TicketsContainer tc = objectMapper.readValue(file, TicketsContainer.class);


            // process tickets and print result
            TicketsProcessorImplementation tpi = new TicketsProcessorImplementation((tc));
            System.out.println("\n\n");
            System.out.println(tpi.getProcessingResult());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
