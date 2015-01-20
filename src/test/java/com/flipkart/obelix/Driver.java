package com.flipkart.obelix;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.stats.Snapshot;

public class Driver
{

	public static void main(String[] args) throws IOException
	{
		int testNumber = 2;
		int numLoops = 1000;
		try
		{
			if (args.length > 0)
			{
				testNumber = Integer.parseInt(args[0]);
			}
			if (args.length > 1)
			{
				numLoops = Integer.parseInt(args[1]);
			}
		} catch (NumberFormatException e)
		{

		}
		Collection<DimensionName> inputDimensions = Lists.newArrayList(DimensionName.SOURCE, DimensionName.DESTINATION, DimensionName.VENDOR,
				DimensionName.SIZE, DimensionName.CATEGORY);
		Collection<DimensionName> outputDimensions = Lists.newArrayList(DimensionName.SLA, DimensionName.COST);
		DimensionGroup dimensionGroup = new DimensionGroup(inputDimensions, outputDimensions);
		MasterDataManager mdm = new LocalMasterDataManager();

		Dimension source = new Dimension(DimensionName.SOURCE, Lists.newArrayList(1, 2, 3, 45, 6, 7, 8, 9, 10), mdm);
		Dimension destination = new Dimension(DimensionName.DESTINATION, Lists.newArrayList(1, 2, 3, 45, 6, 7, 8, 9, 10), mdm);
		Dimension vendor = new Dimension(DimensionName.VENDOR, Lists.newArrayList(1, 2, 3, 45, 6, 7, 8, 9, 10), mdm);
		Dimension size = new Dimension(DimensionName.SIZE, Lists.newArrayList(1), mdm);
		Dimension category = new Dimension(DimensionName.CATEGORY, Lists.newArrayList(4), mdm);

		Dimension sla = new Dimension(DimensionName.SLA, Lists.newArrayList(2), mdm);
		Dimension cost = new Dimension(DimensionName.COST, Lists.newArrayList(2), mdm);

		dimensionGroup.addRule(Lists.newArrayList(source, destination, vendor, size, category), Lists.newArrayList(sla, cost));

		Dimension categoryInput = new Dimension(DimensionName.CATEGORY, Lists.newArrayList(4), mdm);
		Dimension sourceInput = new Dimension(DimensionName.SOURCE, Lists.newArrayList(45), mdm);

		Collection<Dimension> output = dimensionGroup.match(Lists.newArrayList(categoryInput, sourceInput));
		// Collection<Dimension> output = dimensionGroup.match(Lists.newArrayList(source, destination, vendor, size, category));
		for (Dimension dimension : output)
		{
			System.out.println(dimension.value());
		}
		List<Rule> rules = generateRules(mdm);
		dimensionGroup = new DimensionGroup(inputDimensions, outputDimensions);
		long start = System.currentTimeMillis();
		for (Rule rule : rules)
		{
			dimensionGroup.addRule(rule.getInput().values(), rule.getOutputDimensions());
		}
		long end = System.currentTimeMillis();
		System.out.println("Time taken to add " + rules.size() + " rules in millis " + (end - start));
		switch (testNumber)
		{
		case 1:
			runWorstCaseRule(dimensionGroup, rules, mdm, false);
			break;
		case 2:
			runWorstCaseRuleInLoop(dimensionGroup, rules, mdm, numLoops);
			break;
		default:
			runAllRules(dimensionGroup, rules);
			break;
		}
	}

	private static void runWorstCaseRuleInLoop(DimensionGroup dimensionGroup, List<Rule> rules, MasterDataManager mdm, int numLoops)
	{
		Timer timer = Metrics.newTimer(Driver.class, "WorstcaseRule.Timer", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
		for (int i = 0; i < numLoops; i++)
		{
			TimerContext time = timer.time();
			try
			{
				runWorstCaseRule(dimensionGroup, rules, mdm, true);
			} finally
			{
				time.stop();
			}
		}
		System.out.println("Min time taken " + timer.min());
		System.out.println("Max time taken " + timer.max());
		Snapshot snapshot = timer.getSnapshot();
		System.out.println("(99.9th Percentile " + snapshot.get999thPercentile());
		System.out.println("(99th Percentile " + snapshot.get99thPercentile());
		System.out.println("(75th Percentile " + snapshot.get75thPercentile());
	}

	private static void runWorstCaseRule(DimensionGroup dimensionGroup, List<Rule> rules, MasterDataManager mdm, boolean quiet)
	{
		if (!quiet)
		{
			System.out.println("Going to run the worst case rule ");
		}
		long start = System.currentTimeMillis();

		Dimension source = new Dimension(DimensionName.SOURCE, Lists.newArrayList(80), mdm);
		Dimension destination = new Dimension(DimensionName.DESTINATION, Lists.newArrayList(28860), mdm);
		Dimension vendor = new Dimension(DimensionName.VENDOR, Lists.newArrayList(20), mdm);
		Dimension size = new Dimension(DimensionName.SIZE, Lists.newArrayList(8), mdm);
		Dimension category = new Dimension(DimensionName.CATEGORY, Lists.newArrayList(10), mdm);
		Collection<Dimension> rule = Lists.newArrayList(source, destination, vendor, size, category);

		long numBitAndsStart = MetricsHelper.numBitAnds();
		Collection<Dimension> match = dimensionGroup.match(rule);
		if (match.size() > 0)
		{
			throw new RuntimeException("Should not find a match for worst case rule ");
		}
		long numBitAndsEnd = MetricsHelper.numBitAnds();
		long end = System.currentTimeMillis();
		if (!quiet)
		{
			System.out.println("Time taken to match 1 worst case rule against each against " + rules.size() + " rules in millis " + (end - start)
					+ " Number of bit ands performed " + (numBitAndsEnd - numBitAndsStart));
		}
	}

	private static void runAllRules(DimensionGroup dimensionGroup, List<Rule> rules)
	{
		System.out.println("Going to run all rules to match against all rules");
		long start = System.currentTimeMillis();
		rules = Lists.reverse(rules);
		long numBitAndsStart = MetricsHelper.numBitAnds();
		for (Rule rule : rules)
		{
			Collection<Dimension> match = dimensionGroup.match(rule.getInput().values());
			if (match.size() == 0)
			{
				throw new RuntimeException("Every input should find a matching rule ");
			}
		}
		long numBitAndsEnd = MetricsHelper.numBitAnds();
		System.out.println(numBitAndsEnd - numBitAndsStart);
		long end = System.currentTimeMillis();
		System.out.println("Time taken to match " + rules.size() + " inputs against each against " + rules.size() + " rules in millis "
				+ (end - start) + " Number of bit ands performed " + (numBitAndsEnd - numBitAndsStart));
	}

	private static List<Rule> generateRules(MasterDataManager mdm)
	{
		List<Rule> rules = Lists.newArrayList();
		Dimension allCategory = new Dimension(DimensionName.CATEGORY, Lists.newArrayList(mdm.getData(DimensionName.CATEGORY).keySet()), mdm);
		Dimension allSize = new Dimension(DimensionName.SIZE, Lists.newArrayList(mdm.getData(DimensionName.SIZE).keySet()), mdm);
		Set<Integer> sources = mdm.getData(DimensionName.SOURCE).keySet();
		Set<Integer> vendors = mdm.getData(DimensionName.VENDOR).keySet();
		Set<Integer> destinations = mdm.getData(DimensionName.DESTINATION).keySet();
		List<List<Integer>> listOfDestinations = Lists.partition(Lists.newArrayList(destinations), 5000);

		Dimension sla = new Dimension(DimensionName.SLA, Lists.newArrayList(2), mdm);
		Dimension cost = new Dimension(DimensionName.COST, Lists.newArrayList(2), mdm);
		Collection<Dimension> outputDimensions = Lists.newArrayList(sla, cost);
		for (Integer source : sources)
		{
			for (Integer vendor : vendors)
			{
				for (Collection<Integer> destinationsSplit : listOfDestinations)
				{
					Rule rule = new Rule(Lists.newArrayList(new Dimension(DimensionName.SOURCE, Lists.newArrayList(source), mdm), new Dimension(
							DimensionName.VENDOR, Lists.newArrayList(vendor), mdm),
							new Dimension(DimensionName.DESTINATION, Lists.newArrayList(destinationsSplit), mdm), allSize, allCategory),
							outputDimensions);
					rules.add(rule);
				}
			}
		}
		return rules;
	}
}
