package at.caspase.rxdroid;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

/**
 * Very simple class for handling fractions.
 * 
 * @author sebbo
 */
public class Fraction extends Number implements Serializable, Comparable<Number> {
	private static final long serialVersionUID = 2050536341303052796L;
	private static final String TAG = Fraction.class.getName();
	private static final Pattern REGEX = Pattern.compile("^\\s*(?:(-?\\d+)\\s+)?\\s*(?:(-?\\d+)\\s*/\\s*(\\d+)\\s*)\\s*$");
		
	private static boolean sDisplayMixedNumbers = true;
		
	private int mNominator = 0;
	private int mDenominator = 1;
	
	/**
	 * A zero value, just for convenience.
	 */
	public static final Fraction ZERO = new Fraction(0);
	
	/**
	 * Construct a fraction of the value zero.
	 */
	public Fraction() {}
			
	/**
	 * Construct a fraction from a whole number.
	 */
	public Fraction(int wholeNum) {
		mNominator = wholeNum;
	}
		
	/**
	 * Construct a fraction from a nominator and denominator.
	 * 
	 * When initializing a negative fraction, always specify the
	 * nominator negative.
	 * 
	 * @throws IllegalArgumentException if {@code denominator <= 0}
	 */
	public Fraction(int nominator, int denominator) {
		construct(0, nominator, denominator);
	}

	/**
	 * Construct a fraction from a mixed number format.
	 * 
	 * When initializing negative fractions, only specify the
	 * wholeNum parameter as negative.
	 * 
	 * @throws IllegalArgumentException if {@code denominator <= 0} or {@code wholeNum != 0 && nominator < 0}
	 */
	public Fraction(int wholeNum, int nominator, int denominator) {
		construct(wholeNum, nominator, denominator);
	}
	
	/**
	 * Add two fractions.
	 */
	public Fraction plus(final Fraction other)
	{
		int nominator, denominator;
		
		if(this.mDenominator != other.mDenominator)
		{
			int lcm = findLCM(this.mDenominator, other.mDenominator);
			
			int multThis = lcm / this.mDenominator;
			int multOther = lcm / other.mDenominator;
			
			denominator = lcm;
			nominator = (this.mNominator * multThis) + (other.mNominator * multOther);
		}
		else
		{
			nominator = this.mNominator + other.mNominator;
			denominator = this.mDenominator;
		}
					
		return new Fraction(nominator, denominator);
	}
	
	public Fraction plus(Integer integer) {
		return plus(new Fraction(integer));		
	}
	
	/**
	 * Subtract two fractions
	 */
	public Fraction minus(final Fraction other) {
		return plus(other.negate());
	}
	
	public Fraction minus(Integer integer) {
		return minus(new Fraction(integer));		
	}
	
	/**
	 * Return the fraction's negative.
	 */
	public Fraction negate() {
		return new Fraction(-mNominator, mDenominator);
	}
	
	public boolean equals(final Number other)
	{
		// TODO ugly, change to equalizing to the same denominator and
		// then comparing the nominators!
		return this.doubleValue() == other.doubleValue();
	}
	
	@Override
	public int compareTo(Number other)
	{
		if(this.equals(other))
			return 0;
		return this.doubleValue() < other.doubleValue() ? -1 : 1;
	}
	
	@Override
	public String toString()
	{
		if(mDenominator == 1)
			return Integer.toString(mNominator);
		
		int wholeNum = mNominator / mDenominator;
		int nominator = mNominator % mDenominator;
		
		if(nominator != 0)
		{
			if(sDisplayMixedNumbers)
				return (wholeNum == 0 ? "" : wholeNum + " ") + (wholeNum < 0 ? Math.abs(nominator) : nominator) + "/" + mDenominator;
			else
				return mNominator + "/" + mDenominator;			
		}
			
		return Integer.toString(wholeNum);
	}
	
	@Override
	public double doubleValue() {
		return (double) mNominator / mDenominator;
	}

	@Override
	public float floatValue() {
		return (float) doubleValue();
	}

	@Override
	public int intValue() {
		return (int) longValue();
	}

	@Override
	public long longValue() {
		return Math.round(doubleValue());
	}
	
	public static Fraction decode(String string) 
	{
		Log.d(TAG, "decode: string=" + string);
		
		int wholeNum = 0, nominator = 0, denominator = 1;
		
		// this matcher will always have a group count of three,
		// but the subgroups that are specified as optional will
		// be null!		
		Matcher matcher = REGEX.matcher(string);
		if(matcher.find())
		{			
			for(int g = 0; g != matcher.groupCount() + 1; ++g)
				Log.d(TAG, "    group[" + g + "]=" + matcher.group(g));
						
			if(matcher.groupCount() != 3)
				throw new NumberFormatException();			
			
			if(matcher.group(1) != null)
				wholeNum = Integer.parseInt(matcher.group(1), 10);
			
			if(matcher.group(2) != null)
			{
				assert matcher.group(3) != null;
				
				nominator = Integer.parseInt(matcher.group(2), 10);
				denominator = Integer.parseInt(matcher.group(3), 10);
				
				if(denominator == 0)
					throw new NumberFormatException();
			}
		}		
		else
		{
			string = string.trim();
			
			if(string.length() == 0)
				throw new NumberFormatException();
			
			// FIXME the regex currently fails to handle single numbers correctly,
			// so we assume try to parse the whole string in case the regex-matching
			// failed
			wholeNum = Integer.parseInt(string, 10);
		}
		
		Log.d(TAG, "  " + wholeNum + " " + nominator + "/" + denominator);
		Log.d(TAG, "-------------------------------------");
		
				
		return new Fraction(wholeNum, nominator, denominator);
	}
	

	public static void setDisplayMixedNumbers(boolean displayMixedNumbers) {
		sDisplayMixedNumbers = displayMixedNumbers;
	}
	
	private void construct(int wholeNum, int nominator, int denominator)
	{
		if(denominator <= 0)
			throw new IllegalArgumentException("Denominator must be greater than zero");
		
		if(wholeNum != 0 && nominator < 0)
			throw new IllegalArgumentException("Nominator must not be negative if wholeNum is non-zero");
				
		// set mNominator, even though we divide it by the GCD later,
		// so as to pass the original argument to this function to
		// findGCD
		if(wholeNum >= 0)
			mNominator = wholeNum * denominator + nominator;
		else
			mNominator = wholeNum * denominator - nominator;
		
		// the sign, if present, has been moved to the nominator by now
		denominator = Math.abs(denominator);
		
		final int divisor = findGCD(Math.abs(nominator), denominator);
		
		mNominator = mNominator / divisor;
		mDenominator = denominator / divisor;		
	}
					
	private static int findLCM(int n1, int n2)
	{
		int product = n1 * n2;
		
		do {
			if(n1 < n2) {
				int tmp = n1;
				n1 = n2;
				n2 = tmp;
			}
			n1 = n1 % n2;
		} while(n1 != 0);
		
		return product / n2;
	}
	
	private static int findGCD(int n1, int n2)
	{
		if(n2 == 0)
			return n1;
		return findGCD(n2, n1 % n2);
	}

}