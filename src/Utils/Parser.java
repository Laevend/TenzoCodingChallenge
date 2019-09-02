package Utils;

import java.time.LocalTime;

import Tenzo.WorkShift;

public class Parser
{
	private static StringBuilder finalString = new StringBuilder();
	private static StringBuilder sb = new StringBuilder();
	private static String breakTime;
	private static int charIndex = 0;
	
	/*
	Grammar

	[Number]
		[Number]
		[PM]
		[Dash]
		[Symbol]
	
	[PM]
		[Dash]
	
	[Dash]
		[Number]
	
	[Symbol]
		[Number]
	*/
	
	/**
	 * Parses break time notes to try and get a standardised 24hr clock time
	 * @param s The break time notes
	 * @return A 24hr clock time
	 */
	public static String parseBreakTimes(String s)
	{				
		// Set the global string being parsed
		breakTime = s;
		
		// Reset the charIndex
		charIndex = 0;
		
		// Clear the string builder
		sb.delete(0,sb.length());
		
		// Clear the final string builder
		finalString.delete(0,finalString.length());
		
		// Remove all spaces from this string
		breakTime = breakTime.replaceAll("\\s+","");
		
		System.out.println(" --------------------------------> " + breakTime);
		
		// Expecting number first
		if(parseNumber())
		{
			System.out.println("Final String -> " + finalString.toString());
			return finalString.toString();
		}
		else
		{
			System.out.println("Parsing Failed!");
		}
		
		return "";
	}
	
	/**
	 * Parses a single digit number
	 * @return If parsing was successful
	 */
	private static boolean parseNumber()
	{
		System.out.println(" ==================================================================== PARSE NUMBER");
		
		// Check if we are at the end of the string
		if(charIndex == breakTime.length())
		{ 
			// Append final part of string and return
			finalString.append(convertTime());			
			return true;
		}
		
		System.out.println("Length -> " + breakTime.length());
		System.out.println("BreakTime Char At " + charIndex + " -> " + breakTime.charAt(charIndex));
		
		// Check this character to see if it's a number
		if(Character.isDigit(breakTime.charAt(charIndex)))
		{
			// Append the number to the string builder
			sb.append(breakTime.charAt(charIndex));
			
			// Increase index to look at next character
			charIndex++;
		}
		else
		{
			System.out.println("parse failed");
			// This value does not fit with the grammar
			return false;
		}
		
		// Parse Number
		if(parseNumber()) { return true; }
		
		// Parse PM
		if(parsePM()) { return true; }
		
		// Parse Dash
		if(parseDash()) { return true; }
		
		// Parse Symbol
		if(parseSymbol()) { return true; }
		
		// This value does not fit with the grammar
		return false;
	}
	
	/**
	 * Parses the 'PM' keyword found sometimes at the end of a time. Converts the time it's adjacent to from 12hr time to 24hr
	 * @return If parsing was successful
	 */
	private static boolean parsePM()
	{		
		System.out.println(" ==================================================================== PARSE PM");
		
		// Check if we are at the end of the string
		if(charIndex == breakTime.length())
		{ 
			// Append final part of string and return
			finalString.append(convertTime());
			return true;
		}
		
		// Check this character to see if it's AM
		if(breakTime.charAt(charIndex) == 'p' || breakTime.charAt(charIndex) == 'P')
		{
			// Check if the P / PM is at the end of this string
			if(charIndex == (breakTime.length() - 1))
			{
				// (P) Increase index to look at next character
				System.out.println("PM: -> " + breakTime.substring(charIndex,charIndex + 1));
				charIndex++;
			}
			else
			{
				// (PM) Increase index to look at next character
				System.out.println("PM: -> " + breakTime);
				System.out.println("PM: -> " + breakTime.length() + " -> " + charIndex);
				System.out.println("PM: -> " + breakTime.charAt(charIndex) + breakTime.charAt(charIndex + 1));
				charIndex+= 2;
			}
		}
		else
		{
			System.out.println("parse failed");
			// This value does not fit with the grammar
			return false;
		}
		
		// Parse Dash
		if(parseDash()) { return true; }
		
		// This value does not fit with the grammar
		return false;
	}
	
	/**
	 * Parses the dash symbol, used to indicate the start of a new time
	 * @return If parsing was successful
	 */
	private static boolean parseDash()
	{
		System.out.println(" ==================================================================== PARSE DASH");
		
		// Check if we are at the end of the string
		if(charIndex == breakTime.length())
		{ 
			// Append final part of string and return
			finalString.append(convertTime());
			return true;
		}
		
		// Check this character to see if it's a dash
		if(breakTime.charAt(charIndex) == '-')
		{
			// Get the time parsed up previously
			String previousTime = convertTime();
			
			// Append to final string
			finalString.append(previousTime + "-");
			
			System.out.println("Dash: appending to final string: " + previousTime + "-");
			
			// Clear the string builder
			sb.delete(0,sb.length());
			System.out.println("Dash: sb after cleared: " + sb.toString());
			charIndex++;
		}
		else
		{
			System.out.println("parse failed");
			// This value does not fit with the grammar
			return false;
		}
		
		// Parse Number
		if(parseNumber()) { return true; }
		
		// This value does not fit with the grammar
		return false;
	}
	
	/**
	 * Parses anything other than alphanumeric that is used as a separator between hours and minutes
	 * @return If parsing was successful
	 */
	private static boolean parseSymbol()
	{
		System.out.println(" ==================================================================== PARSE SYMBOL");
		
		// Check if we are at the end of the string
		if(charIndex == breakTime.length())
		{ 
			// Append final part of string and return
			finalString.append(convertTime());
			return true;
		}
		
		String specialCharacter = String.valueOf(breakTime.charAt(charIndex));
		System.out.println("specialCharacter -> " +specialCharacter);
		
		
		// Check this character to see if it's a symbol
		if(!specialCharacter.matches("^[A-Za-z0-9]+$"))
		{
			sb.append(":");
			System.out.println("Symbol: appending to final string: ':'");
			charIndex++;
		}
		else
		{
			System.out.println("parse failed");
			// This value does not fit with the grammar
			return false;
		}
		
		// Parse Number
		if(parseNumber()) { return true; }
		
		// This value does not fit with the grammar
		return false;
	}
	
	/**
	 * Converts whatever time has been entered into a 4-digit 24hr clock time
	 * @return The converted time
	 */
	private static String convertTime()
	{
		// Get the time parsed up previously
		String previousTime = sb.toString();
		
		if(previousTime.contains(":"))
		{
			int hours = Integer.parseInt(previousTime.split(":")[0]);
			int minutes = Integer.parseInt(previousTime.split(":")[1]);
			
			// Clear string
			previousTime = "";
			
			// Check if the number of hours is less than 10
			if(hours < 10)
			{
				previousTime = previousTime + "0" + String.valueOf(hours) + ":";
			}
			else
			{
				previousTime = previousTime + String.valueOf(hours) + ":";
			}
			
			// Check if the number of minutes is less than 10
			if(minutes < 10)
			{
				previousTime = previousTime + "0" + String.valueOf(minutes);
			}
			else
			{
				previousTime = previousTime + String.valueOf(minutes);
			}
		}
		else
		{
			// If this time uses only 1-digit hours
			if(previousTime.length() < 2)
			{
				previousTime = "0" + previousTime + ":00";
			}
			// If this time uses only 2-digit hours
			else if(previousTime.length() < 3)
			{
				previousTime = previousTime + ":00";
			}
		}
		
		return previousTime;
	}
	
	/**
	 * Compares break time notes to employee work hours to check they make sense
	 * @param breakTimeNotes
	 */
	public static void compareBreakTimes(WorkShift shift)
	{		
		// Split the start break time
		int breakTimeStartHour = shift.getBreakTimeStart().getHour();
		
		// Split the end break time
		int breakTimeEndHour = shift.getBreakTimeEnd().getHour();
		
		// Get starting hour
		int shiftStartHour = shift.getStartTime().getHour();
		
		// Get ending hour
		int shiftEndHour = shift.getEndTime().getHour();
		
		// Check if the starting break time makes sense
		if(!Utils.valueIsBetween(shiftStartHour,shiftEndHour,breakTimeStartHour))
		{
			breakTimeStartHour += 12;
		}
		
		// Check if the ending break time makes sense
		if(!Utils.valueIsBetween(shiftStartHour,shiftEndHour,breakTimeEndHour))
		{
			breakTimeEndHour += 12;
		}
		
		// Re-pack break time notes
		LocalTime breakTimeStart = LocalTime.of(breakTimeStartHour,shift.getBreakTimeStart().getMinute());
		LocalTime breakTimeEnd = LocalTime.of(breakTimeEndHour,shift.getBreakTimeEnd().getMinute());
		
		// Re-set the newly corrected break time notes
		shift.setBreakNotes(breakTimeStart,breakTimeEnd);
	}
}
