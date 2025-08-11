import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

public class TicketsProcessorImplementation {

    private class TicketTime
    {
        private String mCity;
        private String mTime;
        private String mDate;

        TicketTime(String city, String time, String date)
        {
            mCity = city;
            mTime = time;
            mDate = date;
        }
    };

    private final static DateTimeFormatter INCOME_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
    private final static String OUTCOME_TIME_FORMAT = "%02d:%02d";
    private final static String CITY1 = "Владивосток";
    private final static String CITY2 = "Тель-Авив";

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
     *
     * @param tc
     * @return based upon ticket time returns UTC (taking into account whether summer or winter time)
     */
    LocalDateTime getUTCTime(TicketTime tc)
    {
        LocalDateTime retValue = null;
        try
        {
            // get local time from ticket
            StringBuilder departureDateTime = new StringBuilder(tc.mDate).append(" ");
            complementTimeIfNecessary(departureDateTime, tc.mTime);
            departureDateTime.append(tc.mTime);
            retValue = LocalDateTime.parse(departureDateTime, INCOME_DATE_TIME_FORMAT);

            // take into account time shift
            retValue = retValue.plusHours(SummerWinterTimeHandler.getTimeShift(retValue, tc.mCity));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return retValue;
    }

    /**
     * @param departureTime
     * @param arrivalTime
     *
     * @return time of flight calculated based upon parameters in Duration form
     *
     */
    private Duration timeOfFilght(TicketTime departureTime, TicketTime arrivalTime)
    {
        Duration retValue = null;

        try
        {
            retValue = Duration.between(getUTCTime(departureTime), getUTCTime(arrivalTime));
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
     */
    String getFlightTimeString(Duration dur)
    {
        String retValue="";

        // get hours and minutes of duration and take into account local time zone shift
        long hours = Math.abs(dur.toHours());
        long minutes = Math.abs(dur.toMinutes()) - 60 * hours;

        retValue = String.format(OUTCOME_TIME_FORMAT, hours, minutes);
        return retValue;
    }

    /**
     *
     * @param values
     * @return mean value of values
     */
    double getMeanValue(ArrayList<Double> values)
    {
        double retValue = 0.0;

        for(Double val : values)
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
    double getMedian(ArrayList<Double> values)
    {
        double retValue = 0.0;

        int mediumIndex = values.size() / 2 - 1;

        // different approaches for even and uneven numbers of values
        if (0 == (values.size() % 2))
        {
            retValue = (values.get(mediumIndex) + values.get(mediumIndex + 1)) / 2;
        }
        else
        {
            retValue = values.get(mediumIndex + 1);
        }

        return retValue;
    }

    TicketsProcessorImplementation(TicketsContainer tc)
    {
        ArrayList<Double> prices = new ArrayList<Double>();

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
                Duration currentFlightTime = timeOfFilght(new TicketTime(ticket.origin_name, ticket.departure_time, ticket.departure_date),
                new TicketTime(ticket.destination_name, ticket.arrival_time, ticket.arrival_date));
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
                prices.add(Double.parseDouble(ticket.price));
            }

            // in order to calculate median value, we need sorted prices
            Collections.sort(prices);

            // build result string
            StringBuilder sb = new StringBuilder("Minimum flight times by carriers:").append("\n");
            for(String val : minimumTimes.keySet())
            {
                sb.append(val).append(":   ").append(getFlightTimeString(minimumTimes.get(val))).append("\n");
            }

            sb.append("\n").append("\n");
            sb.append("Difference between mean price and median: ")
                    .append(String.format("%.2f",getMeanValue(prices) - getMedian(prices)));

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