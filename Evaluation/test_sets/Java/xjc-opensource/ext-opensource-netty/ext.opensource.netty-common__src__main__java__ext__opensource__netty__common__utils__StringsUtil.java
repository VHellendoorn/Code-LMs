package ext.opensource.netty.common.utils;

import java.util.UUID;

/**
 * @author ben
 * @Title: basic
 * @Description:
 **/

public class StringsUtil {
	public static final String EMPTY = "";
	
	/**
	 * 首字母转小写
	 * 
	 * @param s
	 * @return
	 */
	public static String toLowerCaseFirstOne(String s) {
		if (null == s) {
			return s;
		}
		if (Character.isLowerCase(s.charAt(0))) {
			return s;
		} else {
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
		}
	}

	/**
	 * 首字母转大写
	 * 
	 * @param s
	 * @return
	 */
	public static String toUpperCaseFirstOne(String s) {
		if (null == s) {
			return s;
		}
		if (Character.isUpperCase(s.charAt(0))) {
			return s;
		} else {
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
		}
	}
	
	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}
	
	public static String str(CharSequence cs) {
		return null == cs ? null : cs.toString();
	}
	
	public static String removeSuffix(CharSequence str, CharSequence suffix) {
		if (isEmpty(str) || isEmpty(suffix)) {
			return str(str);
		}

		final String str2 = str.toString();
		if (str2.endsWith(suffix.toString())) {
			// 截取前半段
			return subPre(str2, str2.length() - suffix.length());
		}
		return str2;
	}
	
	public static String subPre(CharSequence string, int toIndex) {
		return sub(string, 0, toIndex);
	}
	
	public static String sub(CharSequence str, int fromIndex, int toIndex) {
		if (isEmpty(str)) {
			return str(str);
		}
		int len = str.length();

		if (fromIndex < 0) {
			fromIndex = len + fromIndex;
			if (fromIndex < 0) {
				fromIndex = 0;
			}
		} else if (fromIndex > len) {
			fromIndex = len;
		}

		if (toIndex < 0) {
			toIndex = len + toIndex;
			if (toIndex < 0) {
				toIndex = len;
			}
		} else if (toIndex > len) {
			toIndex = len;
		}

		if (toIndex < fromIndex) {
			int tmp = fromIndex;
			fromIndex = toIndex;
			toIndex = tmp;
		}

		if (fromIndex == toIndex) {
			return EMPTY;
		}

		return str.toString().substring(fromIndex, toIndex);
	}
	
	

	public static int count(String str, String subStr) {
		String[] strary = str.split(subStr);
		return strary.length;
	}
	
	public static String getUuid() {
		return UUID.randomUUID().toString().replace("-", "").toLowerCase();
	}
}
