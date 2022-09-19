package it.ais;

import it.ais.bean.ShipReport;
import it.ais.bean.ShipTime;
import it.ais.db.ManageReportToDatabase;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateReportTableDatabaseFromCoords
{

   private static final long DIFF_TIME_DAYS = 1;//a day

   public static void main(String[] args)
   {
      Logger.getLogger(CreateReportTableDatabaseFromCoords.class.getName()).log(Level.INFO, "Start CreateReportTableDatabase");

      ManageReportToDatabase db = new ManageReportToDatabase();

      //truncate table
      db.truncateTableReportCoords();

      //distinct ships
      ArrayList<String> mmsis = db.selectDistinctShip();

      //cycle mmsis
      for (String mmsi : mmsis)
      {
         ArrayList<ShipTime> shipTimes = db.findShip(mmsi);

         //find and insert ship trips
         findAndInsertTrips(db, shipTimes);
      }

      Logger.getLogger(CreateReportTableDatabaseFromCoords.class.getName()).log(Level.INFO, "End CreateReportTableDatabase");
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
            long timeMS = shipTime.getLoadingDate().getTime();
            long timePrevMS = loadingDatePrev.getTime();
            long diffTime = TimeUnit.MILLISECONDS.toDays((timeMS - timePrevMS));

            //check distance
            double[] coorsAVG = findAnchorCoordsAVG(shipTimes, i);

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
               int rows = db.insertRowCoords(shipReport);
               Logger.getLogger(CreateReportTableDatabaseFromCoords.class.getName()).log(Level.INFO, "Rows inserted {0}", rows);

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
            LocalDateTime ldt = LocalDateTime.now();
            ZoneId zone = ZoneId.of("Europe/Rome");
            ZonedDateTime zdt = ldt.atZone(zone);
            long timeMS = zdt.toInstant().toEpochMilli();
            long timePrevMS = loadingDatePrev.getTime();
            long diffTime = TimeUnit.MILLISECONDS.toDays((timeMS - timePrevMS));

            //check diff time
            if (diffTime > DIFF_TIME_DAYS)
            {
               //insert bean
               ShipReport shipReport = new ShipReport();
               shipReport.setMmsi(shipTime.getMmsi());
               shipReport.setTstampArr(tstampArr);
               shipReport.setTstampDep(tstampPrev);
               shipReport.setLoadingDateArr(loadingDateArr);
               shipReport.setLoadingDateDep(loadingDatePrev);

               //insert rows
               int rows = db.insertRowCoords(shipReport);
               Logger.getLogger(CreateReportTableDatabaseFromCoords.class.getName()).log(Level.INFO, "Rows inserted {0}", rows);
            }//if (diffHours >= 1)
         }//if (i == (shipTimes.size() - 1))             
      }//for (int i = 0; i<shipTimes.size(); i++) 
   }

   public static double[] findAnchorCoordsAVG(ArrayList<ShipTime> shipTimes, int size)
   {
      double[] coordinates = new double[2];//0: latitude, 1: longitude

      //cycle shipTimes      
      double totalLatitude = 0;
      double totalLongitude = 0;
      int sizeStopped = 0;
      for (int i = 0; i < size; i++)
      {
         ShipTime shipTime = shipTimes.get(i);
         if (shipTime.getNavstat().equals("1")
            || //at anchor
            shipTime.getNavstat().equals("5"))   //moored
         {
            sizeStopped++;

            totalLatitude += Double.parseDouble(shipTime.getLatitude());
            totalLongitude += Double.parseDouble(shipTime.getLongitude());
         }
      }

      coordinates[0] = totalLatitude / sizeStopped;
      coordinates[1] = totalLongitude / sizeStopped;

      return coordinates;
   }

}
