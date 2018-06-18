package cz.it4i.fiji.haas;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class TestFormat {
	public static void main(String[] args) {
		System.out.println(Locale.getDefault());
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendOptional(DateTimeFormatter.ofPattern("EEE MMM dd kk:mm:ss z yyyy"))
				.appendOptional(DateTimeFormatter.ofPattern("EEE MMM dd kk:mm:ss yyyy")).toFormatter();
		
		String value = "Thu Jun 14 09:08:43 2018";
		System.out.println(LocalDateTime.parse(value, formatter).toEpochSecond(ZoneOffset.UTC));
		
	}

}
