package com.microsoft.azure.springsecuritywebapp;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
	
	static String ISO_DATE_FORMAT_ZERO_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	 static  String UTC_TIMEZONE_NAME = "UTC";

	  static SimpleDateFormat provideDateFormat() {
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ISO_DATE_FORMAT_ZERO_OFFSET);
	    simpleDateFormat.setTimeZone(TimeZone.getTimeZone(UTC_TIMEZONE_NAME));
	    return simpleDateFormat;
	  }
	  
	  
	static int  getDateValueFromStringJson(String date)
	{
		 DateTimeFormatter formatter= DateTimeFormatter.ofPattern(DateUtil.ISO_DATE_FORMAT_ZERO_OFFSET);
		 LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
		 return dateTime.getDayOfYear();
	}

	  
	  public static void main(String[] args) {

		  DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		  
		   LocalDateTime now = LocalDateTime.now();  
		   
		   System.out.println(now.getDayOfYear()+"qweeeeeeeeeeeeeeeeeeeeeery");  
			
		    DateTimeFormatter formatter= DateTimeFormatter.ofPattern(DateUtil.ISO_DATE_FORMAT_ZERO_OFFSET);
		    String text = "2019-01-14T12:00:00.000Z";
		    LocalDateTime dateTime = LocalDateTime.parse(text, formatter);
		    System.out.println(dateTime.getDayOfYear());
	}
}
