package ru.foxling.graphit;

import static org.junit.Assert.*;

import org.junit.Test;

import ru.foxling.graphit.config.DataType;
import ru.foxling.graphit.config.Format;

public class DateTypeTest {
	@Test
	public void Date_AddFormat_Test() {
		Format f = new Format("caption", "value");
		DataType d = DataType.DATE;
		try {
			d.getFormatList().add(f);
		} catch (Exception e) {
			System.out.println(e);
		}
		assertTrue(d.getFormatList().contains(f));
	}
}
