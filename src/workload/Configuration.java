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
import java.util.List;
import result.ResultError;
import result.ResultGenerator;

/**
 *
 * @author radim
 */
public class Configuration {
    // warm-up config
    static public boolean perform_warm_up = false;
    static public int warm_up_randomly_select = -1; // <warm_up randomly_select='XXX'/>
    static public boolean warm_up_run_all_sessions = false;
    
    // which statements to run and how to perform them
    static public String perform = "commands";
    static public int repeat = 1;
    static public String order_of_commands = "random";
    static public boolean compute_mean = false;
    static public boolean compute_average = false;
    static private List<Integer> processed_types = new ArrayList<Integer>();
    
    // what should be in the output
    static public boolean time_for_each_statement = false;
    static public boolean omit_cost = false;
    
    
    public static void ImplicitTextConfiguration()
    {
       perform_warm_up = true;
       warm_up_randomly_select = 100;
       perform = "commands";
       repeat = 5;
       order_of_commands = "random";
       compute_mean = false;
       compute_average = false;
       time_for_each_statement = false;
       omit_cost = false;
       Init();
    }
    
    public static void CheckConfiguration()
    {
//        if (warm_up_randomly_select != -1 && warm_up_run_all_sessions)
//        {
//            ResultGenerator.errors.add(new ResultError(ResponseTexts.CONFIGURATION_WARNING_WARM_UP_CONRADICTORY, null, ResultError.TYPE_WARNING));            
//        }
        if (compute_mean && compute_average)
        {
            ResultGenerator.errors.add(new ResultError(ResponseTexts.CONFIGURATION_WARNING_COMPUTE_AVERAGE_CONRADICTORY, null, ResultError.TYPE_WARNING));
        }
    }
    
    public static void Init()
    {
        processed_types.clear();
        processed_types.add(SQLStatement.TYPE_QUERY);
    }
    
    public static void addProcessedType(int type)
    {
        processed_types.add(type);
    }
    
    public static boolean shouldProcessThisType(int type)
    {
        for (int it: processed_types)
        {
            if (it == type)
            {
                return true;
            }
        }
        return false;
    }
}
