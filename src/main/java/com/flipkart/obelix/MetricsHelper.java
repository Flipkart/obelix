package com.flipkart.obelix;

import java.util.BitSet;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;

public class MetricsHelper
{
	private final static Meter bitAndsMeter = Metrics.newMeter(Dimension.class, "number_of_bit_ands", "listings", TimeUnit.SECONDS);

	public static void markBitAnds(BitSet value)
	{
		bitAndsMeter.mark(value.size());
	}

	public static long numBitAnds()
	{
		return bitAndsMeter.count();
	}
}
