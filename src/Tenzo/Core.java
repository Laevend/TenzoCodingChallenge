package Tenzo;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Utils.Parser;
import Utils.Utils;

public class Core
{	
	final String transactionsPath = "files" + File.separator + "transactions.csv";		// The directory where the transactions file is saved
	final String workShiftsPath = "files" + File.separator + "work_shifts.csv";			// The directory where the work shifts file is saved
	
	// Exit main method
	public static void main(String args[])
	{
		new Core();
	}
	
	public Core()
	{
		Map<String,BigDecimal> transactionsMap = process_sales(transactionsPath);
		
		System.out.println(String.format("| %-11s | %-11s |","Time","Amount"));
		System.out.println("|=============|=============|");
		
		for(String key : transactionsMap.keySet())
		{
			System.out.println(String.format("| %-11s | %-11s |",key,transactionsMap.get(key)));
		}
		
		System.out.println("");
		
		Map<String,WorkShift> shiftsMap = process_shifts(workShiftsPath);
		
		System.out.println("");
		
		System.out.println(String.format("| %-11s | %-11s | %-11s | %-11s | %-11s | %-11s |","Employee","Start Time","End Time","Pay Rate","Break Notes","Pay for Day"));
		System.out.println("|=============|=============|=============|=============|=============|=============|");
		
		for(String key : shiftsMap.keySet())
		{
			System.out.println(String.format("| %-11s | %-11s | %-11s | %-11s | %-11s | %-11s |",
					key,
					shiftsMap.get(key).getStartTime(),
					shiftsMap.get(key).getEndTime(),
					shiftsMap.get(key).getPayRate(),
					shiftsMap.get(key).getBreakNotes(),
					shiftsMap.get(key).calculateDailyPay()));
		}
		
		compute_percentage(shiftsMap,transactionsMap);
	}
	
	private Map<String,WorkShift> process_shifts(String path_to_csv)
	{
		// Read CSV
		List<String> workShift = Utils.readCSV(path_to_csv);
		
		// Create a new hashmap which will be used to represent this data internally
		Map<String,WorkShift> workShiftMap = new HashMap<String,WorkShift>();
		
		for(int i = 1; i < workShift.size(); i++)
		{
			// Split the csv into individual strings
			String[] csvParts = workShift.get(i).split(",");
			
			// Add data to map
			workShiftMap.put("Employee_" + i,new WorkShift(csvParts[3],csvParts[1],new BigDecimal(csvParts[2]),Parser.parseBreakTimes(csvParts[0])));
			
			// Check that break times are inside shift times
			Parser.compareBreakTimes(workShiftMap.get("Employee_" + i));
		}
		
		return workShiftMap;
	}
	
	private Map<String,BigDecimal> process_sales(String path_to_csv)
	{		
		// Read CSV
		List<String> transactions = Utils.readCSV(path_to_csv);
		
		// Create a new hashmap which will be used to represent this data internally
		Map<String,BigDecimal> transactionsMap = new HashMap<String,BigDecimal>();
		
		for(int i = 1; i < transactions.size(); i++)
		{
			// Split the csv into individual strings
			String[] csvParts = transactions.get(i).split(",");
			
			// Key duplication check
			if(transactionsMap.containsKey(csvParts[1]))
			{
				// A duplicate key was found, merge the amounts together
				BigDecimal currentAmount = transactionsMap.get(csvParts[1]);
				currentAmount = currentAmount.add(new BigDecimal(csvParts[0]));				
				transactionsMap.put(csvParts[1],currentAmount);
			}
			else
			{
				// There is no duplicate of this key, add it to the map
				transactionsMap.put(csvParts[1],new BigDecimal(csvParts[0]));
			}
		}
		
		return transactionsMap;
	}
	
	private void compute_percentage(Map<String,WorkShift> shiftsMap,Map<String,BigDecimal> transactionsMap)
	{
		// Sales for each hour
		BigDecimal[] sales = new BigDecimal[24];
		
		// For each transaction
		for(String key : transactionsMap.keySet())
		{
			// Get the hour of sales
			int hourOfSales = Integer.parseInt(key.split(":")[0]);
			
			// Add together all sales for each hour of the day
			if(sales[hourOfSales] == null)
			{
				// Sales is empty, add a new sales value
				sales[hourOfSales] = transactionsMap.get(key);
			}
			else
			{
				// Sales value is not empty, add this to the existing sales figure
				sales[hourOfSales] = sales[hourOfSales].add(transactionsMap.get(key));
			}
		}
		
		LocalTime time;
		Map<LocalTime,Integer> hourEntrys = new LinkedHashMap<LocalTime,Integer>();
		
		System.out.println("");
		System.out.println(String.format("| %-11s | %-11s | %-11s | %-11s |","Hour","Sales","Labour","%"));
		System.out.println("|=============|=============|=============|=============|");
		
		for(int i = 0; i < 24; i++)
		{
			// Get the next hour
			time = LocalTime.of(i,00);
			
			// The total cost of labour in an hour
			BigDecimal labour = new BigDecimal(0);
			
			for(String key : shiftsMap.keySet())
			{
				// The employee is working and is not on break this hour (0)
				if(shiftsMap.get(key).getBreakTimeMinutes(time) == 0)
				{
					BigDecimal newPay = shiftsMap.get(key).getPayRate();
					
					//System.out.println(key + " -> " + shiftsMap.get(key).getPayRate());
					
					labour = labour.add(newPay.setScale(2,RoundingMode.HALF_UP));
				}
				// The employee is on break this hour, deductions need to be made to payment (1-60)
				else if(shiftsMap.get(key).getBreakTimeMinutes(time) >= 1)
				{
					// The time worked in this hour (time in minutes left after deduction from break time)
					BigDecimal timeWorkedFromHour = new BigDecimal(60 - shiftsMap.get(key).getBreakTimeMinutes(time));
					
					//System.out.println(key + " ----> timeWorkedFromHour -> " + timeWorkedFromHour);
					
					// If the remaining time worked from the hour is at least 1 minute
					if(timeWorkedFromHour.compareTo(new BigDecimal(0)) == 1)
					{
						BigDecimal newPay = timeWorkedFromHour.divide(new BigDecimal(60),16,RoundingMode.HALF_UP);
						
						//System.out.println(key + " ----> " + newPay + " * " + shiftsMap.get(key).getPayRate());
						
						newPay = newPay.multiply(shiftsMap.get(key).getPayRate());
						
						//System.out.println(key + " ----> " + newPay);
						
						// Labour value is not empty, add this to the existing Labour figure
						labour = labour.add(newPay.setScale(2,RoundingMode.HALF_UP));
					}
				}
			}
			
			// If a sales hour is null then set it to 0
			if(sales[i] == null) { sales[i] = new BigDecimal(0); }
			
			// Cost of labour as a percentage
			BigDecimal percentage = new BigDecimal(0);
			
			// Calculate percentage for labour
			if(!labour.equals(new BigDecimal(0)))
			{
				if(!sales[i].equals(new BigDecimal(0.00)))
				{
					percentage = sales[i].divide(new BigDecimal(100));
					percentage = labour.divide(percentage,2,RoundingMode.HALF_UP);
				}
				else
				{
					percentage = percentage.add(new BigDecimal(100));
					labour = labour.multiply(new BigDecimal(-1));
				}
			}
			
			System.out.println(String.format("| %-11s | %-11s | %-11s | %-11s |",
					time,
					sales[i],
					labour,
					percentage));
			
			// Multiply by 100 to make it an integer for easy sorting
			BigDecimal multipliedPercentage = percentage.multiply(new BigDecimal(100));
			
			// If the percentage is greater than 0
			if(multipliedPercentage.intValue() >= 1)
			{
				// Add value to linked hash map
				hourEntrys.put(time,multipliedPercentage.intValue());
			}
		}
		
		best_and_worst_hour(hourEntrys);
	}
	
	/**
	 * Displays the best and worst hours
	 * @param hourEntrys The entry of hours
	 */
	private void best_and_worst_hour(Map<LocalTime,Integer> hourEntrys)
	{		
		// Sort map
		Map<LocalTime,Integer> sortedHourEntrys = hourEntrys.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(e1,e2) -> e1,LinkedHashMap::new));
		
		// Create a list of all the keys of this map
		List<LocalTime> listedTimes = new ArrayList<LocalTime>(sortedHourEntrys.keySet());
		
		// Get best hour
		BigDecimal bestHour = new BigDecimal(sortedHourEntrys.get(listedTimes.get(0)));
		bestHour = bestHour.divide(new BigDecimal(100));
		
		// Get worst hour
		BigDecimal worstHour = new BigDecimal(sortedHourEntrys.get(listedTimes.get(listedTimes.size() - 1)));
		worstHour = worstHour.divide(new BigDecimal(100));
		
		// Display best hour
		System.out.println("");
		System.out.println(String.format("| %-11s | %-11s |","Best Hour","%"));
		System.out.println("|=============|=============|");
		System.out.println(String.format("| %-11s | %-11s |",listedTimes.get(0),bestHour));
		
		// Display worst hour
		System.out.println("");
		System.out.println(String.format("| %-11s | %-11s |","Worst Hour","%"));
		System.out.println("|=============|=============|");
		System.out.println(String.format("| %-11s | %-11s |",listedTimes.get(listedTimes.size() - 1),worstHour));
	}
}
