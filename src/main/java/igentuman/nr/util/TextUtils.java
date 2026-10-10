package igentuman.nr.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.text.DecimalFormat;
import java.util.Locale;

public class TextUtils
{
	public static MutableComponent __(String text, Object... pArgs)
	{
		return Component.translatable(text, pArgs);
	}

	public static MutableComponent applyFormat(Component component, ChatFormatting... color)
	{
		Style style = component.getStyle();
		for(ChatFormatting format : color)
			style = style.applyFormat(format);
		return component.copy().setStyle(style);
	}

	public static String numberFormat(double value)
	{
		String preffix = "";
		if(value < 1 && value > 0) {
			preffix = "0";
		}
		if(value > -1 && value < 0) {
			preffix = "0";
		}
		DecimalFormat df = new DecimalFormat("#.0");
		if (value == (int) value) {
			return String.valueOf((int)value);
		}
		return  preffix+df.format(value);
	}

	public static String numberFormat(String value)
	{
		return numberFormat(Double.parseDouble(value));
	}

	public static String roundFormat(double value)
	{
		if(Math.round(value) == 0) {
			return "0";
		}
		String preffix = "";
		if(value < 1 && value > 0) {
			preffix = "0";
		}
		if(value > -1 && value < 0) {
			preffix = "0";
		}
		DecimalFormat df = new DecimalFormat("#.0");
		if(preffix.isEmpty()) {
			df = new DecimalFormat("#");
		}

		if (value == (int) value) {
			return String.valueOf((int)value);
		}
		String formatted = preffix+df.format(value);
		if (formatted.equals("NaN")) {
			return "0";
		}
		return  preffix+df.format(value);
	}

	public static String scaledFormat(double value)
	{
		if(value >= 1000000000000D) {
			return numberFormat(value/1000000000000D)+" T";
		}
		if(value >= 1000000000) {
			return numberFormat(value/1000000000)+" G";
		}
		if(value >= 1000000) {
			return numberFormat(value/1000000)+" M";
		}
		if(value >= 1000) {
			return numberFormat(value/1000)+" k";
		}
		return numberFormat(value);
	}

	public static String convertToName(String key)
	{
		StringBuilder result = new StringBuilder();
		String[] parts = key.split("_|/");
		for(String l: parts) {
			if(l.isEmpty()) continue;
			if(result.length() == 0) {
				result = new StringBuilder(l.substring(0, 1).toUpperCase() + l.substring(1));
			} else {
				result.append(" ").append(l.substring(0, 1).toUpperCase()).append(l.substring(1));
			}
		}
		return result.toString();
	}

	public static String formatLiquid(int val)
	{
		if (val < 1000) {
			return val + " mB";
		}
		return TextUtils.numberFormat((double) val/1000)+" B";
	}


	public static String formatMass(long mass)
	{
		if(mass >= 1000000000000L) {
			return TextUtils.numberFormat(mass/1000000000000d)+" TT";
		}
		if(mass >= 1000000000) {
			return TextUtils.numberFormat(mass/1000000000d)+" GT";
		}
		if(mass >= 1000000) {
			return TextUtils.numberFormat(mass/1000000d)+" MT";
		}
		if(mass >= 1000) {
			return TextUtils.numberFormat(mass/1000d)+" kT";
		}
		return TextUtils.numberFormat(mass)+" T";
	}

	public static String formatSv(double sv) {
		return formatSiSmall(sv, "Sv");
	}

	public static String formatSvPerHour(double svh) {
		return formatSiSmall(svh, "Sv/h");
	}

	public static String formatBq(double bq) {
		return formatSi(bq, "Bq");
	}

	public static String formatSi(double value, String unit) {
		double abs = Math.abs(value);
		if (abs < 1e-9) {
			return "0 " + unit;
		}
		if (abs >= 1e15) {
			return String.format(Locale.US, "%.2f P%s", value / 1e15, unit);
		}
		if (abs >= 1e12) {
			return String.format(Locale.US, "%.2f T%s", value / 1e12, unit);
		}
		if (abs >= 1e9) {
			return String.format(Locale.US, "%.2f G%s", value / 1e9, unit);
		}
		if (abs >= 1e6) {
			return String.format(Locale.US, "%.2f M%s", value / 1e6, unit);
		}
		if (abs >= 1e3) {
			return String.format(Locale.US, "%.2f k%s", value / 1e3, unit);
		}
		if (abs >= 1) {
			return String.format(Locale.US, "%.2f %s", value, unit);
		}
		return formatSiSmall(value, unit);
	}

	public static String formatSiSmall(double value, String unit) {
		double abs = Math.abs(value);
		if (abs < 1e-9) {
			return "0 " + unit;
		}
		if (abs >= 1) {
			return String.format(Locale.US, "%.2f %s", value, unit);
		}
		if (abs >= 1e-3) {
			return String.format(Locale.US, "%.2f m%s", value * 1e3, unit);
		}
		if (abs >= 1e-6) {
			return String.format(Locale.US, "%.2f µ%s", value * 1e6, unit);
		}
		return String.format(Locale.US, "%.2f n%s", value * 1e9, unit);
	}

	public static String formatRads(long radiation) {
		if(radiation >= 1000000) {
			return String.format(Locale.US,"%.2f", (float)radiation/1000000)+" Rad";
		}
		if(radiation >= 1000) {
			return String.format(Locale.US,"%.2f", (float)radiation/1000)+" mRad";
		}
		return String.format(Locale.US,"%.2f", (float)radiation)+" uRad";
	}
}
