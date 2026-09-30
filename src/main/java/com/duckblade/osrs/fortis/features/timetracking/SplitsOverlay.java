package com.duckblade.osrs.fortis.features.timetracking;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateTracker;
import com.duckblade.osrs.fortis.util.TimerMode;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Slf4j
@Singleton
public class SplitsOverlay extends OverlayPanel implements PluginLifecycleComponent
{

	private final OverlayManager overlayManager;

	private final Client client;
	private final FortisColosseumConfig config;
	private final SplitsTracker splitsTracker;
	private final ColosseumStateTracker stateTracker;

	@Inject
	public SplitsOverlay(
		OverlayManager overlayManager,
		Client client,
		FortisColosseumConfig config,
		SplitsTracker splitsTracker,
		ColosseumStateTracker stateTracker
	)
	{
		this.overlayManager = overlayManager;
		this.client = client;
		this.config = config;
		this.splitsTracker = splitsTracker;
		this.stateTracker = stateTracker;

		setPosition(OverlayPosition.CANVAS_TOP_RIGHT);
	}

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState state)
	{
		return state.isInColosseum() && config.splitsOverlayMode() != SplitsOverlayMode.OFF;
	}

	@Override
	public void startUp()
	{
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(this);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		long start = System.currentTimeMillis();
		getPanelComponent().getChildren()
			.add(TitleComponent.builder()
				.text("Fortis Colosseum Splits")
				.build());

		TimerMode timerMode = TimerMode.fromClient(client);
		SplitsOverlayMode overlayMode = config.splitsOverlayMode();
		boolean waveStarted = stateTracker.getCurrentState().isWaveStarted();
		int wavesWanted = config.splitsOverlayLines();

		if (wavesWanted > 0)
		{
			List<Split> allSplits = splitsTracker.getSplits();
			boolean showCurrentWave = waveStarted && allSplits.size() < 12;

			int visibleSplitsCount = wavesWanted - (showCurrentWave ? 1 : 0);
			if (visibleSplitsCount > 0)
			{
				List<Split> visibleSplits = allSplits.subList(Math.max(0, allSplits.size() - visibleSplitsCount), allSplits.size()); // last N splits
				for (Split s : visibleSplits)
				{
					addLine("Wave " + s.getWave(), overlayMode.formatSplit(timerMode, s));
				}
			}

			Split inProgress = splitsTracker.getInProgressSplit();
			if (showCurrentWave)
			{
				String text = overlayMode.formatSplit(timerMode, inProgress);
				addLine("Wave " + inProgress.getWave(), text);
			}
		}

		if (config.splitsOverlayShowTotal())
		{
			String text = overlayMode.formatTotal(timerMode, splitsTracker.getWaveCumulativeDuration(), splitsTracker.getCumulativeDuration());
			addLine("Total", text);
		}

		long ms = System.currentTimeMillis() - start;
		if (ms > 1)
		{
			log.debug("splits overlay rendered in {}ms", ms);
		}
		return super.render(graphics);
	}

	private void addLine(String left, String right)
	{
		getPanelComponent()
			.getChildren()
			.add(LineComponent.builder()
				.left(left)
				.right(right)
				.build());
	}
}
