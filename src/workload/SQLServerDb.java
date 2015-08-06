/*
 * Copyright (C) 2015 Radim Baca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package workload;

import java.sql.*;
import java.lang.Thread;
import java.util.Random;
import result.ResultError;
import result.ResultGenerator;

/**
 * Creating a SQL Server connection
 *
 * @author Bača Radim
 */
public class SQLServerDb extends Thread
{
    protected Connection sConnection = null;

    protected Random rand = new Random();

    public static int type;
    public static String inputFileName;
    public static String outputFileName;
    public static String host;
    public static String dbname;
    public static String user;
    public static String pass;
    PreparedStatement pstmt;

    public void Open() throws Exception
    {
        if (sConnection == null)
        {
            String url = "jdbc:sqlserver://" + host + ";databaseName=" + dbname;
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            sConnection = DriverManager.getConnection(url, user, pass);
            sConnection.setTransactionIsolation(sConnection.TRANSACTION_SERIALIZABLE);
        }
    }

    public void Close() throws SQLException
    {
        if (sConnection != null)
        {
            sConnection.close();
            sConnection = null;
        }
    }

    /**
     * Perform one SQL command
     * @param sqlStat Object representing the SQL command
     * @param sqlParameterOrder Order of the parameters values (since there can be more than one parameter value corresponding to one SQL command)
     * @param measure Whether the user time of the SQL command should be measured or not
     */
    protected void PerformSQLCommand(SQLStatement sqlStat, int sqlParameterOrder, boolean measure) {
        try
        {
            pstmt = sConnection.prepareStatement(sqlStat.mStatementText);
            for (int i = 1; i <= sqlStat.mParameterCount; i++) {
                //System.out.println(i + ":" + sqlStat.mParameters.get(sqlParameterOrder)[i - 1]);
                pstmt.setString(i, sqlStat.mParameters.get(sqlParameterOrder)[i - 1]);
            }
            long before = System.nanoTime();
            pstmt.execute();
            long after = System.nanoTime();

            SQLWarning warning = pstmt.getWarnings();
            while (warning != null) {
                //System.out.println(warning.getMessage());
                ResultGenerator.errors.add(new ResultError(ResponseTexts.SQL_WARNING + sqlStat.mStatementText, warning.getMessage(), ResultError.TYPE_WARNING));
                warning = warning.getNextWarning();
            }
            if (measure)
            {
                sqlStat.addMeasuredTime(sqlParameterOrder, (int) ((after - before) / 1000000.0));
            }
            pstmt.clearParameters();
        } catch (SQLException e) {
            ResultGenerator.errors.add(new ResultError(ResponseTexts.SQL_EXCEPTION + sqlStat.mStatementText, e.getMessage(), ResultError.TYPE_ERROR));
            //System.out.println(e.getMessage());
        }        
    }
}