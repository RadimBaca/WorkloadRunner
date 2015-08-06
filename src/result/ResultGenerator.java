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
package result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import workload.Configuration;
import workload.SQLStatement;
import workload.Support;

/**
 * Object for generating the result XML file according to the result of the workload run.
 * 
 * @author Radim Baca
 */
public class ResultGenerator {
    public static ArrayList<ResultError> errors = new ArrayList<ResultError>(); // set of errors that occured the workload run
    
    public static String fileName;
    public static OutputStream outputStream = null;    
            
    static public void Init(String pFileName) throws FileNotFoundException
    {
        // TODO - co když nepůjde vytvořit daný soubor?
        outputStream = new FileOutputStream(new File(pFileName));
        fileName = pFileName;
    }
    
    /**
     * Generate final output XML file according to the configuration
     */
    static public void GenerateXMLFile()
    {
        assert(outputStream != null);
        
        XMLStreamWriter out;
        try {
            out = XMLOutputFactory.newInstance().createXMLStreamWriter(
                    new OutputStreamWriter(outputStream, "utf-8"));

            out.writeStartDocument();
            out.writeStartElement("response");

            PrintErrors(out);
            
            if (Configuration.time_for_each_statement)
            {
                PrintSQLcommandsWithTime(out);
            }

            if (!Configuration.omit_cost)                
            {            
                PrintCost(out);             
            }
            
//            out.writeStartElement("title");
//            out.writeCharacters("Document Title");
//            out.writeEndElement();
            
            out.writeEndElement();            
            out.writeEndDocument();

            out.close();

            outputStream.close();
            outputStream = null;
        } catch (XMLStreamException ex) {
            HandleException(ex);            
        } catch (UnsupportedEncodingException ex) {
            HandleException(ex);              
        } catch (IOException ex) {
            HandleException(ex);       
        }
    }

    /**
     * Iterate all errors produced during the evaluation and print them into the XML file
     * @param out Output XML file
     * @throws XMLStreamException 
     */
    private static void PrintErrors(XMLStreamWriter out) throws XMLStreamException {
        for(ResultError error : errors)
        {
            if (error.errorSeverity == ResultError.TYPE_ERROR)
            {
                out.writeStartElement("error");
            }
            if (error.errorSeverity == ResultError.TYPE_SEVERE_ERROR)
            {
                out.writeStartElement("severe_error");
            }
            if (error.errorSeverity == ResultError.TYPE_WARNING)
            {
                out.writeStartElement("severe_error");
            }
            out.writeStartElement("message");
            out.writeCharacters(error.errorText);
            out.writeEndElement();
            if (error.exceptionText != null)
            {
                out.writeStartElement("exceptionMessage");
                out.writeCharacters(error.exceptionText);
                out.writeEndElement();
            }
            out.writeEndElement();
        }
    }

    /**
     * Print the final cost of the evaluation
     * @param out Output XML file
     * @throws XMLStreamException 
     */
    private static void PrintCost(XMLStreamWriter out) throws XMLStreamException {
        double cost  = 0;
        for(SQLStatement s: Support.sqlStatements)
        {
            if (Configuration.shouldProcessThisType(s.mType))
            {
                cost += s.cost();
            }
        }
        out.writeStartElement("cost");
        out.writeCharacters(String.valueOf(cost));
        out.writeEndElement();
    }

    /**
     * Print all evaluated SQL commands together with their average time
     * @param out Output XML file
     * @throws XMLStreamException 
     */
    private static void PrintSQLcommandsWithTime(XMLStreamWriter out) throws XMLStreamException {
        for(SQLStatement s: Support.sqlStatements)
        {
            out.writeStartElement("sql");
            out.writeAttribute("id", String.valueOf(s.mId));
            out.writeAttribute("text", s.mStatementText);
            // compute average, median, rozptyl
            for (int i = 0; i < s.instanceCount(); i++)
            {
                out.writeStartElement("value");
                out.writeCharacters(String.valueOf(s.computeAverage(i)));
                out.writeEndElement();
            }
            out.writeEndElement();
        }
    }

    private static void HandleException(Exception ex) {
        // TODO - create plain text file with error message
        Logger.getLogger(ResultGenerator.class.getName()).log(Level.SEVERE, null, ex);
        try {
            outputStream.close();
        } catch (IOException ex1) {
            Logger.getLogger(ResultGenerator.class.getName()).log(Level.SEVERE, null, ex1);
        }
        outputStream = null;
    }
}
