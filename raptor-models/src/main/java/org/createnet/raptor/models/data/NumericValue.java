/*
 * Copyright 2017 FBK/CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.models.data;

import org.createnet.raptor.models.exception.ValueConversionException;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class NumericValue extends Number {

    final protected Object raw;

    public NumericValue(Object raw) {
        this.raw = raw;
    }

    public boolean isNumber() {
        return isFloat() || isDouble() || isInteger() || isLong();
    }

    public boolean isFloat() {
        return raw != null && raw instanceof Float;
    }

    public boolean isLong() {
        return raw != null && raw instanceof Long;
    }

    public boolean isInteger() {
        return raw != null && raw instanceof Integer;
    }

    public boolean isDouble() {
        return raw != null && raw instanceof Double;
    }

    public boolean isString() {
        return raw != null && raw instanceof String;
    }

    /**
     * Return the value as integer
     * @return
     */    
    public Integer getInteger() {
        try {

            if (raw == null) {
                return null;
            }

            if (isInteger()) {
                return (Integer) raw;
            } else if (isLong()) {
                return ((Long) raw).intValue();
            } else if (isString()) {
                return Integer.valueOf((String) raw);
            }

        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }

        return null;
    }

    /**
     * Return the value as long
     * @return
     */    
    public Long getLong() {
        try {

            if (raw == null) {
                return null;
            }

            if (isLong()) {
                return (Long) raw;
            } else if (isInteger()) {
                return ((Integer) raw).longValue();
            } else if (isString()) {
                return Long.valueOf((String) raw);
            }

        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }

        return null;
    }

    /**
     * Return the value as float
     * @return
     */    
    public Float getFloat() {
        try {

            if (raw == null) {
                return null;
            }

            if (isFloat()) {
                return (Float) raw;
            } else if (isDouble()) {
                return ((Double) raw).floatValue();
            } else if (isLong()) {
                return ((Long) raw).floatValue();
            } else if (isInteger()) {
                return ((Integer) raw).floatValue();
            } else if (isString()) {
                return Float.valueOf((String) raw);
            }

        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }

        return null;
    }

    /**
     * Return the value as double 
     * @return
     */
    public Double getDouble() {
        try {

            if (raw == null) {
                return null;
            }

            if (isFloat()) {
                return ((Float) raw).doubleValue();
            } else if (isDouble()) {
                return (Double) raw;
            } else if (isLong()) {
                return ((Long) raw).doubleValue();
            } else if (isInteger()) {
                return ((Integer) raw).doubleValue();
            } else if (isString()) {
                return Double.valueOf((String) raw);
            }

        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }

        return null;
    }

    @Override
    public int intValue() {
        return getInteger();
    }

    @Override
    public long longValue() {
        return getLong();
    }

    @Override
    public float floatValue() {
        return getFloat();
    }

    @Override
    public double doubleValue() {
        return getDouble();
    }

}
