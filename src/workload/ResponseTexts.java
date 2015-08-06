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

/**
 * This class contains just set of response texts that are displayed to the client
 * 
 * @author Radim Baca
 */
public class ResponseTexts {
    static final String OPEN_OUTPUT_FILE_FAILED = "Opening of the output XML file failed. Please check the priviledges or whether the file is not exclusively opened by other application.";
    static final String INPUT_PARAMETER_IN_NOT_NUMERIC = "The first parameter has to be a numeric type";
    static final String SUCCESFULY_PARSED = " has been succesfuly parsed.";

    /*
    Errors
    */
    static final String CONNECTION_FAILED = "There was an error when connecting to the database. Please check your connection credentials.";
    static final String FILE_NOT_FOUND_ERROR = " file can not be opened. Please make sure that input file with workload is accessible.";
    static final String ERROR_WHEN_CLOSING_INPUT_FILE = "There was an error when closing the input file.";
    static final String ERROR_INPUT_XML_FILE_NOT_WELL_FORMED = " XML file is not well formed.";
    static final String ERROR_WAITING_FOR_A_THREAD_TO_FINNISH = " there was na error in thread. Statistics may be incomplete, please run the workload again.";
    static final String NOT_IMPLEMENTED_YET = " not implemented yet.";
    static final String ELEMENT = "Element ";
    static final String ALREADY_DEFINED = " already defined. WorkloadRunner accept the last occurence.";
    static final String SQL_EXCEPTION = "There was an error when processing the following SQL command: ";
    static final String SQL_WARNING = "There was a warning when processing the following SQL command: ";
    

    /*
    Warnings
    */
    static final String CONFIGURATION_WARNING_COMPUTE_AVERAGE_CONRADICTORY = "Configuration warning: compute_average and compute_mean are contradictory, therefore, compute_average is ignored.";
    static final String CONFIGURATION_WARNING_REPEAT = "Configuration warning: the value of repeat element has to be within the <1, 20> interval, therefore, repeat was adjusted to a new value ";
    static final String CONFIGURATION_WARNING_ORDER_OF_COMMANDS = "The value of the order_of_commands element can be just random or sequential. Default value is random.";
    static final String CONFIGURATION_WARNING_COMMAND_TYPE_DOES_NOT_EXISTS = " value in the command_type element is not supported and it is ignored.";
    //static final String CONFIGURATION_WARNING_WARM_UP_CONRADICTORY = "Configuration warning: randomly_select and run_all_sessions are contradictory, therefore, run_all_sessions is ignored.";
    
            
}
