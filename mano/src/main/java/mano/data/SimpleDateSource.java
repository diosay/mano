package mano.data;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author junhwong
 */


public class SimpleDateSource implements DataSource{

    private String driverClassName="com.mysql.jdbc.Driver";
    private String url;
    private String username;
    private String password;
    private Class<?> driverType;
    private Driver driver;
    @Override
    public Connection getConnection() throws SQLException {
        return this.getConnection(this.getUsername(), this.getPassword());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
//        try {
//            System.out.println(Class.forName(this.getDriverClassName()));
//        } catch (ClassNotFoundException ex) {
//            ex.printStackTrace();
//        }
        //java.sql.Driver d;d.
        if(this.getDriverType()!=null){
            try {
                Driver driver=(Driver)this.getDriverType().newInstance();
                if(driver.acceptsURL(this.getUrl())){
                    Properties props = new Properties();
                    props.put("user", this.getUsername());
                    props.put("password", this.getPassword());
                    return driver.connect(this.getUrl(), props);
                }
                
            } catch (Exception ex) {
                throw new SQLException(ex);
            }
        }
        return DriverManager.getConnection(this.getUrl(), username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the driverClassName
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * @param driverClassName the driverClassName to set
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the driverType
     */
    public Class<?> getDriverType() {
        return driverType;
    }

    /**
     * @param driverType the driverType to set
     */
    public void setDriverType(Class<?> driverType) {
        this.driverType = driverType;
    }
    
}
