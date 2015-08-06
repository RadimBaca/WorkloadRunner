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

import java.util.ArrayList;
import result.ResultError;
import result.ResultGenerator;

/**
 *
 * @author Radim Baca
 */
public class SQLStatement {
    
    class IntegerList extends ArrayList<Integer> {
           
    }
    
    public static final int TYPE_QUERY = 0; // SELECT statement
    public static final int TYPE_DML = 1; // DML statement
    public static final int TYPE_COMMIT = 2; 
    public static final int TYPE_ROLLBACK = 3;
    public static final int TYPE_PROCEDURE = 4; // procedure or function call
    public static final int TYPE_DDL = 5; // ddl command
    public static final int TYPE_CONFIG = 6; // configuration command
    public static final int TYPE_UNKNOWN = 7; // the specified type was not recognized
    
    public int mId;
    public String mStatementText;
    public int mParameterCount; // number of parameters in the SQL command
    public int mNumberOfOccurencesInSessions;  
    public int mType;
    public ArrayList<String[]> mParameters = new ArrayList<String[]>();
    
    private IntegerList[] mMilisecondProcessingTime;  

    
    public SQLStatement(int id, String text, int paramCount, int type)
    {
        mId = id;
        mStatementText = text;
        mParameterCount = paramCount;
        mNumberOfOccurencesInSessions = 0;
        mType = type;
    }
    
    public static int mapCommandType(String typeName)
    {
        switch(typeName.toLowerCase())
        {
            case "dql": return TYPE_QUERY; 
            case "dml": return TYPE_DML;
            case "commit": return TYPE_COMMIT;
            case "rollback": return TYPE_ROLLBACK;
            case "procedure": return TYPE_PROCEDURE;
            case "ddl": return TYPE_DDL;
            case "tcl": return TYPE_CONFIG;
        }
        ResultGenerator.errors.add(new ResultError(ResponseTexts.CONFIGURATION_WARNING_COMMAND_TYPE_DOES_NOT_EXISTS, null, ResultError.TYPE_WARNING));                
        return -1;
    }
            
    public void addParameters(String[] param)
    {
        assert (param.length == mParameterCount);
        mParameters.add(param);
    }
    
    public void initMeasuringTime()
    {
        int measuredCommands = 1;
        if (!mParameters.isEmpty())
        {
            measuredCommands = mParameters.size();            
        }        
        mMilisecondProcessingTime = new IntegerList[measuredCommands];            
        for (int i = 0; i < measuredCommands; i++)
        {
            mMilisecondProcessingTime[i] = new IntegerList();
        }
    }
    
    /**
     * Store measured CPU time of one SQL command processing 
     * @param order Order of the parameter values in mParameters
     * @param time Measured time in milliseconds
     */
    public void addMeasuredTime(int order, int time)
    {
        mMilisecondProcessingTime[order].add(time);
    }

    public double computeAverage(int order)
    {
        int sum = 0;
        for(int v: mMilisecondProcessingTime[order])
        {
            sum += v;
        }        
        return (double)sum / (double)mMilisecondProcessingTime[order].size();
    }
 
    public double instanceCount()
    {
        return mMilisecondProcessingTime.length;
    }
    
    public double cost()
    {
        double sum = 0;
        for(int i = 0; i < mMilisecondProcessingTime.length; i++)
        {
            sum += computeAverage(i);
        }
        return sum;
    }
}
