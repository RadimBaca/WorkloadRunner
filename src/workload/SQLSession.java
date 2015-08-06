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

/**
 * Simple container that store all data related to one session.
 * Major variable is an ArrayList (statements) that contains a list of SQL commands that should be performed during the session
 * @author Radim Baca
 */
public class SQLSession {
    public int startAt;
    public ArrayList<int[]> statements = new ArrayList<int[]>(); // ArrayList that store triplets (sql Id, param order, start time in seconds)
    public int id;
    
    public SQLSession(int start, int pid)
    {
        startAt = start;
        id = pid;
    }
}
