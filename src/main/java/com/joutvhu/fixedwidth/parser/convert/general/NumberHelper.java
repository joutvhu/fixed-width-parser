package com.joutvhu.fixedwidth.parser.convert.general;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormatSymbols;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Check field is number field
 *
 * @author Giao Ho
 * @since 1.0.1
 */
public interface NumberHelper {
    default boolean isNumeric(FixedTypeInfo info, FixedParseStrategy strategy) {
        Class<?> type = info.getType();
        boolean isDecimal = TypeConstants.DECIMAL_NUMBER_TYPES.contains(type);
        if (!TypeConstants.INTEGER_NUMBER_TYPES.contains(type) && !isDecimal)
            return false;
        this.setIsDecimal(isDecimal);
        return true;
    }

    void setIsDecimal(boolean isDecimal);

    default DecimalFormat getDecimalFormat(FixedFormat format) {
        if (format != null && CommonUtil.isNotBlank(format.format())) {
            DecimalFormat decimalFormat = new DecimalFormat(format.format(), getFormatSymbols(format.formatSymbols()));
            decimalFormat.setParseBigDecimal(true);
            return decimalFormat;
        }
        return null;
    }

    default DecimalFormatSymbols getFormatSymbols(FixedFormatSymbols formatSymbols) {
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols
                .getInstance(Locale.getDefault(Locale.Category.FORMAT));

        if (CommonUtil.isNotBlank(formatSymbols.decimalSeparator()))
            decimalFormatSymbols.setDecimalSeparator(formatSymbols.decimalSeparator().charAt(0));
        if (CommonUtil.isNotBlank(formatSymbols.groupingSeparator()))
            decimalFormatSymbols.setGroupingSeparator(formatSymbols.groupingSeparator().charAt(0));
        if (CommonUtil.isNotBlank(formatSymbols.patternSeparator()))
            decimalFormatSymbols.setPatternSeparator(formatSymbols.patternSeparator().charAt(0));
        if (CommonUtil.isNotBlank(formatSymbols.percent()))
            decimalFormatSymbols.setPercent(formatSymbols.percent().charAt(0));
        if (CommonUtil.isNotBlank(formatSymbols.zeroDigit()))
            decimalFormatSymbols.setZeroDigit(formatSymbols.zeroDigit().charAt(0));
        if (CommonUtil.isNotBlank(formatSymbols.digit()))
            decimalFormatSymbols.setDigit(formatSymbols.digit().charAt(0));
        if (CommonUtil.isNotBlank(formatSymbols.minusSign()))
            decimalFormatSymbols.setMinusSign(formatSymbols.minusSign().charAt(0));
        if (CommonUtil.isNotBlank(formatSymbols.perMill()))
            decimalFormatSymbols.setPerMill(formatSymbols.perMill().charAt(0));
        if (CommonUtil.isNotBlank(formatSymbols.monetarySeparator()))
            decimalFormatSymbols.setMonetaryDecimalSeparator(formatSymbols.monetarySeparator().charAt(0));

        if (CommonUtil.isNotBlank(formatSymbols.exponentialSeparator()))
            decimalFormatSymbols.setExponentSeparator(formatSymbols.exponentialSeparator());
        if (CommonUtil.isNotBlank(formatSymbols.infinity()))
            decimalFormatSymbols.setInfinity(formatSymbols.infinity());
        if (CommonUtil.isNotBlank(formatSymbols.naN()))
            decimalFormatSymbols.setNaN(formatSymbols.naN());
        if (CommonUtil.isNotBlank(formatSymbols.internationalCurrencySymbol()))
            decimalFormatSymbols.setInternationalCurrencySymbol(formatSymbols.internationalCurrencySymbol());
        if (CommonUtil.isNotBlank(formatSymbols.currencySymbol()))
            decimalFormatSymbols.setCurrencySymbol(formatSymbols.currencySymbol());

        return decimalFormatSymbols;
    }
}
