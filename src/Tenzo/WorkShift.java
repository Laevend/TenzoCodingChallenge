package Tenzo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

public class WorkShift
{
	private LocalTime startTime;			// The time the employee started work
	private LocalTime endTime;				// The time the employee finished work
	private BigDecimal payRate;				// The employees pay rate
	private String breakNotes;				// The employees break notes
	
	public WorkShift(String startTime,String endTime,BigDecimal payRate,String breakNotes)
	{
		this.startTime = LocalTime.parse(startTime);
		this.endTime = LocalTime.parse(endTime);
		this.payRate = payRate;
		this.breakNotes = breakNotes;
	}

	// Get start time
	public LocalTime getStartTime()
	{
		return startTime;
	}
	
	// Set start time
	public void setStartTime(LocalTime startTime)
	{
		this.startTime = startTime;
	}
	
	// Get end time
	public LocalTime getEndTime()
	{
		return endTime;
	}
	
	// Set end time
	public void setEndTime(LocalTime endTime)
	{
		this.endTime = endTime;
	}
	
	// Get pay rate
	public BigDecimal getPayRate()
	{
		return payRate;
	}
	
	// Set pay rate
	public void setPatRate(BigDecimal payRate)
	{
		this.payRate = payRate;
	}
	
	// Get break notes
	public String getBreakNotes()
	{
		return breakNotes;
	}
	
	// Set break notes
	public void setBreakNotes(String breakNotes)
	{
		this.breakNotes = breakNotes;
	}
	
	// Set break notes using local time
	public void setBreakNotes(LocalTime start,LocalTime end)
	{
		this.breakNotes = start.toString() + "-" + end.toString();
	}
	
	// Get the break time start
	public LocalTime getBreakTimeStart()
	{
		return LocalTime.parse(breakNotes.split("-")[0]);
	}
	
	// Get break time 
	public LocalTime getBreakTimeEnd()
	{
		return LocalTime.parse(breakNotes.split("-")[1]);
	}
	
	public int getBreakTimeMinutes(LocalTime time)
	{
		// Converts times into big decimals to be compared against
		BigDecimal shiftStart = new BigDecimal(this.startTime.getHour() + "." + this.startTime.getMinute());
		BigDecimal shiftEnd = new BigDecimal(this.endTime.getHour() + "." + this.endTime.getMinute());
		
		BigDecimal breakStart = new BigDecimal(this.getBreakTimeStart().getHour() + "." + this.getBreakTimeStart().getMinute());
		BigDecimal breakEnd = new BigDecimal(this.getBreakTimeEnd().getHour() + "." + this.getBreakTimeEnd().getMinute());
		
		// Create 1 hour bracket to check (1 hour defined at from HH:00 to HH:59)
		BigDecimal currentTimeStart = new BigDecimal(time.getHour() + ".00");
		BigDecimal currentTimeEnd = new BigDecimal(time.getHour() + ".59");
		
		// Checks if this employee is working based on their working shifts
		if((currentTimeStart.compareTo(shiftStart) == 1 || currentTimeStart.compareTo(shiftStart) == 0) &&
				(currentTimeStart.compareTo(shiftEnd) == -1 || currentTimeStart.compareTo(shiftEnd) == 0))
		{
			// Check if a break time starts in this hour
			if((breakStart.compareTo(currentTimeStart) == 1 || breakStart.compareTo(currentTimeStart) == 0) &&
					(breakEnd.compareTo(currentTimeEnd) == -1 || breakEnd.compareTo(currentTimeEnd) == 0))
			{
				// Return the number of minutes of this total break
				return this.getBreakTimeEnd().getMinute() - this.getBreakTimeStart().getMinute();
			}
			// Check if a break time starts in this hour but does not finish
			else if((breakStart.compareTo(currentTimeStart) == 1 || breakStart.compareTo(currentTimeStart) == 0) &&
					(breakStart.compareTo(currentTimeEnd) == -1 || breakStart.compareTo(currentTimeEnd) == 0))
			{
				// Return the number of minutes from when the break starts to the end of the hour
				return (60 - this.getBreakTimeStart().getMinute());
			}
			// Check if a break time finishes in this hour but does not start
			else if((breakEnd.compareTo(currentTimeStart) == 1 || breakEnd.compareTo(currentTimeStart) == 0) && 
					(breakEnd.compareTo(currentTimeEnd) == -1 || breakEnd.compareTo(currentTimeEnd) == 0))
			{
				// Return the number of minutes from when the hour starts to when the break ends
				return this.getBreakTimeEnd().getMinute();
			}
			// Check if a break time is happening but does not finish in this hour
			else if((breakStart.compareTo(currentTimeStart) == -1 && breakEnd.compareTo(currentTimeEnd) == 1))
			{
				// Return 60 - the number of minutes in an hour
				return 60;
			}
			else
			{
				// Return 0 - the employee is working and is not on break
				return 0;
			}
		}
		else
		{
			// Return -1 - the employee is not working as their shift has not started/ended
			return -1;
		}
	}
	
	public BigDecimal calculateDailyPay()
	{
		// Calculate the daily wage of this employee
		int shiftHoursDifference = this.endTime.getHour() - this.startTime.getHour();
		int breakHoursDifference = this.getBreakTimeEnd().getHour() - this.getBreakTimeStart().getHour();
		int shiftMinutesDifference = this.endTime.getMinute() - this.startTime.getMinute();
		int breakMinutesDifference = this.getBreakTimeEnd().getMinute() - this.getBreakTimeStart().getMinute();
		
		//System.out.println("shiftHoursDifference -> " + shiftHoursDifference);
		//System.out.println("breakHoursDifference -> " + breakHoursDifference);
		//System.out.println("shiftMinutesDifference -> " + shiftMinutesDifference);
		//System.out.println("breakMinutesDifference -> " + breakMinutesDifference);
		
		// Remove break times
		shiftHoursDifference -= breakHoursDifference;
		shiftMinutesDifference -= breakMinutesDifference;
		
		//System.out.println("shiftHoursDifference -> " + shiftHoursDifference);
		//System.out.println("shiftMinutesDifference -> " + shiftMinutesDifference);
		
		// Get the pay-rate per minute
		BigDecimal payRatePerMinute = this.payRate.divide(new BigDecimal(60),2,RoundingMode.HALF_UP);
		
		//System.out.println("payRatePerMinute -> " + payRatePerMinute);
		
		// Multiply the number of minutes worked by the pay-rate per minute
		BigDecimal totalPayMinutes = payRatePerMinute.multiply(new BigDecimal(shiftMinutesDifference));
		
		//System.out.println("totalPayMinutes -> " + totalPayMinutes);
		
		// Multiply the number of hours worked by the pay-rate per hour
		BigDecimal totalPayHours = this.payRate.multiply(new BigDecimal(shiftHoursDifference));
		
		//System.out.println("totalPayHours -> " + totalPayHours);
		//System.out.println("total -> " + totalPayHours.add(totalPayMinutes));
		
		// Return this employees pay for the day
		return totalPayHours.add(totalPayMinutes);
	}
}
