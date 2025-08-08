import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

public class TicketsProcessorImplementation {

    private final static DateTimeFormatter INCOME_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
    private final static String OUTCOME_TIME_FORMAT = "%02d:%02d";
    private final static String CITY1 = "Владивосток";
    private final static String CITY2 = "Тель-Авив";
    private final static long CITIES_TIME_SHIFT = 8; // for the sake of simplicity, ignore Israel summer/winter time, suppose winter only

    private String mProcessingResult;

    /**
    * @return does ticket from/to to/from necessary cities
     */
    private boolean checkTicket(TicketsContainer.TicketStructure ticket)
    {
        return ((ticket.origin_name.equals(CITY1) && ticket.destination_name.equals(CITY2)) ||
                (ticket.destination_name.equals(CITY2)) && ticket.origin_name.equals(CITY1));
    }

    /**
     *
     * @param dateTimeString
     * @param time
     *
     * if time contains hours with 1 not 2 digits (e.g. "1"), complement it to 2 digits (i.e. "01")
     * otherwise does nothing
     * corrected or not time then append to dataTimeString
     *
     * such complementing is necessary to correct parse dateTimeString
     */
    private void complementTimeIfNecessary(StringBuilder dateTimeString, String time)
    {
        if (4 == time.length())
        {
            dateTimeString.append("0");
        }
    }

    /**
     * @param departureDate
     * @param departureTime
     * @param arrivalDate
     * @param arrivalTime
     *
     * @return time of flight calculated based upon parameters in Duration form
     * local time zone shifts doesn't take into account
     *
     */
    private Duration timeOfFilght(String departureDate, String departureTime,
                              String arrivalDate, String arrivalTime)
    {
        Duration retValue = null;

        try
        {
            // get departure and arrival time
            StringBuilder departureDateTime = new StringBuilder(departureDate).append(" ");
            complementTimeIfNecessary(departureDateTime, departureTime);
            departureDateTime.append(departureTime);
            LocalDateTime d1 = LocalDateTime.parse(departureDateTime, INCOME_DATE_TIME_FORMAT);

            StringBuilder arrivalDateTime = new StringBuilder(arrivalDate).append(" ");
            complementTimeIfNecessary(arrivalDateTime, arrivalTime);
            arrivalDateTime.append(arrivalTime);
            LocalDateTime d2 = LocalDateTime.parse(arrivalDateTime, INCOME_DATE_TIME_FORMAT);

            // calculate time of flight without local time shift
            retValue = Duration.between(d1, d2);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return retValue;
    }

    /**
     *
     * @param dur
     * @return in accordanve with flight time (dur) return string of flight time
     * in accordance with format TicketsProcessorImplementation.OUTCOME_TIME_FORMAT
     * local time zone shift has taken into account here
     */
    String getFlightTimeString(Duration dur)
    {
        String retValue="";

        // get hours and minutes of duration and take into account local time zone shift
        long hours = Math.abs(dur.toHours());
        long minutes = Math.abs(dur.toMinutes()) - 60 * hours;
        hours += CITIES_TIME_SHIFT;

        retValue = String.format(OUTCOME_TIME_FORMAT, hours, minutes);
        return retValue;
    }

    /**
     *
     * @param values
     * @return mean value of values
     */
    double getMeanValue(ArrayList<Long> values)
    {
        double retValue = 0.0;

        for(Long val : values)
        {
            retValue += val;
        }

        retValue /= values.size();

        return retValue;
    }

    /**
     *
     * @param values
     * @return median of values
     */
    String getMedian(ArrayList<Long> values)
    {
        String retValue = "";

        int mediumIndex = values.size() / 2 - 1;

        // different approaches for even and uneven numbers of values
        if (0 == (values.size() % 2))
        {
            double meanValue = (values.get(mediumIndex) + values.get(mediumIndex + 1)) / 2;
            retValue = String.format("%.2f", meanValue);
        }
        else
        {
            retValue = String.format("%d", values.get(mediumIndex + 1));
        }

        return retValue;
    }

    TicketsProcessorImplementation(TicketsContainer tc)
    {
        // not Set, 'cause could be duplicated prices
        // also, I assume prices are integer values not float/double, i.e. 100$, 300$ (not, say, 255.75$)-at least json file contains such prices
        ArrayList<Long> prices = new ArrayList<Long>();

        // carriers/minimum flight times
        Map<String, Duration> minimumTimes = new HashMap<String, Duration>();

        try
        {
            for(TicketsContainer.TicketStructure ticket : tc.tickets)
            {
                // ensure we're processing necessary ticket
                if (!checkTicket(ticket))
                {
                    continue;
                }

                // calculate and update flight time for carrier
                Duration currentFlightTime = timeOfFilght(ticket.departure_date, ticket.departure_time,
                        ticket.arrival_date, ticket.arrival_time);
                if (minimumTimes.containsKey(ticket.carrier))
                {
                    if (currentFlightTime.toMinutes() < minimumTimes.get(ticket.carrier).toMinutes())
                    {
                        minimumTimes.replace(ticket.carrier, currentFlightTime);
                    }
                }
                else
                {
                    minimumTimes.put(ticket.carrier, currentFlightTime);
                }

                // save all prices to calculate in following mean/median values
                prices.add(Long.parseLong(ticket.price));
            }

            // in order to calculate median value, we need sorted prices
            Collections.sort(prices);

            // build result string
            StringBuilder sb = new StringBuilder("Minimum flight times by carriers:").append("\n");
            for(String val : minimumTimes.keySet())
            {
                sb.append(val).append(":   ").append(getFlightTimeString(minimumTimes.get(val))).append("\n");
            }

            sb.append("\n").append("\n").append("mean value: ").append(getMeanValue(prices)).append("\n");
            sb.append("median value: ").append(getMedian(prices));

            mProcessingResult = sb.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     *
     * @return result of tickets processing
     */
    String getProcessingResult()
    {
        return mProcessingResult;
    }
}