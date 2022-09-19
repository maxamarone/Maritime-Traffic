package it.ais;

import it.ais.bean.ShipReport;
import it.ais.bean.ShipTime;
import it.ais.db.ManageReportToDatabase;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTimeZone;

public class CreateReportTableDatabaseFromTime
{

   private static final long DIFF_TIME_DAYS = 1;//a day

   public static void main(String[] args)
   {
      Logger.getLogger(CreateReportTableDatabaseFromTime.class.getName()).log(Level.INFO, "Start CreateReportTableDatabase");

      ManageReportToDatabase db = new ManageReportToDatabase();

      //truncate table
      db.truncateTableReportTime();

      //distinct ships
      ArrayList<String> mmsis = db.selectDistinctShip();

      //cycle mmsis
      for (String mmsi : mmsis)
      {
         ArrayList<ShipTime> shipTimes = db.findShip(mmsi);

         //find and insert ship trips
         findAndInsertTrips(db, shipTimes);
      }

      Logger.getLogger(CreateReportTableDatabaseFromTime.class.getName()).log(Level.INFO, "End CreateReportTableDatabase");
   }

   public static void findAndInsertTrips(ManageReportToDatabase db, ArrayList<ShipTime> shipTimes)
   {
      String tstampArr = null;
      Timestamp loadingDateArr = null;
      String tstampPrev = null;
      Timestamp loadingDatePrev = null;

      //cycle shipTimes        
      for (int i = 0; i < shipTimes.size(); i++)
      {
         ShipTime shipTime = shipTimes.get(i);

         //first row
         if (i == 0)
         {
            tstampArr = shipTime.getTstamp();
            loadingDateArr = shipTime.getLoadingDate();
         }

         // 
         if (loadingDatePrev != null)
         {
            /*loading datetime */
            //long timeMS = shipTime.getLoadingDate().getTime();
            //long timePrevMS = loadingDatePrev.getTime();
            //long diffTime = TimeUnit.MILLISECONDS.toDays((timeMS - timePrevMS));
            /*tstamp datetime */
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            DateTime dtime = dtf.parseDateTime(shipTime.getTstamp().replace(" GMT", ""));
            long timeMS = dtime.getMillis();
            DateTime dtimePrev = dtf.parseDateTime(tstampPrev.replace(" GMT", ""));
            long timePrevMS = dtimePrev.getMillis();
            long diffTime = TimeUnit.MILLISECONDS.toDays((timeMS - timePrevMS));

            //check diff time 
            if (diffTime >= DIFF_TIME_DAYS)
            {
               //insert bean
               ShipReport shipReport = new ShipReport();
               shipReport.setMmsi(shipTime.getMmsi());
               shipReport.setTstampArr(tstampArr);
               shipReport.setTstampDep(tstampPrev);
               shipReport.setLoadingDateArr(loadingDateArr);
               shipReport.setLoadingDateDep(loadingDatePrev);

               //insert rows
               int rows = db.insertRowTime(shipReport);
               Logger.getLogger(CreateReportTableDatabaseFromTime.class.getName()).log(Level.INFO, "Rows inserted {0}", rows);

               //remove item already elaborated
               for (int ii = 0; ii < i; ii++)
               {
                  shipTimes.remove(0);
               }

               findAndInsertTrips(db, shipTimes);
            }//if (diffHours >= 1)
         }//if (loadingDatePrev != null)

         //set prev
         loadingDatePrev = shipTime.getLoadingDate();
         tstampPrev = shipTime.getTstamp();

         //last row
         if (i == (shipTimes.size() - 1))
         {
            /*loading datetime */
            //LocalDateTime ldt = LocalDateTime.now();
            //ZoneId zone = ZoneId.of("Europe/Rome"); 
            //ZonedDateTime zdt = ldt.atZone(zone);
            //long timeMS = zdt.toInstant().toEpochMilli();
            //long timePrevMS = loadingDatePrev.getTime();
            /*tstamp datetime */
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            DateTime dtime = DateTime.now(DateTimeZone.UTC);
            long timeMS = dtime.getMillis();
            DateTime dtimePrev = dtf.parseDateTime(tstampPrev.replace(" GMT", ""));
            long timePrevMS = dtimePrev.getMillis();
            long diffTime = TimeUnit.MILLISECONDS.toDays((timeMS - timePrevMS));

            //check diff time
            if (diffTime >= DIFF_TIME_DAYS)
            {
               //insert bean
               ShipReport shipReport = new ShipReport();
               shipReport.setMmsi(shipTime.getMmsi());
               shipReport.setTstampArr(tstampArr);
               shipReport.setTstampDep(tstampPrev);
               shipReport.setLoadingDateArr(loadingDateArr);
               shipReport.setLoadingDateDep(loadingDatePrev);

               //insert rows
               int rows = db.insertRowTime(shipReport);
               Logger.getLogger(CreateReportTableDatabaseFromTime.class.getName()).log(Level.INFO, "Rows inserted {0}", rows);
            }//if (diffHours >= 1)
         }//if (i == (shipTimes.size() - 1))             
      }//for (int i = 0; i<shipTimes.size(); i++) 
   }

}
