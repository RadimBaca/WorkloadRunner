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
import java.util.Random;

/**
 * Static container consisting of basic objects such as arrayList of SQL commands 
 * and ArrayList of sessions.
 *
 * @author Bača Radim
 */
public class Support {

    static public ArrayList<SQLStatement> sqlStatements = new ArrayList<SQLStatement>();
    static public ArrayList<SQLSession> sessions = new ArrayList<SQLSession>();
    static public boolean canNotConnectToDatabase;
    //static public ArrayList<>

 
    public static SQLStatement getStatement(int id)
    {
        return sqlStatements.get(id - 1);
    }
 
}
