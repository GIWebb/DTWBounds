package Utils;

public final class OSUtils
{
   private static String OS = null;
   public static String getOsName()
   {
      if(OS == null) { OS = System.getProperty("os.name"); }
      return OS;
   }
   public static boolean isWindows()
   {
      return getOsName().startsWith("Windows");
   }
   public static String directorySep()
   {
      if (getOsName().startsWith("Windows")) return "\\";
      else return "/";
   }
}