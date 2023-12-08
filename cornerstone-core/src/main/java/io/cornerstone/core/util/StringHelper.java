package io.cornerstone.core.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

public class StringHelper {

	public static String pluralOf(String string) {

		if (!StringUtils.hasLength(string) || string.endsWith("ses") || string.endsWith("ies")) {
			return string;
		}

		int index = -1;
		boolean underscore = string.indexOf('_') > 0;
		if (underscore) {
			index = string.lastIndexOf('_') + 1;
		}
		else {
			// camel case
			for (int i = 0; i < string.length(); i++) {
				if (Character.isUpperCase(string.charAt(i))) {
					index = i;
				}
			}
		}
		String prefix;
		String word;
		if (index > 0) {
			prefix = string.substring(0, index);
			word = string.substring(index).toLowerCase();
		}
		else {
			prefix = "";
			word = string;
		}
		if (specialCases.containsValue(word)) {
			return string;
		}
		String plural = specialCases.get(word);
		if (plural == null) {
			int length = word.length();
			char lastLetter = word.charAt(length - 1);
			char secondLast = word.charAt(length - 2);
			if (("sxzo".indexOf(lastLetter) >= 0)
					|| ((lastLetter == 'h') && ((secondLast == 's') || (secondLast == 'c')))) {
				plural = word + "es";
			}
			else if (lastLetter == 'y') {
				if ("aeiou".indexOf(secondLast) >= 0) {
					plural = word + "s";
				}
				else {
					plural = word.substring(0, length - 1) + "ies";
				}
			}
			else {
				plural = word + "s";
			}
		}
		return prefix + (underscore ? plural : prefix.isEmpty() ? plural : StringUtils.capitalize(plural));
	}

	private static final Map<String, String> specialCases = new HashMap<>();
	static {
		specialCases.put("alumnus", "alumni");
		specialCases.put("bison", "bison");
		specialCases.put("child", "children");
		specialCases.put("datum", "data");
		specialCases.put("elf", "elves");
		specialCases.put("elk", "elk");
		specialCases.put("fish", "fish");
		specialCases.put("foot", "feet");
		specialCases.put("gentleman", "gentlemen");
		specialCases.put("gentlewoman", "gentlewomen");
		specialCases.put("goose", "geese");
		specialCases.put("grouse", "grouse");
		specialCases.put("half", "halves");
		specialCases.put("knife", "knives");
		specialCases.put("leaf", "leaves");
		specialCases.put("life", "lives");
		specialCases.put("louse", "lice");
		specialCases.put("man", "men");
		specialCases.put("money", "monies");
		specialCases.put("moose", "moose");
		specialCases.put("mouse", "mice");
		specialCases.put("ox", "oxen");
		specialCases.put("self", "selves");
		specialCases.put("sheaf", "sheaves");
		specialCases.put("sheep", "sheep");
		specialCases.put("shelf", "shelves");
		specialCases.put("squid", "squid");
		specialCases.put("thief", "thieves");
		specialCases.put("tooth", "teeth");
		specialCases.put("wharf", "wharves");
		specialCases.put("wife", "wives");
		specialCases.put("wolf", "wolves");
		specialCases.put("woman", "women");
	}

}
