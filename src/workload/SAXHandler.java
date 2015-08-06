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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import result.ResultError;
import result.ResultGenerator;

/**
 *
 * @author Radim Baca
 */
public class SAXHandler extends DefaultHandler {
    
    private int order;
    private String tempValue;
    private SQLStatement lastStatement;
    private SQLSession lastSession;
    private String[] values;
    private int sessionId = 0;
    
//    public void startDocument() throws SAXException {
//    }
//
//    public void endDocument() throws SAXException { 
//    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName, 
                             Attributes atts)
        throws SAXException {

        if (qName.equalsIgnoreCase("sql"))
        {
               int id = Integer.parseInt(atts.getValue("id")); 
               int paramCount = Integer.parseInt(atts.getValue("paramcount")); 
               int type = SQLStatement.mapCommandType(atts.getValue("type"));
//               switch(atts.getValue("type"))
//               {
//                   case "query": type = SQLStatement.TYPE_QUERY; break;
//                   case "dml": type = SQLStatement.TYPE_DML; break;
//                   case "commit": type = SQLStatement.TYPE_COMMIT; break;
//                   case "rollback": type = SQLStatement.TYPE_ROLLBACK; break;
//                   case "procedure": type = SQLStatement.TYPE_PROCEDURE; break;
//                   case "ddl": type = SQLStatement.TYPE_DDL; break;
//                   case "config": type = SQLStatement.TYPE_CONFIG; break;
//               }
               SQLStatement stmt = new SQLStatement(id, atts.getValue("text"), paramCount, type);
               Support.sqlStatements.add(stmt);
               lastStatement = stmt;
               values = new String[paramCount];
               order = 0;
        }
        if (qName.equalsIgnoreCase("session"))
        {
            int startAt = parseTime(atts.getValue(0));
            lastSession = new SQLSession(startAt, sessionId++);
        }
        if (qName.equalsIgnoreCase("statement"))
        {
            int[] stmt = new int[3];
            stmt[0] = Integer.parseInt(atts.getValue(0));
            stmt[1] = Integer.parseInt(atts.getValue(1));
            stmt[2] = parseTime(atts.getValue(2));
            lastSession.statements.add(stmt);
            Support.getStatement(stmt[0]).mNumberOfOccurencesInSessions++;
        }
        
        // configuration constants
        if (qName.equalsIgnoreCase("warm_up"))
        {
            if (Configuration.perform_warm_up)
            {
                ResultGenerator.errors.add(new ResultError(ResponseTexts.ELEMENT + "warm_up" + ResponseTexts.ALREADY_DEFINED, null, ResultError.TYPE_WARNING));
            }
            Configuration.perform_warm_up = true;
            if (atts.getValue("randomly_select") != null)
            {
                Configuration.warm_up_randomly_select = Integer.parseInt(atts.getValue("randomly_select")); 
            }
            if (atts.getValue("run_all_sessions") != null)
            {
                Configuration.warm_up_run_all_sessions = true; 
            }
        }
        if (qName.equalsIgnoreCase("compute_mean"))
        {
            Configuration.compute_mean = true;
        }         
        if (qName.equalsIgnoreCase("compute_average"))
        {
            Configuration.compute_average = true;
        }        
        if (qName.equalsIgnoreCase("time_for_each_statement"))
        {
            Configuration.time_for_each_statement = true;
        } 
        if (qName.equalsIgnoreCase("omit_cost"))
        {
            Configuration.omit_cost = true;
        } 
    }

    private int parseTime(String timeString) throws NumberFormatException {
        String[] numbers = timeString.split(":");
        return Integer.parseInt(numbers[0]) * 3600 + Integer.parseInt(numbers[1]) * 60 + (int)Float.parseFloat(numbers[2]);
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException {
            tempValue = new String(ch,start,length);
    }

    public void endElement(String uri, 
            String localName, 
            String qName) 
            throws SAXException {

        if (qName.equalsIgnoreCase("values"))
        {
            lastStatement.addParameters(values);
            order = 0;
            values = new String[values.length];
        }
        if (qName.equalsIgnoreCase("p"))
        {
            values[order++] = tempValue;
        }
        if (qName.equalsIgnoreCase("session"))
        {
            Support.sessions.add(lastSession);
        }
        
        // configuration constants
        if (qName.equalsIgnoreCase("repeat"))
        {
            Configuration.repeat = Integer.parseInt(tempValue);
            if (Configuration.repeat < 1 || Configuration.repeat > 20)
            {
                if (Configuration.repeat < 1)
                {
                    Configuration.repeat = 1;
                }
                if (Configuration.repeat > 20)
                {
                    Configuration.repeat = 20;
                }
                ResultGenerator.errors.add(new ResultError(ResponseTexts.CONFIGURATION_WARNING_REPEAT + Configuration.repeat, null, ResultError.TYPE_WARNING));                
            }
        }      
        if (qName.equalsIgnoreCase("perform"))
        {
            Configuration.perform = tempValue;
        }        
        if (qName.equalsIgnoreCase("order_of_commands"))
        {
            if (!"random".equalsIgnoreCase(tempValue) && !"sequential".equalsIgnoreCase(tempValue))
            {
                ResultGenerator.errors.add(new ResultError(ResponseTexts.CONFIGURATION_WARNING_ORDER_OF_COMMANDS, null, ResultError.TYPE_WARNING));                
                Configuration.order_of_commands = "random";
            } else
            {
                Configuration.order_of_commands = tempValue;
            }
        }  
        if (qName.equalsIgnoreCase("command_type"))
        {
            Configuration.perform = tempValue;
        }        
    }

}