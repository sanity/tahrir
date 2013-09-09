package tahrir.tools;

import java.util.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.tools.ShortTextCompressor.FrequencyTable;

public class ShortTextCompressorTest {

	private final static FreqTableFactory englishFactory = new EnglishFamilyFreqTableFactory();
	private final static FreqTableFactory chineseFactory = new ChineseFamilyFreqTableFactory();

	@Test
	public void testEmpty() {
		test("");
	}

	@Test
	public void testOneSymbol() {
		test("a");
	}

	@Test
	public void testDuplicatedSymbols() {
		test("bbbbbbb");
	}

	@Test
	public void testEverySymbol() {
		final char[] chars = new char[256];
		for (int i = 0; i < 256; i++) {
			chars[i] = (char) i;
		}
		test(new String(chars));
	}

	@Test
	public void testChinese() {
		// empty
		test(chineseFactory, "");
		// one symbol
		test(chineseFactory, "中");
		// duplicated symbols
		test(chineseFactory, "中中中中中中中中中中中中");
		// mixed symbols(Chinese and English)
		test(chineseFactory,
				"伴随着 Jython 的出现，使 Java 代码和 Python 代码做到了无缝交互。而 Python 自身强大的字符处理能力和动态执行能力更佳弥补了大型 J2EE 应用的一些先天缺陷。");
	}

	private static void test(final String text) {
		test(englishFactory, text);
	}

	private static void test(final FreqTableFactory factory, final String text) {
		final FrequencyTable table = factory.make(text);
		final ShortTextCompressor compressor = new ShortTextCompressor(table);
		final byte[] bytes = compressor.compress(text);
		final String deCompress = compressor.deCompress(bytes);
		Assert.assertEquals(text, deCompress);
	}

	interface FreqTableFactory {
		public FrequencyTable make(final String toCompress);
	}

	static class EnglishFamilyFreqTableFactory implements FreqTableFactory {

		@Override
		public FrequencyTable make(final String toCompress) {
			return new FrequencyTable() {

				private final int[] frequency = new int[256];

				@Override
				public char getSymbol(final int index) {
					if (index < 0 || index > 255)
						throw new ArrayIndexOutOfBoundsException();
					return (char) index;
				}

				@Override
				public int getFrequency(final int symbolIndex) {
					if (symbolIndex < 0 || symbolIndex > 255)
						throw new ArrayIndexOutOfBoundsException();
					return frequency[symbolIndex];
				}

				@Override
				public int size() {
					return 256;
				}

				FrequencyTable init(final String text) {
					Arrays.fill(frequency, 0);
					final char[] charArray = text.toCharArray();
					for (int i = 0; i < charArray.length; i++) {
						frequency[charArray[i]] = frequency[charArray[i]] + 1;
					}

					return this;
				}
			}.init(toCompress);

		}

	}

	static class ChineseFamilyFreqTableFactory implements FreqTableFactory {

		@Override
		public FrequencyTable make(final String toCompress) {
			return new FrequencyTable() {
				private final Map<Character, Integer> map = new LinkedHashMap<Character, Integer>();
				private Character[] indexs;

				@Override
				public int size() {
					return map.size();
				}

				@Override
				public char getSymbol(final int index) {
					if (index < 0 || index > (size() - 1))
						throw new ArrayIndexOutOfBoundsException();
					return indexs[index];
				}

				@Override
				public int getFrequency(final int symbolIndex) {
					if (symbolIndex < 0 || symbolIndex > (size() - 1))
						throw new ArrayIndexOutOfBoundsException();
					final Integer frequency = map.get(indexs[symbolIndex]);
					return frequency == null ? 0 : frequency.intValue();
				}

				FrequencyTable init(final String text) {
					final char[] charArray = text.toCharArray();
					for (final char c : charArray) {
						if (map.containsKey(c)) {
							map.put(c, map.get(c) + 1);
						} else {
							map.put(c, 1);
						}
					}
					indexs = map.keySet().toArray(new Character[map.size()]);
					return this;
				}
			}.init(toCompress);
		}

	}

}
