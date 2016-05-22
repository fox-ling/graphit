package ru.foxling.graphit;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.Test;

import ru.foxling.graphit.config.DataType;
import ru.foxling.graphit.config.Field;
import ru.foxling.graphit.config.Format;

public class FieldTest {
	Field field = new Field();
	
	@Test
	public void initialized() {
		assertNotNull("Name is NULL", field.getName());
		assertNotNull("Datatype is NULL", field.getDatatype());
		assertNotNull("Delimiter is NULL", field.getDelimiter());
		assertNotNull("Format is NULL", field.getFormat());
		assertNotNull("Parser is NULL", field.getParser());
		assertNotNull("ValueList is NULL", field.getValueList());
	}
	
	@Test
	public void datatypeTest() throws Exception{
		for (int i = 0; i < DataType.values().length; i++) {
			DataType datatype = DataType.values()[i];
			System.out.printf("DataType: %s%n", datatype.getCaption());
			field.setDatatype(datatype);
			assertEquals(datatype, field.getDatatype());
			
			List<Format> formatList = datatype.getFormatList();
			for (int j = 0; j < formatList.size(); j++) {
				Format format = formatList.get(j);
				System.out.printf("Format: %s%n", format.caption);
				field.setFormat(format);
				assertEquals(formatList.get(j), field.getFormat());
				assertNotNull(field.getParser());
				
				String src;
				switch (datatype) {
				case BYTE:
					src = Integer.toString(Byte.MIN_VALUE, Integer.parseInt(format.value));
					assertEquals(Byte.MIN_VALUE, (byte) field.getParser().parse(src));
					src = Integer.toString(Byte.MAX_VALUE, Integer.parseInt(format.value));
					assertEquals(Byte.MAX_VALUE, (byte) field.getParser().parse(src));
					break;
				case SHORT:
					src = Integer.toString(Short.MIN_VALUE, Integer.parseInt(format.value));
					assertEquals(Short.MIN_VALUE, (short) field.getParser().parse(src));
					src = Integer.toString(Short.MAX_VALUE, Integer.parseInt(format.value));
					assertEquals(Short.MAX_VALUE, (short) field.getParser().parse(src));
					break;
				case INTEGER:
					src = Integer.toString(Integer.MIN_VALUE, Integer.parseInt(format.value));
					assertEquals(Integer.MIN_VALUE, (int) field.getParser().parse(src));
					src = Integer.toString(Integer.MAX_VALUE, Integer.parseInt(format.value));
					assertEquals(Integer.MAX_VALUE, (int) field.getParser().parse(src));
					break;
				case DATETIME: {
					LocalDateTime dt = LocalDateTime.now();
					dt = dt.minusNanos(dt.getNano());
					src = dt.format(DateTimeFormatter.ofPattern(format.value));
					assertEquals(dt, (LocalDateTime) field.getParser().parse(src));
				} break;
				case TIME: {
					LocalTime t = LocalTime.now();
					t = t.minusNanos(t.getNano());
					src = t.format(DateTimeFormatter.ofPattern(format.value));
					assertEquals(t, (LocalTime) field.getParser().parse(src));
				} break;
				case OVERFLOWING_TIME_SEQUENCE: {
					LocalTime t = LocalTime.now();
					t = t.minusNanos(t.getNano());
					src = t.format(DateTimeFormatter.ofPattern(format.value));
					assertEquals(t, (LocalTime) field.getParser().parse(src));
				} break;
				case DATE: {
					LocalDate d = LocalDate.now();
					src = d.format(DateTimeFormatter.ofPattern(format.value));
					assertEquals(d, (LocalDate) field.getParser().parse(src));
				} break;
				default:
					break;
				}
			}
			
			switch (datatype) {
			case BOOLEAN:
				assertEquals(false, (boolean) field.getParser().parse("0"));
				assertEquals(true, (boolean) field.getParser().parse("1"));
				break;
			case FLOAT:
				assertEquals(124.34f, (float) field.getParser().parse("124.34"), 0.00000001f);
				assertEquals(-22256.98f, (float) field.getParser().parse("-22256.98"), 0.00000001f);
				break;
			case DOUBLE:
				assertEquals(124.34, (double) field.getParser().parse("124.34"), 0.00000001);
				assertEquals(-22256.98, (double) field.getParser().parse("-22256.98"), 0.00000001);
				break;
			case STRING:
				assertEquals("Hello World!", (String) field.getParser().parse("Hello World!"));
				break;
			default:
				break;
			}
		}
	}

}
