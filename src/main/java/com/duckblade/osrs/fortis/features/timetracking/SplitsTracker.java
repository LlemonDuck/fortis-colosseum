package com.duckblade.osrs.fortis.features.timetracking;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.features.timetracking.livesplit.LiveSplitManager;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateChanged;
import com.duckblade.osrs.fortis.util.ColosseumStateTracker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SplitsTracker implements PluginLifecycleComponent
{

	private static final Pattern WAVE_COMPLETE_PATTERN =
		Pattern.compile("Wave (?<wave>\\d+) completed! Wave duration:.*?(?<duration>[0-9]+:[.0-9]+).*");

	private final EventBus eventBus;

	private final Client client;
	private final ColosseumStateTracker stateTracker;
	private final LiveSplitManager liveSplitManager;

	private int runStart = -1;
	private int lastWaveStart = -1;
	private final List<Split> splits = new ArrayList<>(12);

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState state)
	{
		// always track, conditionally display
		return state.isInColosseum();
	}

	@Override
	public void startUp()
	{
		runStart = -1;
		lastWaveStart = -1;
		splits.clear();
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String msg = e.getMessage();
		if (!msg.contains("duration"))
		{
			return;
		}

		Matcher m = WAVE_COMPLETE_PATTERN.matcher(msg);
		if (!m.matches())
		{
			return;
		}

		int wave = Integer.parseInt(m.group("wave"));
		int duration = parseTimeString(m.group("duration"));
		int cumulative = getCumulativeDuration();
		int cumulativeWave = getCumulativeWaveDuration();

		if (wave == 12)
		{
			// wave 12 message is your final overall time
			duration = duration - cumulativeWave;
		}

		Split newSplit = new Split(wave, duration, cumulative, cumulativeWave + duration);
		splits.add(newSplit);
		lastWaveStart = -1;

		liveSplitManager.onSplit(newSplit);
	}

	@Subscribe
	public void onColosseumStateChanged(ColosseumStateChanged e)
	{
		if (e.getNewState().isWaveStarted() && !e.getPreviousState().isWaveStarted())
		{
			if (e.getNewState().getWaveNumber() == 1)
			{
				runStart = client.getTickCount();
			}
			liveSplitManager.onWaveStart(e.getNewState().getWaveNumber());
			lastWaveStart = client.getTickCount();
		}
	}

	public Split getInProgressSplit()
	{
		return new Split(
			stateTracker.getCurrentState().getWaveNumber(),
			getWaveDuration(),
			getCumulativeDuration(),
			getCumulativeWaveDuration()
		);
	}

	private int getWaveDuration()
	{
		return lastWaveStart == -1 ? -1 : client.getTickCount() - lastWaveStart;
	}

	public int getWaveCumulativeDuration()
	{
		int currentWaveTime = getWaveDuration();
		if (splits.isEmpty())
		{
			return getWaveDuration();
		}

		int cumulative = 0;
		for (Split split : splits)
		{
			int waveDuration = split.getWaveDuration();
			cumulative += waveDuration;
		}

		if (currentWaveTime != -1)
		{
			cumulative += currentWaveTime;
		}

		return cumulative;
	}

	public int getCumulativeDuration()
	{
		if (splits.size() == 12)
		{
			return splits.get(11).getCumulativeDuration();
		}

		return runStart == -1 ? -1 : client.getTickCount() - runStart;
	}

	public int getCumulativeWaveDuration()
	{
		return splits.stream()
			.mapToInt(Split::getWaveDuration)
			.sum();
	}

	public List<Split> getSplits()
	{
		return Collections.unmodifiableList(splits);
	}

	private static int parseTimeString(String timeString)
	{
		String[] components = timeString.split(":");

		int mins = Integer.parseInt(components[0]);
		float seconds = Float.parseFloat(components[1]);
		return mins * 100 + (int) Math.ceil(seconds / 0.6f);
	}

}
