import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class SummerWinterTimeHandler
{
    private interface IIsSummerTime
    {
        boolean isSummerTime(LocalDateTime time);
    }

    private static class TimeZoneDesc
    {
        final private IIsSummerTime mIsSummerTime;
        final int mSummerTimeShift;
        final int mWinterTimeShift;

        TimeZoneDesc(IIsSummerTime isSummerTime, int summerTimeShift, int winterTimeShift)
        {
            mIsSummerTime = isSummerTime;
            mSummerTimeShift = summerTimeShift;
            mWinterTimeShift = winterTimeShift;
        }
    }

    private static Map<String, TimeZoneDesc> LOCAL_TIME_SHIFTS;
    static
    {
        LOCAL_TIME_SHIFTS = new HashMap<String, TimeZoneDesc>();
        LOCAL_TIME_SHIFTS.put("Тель-Авив", new TimeZoneDesc(SummerWinterTimeHandler::isIsraelSummerTime, -3, -2));
        LOCAL_TIME_SHIFTS.put("Владивосток", new TimeZoneDesc(SummerWinterTimeHandler::isRussiaSummerTime, -10, -10));
        LOCAL_TIME_SHIFTS = Collections.unmodifiableMap(LOCAL_TIME_SHIFTS);
    }

    /**
     *
     * @param time
     * @return do time is Israel Summer time
     */
    private static boolean isIsraelSummerTime(LocalDateTime time)
    {
        if ((time.getMonthValue() >= 4) && (time.getMonthValue() <= 10))
        {
            return true;
        }

        // test winter->summer time moving at March (Friday, preceeding last Sunday, 02:00)
        if (3 == time.getMonthValue())
        {
            final int beforeSunday = DayOfWeek.SUNDAY.getValue() - time.getDayOfWeek().getValue();
            LocalDateTime shiftedTime = time.plusDays(beforeSunday);
            if (time.getMonthValue() != shiftedTime.getMonthValue())
            {
                return true;
            }

            // is last Sunday of March?
            if (shiftedTime.getMonthValue() != shiftedTime.plusWeeks(1).getMonthValue())
            {
                if (time.getDayOfWeek().getValue() < DayOfWeek.FRIDAY.getValue())
                {
                    // before Friday
                    return false;
                }
                else if (time.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue())
                {
                    // after Friday
                    return true;
                }
                else
                {
                    return (time.getHour() >= 2); // Friday precceding last March Sunday after 02:00
                }
            }
            else
            {
                // not last Sunday, still winter time
                return false;
            }
        }

        // tests summer->winter time moving, at last Sunday of October, 02:00
        if (10 == time.getMonthValue())
        {
            LocalDateTime shiftedTime = time.plusWeeks(1);
            if (shiftedTime.getMonthValue() == time.getMonthValue())
            {
                // still inside October
                return true;
            }

            final int beforeSunday = DayOfWeek.SUNDAY.getValue() - time.getDayOfWeek().getValue();
            if (0 == beforeSunday)
            {
                return (time.getHour() < 2); // moving to winter time at last Sunday, 02:00
            }
            else
            {
                // still before last Sunday
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param time
     * @return do time is Russia Summer Time
     */
    private static boolean isRussiaSummerTime(LocalDateTime time)
    {
        return true;
    }

    static int getTimeShift(LocalDateTime time, String city)
    {
        int retValue = 0;

        try
        {
            TimeZoneDesc tzd = LOCAL_TIME_SHIFTS.get(city);
            if (null == tzd)
            {
                return retValue;
            }
            IIsSummerTime ist = tzd.mIsSummerTime;
            if (null == ist)
            {
                return retValue;
            }
            retValue = ((ist.isSummerTime(time)) ? tzd.mSummerTimeShift : tzd.mWinterTimeShift);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return retValue;
    }
}
