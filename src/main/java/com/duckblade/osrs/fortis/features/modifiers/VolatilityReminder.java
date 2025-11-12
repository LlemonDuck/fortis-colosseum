package com.duckblade.osrs.fortis.features.modifiers;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.Modifier;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Value;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.RSTimeUnit;

@Singleton
public class VolatilityReminder extends Overlay implements PluginLifecycleComponent
{

	private static final Duration RENDER_DURATION = Duration.of(5, RSTimeUnit.GAME_TICKS);

	@Inject
	public VolatilityReminder(EventBus eventBus, OverlayManager overlayManager, Client client, ModifierSpriteCache spriteCache)
	{
		this.eventBus = eventBus;
		this.overlayManager = overlayManager;
		this.client = client;
		this.spriteCache = spriteCache;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Value
	private static class DeathReminder
	{
		LocalPoint location;
		int z;
		Instant renderUntil;
	}

	private final EventBus eventBus;
	private final OverlayManager overlayManager;

	private final Client client;
	private final ModifierSpriteCache spriteCache;

	private final Set<DeathReminder> reminders = new HashSet<>();

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState colosseumState)
	{
		return config.volatilityReminder()
			&& colosseumState.isInColosseum()
			&& colosseumState.getModifiers().contains(Modifier.VOLATILITY);
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		overlayManager.remove(this);
	}

	@Subscribe
	public void onActorDeath(ActorDeath e)
	{
		if (e.getActor() instanceof NPC)
		{
			int npcID = ((NPC) e.getActor()).getId();
			if (npcID == NpcID.COLOSSEUM_MODIFIER_BEES || npcID == NpcID.COLOSSEUM_HEALING_TOTEM)
			{
				return;
			}

			reminders.add(new DeathReminder(
				e.getActor().getLocalLocation(),
				e.getActor().getLogicalHeight() / 2,
				Instant.now().plus(RENDER_DURATION)
			));
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Instant now = Instant.now();
		reminders.removeIf(r -> r.getRenderUntil().isBefore(now));
		for (DeathReminder reminder : reminders)
		{
			OverlayUtil.renderImageLocation(
				client,
				graphics,
				reminder.getLocation(),
				spriteCache.getSprite(Modifier.VOLATILITY, ModifierSpriteStyle.ICON_ONLY),
				reminder.getZ()
			);
		}

		return null;
	}
}
