import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.net.InetAddress;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Arrays;

public class nick_bot {

  private static ArrayList <commandMore> commandsMore = new ArrayList <commandMore> ();
  private static ArrayList <command>     commands     = new ArrayList <command>     ();
  private static ArrayList <String>      operators    = new ArrayList <String>      ();
  private static ArrayList <String>      IRC          = new ArrayList <String>      ();
  private static ArrayList <String>      OREBuild     = new ArrayList <String>      ();
  private static ArrayList <String>      ORESchool    = new ArrayList <String>      ();
  private static ArrayList <String>      ORESurvival  = new ArrayList <String>      ();
  private static ArrayList <String>      ORESkyblock  = new ArrayList <String>      ();
  private static ArrayList <String>      Servers      = new ArrayList <String>      ();

  // Fetching global variables from "settings.properties"
  private static Properties settings = new Properties();
  static{
    try {
      InputStream input = new FileInputStream("includes/settings.properties");
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
    assembleOPs();
    assembleCommands();
    bot.connect();
    listener();
  }

  // KeepAlive
  public static void keepAlive(String line) {
    bot.sendRaw("PONG " + line.substring(5));
  }

  public static boolean containsCommand(String line) {
    if (line.contains("`")) {
      String temp = line.substring(line.indexOf("`")+1);
      for (int i = 0; i<commands.size(); i++) {
        command comm = commands.get(i);
        if (comm.getCommand().equals(temp)) {
          return true;
        }
      }
    } else {
      return false;
    }
    return false;
  }

  public static ArrayList<String> getVals(String line) {
    String temp = line.substring(line.indexOf("`")+1);
    for (int i = 0; i<commands.size(); i++) {
      command comm = commands.get(i);
      if (comm.getCommand().equals(temp)) {
        return comm.getVals();
      }
    }
    return operators;
  }

  // Listener
  public static void listener() {
    String line = null;
    while ((line = bot.readLine( )) != null) {
      if (line.contains("PING")) {
        keepAlive(line);
      // Basic commands
      } else if (containsCommand(line)) {
        commandParser comm = new commandParser(line);
        ArrayList<String> vals = getVals(line);
        for (int i = 0; i < vals.size(); i++) {
          sendUser(comm, vals.get(i));
        }
      // Complicated commands
      } else if (line.contains("`list")) {
        commandParser comm = new commandParser(line);
        assembleUsers();
        sendUser(comm, OREBuild.toString());
        sendUser(comm, ORESchool.toString());
        sendUser(comm, ORESurvival.toString());
        sendUser(comm, ORESkyblock.toString());
        sendUser(comm, IRC.toString());
      } else if (line.contains("`sudo")) {
        commandParser command = new commandParser(line);
        if (operators.contains(command.getUser())) {
          bot.sendRaw(command.getPostCommand());
        } else {
          sendUser(command, "You are not authorized!");
        }
      } else if (line.contains("`staff")) {
        commandParser command = new commandParser(line);
        postSlack("@channel " + command.getUser() + " (" + command.getService() + "): " + command.getPostCommand());
      } else if (line.contains("`quit")) {
        commandParser command = new commandParser(line);
        if (operators.contains(command.getUser())) {
          quit();
          break;
        } else {
          sendUser(command, "You are not authorized!");
        }
      } else {
        System.out.println(line);
      }
    }
  }

  public static void sendUser(commandParser command, String line) {
    if (command.getService()=="IRC") {
      bot.sendRaw("PRIVMSG " + command.getUser() + " " + line);
    } else {
      bot.sendRaw("PRIVMSG " + command.getService() + " /msg " + command.getUser() + " " + line);
    }
  }

  // Method to gracefully shutdown the bot
  public static void quit() {
    bot.sendRaw("QUIT Time for me to head out!");
  }

  // Generates the list of users across IRC and the servers
  public static void assembleUsers() {
    Servers.clear();
    String line = null;

    bot.sendRaw("PRIVMSG OREBuild /list");
    if ((line = bot.readLine( )) != null) {
      if (line.contains("No such nick")) {
        OREBuild.clear();
        OREBuild.add("Server offline");
      } else {
        line.replaceAll("\\s+", "");
        OREBuild = new ArrayList<String>(Arrays.asList(line.substring(line.lastIndexOf(": ") + 1).split(", ")));
      }
    }

    bot.sendRaw("PRIVMSG ORESchool /list");
    if ((line = bot.readLine( )) != null) {
      if (line.contains("No such nick")) {
        ORESchool.clear();
        ORESchool.add("Server offline");
      } else {
        line.replaceAll("\\s+", "");
        ORESchool = new ArrayList<String>(Arrays.asList(line.substring(line.lastIndexOf(": ") + 1).split(", ")));
      }
    }

    bot.sendRaw("PRIVMSG ORESurvival /list");
    if ((line = bot.readLine( )) != null) {
      if (line.contains("No such nick")) {
        ORESurvival.clear();
        ORESurvival.add("Server offline");
      } else {
        line.replaceAll("\\s+", "");
        ORESurvival = new ArrayList<String>(Arrays.asList(line.substring(line.lastIndexOf(": ") + 1).split(", ")));
      }
    }

    bot.sendRaw("PRIVMSG ORESkyblock /list");
    if ((line = bot.readLine( )) != null) {
      if (line.contains("No such nick")) {
        ORESkyblock.clear();
        ORESkyblock.add("Server offline");
      } else {
        line.replaceAll("\\s+", "");
        ORESkyblock = new ArrayList<String>(Arrays.asList(line.substring(line.lastIndexOf(": ") + 1).split(", ")));
      }
    }

    bot.sendRaw("NAMES " + settings.getProperty("channel") + "\r\n");
    //while ((line = bot.readLine()) != null) {
      //if (line.contains("353 " + settings.getProperty("nick"))) {
    if ((line = bot.readLine( )) != null) {
      IRC = new ArrayList<String>(Arrays.asList(line.substring(line.lastIndexOf(":") + 1).split(" ")));
//        break;
    }
  }

  // URL Shortener
  public static String shorten(String message) {
    String URL = message.substring(message.indexOf("http"));
    String domain = URL.substring(URL.indexOf("/") + 2);
    if (domain.indexOf("/") != -1) {
      domain = domain.substring(0, domain.indexOf("/"));
    }
    System.out.println(domain);
    String shortenedURL = null;
    boolean reachable = false;
    try {
      reachable = InetAddress.getByName(domain).isReachable(500);
    } catch (Exception e) {
      System.out.println(e);
    }
    if (reachable) {
      try {
        if (URL.contains(" ")) {
          URL = URLEncoder.encode(URL.substring(0, URL.indexOf(" ")), "UTF-8");
        } else {
          URL = URLEncoder.encode(URL, "UTF-8");
        }
      } catch (Exception e) {
        System.out.println(e);
      }

      HttpURLConnection conn = null;
      try {
        URL url = new URL("https://api-ssl.bitly.com/v3/shorten?access_token=" + settings.getProperty("bitly") + "&longUrl=" + URL + "&format=txt");
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        shortenedURL = in.readLine();
        in.close();
      } catch (Exception e) {
        System.out.println(e);
      }
    }
    return shortenedURL;
  }
  // Simple method to send a Slack message to the specified channel
  public static void postSlack(String message) {
    String input = "payload={\"channel\": \"#botspam\", \"username\": \"nick_bot\", \"text\": \"" + message.replaceAll(settings.getProperty("allowedChars"), " ") + "\", \"icon_emoji\": \":robot_face:\"}";

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

  // Assembles all operators from operators.txt
  public static void assembleOPs () {
    try {
      String operator;
      BufferedReader in = new BufferedReader(new FileReader("includes/operators.txt"));

      while ((operator = in.readLine()) != null) {
          operators.add(operator);
      }

      in.close();

      System.out.println("LOADED OPERATORS: " + operators.toString());

    } catch (IOException e) {
      System.out.println(e);
    }
  }

  // Assembles all commands from commands.txt
  public static void assembleCommands () {
    try {
      String command;
      BufferedReader in = new BufferedReader(new FileReader("includes/commands.txt"));
      int id = 0;
      while ((command = in.readLine()) != null) {
        id++;
        String com = command.substring(0, command.indexOf("="));
        ArrayList<String> val = new ArrayList<String>(Arrays.asList(command.substring(command.indexOf("=") +1).split(",")));
        command comm = new command(id, com, val);
        commands.add(comm);
        System.out.println("LOADED COMMAND: " + comm.toString());
      }

      in.close();

    } catch (Exception e) {
      System.out.println(e);
    }
  }
}