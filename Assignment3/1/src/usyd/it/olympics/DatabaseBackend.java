package usyd.it.olympics;
 
 
/**
* Database back-end class for simple gui.
*
* The DatabaseBackend class defined in this file holds all the methods to
* communicate with the database and pass the results back to the GUI.
*
*
* Make sure you update the dbname variable to your own database name. You
* can run this class on its own for testing without requiring the GUI.
*/
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAKey;
import java.sql.*;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.*;
import java.text.*;
import java.util.concurrent.CopyOnWriteArrayList;
import oracle.jdbc.proxy.annotation.Pre;
import oracle.sql.DATE;
 
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.xml.crypto.Data;
 
/**
* Database interfacing backend for client. This class uses JDBC to connect to
* the database, and provides methods to obtain query data.
*
* Most methods return database information in the form of HashMaps (sets of
* key-value pairs), or ArrayLists of HashMaps for multiple results.
*
* @author Bryn Jeffries {@literal <bryn.jeffries@sydney.edu.au>}
*/
public class DatabaseBackend {
 
   ///////////////////////////////
   /// DB Connection details
  /// These are set in the constructor so you should never need to read or
  /// write to them yourself
   ///////////////////////////////
   private final String dbUser;
   private final String dbPass;
  private final String connstring;
 
 
   ///////////////////////////////
   /// Student Defined Functions
   ///////////////////////////////
 
   /////  Login and Member  //////
 
   /**
    * Validate memberID details
    *
    * Implements Core Functionality (a)
    *
    * @return basic details of user if username is for a valid memberID and password is correct
    * @throws OlympicsDBException
    * @throws SQLException
    */
    public HashMap<String,Object> checkLogin(String member, char[] password) throws OlympicsDBException  {
        // Note that password is a char array for security reasons.
        // Don't worry about this: just turn it into a string to use in this function
        // with "new String(password)"
        
        HashMap<String,Object> details = null;
        Connection conn = null;
        try {
            conn = getConnection();
            String sql ="SELECT m.member_id, CASE WHEN a.member_id IS NOT NULL THEN 'athlete' WHEN s.member_id IS NOT NULL THEN 'staff' WHEN o.member_id IS NOT NULL THEN 'official' END  AS member_type, pass_word FROM member m LEFT OUTER JOIN ATHLETE a ON a.member_id = m.member_id LEFT OUTER JOIN STAFF s ON s.member_id = m.member_id LEFT OUTER JOIN OFFICIAL o ON o.member_id = m.member_id WHERE m.member_id = ? AND pass_word = ?";
            String passwd = new String(password);
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, member);
            stmt.setString(2,passwd );
            
            ResultSet rs =stmt.executeQuery();
            rs.next();
            
            // finish FIXME: REPLACE FOLLOWING LINES WITH REAL OPERATION
            // Don't forget you have memberID variables memberUser available to
            // use in a query.
            // Query whether login (memberID, password) is correct...
            boolean valid = (member.equals(rs.getString(1)) && new String(password).equals(rs.getString(3)));
            if (valid) {
                details = new HashMap<String,Object>();
                
                // Populate with record data
                details.put("member_type", rs.getString(2));
            }reallyClose(conn);
        } catch (Exception e) {
//            throw new OlympicsDBException("Error checking login details", e);
        }
        finally {
            reallyClose(conn);
        }
        return details;
    }
 
   /**
    * Obtain details for the current memberID
    * @param memberID
    *
    * @return Details of member
    * @throws OlympicsDBException
    */
   public HashMap<String, Object> getMemberDetails(String memberID) throws OlympicsDBException {
 
       HashMap<String, Object> details = new HashMap<String, Object>();
       Connection conn = null;
       try {
           conn=getConnection();
           String sql ="SELECT m.member_id, CASE WHEN a.member_id IS NOT NULL THEN 'athlete' WHEN s.member_id IS NOT NULL THEN 'staff' WHEN o.member_id IS NOT NULL THEN 'Official' END AS member_type, title, given_names, family_name, COALESCE(country_name, 'UNKNOWN') AS country_name, COALESCE(place_name, 'UNKNOWN') AS residence, (SELECT COUNT(booked_for) FROM BOOKING WHERE booked_for = m.member_id) AS num_bookings, (SELECT COUNT(athlete_id) FROM PARTICIPATES WHERE medal = 'G' AND athlete_id = m.member_id) AS GOLD, (SELECT COUNT(athlete_id) FROM PARTICIPATES WHERE medal = 'S' AND athlete_id = m.member_id) AS SILVER, (SELECT COUNT(athlete_id) FROM PARTICIPATES WHERE medal = 'B' AND athlete_id = m.member_id) AS BRONZER FROM member m LEFT OUTER JOIN ATHLETE a ON a.member_id = m.member_id LEFT OUTER JOIN STAFF s ON s.member_id = m.member_id LEFT OUTER JOIN OFFICIAL o ON o.member_id = m.member_id LEFT JOIN COUNTRY c USING (country_code) LEFT JOIN PLACE p ON (m.accommodation = p.place_id) where m.member_id= ?";
           PreparedStatement stmt =conn.prepareStatement(sql);
           stmt.setString(1,memberID);
           ResultSet rs= stmt.executeQuery();
 
           rs.next();
 
           details.put("member_id", memberID);
           details.put("member_type", rs.getString(2));
           details.put("title", rs.getString(3));
           details.put("first_name", rs.getString(4));
           details.put("family_name", rs.getString(5));
           details.put("country_name", rs.getString(6));//join member nation code country
           details.put("residence", rs.getString(7));
           details.put("member_type", rs.getString(2));
           details.put("num_bookings", Integer.valueOf(rs.getString(8)));
           // Some attributes fetched may depend upon member_type
           // This is for an athlete
           details.put("num_gold", Integer.valueOf(rs.getString(9)));
           details.put("num_silver", Integer.valueOf(rs.getString(10)));
           details.put("num_bronze", Integer.valueOf(rs.getString(11)));
       }catch (Exception e){
           e.getMessage();
       }
       finally {
           reallyClose(conn);
       }
       // FIXME: REPLACE FOLLOWING LINES WITH REAL OPERATION
 
       return details;
   }
 
 
   //////////  Events  //////////
 
   /**
    * Get all of the events listed in the olympics for a given sport
    *
    * @param sportId the ID of the sport we are filtering by
    * @return List of the events for that sport
    * @throws OlympicsDBException
    */
   ArrayList<HashMap<String, Object>> getEventsOfSport(Integer sportId) throws OlympicsDBException {
       // FIXME: Replace the following with REAL OPERATIONS!
       ArrayList<HashMap<String, Object>> events = new ArrayList<>();
       Connection conn = null;
       try {
           conn = getConnection();
           String sql="select event_id,SPORT_ID,EVENT_NAME,EVENT_GENDER ,place_name,EVENT_START from event e2 left join place p on p.place_id=e2.SPORT_VENUE where SPORT_ID= ?";
           PreparedStatement stmt= conn.prepareStatement(sql);
           stmt.setInt(1,sportId);
           ResultSet rs = null;
           rs=stmt.executeQuery();
           while (rs.next()){
               HashMap<String,Object> event1 = new HashMap<String,Object>();
               event1.put("event_id", Integer.valueOf(rs.getString("event_id")));
               event1.put("sport_id", Integer.valueOf(rs.getString("sport_id")));
               event1.put("event_name",rs.getString("event_name"));
               event1.put("event_gender", rs.getString("event_gender"));
               event1.put("sport_venue",rs.getString("place_name") );
               event1.put("event_start", rs.getTimestamp("event_start"));
               events.add(event1);
           }
       }catch (Exception e){
           e.getMessage();
       }
       finally {
           reallyClose(conn);
       }
 
       return events;
   }
 
   /**
    * Retrieve the results for a single event
    * @param eventId the key of the event
    * @return a hashmap for each result in the event.
    * @throws OlympicsDBException
    */
   ArrayList<HashMap<String, Object>> getResultsOfEvent(Integer eventId) throws OlympicsDBException {
       // FIXME: Replace the following with REAL OPERATIONS!
       ArrayList<HashMap<String, Object>> results = new ArrayList<>();
       String sql="SELECT family_name ||', ' || given_names AS participant, country_name, CASE WHEN medal = 'G' THEN 'Gold' WHEN medal = 'S' THEN 'Silver' WHEN medal = 'B' THEN 'Bronze' END AS medal FROM PARTICIPATES pa LEFT JOIN MEMBER m ON (athlete_id = m.member_id) LEFT JOIN COUNTRY USING (country_code) WHERE event_id = ? ORDER BY family_name";
       //String eId = eventId.toString();
       String sql2 = "SELECT t.team_name, c.country_name,  CASE WHEN t.medal = 'G' THEN 'Gold'  WHEN t.medal = 'S' THEN 'Silver' WHEN t.medal = 'B' THEN 'Bronze' END AS medal FROM TEAM t LEFT JOIN COUNTRY c ON(t.country_code = c.country_code) WHERE t.event_id = ? ORDER BY t.team_name";
       Connection conn = null;
       try {
           conn = getConnection();
           PreparedStatement stmt= conn.prepareStatement(sql);
           stmt.setInt(1,eventId);
           ResultSet rs = stmt.executeQuery();
           while(rs.next()){
               HashMap<String,Object> result1 = new HashMap<String,Object>();
               result1.put("participant", rs.getString(1));
               result1.put("country_name",rs.getString(2));
               result1.put("medal", rs.getString(3));
               results.add(result1);
           }
           PreparedStatement stmt2 = conn.prepareStatement(sql2);
           stmt2.setInt(1,eventId);
           ResultSet rs2 = stmt2.executeQuery();
           while(rs2.next()){
               HashMap<String,Object> result2 = new HashMap<String,Object>();
               result2.put("participant", rs2.getString(1));
               result2.put("country_name",rs2.getString(2));
               result2.put("medal", rs2.getString(3));
               results.add(result2);
           }

       }catch (Exception e){
           e.getMessage();
       }finally {
       reallyClose(conn);
       }
       return results;
   }
 
 
   ///////   Journeys    ////////
 
   /**
    * Array list of journeys from one place to another on a given date
    * @param journeyDate the date of the journey
    * @param fromPlace the origin, starting place.
    * @param toPlace the destination, place to go to.
    * @return a list of all journeys from the origin to destination
    */
   ArrayList<HashMap<String, Object>> findJourneys(String fromPlace, String toPlace, Date journeyDate) throws OlympicsDBException {
       // FIXME: Replace the following with REAL OPERATIONS!
 
       DateFormat sf2= new SimpleDateFormat("dd/MMM/yy");
       String date2 = sf2.format(journeyDate);
 
       String sql="SELECT j.journey_id, vehicle_code,p1.place_name AS origin_name,p2.place_name AS dest_name,j.depart_time AS when_departs,j.arrive_time AS when_arrives,capacity - nbooked AS available_seats FROM (((JOURNEY j LEFT JOIN VEHICLE USING (vehicle_code)) left join place p1 on (j.from_place = p1.PLACE_ID)) left join place p2 on (j.TO_PLACE=p2.PLACE_ID)) where p1.place_name= ? and p2.place_name= ? and j.ARRIVE_TIME LIKE"+"'"+date2.toUpperCase()+"%'";
       ArrayList<HashMap<String, Object>> journeys = new ArrayList<>();
       Connection conn = null;
       try {
           conn = getConnection();
           PreparedStatement stmt=conn.prepareStatement(sql);
           stmt.setString(1,fromPlace);
           stmt.setString(2,toPlace);
           ResultSet rs = stmt.executeQuery();
           while (rs.next()){
               HashMap<String,Object> journey1 = new HashMap<String,Object>();
               journey1.put("journey_id", Integer.valueOf(rs.getString(1)));
               journey1.put("vehicle_code", rs.getString(2));
               journey1.put("origin_name", rs.getString(3));
               journey1.put("dest_name", rs.getString(4));
               journey1.put("when_departs", new Date(rs.getTimestamp(5).getTime()));
               journey1.put("when_arrives", new Date(rs.getTimestamp(6).getTime()));
               journey1.put("available_seats", Integer.valueOf(rs.getString(7)));
               journeys.add(journey1);
           }
       }catch (Exception e){
           e.getMessage();
       }
       finally {
           reallyClose(conn);
       }
       return journeys;
   }
 
   ArrayList<HashMap<String,Object>> getMemberBookings(String memberID) throws OlympicsDBException {
       ArrayList<HashMap<String,Object>> bookings = new ArrayList<HashMap<String,Object>>();
 
       String sql = "SELECT journey_id, vehicle_code, pf.place_name AS origin_name, pt.place_name AS dest_name, depart_time AS when_departs, arrive_time AS when_arrives FROM (JOURNEY j LEFT JOIN PLACE pf ON (j.from_place = pf.place_id) LEFT JOIN PLACE pt ON (j.to_place = pt.place_id))left join booking b using (journey_id) where b.booked_for = ?";
       // FIXME: DUMMY FUNCTION NEEDS TO BE PROPERLY IMPLEMENTED
       Connection conn = null;
       try {
           conn = getConnection();
           PreparedStatement stmt=conn.prepareStatement(sql);
           stmt.setString(1,memberID);
           ResultSet rs = stmt.executeQuery();
 
           while (rs.next()){
               HashMap<String,Object> bookingex1 = new HashMap<String,Object>();
               bookingex1.put("journey_id", Integer.valueOf(rs.getString(1)));
               bookingex1.put("vehicle_code", rs.getString(2));
               bookingex1.put("origin_name", rs.getString(3));
               bookingex1.put("dest_name", rs.getString(4));
               bookingex1.put("when_departs", new Date(rs.getTimestamp(5).getTime()));
               bookingex1.put("when_arrives", new Date(rs.getTimestamp(6).getTime()));
               bookings.add(bookingex1);
           }
       }catch (Exception e){
           e.getMessage();
       }
       finally {
           reallyClose(conn);
       }
 
       return bookings;
   }
 
   /**
    * Get details for a specific journey
    *
    * @return Various details of journey - see JourneyDetails.java
    * @throws OlympicsDBException
//     * @param journey_id
    */
   public HashMap<String,Object> getJourneyDetails(Integer jouneyId) throws OlympicsDBException {
       // FIXME: REPLACE FOLLOWING LINES WITH REAL OPERATION
 
       String sql="SELECT DISTINCT journey_id, vehicle_code, pf.place_name AS origin_name, pt.place_name AS dest_name, depart_time AS when_departs, arrive_time AS when_arrives, capacity, nbooked FROM JOURNEY j LEFT JOIN VEHICLE USING (vehicle_code) LEFT JOIN PLACE pf ON (j.from_place = pf.place_id) LEFT JOIN PLACE pt ON (j.to_place = pt.place_id) where journey_id = ?";
       HashMap<String,Object> details = new HashMap<String,Object>();
       try {
           Connection connection=getConnection();
           PreparedStatement stmt= connection.prepareStatement(sql);
           stmt.setInt(1,jouneyId);
           ResultSet rs = stmt.executeQuery();
           rs.next();
           details.put("journey_id", Integer.valueOf(rs.getString(1)));
           details.put("vehicle_code", rs.getString(2));
           details.put("origin_name", rs.getString(3));
           details.put("dest_name", rs.getString(4));
           details.put("when_departs", new Date(rs.getTimestamp(5).getTime()));
           details.put("when_arrives", new Date(rs.getTimestamp(6).getTime()));
           details.put("capacity", Integer.valueOf(rs.getString(7)));
           details.put("nbooked", Integer.valueOf(rs.getString(8)));
       }catch (Exception e){
           e.getMessage();
       }
       return details;
   }
 
   public HashMap<String,Object> makeBooking(String byStaff, String forMember, String vehicle, Date departs) throws OlympicsDBException {
       HashMap<String,Object> booking = null;


       SimpleDateFormat dateTime = new SimpleDateFormat("dd/MMM/yy hh:mm:ss");
       SimpleDateFormat dateTime2 = new SimpleDateFormat("aa");
       String Date2 = dateTime.format(departs);
       String date3 = dateTime2.format(departs);
       String sql1 = "select j.journey_id, j.depart_time, mby.family_name || ', ' || mby.given_names AS bookedby_name,mfo.family_name || ', ' || mfo.given_names AS bookedfor_name, p.place_name as departure_name, p2.place_name as dest_name, j.VEHICLE_CODE,j.NBOOKED,j.ARRIVE_TIME , (case when a.member_id is not null then 'athlete' when s.member_id is not null then 'staff' when o.member_id is not null then 'official' end) as member_type from (journey j left join place p on(p.place_id=j.from_place)) left join place p2 on(j.to_place = p2.place_id) LEFT JOIN BOOKING b ON (j.journey_id  = b.journey_id) LEFT JOIN MEMBER mby ON (mby.member_id = b.booked_by) LEFT JOIN MEMBER mfo ON (mfo.member_id = b.booked_for)left outer join athlete a on (a.member_id=b.booked_by) left join staff s on (s.member_id = b.booked_by)left join official o on (o.member_id = b.booked_by) where j.VEHICLE_CODE= ? and j.depart_time LIKE "+"'"+Date2.toUpperCase()+"%' AND j.depart_time like '%"+date3+"'";
       String sql2 ="insert into booking values(?,?,?,?)";
       Timestamp now = new Timestamp(System.currentTimeMillis());
       System.out.print(now);
       String sql3="update journey set NBOOKED=nbooked+1 where journey_id = ?";
       booking = new HashMap<String,Object>();
       Connection connection=null;
       try {
           connection = getConnection();
           PreparedStatement stmt= connection.prepareStatement(sql1);
           stmt.setString(1,vehicle);
           ResultSet rs = stmt.executeQuery();
           rs.next();
           if (rs.getString("member_type").equalsIgnoreCase("staff")){
               booking.put("vehicle", vehicle);
               System.out.print(booking);
               int journeyId= Integer.valueOf(rs.getString(1));
               booking.put("when_departs", rs.getTime(2));
               System.out.println(rs.getTime(2));
               booking.put("bookedfor_name", rs.getString(4));
               booking.put("bookedby_name", rs.getString(3));
               booking.put("when_booked", now);
               booking.put("origin_name", rs.getString(5));
               System.out.println(rs.getString(5));
               booking.put("dest_name", rs.getString(6));
               System.out.println(rs.getString(6));
               booking.put("when_arrives", rs.getTimestamp(9));
               System.out.println(rs.getTime(9));

               System.out.println(journeyId);
               rs.close();
               stmt.close();
               connection.close();

               connection=getConnection();
               Timestamp now2=new Timestamp(System.currentTimeMillis());
               PreparedStatement stmt2 = connection.prepareStatement(sql2);
               stmt2.setString(1,forMember);
               stmt2.setString(2,byStaff);
               stmt2.setTimestamp(3,now2);
               stmt2.setInt(4,journeyId);
               System.out.println(27);
               int judgeInsert= stmt2.executeUpdate();
               stmt2.close();
               System.out.println(judgeInsert);
               System.out.println(28);
               if (judgeInsert==1){
                   System.out.println(4);
                   connection.getAutoCommit();
                   connection.close();
                   connection=getConnection();
                   PreparedStatement stmt3= connection.prepareStatement(sql3);
                   System.out.println(31);
                   stmt3.setInt(1,journeyId);
                   System.out.println(3);
                   stmt3.executeQuery();
                   System.out.println(7);
                   System.out.println(5);
                   return booking;
               }else {
                   System.out.println(8);
                   connection.rollback();
                   connection.close();
                   System.out.println(9);
                   System.err.println("Could not make it.");
                   return null;
               }
           }else {
               System.err.println("Could not make it");
               return null;
           }
       }catch (Exception e){
           e.getMessage();
       }finally {
           reallyClose(connection);
       }
       return null;
       // FIXME: DUMMY FUNCTION NEEDS TO BE PROPERLY IMPLEMENTED
   }
 
   public HashMap<String,Object> getBookingDetails(String memberID, Integer journeyId) throws OlympicsDBException {
      HashMap<String,Object> booking = new HashMap<String,Object>();
      String sql = "SELECT j.journey_id, j.vehicle_code, j.depart_time AS when_departs, pt.place_name AS dest_name, pf.place_name AS origin_name, mby.family_name || ', ' || mby.given_names AS bookedby_name,  mfo.family_name || ', ' || mfo.given_names AS bookedfor_name, when_booked, j.arrive_time AS when_arrives FROM JOURNEY j LEFT JOIN VEHICLE v ON (j.vehicle_code = v.vehicle_code) LEFT JOIN BOOKING b ON (j.journey_id=b.journey_id) LEFT JOIN PLACE pf ON (j.from_place = pf.place_id) LEFT JOIN PLACE pt ON (j.to_place = pt.place_id) LEFT JOIN MEMBER mby ON (b.booked_by = mby.member_id) LEFT JOIN MEMBER mfo ON (b.booked_for = mfo.member_id) where mfo.member_id = ? and j.journey_id = ? ORDER BY mfo.family_name";
      Connection conn = null;
      try {
          conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql);
          stmt.setString(1,memberID);
          stmt.setInt(2,journeyId);
          ResultSet rs = stmt.executeQuery();
          rs.next();
           booking.put("journey_id", journeyId.toString());
           booking.put("vehicle", rs.getString(2));
           booking.put("when_departs", new Date(rs.getTimestamp(3).getTime()));
           booking.put("dest_name", rs.getString(4));
           booking.put("origin_name", rs.getString(5));
           booking.put("bookedby_name", rs.getString(6));
           booking.put("bookedfor_name", rs.getString(7));
           booking.put("when_booked", new Date(rs.getTimestamp(8).getTime()));
           booking.put("when_arrives", new Date(rs.getTimestamp(9).getTime()));
       }catch (Exception e){
          e.getMessage();
       }
      finally {
          reallyClose(conn);
       }
       // FIXME: DUMMY FUNCTION NEEDS TO BE PROPERLY IMPLEMENTED
       return booking;
   }
 
  public ArrayList<HashMap<String, Object>> getSports() throws OlympicsDBException {
     ArrayList<HashMap<String,Object>> sports = new ArrayList<HashMap<String,Object>>();
     Connection conn = null;
     try {
         conn = getConnection();
         String sql = "select * from sport";
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs= stmt.executeQuery();
         while (rs.next()){
               HashMap<String,Object> sport1 = new HashMap<String,Object>();
               sport1.put("sport_id", Integer.valueOf(rs.getString("sport_id")));
               sport1.put("sport_name", rs.getString("sport_name"));
               sport1.put("discipline", rs.getString("discipline"));
               sports.add(sport1);
           }conn.close();
       }catch (Exception e){
         e.getMessage();
       }
       finally {
           reallyClose(conn);
     }
       // FIXME: DUMMY FUNCTION NEEDS TO BE PROPERLY IMPLEMENTED
 
 
     return sports;
  }
 
 
   /////////////////////////////////////////
   /// Functions below don't need
   /// to be touched.
   ///
   /// They are for connecting and handling errors!!
   /////////////////////////////////////////
 
   /**
    * Default constructor that simply loads the JDBC driver and sets to the
    * connection details.
    *
    * @throws ClassNotFoundException if the specified JDBC driver can't be
    * found.
    * @throws OlympicsDBException anything else
    */
   DatabaseBackend(InputStream config) throws ClassNotFoundException, OlympicsDBException {
      Properties props = new Properties();
      try {
        props.load(config);
     } catch (IOException e) {
        throw new OlympicsDBException("Couldn't read config data",e);
     }
 
      dbUser = props.getProperty("username");
      dbPass = props.getProperty("userpass");
      String port = props.getProperty("port");
      String dbname = props.getProperty("dbname");
      String server = props.getProperty("address");;
     
       // Load JDBC driver and setup connection details
      String vendor = props.getProperty("dbvendor");
     if(vendor==null) {
         throw new OlympicsDBException("No vendor config data");
      } else if ("postgresql".equals(vendor)) {
         Class.forName("org.postgresql.Driver");
         connstring = "jdbc:postgresql://" + server + ":" + port + "/" + dbname;
      } else if ("oracle".equals(vendor)) {
         Class.forName("oracle.jdbc.driver.OracleDriver");
         connstring = "jdbc:oracle:thin:@" + server + ":" + port + ":" + dbname;
      } else throw new OlympicsDBException("Unknown database vendor: " + vendor);
    
     // test the connection
     Connection conn = null;
     try {
        conn = getConnection();
     } catch (SQLException e) {
        throw new OlympicsDBException("Couldn't open connection", e);
     } finally {
        reallyClose(conn);
     }
   }
 
  /**
   * Utility method to ensure a connection is closed without
   * generating any exceptions
   * @param conn Database connection
   */
  private void reallyClose(Connection conn) {
     if(conn!=null)
        try {
           conn.close();
        } catch (SQLException ignored) {}
  }
 
   /**
    * Construct object with open connection using configured login details
    * @return database connection
    * @throws SQLException if a DB connection cannot be established
    */
   private Connection getConnection() throws SQLException {
       Connection conn;
       conn = DriverManager.getConnection(connstring, dbUser, dbPass);
       return conn;
   }
 
 
  
}
 
