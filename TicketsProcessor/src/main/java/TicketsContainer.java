import java.util.List;

public class TicketsContainer
{
    public static class TicketStructure
    {
        public String origin_name;
        public String destination_name;
        public String departure_date;
        public String departure_time;
        public String arrival_date;
        public String arrival_time;
        public String carrier;
        public String price;
    }

    public List<TicketStructure> tickets;
}
