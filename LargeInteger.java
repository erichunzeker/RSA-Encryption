import java.util.Random;
import java.math.BigInteger;

public class LargeInteger {

	private final byte[] ONE = {(byte) 1};
	private final byte[] ZERO = {(byte) 0};
	private final byte[] TWO = {(byte) 2};


	private byte[] val;

	/**
	 * Construct the LargeInteger from a given byte array
	 *
	 * @param b the byte array that this LargeInteger should represent
	 */
	public LargeInteger(byte[] b) {
		val = b;
	}

	/**
	 * Construct the LargeInteger by generating a random n-bit number that is
	 * probably prime (2^-100 chance of being composite).
	 *
	 * @param n   the bitlength of the requested integer
	 * @param rnd instance of java.util.Random to use in prime generation
	 */
	public LargeInteger(int n, Random rnd) {
		val = BigInteger.probablePrime(n, rnd).toByteArray();
	}

	/**
	 * Return this LargeInteger's val
	 *
	 * @return val
	 */
	public byte[] getVal() {
		return val;
	}

	/**
	 * Return the number of bytes in val
	 *
	 * @return length of the val byte array
	 */
	public int length() {
		return val.length;
	}

	/**
	 * Add a new byte as the most significant in this
	 *
	 * @param extension the byte to place as most significant
	 */
	public void extend(byte extension) {
		byte[] newv = new byte[val.length + 1];
		newv[0] = extension;
		for (int i = 0; i < val.length; i++) {
			newv[i + 1] = val[i];
		}
		val = newv;
	}

	/**
	 * If this is negative, most significant bit will be 1 meaning most
	 * significant byte will be a negative signed number
	 *
	 * @return true if this is negative, false if positive
	 */
	public boolean isNegative() {
		return (val[0] < 0);
	}

	/**
	 * Computes the sum of this and other
	 *
	 * @param other the other LargeInteger to sum with this
	 */
	public LargeInteger add(LargeInteger other) {
		byte[] a, b;
		// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
		} else {
			a = val;
			b = other.getVal();
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}

		// Actually compute the add
		int carry = 0;
		byte[] res = new byte[a.length];
		for (int i = a.length - 1; i >= 0; i--) {
			// Be sure to bitmask so that cast of negative bytes does not
			//  introduce spurious 1 bits into result of cast
			carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

			// Assign to next byte
			res[i] = (byte) (carry & 0xFF);

			// Carry remainder over to next byte (always want to shift in 0s)
			carry = carry >>> 8;
		}

		LargeInteger res_li = new LargeInteger(res);

		// If both operands are positive, magnitude could increase as a result
		//  of addition
		if (!this.isNegative() && !other.isNegative()) {
			// If we have either a leftover carry value or we used the last
			//  bit in the most significant byte, we need to extend the result
			if (res_li.isNegative()) {
				res_li.extend((byte) carry);
			}
		}
		// Magnitude could also increase if both operands are negative
		else if (this.isNegative() && other.isNegative()) {
			if (!res_li.isNegative()) {
				res_li.extend((byte) 0xFF);
			}
		}

		// Note that result will always be the same size as biggest input
		//  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
		return res_li;
	}

	/**
	 * Negate val using two's complement representation
	 *
	 * @return negation of this
	 */
	public LargeInteger negate() {
		byte[] neg = new byte[val.length];
		int offset = 0;

		// Check to ensure we can represent negation in same length
		//  (e.g., -128 can be represented in 8 bits using two's
		//  complement, +128 requires 9)
		if (val[0] == (byte) 0x80) { // 0x80 is 10000000
			boolean needs_ex = true;
			for (int i = 1; i < val.length; i++) {
				if (val[i] != (byte) 0) {
					needs_ex = false;
					break;
				}
			}
			// if first byte is 0x80 and all others are 0, must extend
			if (needs_ex) {
				neg = new byte[val.length + 1];
				neg[0] = (byte) 0;
				offset = 1;
			}
		}

		// flip all bits
		for (int i = 0; i < val.length; i++) {
			neg[i + offset] = (byte) ~val[i];
		}

		LargeInteger neg_li = new LargeInteger(neg);

		// add 1 to complete two's complement negation
		return neg_li.add(new LargeInteger(ONE));
	}

	/**
	 * Implement subtraction as simply negation and addition
	 *
	 * @param other LargeInteger to subtract from this
	 * @return difference of this and other
	 */
	public LargeInteger subtract(LargeInteger other) {
		return this.add(other.negate());
	}

	/**
	 * Compute the product of this and other
	 *
	 * @param other LargeInteger to multiply by this
	 * @return product of this and other
	 */

	public LargeInteger multiply(LargeInteger other) {
		LargeInteger x = this, y = other;

		if (this.isNegative())
			x = this.negate();
		if (other.isNegative())
			y = other.negate();

		LargeInteger product = new LargeInteger(new byte[this.length() + other.length()]);

		for (int i = x.length() - 1; i >= 0; i--) {
			int currentBit = 1;
			for (int j = 8; j > 0; j--) {
				if ((x.getVal()[i] & currentBit) > 0)
					product = product.add(y);
				currentBit = currentBit << 1;
				y = y.shiftLeft();
			}
		}
		if (this.isNegative() == other.isNegative())
			return fit(product);
		else
			return fit(product.negate());
	}

	private LargeInteger shiftLeft() {
		byte[] shifted;
		if((val[0] & 0xC0) == 0x40)
			shifted = new byte[val.length + 1];
		else
			shifted = new byte[val.length];

		int prevMsb = 0, msb;
		for(int i = 1; i <= val.length; i++) {
			msb = (val[val.length - i] & 0x80) >> 7;
			shifted[shifted.length - i] = (byte) (val[val.length - i] << 1);
			shifted[shifted.length - i] |= prevMsb;
			prevMsb = msb;
		}
		return new LargeInteger(shifted);
	}

	public LargeInteger shiftRight() {
		byte[] shifted;
		int i;
		if(val[0] == 0 && (val[1] & 0x80) == 0x80) {
			shifted = new byte[val.length - 1];
			i = 1;
		} else {
			shifted = new byte[val.length];
			i = 0;
		}
		int prevMsb, msb;
		if(this.isNegative())
			prevMsb = 1;
		else
			prevMsb = 0;

		for(int j = 0; j < shifted.length; j++, i++) {
			msb = val[i] & 0x01;
			shifted[j] = (byte) ((val[i] >> 1) & 0x7f);
			shifted[j] |= prevMsb << 7;
			prevMsb = msb;
		}
		return fit(new LargeInteger(shifted));
	}

	/**
	 * Run the extended Euclidean algorithm on this and other
	 * @param other another LargeInteger
	 * @return an array structured as follows:
	 *   0:  the GCD of this and other
	 *   1:  a valid x value
	 *   2:  a valid y value
	 * such that this * x + other * y == GCD in index 0
	 */

	public LargeInteger[] XGCD (LargeInteger other) {
		if (other.equals(new LargeInteger(ZERO))) {
			return new LargeInteger[]{this, new LargeInteger(ONE), new LargeInteger(ZERO)};
		} else {
			LargeInteger[] result = other.XGCD(this.mod(other));
			return new LargeInteger[]{result[0], fit(result[2]), fit(result[1].subtract(this.divide(other).multiply(result[2])))};
		}
	}

	public LargeInteger mod(LargeInteger other) {
		return fit(this.subtract(other.multiply(this.divide(other))));
	}

	private LargeInteger divide(LargeInteger other) {
		LargeInteger quotient = new LargeInteger(ZERO), dividend = this, divisor = other;

		if (this.isNegative())
			dividend = this.negate();
		if (other.isNegative())
			divisor = other.negate();

		int shift = 0;
		while (!dividend.subtract(divisor).isNegative()) {
			divisor = divisor.shiftLeft();
			shift++;
		}
		divisor = divisor.shiftRight();

		while (shift > 0) {
			quotient = quotient.shiftLeft();
			if (!dividend.subtract(divisor).isNegative()) {
				dividend = dividend.subtract(divisor);
				quotient = quotient.add(new LargeInteger(ONE));
			}
			divisor = divisor.shiftRight();
			shift--;
		}

		if (this.isNegative() == other.isNegative())
			return fit(quotient);
		else
			return fit(quotient.negate());
	}

	/**
	 * Compute the result of raising this to the power of y mod n
	 *
	 * @param y exponent to raise this to
	 * @param n modulus value to use
	 * @return this^y mod n
	 */

	public LargeInteger modularExp(LargeInteger y, LargeInteger n) {
		if(y.equals(new LargeInteger(ONE).negate())) {
			LargeInteger[] GCD = n.XGCD(this);
			if (GCD[2].isNegative())
				return n.add(GCD[2]);
			else
				return GCD[2];

		} else {
			LargeInteger result = new LargeInteger(ONE);
			LargeInteger value = this.mod(n);
			LargeInteger pow = y;
			while (!pow.isNegative() && !pow.equals(new LargeInteger(ZERO))) {
				if (pow.mod(new LargeInteger(TWO)).equals(new LargeInteger(ONE))) {
					result = (result.multiply(value)).mod(n);
				}
				pow = pow.shiftRight();
				value = (value.multiply(value)).mod(n);
			}
			return result;
		}
	}

	public LargeInteger resize() {
		byte[] resize = val;
		while (resize.length > 64) {
			byte[] temp = new byte[this.length() - 1];
			for (int i = 1; i <= temp.length; i++)
				temp[i - 1] = resize[i];
			resize = temp;
		}
		return new LargeInteger(resize);
	}

	public LargeInteger fit(LargeInteger other) {
		if (other.length() > 1 && (((other.getVal()[0] == 0) && ((other.getVal()[1] & 0x80) == 0x00)) || ((other.getVal()[0] & 0xff) == 0xff && (other.getVal()[1] & 0x80) == 0x80))) {
			byte[] result = new byte[other.length() - 1];
			for (int i = 1; i <= result.length; i++)
				result[i - 1] = other.val[i];
			return fit(new LargeInteger(result));
		}
		return other;
	}

	public boolean equals(LargeInteger other) {
		if (this.length() != other.length())
			return false;
		for (int i = 0; i < this.length(); i++)
			if (this.getVal()[i] != other.getVal()[i])
				return false;
		return true;
	}
}

