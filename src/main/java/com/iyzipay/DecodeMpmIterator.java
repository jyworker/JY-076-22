package com.iyzipay;

import com.iyzipay.exception.DecodeValueException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DecodeMpmIterator implements Iterator<DecodeMpmIterator.MpmData> {

    private static final String CRC_TAG = "63";
    private static final int TAG_LENGTH = 2;
    private static final int LENGTH_LENGTH = 2;

    private final String data;
    private int currentIndex;
    private boolean crcFound;

    public DecodeMpmIterator(String data) {
        if (data == null) {
            throw new DecodeValueException("MPM data cannot be null");
        }
        this.data = data;
        this.currentIndex = 0;
        this.crcFound = false;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < data.length();
    }

    @Override
    public MpmData next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        try {
            String tag = data.substring(currentIndex, currentIndex + TAG_LENGTH);
            currentIndex += TAG_LENGTH;

            String lengthStr = data.substring(currentIndex, currentIndex + LENGTH_LENGTH);
            currentIndex += LENGTH_LENGTH;

            int length;
            try {
                length = Integer.parseInt(lengthStr);
            } catch (NumberFormatException e) {
                throw new DecodeValueException("Invalid length format for tag: " + tag, e);
            }

            String value = data.substring(currentIndex, currentIndex + length);
            currentIndex += length;

            if (CRC_TAG.equals(tag)) {
                crcFound = true;
            }

            if (!hasNext() && !crcFound) {
                throw new DecodeValueException("Missing required CRC field (tag 63)");
            }

            return new MpmData(tag, lengthStr, value);

        } catch (StringIndexOutOfBoundsException e) {
            if (!crcFound) {
                throw new DecodeValueException("Missing required CRC field (tag 63)", e);
            }
            throw new DecodeValueException("Invalid MPM data format", e);
        }
    }

    public static class MpmData {
        private final String tag;
        private final String length;
        private final String value;

        public MpmData(String tag, String length, String value) {
            this.tag = tag;
            this.length = length;
            this.value = value;
        }

        public String getTag() {
            return tag;
        }

        public String getLength() {
            return length;
        }

        public String getValue() {
            return value;
        }
    }
}
