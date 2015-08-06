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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import result.ResultError;
import result.ResultGenerator;

/**
 * Class performs SQL commands. It has two basic mode: warm-up and measured.
 * It can process SQL commands ad-hoc or in a specified order (session).
 * Behavior of this class is mainly driven by the static Configuration class.
 * 
 * @author Bača Radim
 */
public class CommonUser extends SQLServerDb {

    Statement stmt;
    int maxUsers;    
    Random rnd = new Random();
    java.util.Date today = new java.util.Date();
    boolean runWarmUp;
    SQLSession session;
    ArrayList<SQLStatement> sqlStatements;

    /**
     * @param pRunWarmUp if it is true then CommonUser perform warm-up instead of measuring the SQL command performance
     * @param psession ArrayList that store triplets (sql Id, param order, start
     * time in seconds)
     * @param psqlStatements ArrayList that store SQL statements and their
     * parameters
     */
    public CommonUser(boolean pRunWarmUp, SQLSession psession, ArrayList<SQLStatement> psqlStatements) {
        try {
            Open();
        } catch (Exception e) {
            ResultGenerator.errors.add(new ResultError(ResponseTexts.CONNECTION_FAILED, null, ResultError.TYPE_SEVERE_ERROR));                
            Logger.getLogger(CommonUser.class.getName()).log(Level.SEVERE, "Opening database failed", e);
            System.out.println(e.getMessage());
            Thread.currentThread().interrupt();
        }
        runWarmUp = pRunWarmUp;
        session = psession;
        sqlStatements = psqlStatements;
      
    }

    /**
     * Starting thread method
     */
    @Override
    public void run() {
        if (runWarmUp)
        {
            if (session == null) {
                runWarmUpCommands();            
            } else {
                // TODO - sessions
            }            
        } else
        {
            if (session == null) {
                runMeasuredCommands();            
            } else {
                runSession();
            }
        }
    }

    /**
     * Method runs session stored in the session variable
     */
    private void runSession() {
        long time = System.nanoTime();
        long wait;
        long actualTime;
        SQLStatement sqlStat;
        Iterator<int[]> it = session.statements.iterator();
        int[] actualStatement = it.next();
        
        while (it.hasNext()) {
            if (System.nanoTime() - time > actualStatement[2] * 1E9) {
                int sqlStatementOrder = actualStatement[0] - 1;
                int sqlParameterOrder = actualStatement[1] - 1;
                sqlStat = sqlStatements.get(sqlStatementOrder);
                PerformSQLCommand(sqlStat, sqlParameterOrder, true);
                //System.out.println("Session> " + session.id + " SQL> " + sqlParameterOrder);
                
                actualStatement = it.next();
                try {
                    actualTime = (long) ((System.nanoTime() - time) / 1E6);
                    wait = (long) (actualStatement[2] * 1E3 - actualTime);
                    if (wait > 0) {
                        Thread.sleep(wait);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(CommonUser.class.getName()).log(Level.SEVERE, "Sleep failed in CommonUser.Run", ex);
                }
            }
        }
    }

    /**
     * Run all runable commands
     */
    private void runMeasuredCommands() {
        int countStatements = CountRunableStatements();
        countStatements *= Configuration.repeat;
        runSpecifiedNumberOfCommands(countStatements, Configuration.repeat, countStatements);           
    }
   
    /**
     * Runs the specified number of warmup commands.
     * The number of commands to run is in Configuration.warm_up_randomly_select variable.
     */
    private void runWarmUpCommands() {
        int repeatEachCommand = 1;
        int countStatements = CountRunableStatements();
        if (Configuration.warm_up_randomly_select > countStatements)
        {
            repeatEachCommand = Configuration.warm_up_randomly_select / countStatements + 1;
        }
        countStatements *= repeatEachCommand;
        assert(Configuration.warm_up_randomly_select < countStatements);        
        int numberOfCommandsToRun = Configuration.warm_up_randomly_select;
        runSpecifiedNumberOfCommands(countStatements, repeatEachCommand, numberOfCommandsToRun);        
    }

    /**
     * Prepare an array of SQL commands, randomize them if required, and perform them.
     * @param countStatements Number of SQL commands that will be prepared in the array
     * @param repeatEachCommand How many times repeat each SQL command.
     * @param numberOfCommandsToRun Number of SQL commands that have to be performed.
     */
    private void runSpecifiedNumberOfCommands(int countStatements, int repeatEachCommand, int numberOfCommandsToRun) {
        SQLStatement sqlStat;
        int[][] commands = new int[countStatements][2];
        int counter = 0;
        int i = 0;
        for (SQLStatement s : sqlStatements) {
            if (Configuration.shouldProcessThisType(s.mType))
            {            
                if (s.mParameters.isEmpty()) {
                    for (int m = 0; m < repeatEachCommand; m++)
                    {
                        commands[counter][0] = i;
                        commands[counter][1] = 0;
                        counter++;
                    }
                } else {
                    for (int j = 0; j < s.mParameters.size(); j++) {
                        for (int m = 0; m < repeatEachCommand; m++)
                        {
                            commands[counter][0] = i;
                            commands[counter][1] = j;
                            counter++;
                        }
                    }
                }
            }
            i++;
        }
        // randomize commands in array
        if ("random".equalsIgnoreCase(Configuration.order_of_commands))
        {
            RandomizeCommandsArray(countStatements, commands);
        }
        // run all commands in array
        for (i = 0; i < numberOfCommandsToRun; i++) {
            int sqlStatementOrder = commands[i][0];
            int sqlParameterOrder = commands[i][1];
            sqlStat = sqlStatements.get(sqlStatementOrder);
            PerformSQLCommand(sqlStat, sqlParameterOrder, !runWarmUp);
        }
    }
    
    
    

    /**
     * Method count SQL commands in sqlStatements variable and skip all statements that should not be processed.
     * @return Number of SQL commands in sqlStatements variable that should be performed
     */
    private int CountRunableStatements() {
        int countStatements = 0;
        for (SQLStatement s : sqlStatements) {
            if (Configuration.shouldProcessThisType(s.mType))
            {
                if (s.mParameters.isEmpty()) {
                    countStatements++;
                } else {
                    countStatements += s.mParameters.size();
                }
            }
        }
        return countStatements;
    }
    
    /**
     * Randomize the commands arrays
     * @param countStatements Size of the commands array
     * @param commands Array of SQL command indexes
     */
    private void RandomizeCommandsArray(int countStatements, int[][] commands) {
        int[] aux;
        for (int i = 0; i < countStatements; i++) {
            int first = i;
            int second = rnd.nextInt(countStatements);
            aux = commands[first];
            commands[first] = commands[second];
            commands[second] = aux;
        }
    }

}
