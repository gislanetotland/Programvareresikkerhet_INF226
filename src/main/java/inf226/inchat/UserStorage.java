package inf226.inchat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.UUID;

import inf226.storage.*;
import inf226.util.*;

import java.sql.*;
import java.io.*;



/**
 * The UserStore stores User objects in a SQL database.
 */
public final class UserStorage
    implements Storage<User,SQLException> {
    
    final Connection connection;
    
    public UserStorage(Connection connection) 
      throws SQLException {
        this.connection = connection;
        connection.createStatement()
                .executeUpdate("CREATE TABLE IF NOT EXISTS User (id TEXT PRIMARY KEY, version TEXT, name TEXT, joined TEXT)");
    }
/*
    @Override
    public Stored<User> save(User user)
            throws SQLException {
        final Stored<User> stored = new Stored<User>(user);
        String sql =  "INSERT INTO User VALUES('" + stored.identity + "','"
                + stored.version  + "','"
                + user.name  + "','"
                + user.joined.toString() + "')";
        connection.createStatement().executeUpdate(sql);
        return stored;
      
    }
*/    
    @Override
    public Stored<User> save(User user)
      throws SQLException {
        final Stored<User> stored = new Stored<User>(user);
        PreparedStatement sql = connection.prepareStatement("INSERT INTO User VALUES(?,?,?,?)");
        sql.setObject(1, stored.identity);
        sql.setObject(2, stored.version);
        sql.setString(3, user.name);
        sql.setString(4, user.joined.toString());
        sql.executeUpdate();
        return stored;


    }

/*    
    @Override
    public synchronized Stored<User> update(Stored<User> user,
                                            User new_user)
        throws UpdatedException,
            DeletedException,
            SQLException {
        final Stored<User> current = get(user.identity);
        final Stored<User> updated = current.newVersion(new_user);
        if(current.version.equals(user.version)) {
        String sql = "UPDATE User SET" +
            " (version,name,joined) =('" 
                            + updated.version  + "','"
                            + new_user.name  + "','"
                            + new_user.joined.toString()
                            + "') WHERE id='"+ updated.identity + "'";
            connection.createStatement().executeUpdate(sql);

          
        } else {
            throw new UpdatedException(current);
        }
        return updated;
    }
*/

    @Override
    public synchronized Stored<User> update(Stored<User> user,
                                            User new_user)
            throws UpdatedException,
            DeletedException,
            SQLException {
        final Stored<User> current = get(user.identity);
        final Stored<User> updated = current.newVersion(new_user);
        if(current.version.equals(user.version)) {
            PreparedStatement sql = connection.prepareStatement("UPDATE User SET (version,name,joined) =(?,?,?) WHERE id=?");
            sql.setObject(1, updated.version);
            sql.setString(2, new_user.name);
            sql.setString(3, new_user.joined.toString());
            sql.setObject(4, updated.identity);
            sql.executeUpdate();


        } else {
            throw new UpdatedException(current);
        }
        return updated;
    }
    
/*
    @Override
    public synchronized void delete(Stored<User> user)
            throws UpdatedException,
            DeletedException,
            SQLException {
        final Stored<User> current = get(user.identity);
        if(current.version.equals(user.version)) {
            String sql =  "DELETE FROM User WHERE id ='" + user.identity + "'";
            connection.createStatement().executeUpdate(sql);

        } else {
            throw new UpdatedException(current);
        }
    }
 */
    @Override
    public synchronized void delete(Stored<User> user)
       throws UpdatedException,
              DeletedException,
              SQLException {
        final Stored<User> current = get(user.identity);
        if(current.version.equals(user.version)) {
            PreparedStatement sql = connection.prepareStatement("DELETE FROM User WHERE id =?");
            sql.setObject(1, user.identity);
            sql.executeUpdate();

        } else {
        throw new UpdatedException(current);
        }
    }
/*
    @Override
    public Stored<User> get(UUID id)
            throws DeletedException,
            SQLException {
        final String sql = "SELECT version,name,joined FROM User WHERE id = '" + id.toString() + "'";
        final Statement statement = connection.createStatement();

        final ResultSet rs = statement.executeQuery(sql);

        if(rs.next()) {
            final UUID version =
                    UUID.fromString(rs.getString("version"));
            final String name = rs.getString("name");
            final Instant joined = Instant.parse(rs.getString("joined"));
            return (new Stored<User>
                    (new User(name,joined),id,version));
        } else {
            throw new DeletedException();
        }
    }
*/
    @Override
    public Stored<User> get(UUID id)
            throws DeletedException,
            SQLException {
        final PreparedStatement sql = connection.prepareStatement("SELECT version,name,joined FROM User WHERE id =?");
        sql.setString(1, id.toString());
      


        final ResultSet rs = sql.executeQuery();

        if(rs.next()) {
            final UUID version =
                    UUID.fromString(rs.getString("version"));
            final String name = rs.getString("name");
            final Instant joined = Instant.parse(rs.getString("joined"));
            return (new Stored<User>
                    (new User(name,joined),id,version));
        } else {
            throw new DeletedException();
        }
    }

/*
    /**
     * Look up a user by their username;
     **
    public Maybe<Stored<User>> lookup(String name) {
        final String sql = "SELECT id FROM User WHERE name = '" + name + "'";

        try{
            final Statement statement = connection.createStatement();
            final ResultSet rs = statement.executeQuery(sql);
            if(rs.next())
                return Maybe.just(
                        get(UUID.fromString(rs.getString("id"))));
        } catch (Exception e) {

        }
        return Maybe.nothing();

    }

}
*/    
    /**
     * Look up a user by their username;
     **/
    public Maybe<Stored<User>> lookup(String name) throws SQLException {
        final PreparedStatement sql = connection.prepareStatement("SELECT id FROM User WHERE name =?");
            sql.setString(1, name);

        try{
            final ResultSet rs = sql.executeQuery();
            if(rs.next())
                return Maybe.just(
                    get(UUID.fromString(rs.getString("id"))));
        } catch (Exception e) {
        
        }
        return Maybe.nothing();
        
    }
    
}


