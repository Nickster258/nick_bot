import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Arrays;

public class bot {

  private static ArrayList <String> IRC = new ArrayList <String> ();
  private static ArrayList <String> OREBuild = new ArrayList <String> ();
  private static ArrayList <String> ORESchool = new ArrayList <String> ();
  private static ArrayList <String> ORESurvival = new ArrayList <String> ();
  private static ArrayList <String> ORESkyblock = new ArrayList <String> ();
  private static ArrayList <String> Servers = new ArrayList <String> ();

  // Fetching global variables from "settings.properties"
  private static Properties settings = new Properties();
  static{
    try {
      InputStream input = new FileInputStream("settings.properties");
      settings.load(input);
      input.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }
  public static String getProperty (String key) {
    return settings.getProperty(key);
  }

  // Setting bot
  private static IRCBot bot = new IRCBot(settings.getProperty("server"), Integer.parseInt(settings.getProperty("port")), settings.getProperty("nick"), settings.getProperty("channel"), settings.getProperty("pass"));

  // Main
  public static void main(String[] args) {
    bot.connect();
    listener();
  }

  // Listener / stayAlive
  public static void listener() {
    String line = null;
    while ((line = bot.readLine( )) != null) {
      if (line.contains("PING")) {
        bot.sendRaw("PONG " + line.substring(5) + "\r\n");
      } else if (line.contains("`staff")) {
        commandParser command = new commandParser(line);
        System.out.println(line.indexOf(":08"));
        System.out.println(command.toString());
        callStaffSlack("@channel " + command.getUser() + " (" + command.getService() + "): " + command.getPostCommand());
      } else if (line.contains(" joined the game for the very first time.")) {
        System.out.println(line);
        bot.sendRaw("PRIVMSG #openredstone Welcome to ORE! Read /motd to get started!");
      } else {
        System.out.println(line);
      }
    }
  }

  // Testing if IRC user is online, mainly for use with servers
  public static boolean isOnline(String user) {
    bot.sendRaw("ISON " + user + "\r\n");
    String line = bot.readLine();
    if (line.contains(":" + user)) {
      return true;
    }
    return false;
  }

  // Fetches the user list from the servers
  public static ArrayList<String> assembleServerUsers(String server) {
    bot.sendUser(server, "/list");
    ArrayList<String> temp = new ArrayList<String> ();
    String line = null;
    while ((line = bot.readLine()) != null) {
      if (line.contains("PRIVMSG " + settings.getProperty("nick"))) {
        temp = new ArrayList<String>(Arrays.asList(line.substring(line.lastIndexOf(": ") + 1).split(", ")));
        break;
      }
    }
    return temp;
  }

  // Generates the list of users across IRC and the servers
  public static void assembleUsers() {
    Servers.clear();
    boolean buildStatus = isOnline("OREBuild");
    boolean schoolStatus = isOnline("ORESchool");
    boolean survivalStatus = isOnline("ORESurvival");
    boolean skyblockStatus = isOnline("ORESkyblock");

    if (buildStatus) {
      OREBuild = assembleServerUsers("OREBuild");
      Servers.add("OREBuild");
    } else {
      OREBuild.clear();
      OREBuild.add("Server not online");
    }

    if (schoolStatus) {
      ORESchool = assembleServerUsers("ORESchool");
      Servers.add("ORESchool");
    } else {
      ORESchool.clear();
      ORESchool.add("Server not online");
    }

    if (survivalStatus) {
      ORESurvival = assembleServerUsers("ORESurvival");
      Servers.add("ORESurvival");
    } else {
      ORESurvival.clear();
      ORESurvival.add("Server not online");
    }

    if (skyblockStatus) {
      ORESkyblock = assembleServerUsers("ORESkyblock");
      Servers.add("ORESkyblock");
    } else {
      ORESkyblock.clear();
      ORESkyblock.add("Server not online");
    }

    bot.sendRaw("NAMES " + settings.getProperty("channel") + "\r\n");
    String line = null;
    while ((line = bot.readLine()) != null) {
      if (line.contains("353 " + settings.getProperty("nick"))) {
        IRC = new ArrayList<String>(Arrays.asList(line.substring(line.lastIndexOf(":") + 1).split(" ")));
        break;
      }
    }
  }

  public static void callStaffSlack(String message) {

    String input = "payload={\"channel\": \"#botspam\", \"username\": \"nick_bot\", \"text\": \"" + message.replaceAll("[^!:(){}'`,._@A-Za-z0-9]", " ") + "\", \"icon_emoji\": \":robot_face:\"}";

    HttpsURLConnection conn = null;
    try {
      URL url = new URL(settings.getProperty("slackURL"));
      conn = (HttpsURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestMethod("POST");

      OutputStream stream = conn.getOutputStream();
      stream.write(input.getBytes());
      stream.flush();

      if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
      }

      BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

      conn.disconnect();

    } catch (MalformedURLException e) {
      System.out.println(e);
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public static String commandHandler(String raw) {
    return "shit";
  }
}
