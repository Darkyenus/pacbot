
package lego.util;

/** High performance lightweight stack.
 *
 * Created by jIRKA on 11.11.2014. */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class ByteArrayArrayList {
	private byte[][] internal;
	private byte size = 0;

	public ByteArrayArrayList (int initialSize) {
		internal = new byte[initialSize][0];
	}

	public boolean isEmpty () {
		return size == 0;
	}

	public void clear () {
		size = 0;
	}

	public byte size () {
		return size;
	}

	public byte[] peek () {
		return internal[size - 1];
	}

	public byte[] get (byte index) {
		return internal[index];
	}

	public void add (byte[] value) {
		int currentSize = internal.length;
		if (size == currentSize) {
			byte[][] newInternal = new byte[currentSize << 2][0];
			System.arraycopy(internal, 0, newInternal, 0, currentSize);
			internal = newInternal;
		}
		internal[size] = value;
		size++;
	}

	public byte[] getCopyAsArray () {
		byte[] res = new byte[size];
		System.arraycopy(internal, 0, res, 0, size);
		return res;
	}

}
