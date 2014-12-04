
package lego.util;

import lego.api.controllers.EnvironmentController;

/** High performance lightweight stack.
 *
 * Created by jIRKA on 11.11.2014. */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class DirectionArrayList {
	private EnvironmentController.Direction[] internal;
	private byte size = 0;

	public DirectionArrayList (int initialSize) {
		internal = new EnvironmentController.Direction[initialSize];
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

	public EnvironmentController.Direction peek () {
		return internal[size - 1];
	}

	public EnvironmentController.Direction get (byte index) {
		return internal[index];
	}

	public void add (EnvironmentController.Direction value) {
		int currentSize = internal.length;
		if (size == currentSize) {
			EnvironmentController.Direction[] newInternal = new EnvironmentController.Direction[currentSize << 1];
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
