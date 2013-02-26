package tahrir.tools;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public class ShortTextCompressor {

	// Maybe we should extract this interface as a standalone file?
	public static interface FrequencyTable {
		char getSymbol(int index);

		int getFrequency(int symbolIndex);

		int size();
	}

	private final static Logger logger = LoggerFactory.getLogger(ShortTextCompressor.class);

	private final HuffmanTree tree;

	private Map<Character, boolean[]> dictionary = null;

	public ShortTextCompressor(final FrequencyTable freqTable) {
		checkNotNull(freqTable, "The character frequence table can't be null.");
		tree = new HuffmanTree(freqTable);
	}

	public byte[] compress(final String toCompress) {
		checkArgument(toCompress != null, "toCompress text can't be null.");
		final char[] charArray = toCompress.toCharArray();
		// Just only one character to be encoding is unnecessary.
		// To return zero size byte array is ugly, maybe we should consider a
		// better solution for this case?
		if (charArray.length == 1)
			return new byte[0];

		// The dictionary just for encode, so we lazy build it
		if (dictionary == null) {
			dictionary = tree.buildDictionary();
		}

		final BooleanArraysToByteArray converter = new BooleanArraysToByteArray(charArray.length);
		for (final char c : charArray) {
			final boolean[] code = dictionary.get(c);
			if (code == null)
				throw new IllegalStateException(
						"Can't compress text, maybe the text and the frequency table does not match.");
			converter.add(code);

		}
		return converter.toArray();
	}

	public String deCompress(final byte[] toDeCompress) {
		checkArgument(toDeCompress != null, "toDeCompress otherData can't be null.");

		// The total number of characters to be decompress
		final int charNum = tree.leafFrequencySum();
		if (charNum == 0) {
			// for extreme case, e.g., empty string
			if (toDeCompress.length == 0)
				return "";
			else
				throw new IllegalStateException("The toDecompress otherData isn't encoded correctly.");
		}

		// The total number of character categories
		final int charCategory = tree.leafSize();

		// It means that the Huffman tree have only one node(root node),decoding
		// is unnecessary.
		if (charCategory == 1) {
			final char symbol = tree.getRoot().getSymbol();
			assert symbol != TreeNode.NULL;
			final int frequency = tree.getRoot().getPriority();
			final char[] chars = new char[frequency];
			Arrays.fill(chars, symbol);
			return new String(chars);
		}

		final ByteArrayBitIterable bitIterable = new ByteArrayBitIterable(toDeCompress);
		final StringBuilder result = new StringBuilder();
		TreeNode position = tree.getRoot();

		for (final Boolean isBitSet : bitIterable) {

			// reach the endpoint, so we can decode the character
			if (position.isLeaf()) {
				assert position.getSymbol() != TreeNode.NULL;
				result.append(position.getSymbol());

				// decoding has been completed
				if (result.length() == charNum)
					return result.toString();

				// reset position for next turn
				position = tree.getRoot();
			}
			// go through the tree path
			position = isBitSet ? position.getRight() : position.getLeft();
		}

		// The last bit must reach a leaf node
		if (position.isLeaf()) {
			result.append(position.getSymbol());
		}

		if (result.length() != charNum)
			throw new IllegalStateException("The toDecompress otherData isn't encoded correctly.");

		return result.toString();
	}

	// just for log
	static String booleanArrayToString(final boolean[] array) {
		final StringBuilder builder = new StringBuilder();
		for (final boolean b : array) {
			builder.append(b ? '1' : '0');
		}
		return builder.toString();
	}

	private static class HuffmanTree {
		private final TreeNode root;
		// the total number of all nodes.
		private int size = 0;
		// the total number of all leaf nodes.
		private int leafSize = 0;
		// the sum of all leaf nodes
		// priority(frequency). that is,the
		// total number of characters
		private int leafFrequencySum = 0;

		public HuffmanTree(final FrequencyTable table) {

			final int tableSize = table.size();

			// the priority queue which hold tree nodes will help us to build
			// a Huffman tree
			final PriorityQueue<TreeNode> queue = new PriorityQueue<TreeNode>(Math.max(1, tableSize));

			logger.debug("building priority queue.");
			for (int i = 0; i < tableSize; i++) {
				final char symbol = table.getSymbol(i);
				final int frequency = table.getFrequency(i);

				if (frequency != 0) {
					final TreeNode node = new TreeNode(symbol, frequency);
					leafFrequencySum += frequency;
					logger.debug("offer to queue: node={}", node);
					queue.offer(node);
				}
			}

			// build the Huffman tree
			// algorithm details is here:
			// http://en.nerdaholyc.com/huffman-coding-on-a-string/

			size = leafSize = queue.size();
			logger.debug("building Huffman tree.");
			while (queue.size() > 1) {
				final TreeNode left = queue.poll();
				final TreeNode right = queue.poll();
				assert (left != null) && (right != null);

				final TreeNode node = new TreeNode(left.getPriority() + right.getPriority(), left, right);
				size++;
				queue.offer(node);
			}

			root = queue.poll();
			if (root != null) {
				size++;
			}

			logger.debug("Huffman tree: {}", root);
		}

		public TreeNode getRoot() {
			return root;
		}

		@SuppressWarnings("unused")
		public int size() {
			return size;
		}

		public int leafSize() {
			return leafSize;
		}

		public int leafFrequencySum() {
			return leafFrequencySum;
		}

		public Map<Character, boolean[]> buildDictionary() {
			final Map<Character, boolean[]> dictionary = new HashMap<Character, boolean[]>();
			final int step = 0;
			traverse(root, dictionary, step, new boolean[step]);
			return dictionary;
		}

		// TODO Maybe we should implement store/restore method, so wen can
		// serialize/unserialize the tree via IO?
		//
		// store method signture
		// public byte[] store() or
		// public void store(OutputStream out)
		//
		// restore method signture
		// public HuffmanTree restore(byte[] treeData) or
		// public HuffmanTree restore(InputStream in)

		private void traverse(final TreeNode node, final Map<Character, boolean[]> dictionary, final int step,
				final boolean[] currentPath) {
			// for extreme case, e.g., empty tree?
			if (node == null)
				return;

			if (node.isLeaf()) {
				assert node.getSymbol() != TreeNode.NULL;
				dictionary.put(node.getSymbol(), currentPath);
				logger.debug("put entry into dictionary: symbol={}, code={}", node.getSymbol(),
						booleanArrayToString(currentPath));
			}

			if (node.hasLeft()) {
				final boolean[] nextPath = Arrays.copyOf(currentPath, step + 1);
				// mark left child branch with '0'(false)
				nextPath[step] = false;
				traverse(node.getLeft(), dictionary, step + 1, nextPath);
			}

			if (node.hasRight()) {
				final boolean[] nextPath = Arrays.copyOf(currentPath, step + 1);
				// mark right child branch with '1'(true)
				nextPath[step] = true;
				traverse(node.getRight(), dictionary, step + 1, nextPath);
			}
		}

	}

	private static class TreeNode implements Comparable<TreeNode> {
		public static final char NULL = '\0';
		private final int priority;
		private final char symbol;
		private TreeNode left;
		private TreeNode right;

		public TreeNode(final char symbol, final int priority) {
			this.symbol = symbol;
			this.priority = priority;
			left = null;
			right = null;
		}

		public TreeNode(final int priority, final TreeNode left, final TreeNode right) {
			symbol = NULL;
			this.priority = priority;
			this.left = left;
			this.right = right;
		}

		public TreeNode getLeft() {
			return left;
		}

		@SuppressWarnings("unused")
		public void setLeft(final TreeNode left) {
			this.left = left;
		}

		public TreeNode getRight() {
			return right;
		}

		@SuppressWarnings("unused")
		public void setRight(final TreeNode right) {
			this.right = right;
		}

		public int getPriority() {
			return priority;
		}

		public char getSymbol() {
			return symbol;
		}

		public boolean isLeaf() {
			return !(hasLeft() || hasRight());
		}

		public boolean hasLeft() {
			return left != null;
		}

		public boolean hasRight() {
			return right != null;
		}

		@Override
		public int compareTo(final TreeNode o) {
			return getPriority() < o.getPriority() ? -1 : (getPriority() == o.getPriority() ? 0 : 1);
		}

		@Override
		public String toString() {
			final ToStringHelper helper = Objects.toStringHelper(this).add("symbol", symbol == NULL ? "null" : symbol)
					.add("priority", priority);
			if (hasLeft()) {
				helper.add("left", left);
			}
			if (hasRight()) {
				helper.add("right", right);
			}
			return helper.toString();
		}
	}

	// auxiliary class
	// To iterate bit via a byte array
	private static class ByteArrayBitIterable implements Iterable<Boolean> {
		private final byte[] array;

		public ByteArrayBitIterable(final byte[] array) {
			this.array = array;
		}

		@Override
		public Iterator<Boolean> iterator() {
			return new Iterator<Boolean>() {
				private int bitIndex = 0;
				private int byteIndex = 0;

				@Override
				public boolean hasNext() {
					return (byteIndex < array.length) && (bitIndex < Byte.SIZE);
				}

				@Override
				public Boolean next() {
					final Boolean current = isBitSet(array[byteIndex], bitIndex);
					bitIndex++;
					if (bitIndex == 8) {
						bitIndex = 0;
						byteIndex++;
					}
					return current;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				private Boolean isBitSet(final byte b, final int bitIndex) {
					return ((b >> (7 - bitIndex)) & 0x01) == 1;
				}
			};
		}

	}

	// auxiliary class
	// To merge all of boolean arrays to a byte array
	private static class BooleanArraysToByteArray {
		private final List<boolean[]> arrayList;

		public BooleanArraysToByteArray(final int initialCapacity) {
			arrayList = new ArrayList<boolean[]>(initialCapacity);
		}

		public void add(final boolean[] array) {
			arrayList.add(array);
		}

		public byte[] toArray() {
			int length = 0;
			for (final boolean[] array : arrayList) {
				length += array.length;
			}

			// for extreme case, e.g., empty boolean array
			if (length == 0)
				return new byte[0];

			final byte[] result = new byte[(length + 7) / 8];
			Arrays.fill(result, (byte) 0);
			// Iterate all of arrays and record the current position
			int pos = 0;
			for (final boolean[] array : arrayList) {
				for (int i = 0; i < array.length; i++, pos++) {
					// It means current bit is set
					if (array[i]) {
						result[pos / 8] |= 1 << (7 - pos % 8);
					}
				}
			}
			return result;
		}
	}

}
