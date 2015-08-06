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

/**
 * If an error occurs than we store the text of the error in this object 
 * which it is later serialized by the @ResultGenerator
 * 
 * 
 * @author Radim Baca
 */
public class ResultError {
    public String errorText;
    public String exceptionText;
    public int errorSeverity;
    
    public static final int TYPE_SEVERE_ERROR = 1; // an error that lead to an immediate end of the application
    public static final int TYPE_ERROR = 2; // an error tha occurs during the run that could influence the final results
    public static final int TYPE_WARNING = 3; // warning where we inform an user that something unussual hapened
    
    public ResultError(String perrorText, String pexceptionText, int severity)
    {
        errorText = perrorText;
        exceptionText = pexceptionText;
        errorSeverity = severity;
    }
}
