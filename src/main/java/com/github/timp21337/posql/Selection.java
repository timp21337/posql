package com.github.timp21337.posql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.webmacro.Template;
import org.webmacro.servlet.HandlerException;
import org.webmacro.servlet.WMServlet;
import org.webmacro.servlet.WebContext;

/**
 * Plain Old SQL - issue an SQL query against a db.
 * 
 * Note that the name of the database is a logical name, 
 * so that we can refer to two databases with the same 
 * name on two different machines. 
 * 
 */
public class Selection extends WMServlet {

  public Template handle(WebContext context) throws HandlerException {
    String caption = context.getForm("caption");
    if (caption == null)
     caption =  "Plain Old SQL";
    context.put("caption", caption);
    
    context.put("hideForm", context.getForm("hideForm"));
    
    String dbLogicalName = context.getForm("db");
    if (dbLogicalName == null)
      dbLogicalName="database";
    context.put("db", dbLogicalName);
    
    String csv = context.getForm("csv");
    context.put("csv", csv);
    String templateName = csv == null ? "POSQL.wm" : "CSV.wm";
    
    String filename = context.getForm("filename");
    if (filename == null) 
      filename = "selection.csv";
    context.put("filename", filename);
    
    String query = context.getForm("query");
    //context.put("debug", "Query:" + query);
    if (query != null) {
      checkQuery(query);
      context.put("query", query);
      try {
        Connection conn = getConnection(dbLogicalName);
        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(query);
        Vector<Vector<String>> results = new Vector<Vector<String>>();
        Vector<String> titles = new Vector<String>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i < meta.getColumnCount() + 1; i++) {
          titles.addElement(csvEscaped(meta.getColumnLabel(i)));
        }
        int rowCount = 0;
        while (rs.next()) {
          rowCount++;
          Vector<String> result = new Vector<String>();
          for (int i = 1; i < meta.getColumnCount() + 1; i++) {
            String item = rs.getString(i);
            if (item == null)
              item = "";
            result.addElement(csvEscaped(item));
          }
          results.addElement(result);
        }
        context.put("titles", titles);
        context.put("results", results);
        context.put("rowCount", rowCount);
        context.put("status", "OK");
        s.close();
        conn.close();
      } catch (Exception e) {
        context.put("exception", e);
        context.put("trace", stackTraceToString(e));
      }
    } else { 
      query = "select ";
      context.put("query", query);
    }

    try {
      return (Template) context.getBroker().get("template", templateName);
    } catch (Exception e) {
      throw new HandlerException("Problem in template: " + templateName 
              + "; " + e.getMessage());
    }
  }

  
  private void checkQuery(String query) throws HandlerException {
    if (!query.trim().toUpperCase().startsWith("SELECT"))
      throw new HandlerException("Queries must start with SELECT.");
    if (query.indexOf(';') != -1)
      throw new HandlerException("No semi-colons allowed in query");      
  }

  public String stackTraceToString(Throwable e) {
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement element : e.getStackTrace()) {
      sb.append(element.toString());
      sb.append("\n");
    }
    return sb.toString();
  }

  private Connection getConnection(String dbLogicalName) {
    Connection conn = null;
    Configuration config = new Configuration("posql", dbLogicalName);
    String dbUrl = config.getSetProperty("dbUrl"); // "jdbc:mysql://localhost:3306/repository";
    String driver = config.getSetProperty("driver"); // "com.mysql.jdbc.Driver";
    String user = config.getSetProperty("user"); // "root";
    String password = config.get("password"); // optional
    try {
      Class.forName(driver).newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    try {
      conn = DriverManager.getConnection(dbUrl, user, password);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return conn;
  }

  /**
   * @param in the String to escape
   * @return the escaped String
   */
  public static String csvEscaped(String in) { 
    StringBuffer b = new StringBuffer();
    appendEscaped(b, in, '"', '"');
    return b.toString();
  }
  
  /**
   * Append a String to a StringBuffer, and escaping any occurances 
   * of the char in the String.
   * 
   * @param b the buffer to append to 
   * @param s the String to append
   * @param character the character to escape
   * @param escapeChar the character to escape with
   */
  public static void appendEscaped(StringBuffer b, 
          String s, char character, char escapeChar) {
    int l = s.length();
    for (int i = 0; i < l; ++i) {
      char c = s.charAt(i);
      if (c == escapeChar || c == character) {
        // damn, found one; catch up to here ...

        for (int j = 0; j < i; ++j)
          b.append(s.charAt(j));
        b.append(escapeChar);
        b.append(c);

        // ... and continue

        for (++i; i < l; ++i) {
          c = s.charAt(i);
          if (c == escapeChar || c == character)
            b.append(escapeChar);
          b.append(c);
        }
        return;
      }
    }

    b.append(s);
  }
  
}
