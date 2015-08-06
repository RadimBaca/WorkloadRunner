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

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import result.ResultError;
import result.ResultGenerator;
import static workload.SQLServerDb.inputFileName;

/**
 * Simulates a work of several users
 * @author Bača Radim
 */
public class Main {

    public static void main(String[] args)
    {
        try
        {
            if (args.length == 7)
            {
                SQLServerDb.type = Integer.parseInt(args[0]);     // 0 - seznam SQL příkazů, 1 - XML soubor s SQL příkazy, hodnotami a případně konfigurací sběru dat
                SQLServerDb.inputFileName = args[1];
                SQLServerDb.outputFileName = args[2];
                SQLServerDb.host = args[3];
                SQLServerDb.dbname = args[4];
                SQLServerDb.user = args[5];
                SQLServerDb.pass = args[6];
            }
            if (args.length != 7)
            {
                System.out.println(" There are seven arguments that has to be passed to the application:\n" +
"	- Type - type of an input file. 0 - stands for a plain SQL file, 1 - for an XML file\n" +
"	- InputFile - an existing file with input workload\n" +
"	- OutputFile  - a file name where the result will be serialized in XML format\n" +
"	- Instance - usually a hostname\\instanceName which specify a database system\n" +
"	- Dbname - name of the database with a data\n" +
"	- UserName - name of an existing database user with access to the database\n" +
"	- Password - password of the user");
                System.exit(0);
            }

            InitiallizeStaticClasses();
        }
        catch (NumberFormatException e)
        {
            System.out.print(e.getMessage());
            System.out.printf(ResponseTexts.INPUT_PARAMETER_IN_NOT_NUMERIC);
            System.exit(1);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.out.printf(ResponseTexts.OPEN_OUTPUT_FILE_FAILED);
            System.exit(1);
        }


        switch(SQLServerDb.type)
        {
            case 0:
                //ResultGenerator.errors.add(new ResultError("Type 0" + ResponseTexts.NOT_IMPLEMENTED_YET, null, ResultError.TYPE_ERROR));
                InputWorkloadIsText();
                break;
            case 1:
                InputWorkloadIsXML();
                break;
        }
              
        ResultGenerator.GenerateXMLFile();
        //System.out.println("Workload end");

    }

    private static void InitiallizeStaticClasses() throws FileNotFoundException {
        ResultGenerator.Init(SQLServerDb.outputFileName);
        Configuration.Init();
    }


    private static void InputWorkloadIsText() {
        ParseInputTextFile();
        Configuration.ImplicitTextConfiguration();
        WarmUp();
        
        for (SQLStatement stmt: Support.sqlStatements)
        {
            stmt.initMeasuringTime();
        }
        
        PerformSQLCommands();
    }
    
    private static void InputWorkloadIsXML() {
        ParseInputXMLFile();
        Configuration.CheckConfiguration();
        WarmUp();
        
        for (SQLStatement stmt: Support.sqlStatements)
        {
            stmt.initMeasuringTime();
        }
        
        PerformSQLCommands();
    }

    private static void PerformSQLCommands() {
        ArrayList<Thread> threads = new ArrayList<Thread>();
        if ("commands".equals(Configuration.perform))
        {
            Thread t = new CommonUser(false, null, Support.sqlStatements);
            threads.add(t);
            t.start();
        }
        
        if ("sessions".equals(Configuration.perform))
        {
            Support.canNotConnectToDatabase = false;
            //System.out.println("Workload start");
            
            for (SQLSession session : Support.sessions) {
                Thread t = new CommonUser(false, session, Support.sqlStatements);
                threads.add(t);
                t.start();
                if (Support.canNotConnectToDatabase)
                {
                    System.exit(1);
                }
            }
        }
        
        for (Thread t : threads)
        {
            try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                ResultGenerator.errors.add(new ResultError(SQLServerDb.inputFileName + ResponseTexts.ERROR_WAITING_FOR_A_THREAD_TO_FINNISH, ex.getMessage(), ResultError.TYPE_ERROR));
            }
        }
    }

     private static void ParseInputTextFile() {        
         BufferedReader br = null; 
        try {
            String sCurrentLine;
            String currentStatement = "";
            int id = 0;
            br = new BufferedReader(new FileReader(SQLServerDb.inputFileName));
            while ((sCurrentLine = br.readLine()) != null) {
                //System.out.println(sCurrentLine);
                if (sCurrentLine.trim().isEmpty())
                {
                    if (!currentStatement.isEmpty())
                    {
                        Support.sqlStatements.add(new SQLStatement(id, currentStatement, 0, SQLStatement.TYPE_QUERY));
                        currentStatement = "";
                        id++;
                    }
                } else
                {
                    currentStatement = currentStatement + ' ' + sCurrentLine;
                }
            }
        } catch (FileNotFoundException ex) {
            ResultGenerator.errors.add(new ResultError(SQLServerDb.inputFileName + ResponseTexts.FILE_NOT_FOUND_ERROR, ex.getMessage(), ResultError.TYPE_SEVERE_ERROR));
            ResultGenerator.GenerateXMLFile();
            System.exit(1);
        } catch (IOException ex) {
            ResultGenerator.errors.add(new ResultError(SQLServerDb.inputFileName + ResponseTexts.FILE_NOT_FOUND_ERROR, ex.getMessage(), ResultError.TYPE_SEVERE_ERROR));
            ResultGenerator.GenerateXMLFile();
            System.exit(1);

        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                ResultGenerator.errors.add(new ResultError(SQLServerDb.inputFileName + ResponseTexts.ERROR_WHEN_CLOSING_INPUT_FILE, ex.getMessage(), ResultError.TYPE_SEVERE_ERROR));
                ResultGenerator.GenerateXMLFile();
                System.exit(1);
            }
        }
     }   
    
    /**
     * Read file specified in @SQLServerDb.inputFileName and store the informations in
     * Support.sqlStatements and Support.sessions
     */
    private static void ParseInputXMLFile() {
        try
        {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(new SAXHandler());
            // TODO - kontrola, ze soubor existuje
            xmlReader.parse(convertToFileURL(SQLServerDb.inputFileName));

            System.out.println(SQLServerDb.inputFileName + ResponseTexts.SUCCESFULY_PARSED);
        }
        catch (ParserConfigurationException e) {
            System.out.print(e.getMessage());
            System.exit(1);
        }
        catch (SAXException e) {
            //System.out.print(e.getMessage());
            ResultGenerator.errors.add(new ResultError(SQLServerDb.inputFileName + ResponseTexts.ERROR_INPUT_XML_FILE_NOT_WELL_FORMED, e.getMessage(), ResultError.TYPE_SEVERE_ERROR));
            ResultGenerator.GenerateXMLFile();
            System.exit(1);
        } catch (IOException e) {
            ResultGenerator.errors.add(new ResultError(SQLServerDb.inputFileName + ResponseTexts.FILE_NOT_FOUND_ERROR, e.getMessage(), ResultError.TYPE_SEVERE_ERROR));
            ResultGenerator.GenerateXMLFile();
            System.exit(1);
        }
    }

    private static void WarmUp()
    {
        if (Configuration.perform_warm_up)
        {
            if (Configuration.warm_up_run_all_sessions)
            {
                ResultGenerator.errors.add(new ResultError("run_all_sessions" + ResponseTexts.NOT_IMPLEMENTED_YET, null, ResultError.TYPE_ERROR));
            }
            if (Configuration.warm_up_randomly_select != -1)
            {
                try {
                    Thread t = new CommonUser(true, null, Support.sqlStatements);                
                    t.start();
                    t.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    ResultGenerator.errors.add(new ResultError(SQLServerDb.inputFileName + ResponseTexts.ERROR_WAITING_FOR_A_THREAD_TO_FINNISH, ex.getMessage(), ResultError.TYPE_ERROR));
                }
            }
        }
    }


    private static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }


}
