package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils
{		
	/**
	 * Reads CSV data
	 * @param path The path to the file
	 * @return A list of CSV's
	 */
	public static List<String> readCSV(String path)
	{
		// Create a blank data array list
		ArrayList<String> data = new ArrayList<String>();
		
		// Create file
		File file = new File(path);
		
		// Check the directory exists
		if(!file.exists())
	    {
			System.out.println("File could not be found!");
			return Collections.emptyList();
	    }
		
		// If the directory exists, create a buffered reader and read
		try 
        {
        	// Create a buffered reader and read
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			// Get the first line of the file
        	String line = br.readLine();
        	
        	// While there is still data to be read from the file, add it to the array list
        	while(line != null)
        	{
        		// Add newly read line from file to the array list
        		data.add(line);
        		line = br.readLine();
        	}
        	
        	// Close the buffered reader
        	br.close();
        	
        	// Return the data
        	return data;
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        }
		
		return Collections.emptyList();
	}
	
	/**
	 * Checks to see if a value is between 2 other values
	 * @param lower The lower value
	 * @param higher The higher value
	 * @param value The value to be tested
	 * @return If the value is in-between lower and higher
	 */
	public static boolean valueIsBetween(int lower,int higher,int value)
	{
		System.out.println("ValueIsBetween -> Lower: " + lower + " Higher: " + higher + " Value: " + value);
		return value >= lower && value <= higher ? true : false;
	}
}
