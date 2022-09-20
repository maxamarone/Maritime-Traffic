package it.ais;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadGZipFileFromURL
{

   @SuppressWarnings("ConvertToTryWithResources")
   public static void main(String[] args)
   {

      try
      {
         Logger.getLogger(DownloadGZipFileFromURL.class.getName()).log(Level.INFO, "Start DownloadGZipFileFromURL");

         String fileString = "AIS/files/downloaded/data.csv.gz";

         Logger.getLogger(DownloadGZipFileFromURL.class.getName()).log(Level.INFO, "URL {0}", urlString);
         Logger.getLogger(DownloadGZipFileFromURL.class.getName()).log(Level.INFO, "File {0}", fileString);

         //proxy setting
         System.setProperty("https.proxyHost", "********");
         System.setProperty("https.proxyPort", "1234");

         URL url = new URL(urlString);

         ReadableByteChannel rbc = Channels.newChannel(url.openStream());
         FileOutputStream fos = new FileOutputStream(fileString);
         fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

         fos.close();
         rbc.close();

         Logger.getLogger(DownloadGZipFileFromURL.class.getName()).log(Level.INFO, "End DownloadGZipFileFromURL");
      }
      catch (MalformedURLException ex)
      {
         Logger.getLogger(DownloadGZipFileFromURL.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (IOException ex)
      {
         Logger.getLogger(DownloadGZipFileFromURL.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

}
