package com.iyzipay;

import com.iyzipay.exception.DecodeValueException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DecodeMpmIteratorTest {

    @Test
    public void should_parse_mpm_data_with_crc() {
        String mpmData = "0002010102126304ABCD";
        DecodeMpmIterator iterator = new DecodeMpmIterator(mpmData);

        List<DecodeMpmIterator.MpmData> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }

        assertEquals(3, result.size());
        assertEquals("00", result.get(0).getTag());
        assertEquals("02", result.get(0).getLength());
        assertEquals("01", result.get(0).getValue());
        assertEquals("01", result.get(1).getTag());
        assertEquals("02", result.get(1).getLength());
        assertEquals("12", result.get(1).getValue());
        assertEquals("63", result.get(2).getTag());
        assertEquals("04", result.get(2).getLength());
        assertEquals("ABCD", result.get(2).getValue());
    }

    @Test
    public void should_throw_decode_value_exception_when_missing_crc_at_end() {
        String mpmDataWithoutCrc = "000201010212";
        DecodeMpmIterator iterator = new DecodeMpmIterator(mpmDataWithoutCrc);

        List<DecodeMpmIterator.MpmData> result = new ArrayList<>();
        DecodeValueException exception = null;
        try {
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        } catch (DecodeValueException e) {
            exception = e;
        }

        assertNotNull("Expected DecodeValueException to be thrown", exception);
        assertTrue("Exception message should mention missing CRC field",
                exception.getMessage().contains("Missing required CRC field (tag 63)"));
    }

    @Test
    public void should_throw_decode_value_exception_when_truncated_data_causes_out_of_bounds() {
        String truncatedMpmData = "000201015";
        DecodeMpmIterator iterator = new DecodeMpmIterator(truncatedMpmData);

        DecodeValueException exception = null;
        try {
            while (iterator.hasNext()) {
                iterator.next();
            }
        } catch (DecodeValueException e) {
            exception = e;
        }

        assertNotNull("Expected DecodeValueException to be thrown", exception);
        assertTrue("Exception message should mention missing CRC field or invalid format",
                exception.getMessage().contains("Missing required CRC field (tag 63)") ||
                        exception.getMessage().contains("Invalid MPM data format"));
    }

    @Test
    public void should_wrap_string_index_out_of_bounds_as_decode_value_exception() {
        String dataWithInvalidLength = "0010AB";
        DecodeMpmIterator iterator = new DecodeMpmIterator(dataWithInvalidLength);

        DecodeValueException exception = null;
        try {
            iterator.next();
        } catch (DecodeValueException e) {
            exception = e;
        }

        assertNotNull("Expected DecodeValueException to be thrown", exception);
        assertTrue("Exception message should mention missing CRC or invalid format",
                exception.getMessage().contains("Missing required CRC field (tag 63)") ||
                        exception.getMessage().contains("Invalid MPM data format"));
    }

    @Test(expected = DecodeValueException.class)
    public void should_throw_decode_value_exception_for_null_data() {
        new DecodeMpmIterator(null);
    }

    @Test
    public void should_parse_complex_mpm_data_with_crc() {
        String mpmData = "0002015204411153039495802TR5908Example16008Istanbul6304A1B2";
        DecodeMpmIterator iterator = new DecodeMpmIterator(mpmData);

        List<DecodeMpmIterator.MpmData> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }

        assertTrue(result.size() > 0);
        assertEquals("63", result.get(result.size() - 1).getTag());
        assertEquals("A1B2", result.get(result.size() - 1).getValue());
    }

    @Test
    public void should_throw_exception_when_last_entry_not_crc() {
        String mpmData = "000201010212";
        DecodeMpmIterator iterator = new DecodeMpmIterator(mpmData);

        DecodeValueException exception = null;
        try {
            while (iterator.hasNext()) {
                iterator.next();
            }
        } catch (DecodeValueException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Missing required CRC field (tag 63)", exception.getMessage());
    }
}
