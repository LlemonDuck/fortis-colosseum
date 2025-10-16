package com.duckblade.osrs.fortis.features.timetracking;

import com.duckblade.osrs.fortis.features.timetracking.livesplit.LiveSplitManager;
import java.util.List;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SplitsTrackerTest
{

	@SuppressWarnings("unused") // void onSplit hook needs to be mocked
	@Mock
	LiveSplitManager liveSplitManager;

	@InjectMocks
	SplitsTracker splitsTracker;

	@Test
	void wave12DurationShouldBeParsedFromOverallTime()
	{
		splitsTracker.onChatMessage(buildChatMessage("Wave 1 completed! Wave duration: 0:31.80"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 2 completed! Wave duration: 1:01.80"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 3 completed! Wave duration: 2:42.60"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 4 completed! Wave duration: 1:46.20"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 5 completed! Wave duration: 2:03.60"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 6 completed! Wave duration: 3:10.20"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 7 completed! Wave duration: 3:15.00"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 8 completed! Wave duration: 3:02.40"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 9 completed! Wave duration: 1:57.00"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 10 completed! Wave duration: 3:21.00"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 11 completed! Wave duration: 4:07.80"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 12 completed! Wave duration: 29:42.60"));

		List<Split> splits = splitsTracker.getSplits();
		assertEquals(272, splits.get(11).getWaveDuration());
	}

	@Test
	void cumulativeWaveTimeShouldBeComputedFromTheSumOfAllWaves()
	{
		splitsTracker.onChatMessage(buildChatMessage("Wave 1 completed! Wave duration: 0:31.80"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 2 completed! Wave duration: 1:01.80"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 3 completed! Wave duration: 2:42.60"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 4 completed! Wave duration: 1:46.20"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 5 completed! Wave duration: 2:03.60"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 6 completed! Wave duration: 3:10.20"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 7 completed! Wave duration: 3:15.00"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 8 completed! Wave duration: 3:02.40"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 9 completed! Wave duration: 1:57.00"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 10 completed! Wave duration: 3:21.00"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 11 completed! Wave duration: 4:07.80"));
		splitsTracker.onChatMessage(buildChatMessage("Wave 12 completed! Wave duration: 29:42.60"));

		List<Split> splits = splitsTracker.getSplits();
		assertEquals(53, splits.get(0).getCumulativeWaveDuration());
		assertEquals(156, splits.get(1).getCumulativeWaveDuration());
		assertEquals(427, splits.get(2).getCumulativeWaveDuration());
		assertEquals(2971, splits.get(11).getCumulativeWaveDuration());
	}

	private ChatMessage buildChatMessage(String msg)
	{
		return new ChatMessage(null, ChatMessageType.GAMEMESSAGE, null, msg, null, 0);
	}

}
