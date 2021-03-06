package inf226.inchat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

import inf226.storage.*;
import inf226.util.*;

import java.sql.*;
import java.io.*;




public final class EventStorage
    implements Storage<Channel.Event,SQLException> {
    
    private final Connection connection;
    
    public EventStorage(Connection connection) 
      throws SQLException {
        this.connection = connection;
        connection.createStatement()
                .executeUpdate("CREATE TABLE IF NOT EXISTS Event (id TEXT PRIMARY KEY, version TEXT, channel TEXT, type INTEGER, time TEXT, FOREIGN KEY(channel) REFERENCES Channel(id) ON DELETE CASCADE)");
        connection.createStatement()
                .executeUpdate("CREATE TABLE IF NOT EXISTS Message (id TEXT PRIMARY KEY, sender TEXT, content Text, FOREIGN KEY(id) REFERENCES Event(id) ON DELETE CASCADE)");
        connection.createStatement()
                .executeUpdate("CREATE TABLE IF NOT EXISTS Joined (id TEXT PRIMARY KEY, sender TEXT, FOREIGN KEY(id) REFERENCES Event(id) ON DELETE CASCADE)");
    }
/*
    @Override
    public Stored<Channel.Event> save(Channel.Event event)
            throws SQLException {

        final Stored<Channel.Event> stored = new Stored<Channel.Event>(event);

        String sql =  "INSERT INTO Event VALUES('" + stored.identity + "','"
                + stored.version  + "','"
                + event.channel + "','"
                + event.type.code + "','"
                + event.time  + "')";
        connection.createStatement().executeUpdate(sql);
         switch (event.type) {
            case message:
                sql = "INSERT INTO Message VALUES('" + stored.identity + "','"
                                                     + event.sender + "','"
                                                     + event.message +"')";
                break;
            case join:
                sql = "INSERT INTO Joined VALUES('" + stored.identity + "','"
                                                  + event.sender +"')";

                break;
        }
        connection.createStatement().executeUpdate(sql);
        return stored;
    }

*/


    @Override
    public Stored<Channel.Event> save(Channel.Event event)
      throws SQLException {
        
        final Stored<Channel.Event> stored = new Stored<Channel.Event>(event);
        
        PreparedStatement sql = connection.prepareStatement("INSERT INTO Event VALUES(?,?,?,?,?)");
        sql.setObject(1, stored.identity);
        sql.setObject(2, stored.version);
        sql.setObject(3, event.channel);
        sql.setInt(4, event.type.code);
        sql.setObject (5, event.time);
        sql.executeUpdate();

        switch (event.type) {
            case message:
                sql = connection.prepareStatement("INSERT INTO Message VALUES(?,?,?)");
                sql.setObject(1, stored.identity);
                sql.setString(2, event.sender);
                sql.setString(3, event.message);

                break;
            case join:

                sql = connection.prepareStatement("INSERT INTO Joined VALUES(?,?)");
                sql.setObject(1, stored.identity);
                sql.setString(2, event.sender);

                break;
        }
        sql.executeUpdate();
        return stored;
    }
/*    
    @Override
    public synchronized Stored<Channel.Event> update(Stored<Channel.Event> event,
                                                     Channel.Event new_event)
            throws UpdatedException,
            DeletedException,
            SQLException {
        final Stored<Channel.Event> current = get(event.identity);
        final Stored<Channel.Event> updated = current.newVersion(new_event);
        if(current.version.equals(event.version)) {
            String sql = "UPDATE Event SET" +
                    " (version,channel,time,type) =('"
                    + updated.version  + "','"
                    + new_event.channel  + "','"
                    + new_event.time  + "','"
                    + new_event.type.code
                    + "') WHERE id='"+ updated.identity + "'";
            connection.createStatement().executeUpdate(sql);
            
        switch (new_event.type) {
            case message:
                sql = "UPDATE Message SET (sender,content)=('" + new_event.sender + "','"
                                                     + new_event.message +"') WHERE id='"+ updated.identity + "'";

                break;
            case join:
    sql = "UPDATE Joined SET (sender)=('" + new_event.sender +"') WHERE id='"+ updated.identity + "'";

                break;
}
        connection.createStatement().executeUpdate(sql);
                } else {
                throw new UpdatedException(current);
                }
                return updated;
                }
  */  
    @Override
    public synchronized Stored<Channel.Event> update(Stored<Channel.Event> event,
                                            Channel.Event new_event)
        throws UpdatedException,
            DeletedException,
            SQLException {
    final Stored<Channel.Event> current = get(event.identity);
    final Stored<Channel.Event> updated = current.newVersion(new_event);
    if(current.version.equals(event.version)) {
        
        PreparedStatement sql = connection.prepareStatement("UPDATE Event SET (version,channel,time,type) = (?,?,?,?) WHERE id=?");
        sql.setObject(1, updated.version);
        sql.setObject(2, new_event.channel);
        sql.setObject(3, new_event.time);
        sql.setInt(4, new_event.type.code);
        sql.setObject(5, updated.identity);
        sql.executeUpdate();


        switch (new_event.type) {
            case message:
                sql = connection.prepareStatement("UPDATE Message SET (sender,content)=(?,?) WHERE id=?");
                sql.setString(1, new_event.sender);
                sql.setString(2, new_event.message);
                sql.setObject(3, updated.identity);

                break;
            case join:
                sql = connection.prepareStatement("UPDATE Joined SET (sender)=(?) WHERE id=?");
                sql.setString(1, new_event.sender);
                sql.setObject(2, updated.identity);


                break;
        }
        sql.executeUpdate();
    } else {
        throw new UpdatedException(current);
    }
        return updated;
    }
/*
    @Override
    public synchronized void delete(Stored<Channel.Event> event)
            throws UpdatedException,
            DeletedException,
            SQLException {
        final Stored<Channel.Event> current = get(event.identity);
        if(current.version.equals(event.version)) {
            String sql =  "DELETE FROM Event WHERE id ='" + event.identity + "'";
            connection.createStatement().executeUpdate(sql);

        } else {
            throw new UpdatedException(current);
        }
    }
*/    
    @Override
    public synchronized void delete(Stored<Channel.Event> event)
       throws UpdatedException,
              DeletedException,
              SQLException {
        final Stored<Channel.Event> current = get(event.identity);
        if(current.version.equals(event.version)) {
        PreparedStatement sql = connection.prepareStatement("DELETE FROM Event WHERE id =?");
        sql.setObject(1, event.identity);
        sql.executeUpdate();


        } else {
        throw new UpdatedException(current);
        }
    }
/*
    @Override
    public Stored<Channel.Event> get(UUID id)
            throws DeletedException,
            SQLException {
        final String sql = "SELECT version,channel,time,type FROM Event WHERE id = '" + id.toString() + "'";

        final Statement statement = connection.createStatement();
        final ResultSet rs = statement.executeQuery(sql);

        if(rs.next()) {
            final UUID version = UUID.fromString(rs.getString("version"));
            final UUID channel =
                    UUID.fromString(rs.getString("channel"));
            final Channel.Event.Type type =
                    Channel.Event.Type.fromInteger(rs.getInt("type"));
            final Instant time =
                    Instant.parse(rs.getString("time"));

            final Statement mstatement = connection.createStatement();
            switch(type) {
                case message:
                    final String msql = "SELECT sender,content FROM Message WHERE id = '" + id.toString() + "'";

                    final ResultSet mrs = mstatement.executeQuery(msql);
                    mrs.next();
                    return new Stored<Channel.Event>(
                            Channel.Event.createMessageEvent(channel,time,mrs.getString("sender"),mrs.getString("content")),
                            id,
                            version);
                case join:
                    final String asql = "SELECT sender FROM Joined WHERE id = '" + id.toString() + "'";

                    final ResultSet ars = mstatement.executeQuery(asql);
                    ars.next();
                    return new Stored<Channel.Event>(
                            Channel.Event.createJoinEvent(channel,time,ars.getString("sender")),
                            id,
                            version);
            }
        }
        throw new DeletedException();
    }

}
*/
    @Override
    public Stored<Channel.Event> get(UUID id)
      throws DeletedException,
             SQLException {
        PreparedStatement sql = connection.prepareStatement("SELECT version,channel,time,type FROM Event WHERE id =?");
        sql.setString(1, id.toString());
        
        final ResultSet rs = sql.executeQuery();

        if(rs.next()) {
            final UUID version = UUID.fromString(rs.getString("version"));
            final UUID channel = 
                UUID.fromString(rs.getString("channel"));
            final Channel.Event.Type type = 
                Channel.Event.Type.fromInteger(rs.getInt("type"));
            final Instant time = 
                Instant.parse(rs.getString("time"));
            
            final Statement mstatement = connection.createStatement();
            switch(type) {
                case message:
                    PreparedStatement msql = connection.prepareStatement("SELECT sender,content FROM Message WHERE id =?");
                    msql.setString(1, id.toString());

                    final ResultSet mrs = msql.executeQuery();
                    mrs.next();
                    return new Stored<Channel.Event>(
                            Channel.Event.createMessageEvent(channel,time,mrs.getString("sender"),mrs.getString("content")),
                            id,
                            version);
                case join:
                    PreparedStatement asql = connection.prepareStatement("SELECT sender FROM Joined WHERE id =?");
                     asql.setString(1,id.toString());


                    final ResultSet ars = asql.executeQuery();
                    ars.next();
                    return new Stored<Channel.Event>(
                            Channel.Event.createJoinEvent(channel,time,ars.getString("sender")),
                            id,
                            version);
            }
        }
        throw new DeletedException();
    }
    
}


 
