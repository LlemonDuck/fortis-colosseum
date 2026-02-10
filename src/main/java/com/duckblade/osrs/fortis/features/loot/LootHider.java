package com.duckblade.osrs.fortis.features.loot;

import com.duckblade.osrs.fortis.FortisColosseumConfig;
import com.duckblade.osrs.fortis.module.PluginLifecycleComponent;
import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.ColosseumStateTracker;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.SoundEffectID;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class LootHider implements PluginLifecycleComponent
{

	private static final int SCRIPT_MODIFIER_SELECT_INIT = 4931;

	private static final int[] SPRITE_IDS_STANDARD = {
		913, 914, 915, 916, 917, 918, 919, 920,
	};
	private static final int[] SPRITE_IDS_HOVER = {
		921, 922, 923, 924, 925, 926, 927, 928,
	};

	private final EventBus eventBus;
	private final Client client;
	private final ClientThread clientThread;
	private final ColosseumStateTracker stateTracker;

	private final Deque<Runnable> gameTickQueue = new ArrayDeque<>(2);

	private LootHiderMode hidePrevious;
	private LootHiderMode hideNext;

	@Override
	public boolean isEnabled(FortisColosseumConfig config, ColosseumState state)
	{
		hidePrevious = config.lootInterfaceHidePreviousWaves();
		hideNext = config.lootInterfaceHideNextWave();
		return (hidePrevious != LootHiderMode.OFF || hideNext != LootHiderMode.OFF)
			&& state.isInColosseum();
	}

	@Override
	public void startUp()
	{
		gameTickQueue.clear();
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		runGameTickQueuedActions();
		eventBus.unregister(this);
	}

	@Subscribe(priority = -10) // priority low just in case some other plugin reads the items out of here
	public void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() != SCRIPT_MODIFIER_SELECT_INIT)
		{
			return;
		}

		if (shouldHide(hidePrevious))
		{
			hide(
				client.getWidget(InterfaceID.ColosseumIntermission2.LEFT_LOOT),
				client.getWidget(InterfaceID.ColosseumIntermission2.LEFT_LOOT_VALUE),
				client.getWidget(InterfaceID.ColosseumIntermission2.LEFT_LOOT_BACK),
				hideTotal()
			);
		}

		if (shouldHide(hideNext))
		{
			hide(
				client.getWidget(InterfaceID.ColosseumIntermission2.RIGHT_LOOT),
				client.getWidget(InterfaceID.ColosseumIntermission2.RIGHT_LOOT_VALUE),
				client.getWidget(InterfaceID.ColosseumIntermission2.RIGHT_LOOT_BACK),
				null
			);
		}
	}

	private void hide(Widget loot, Widget value, Widget back, Widget totalEarned)
	{
		if (loot == null || value == null || back == null)
		{
			return;
		}
		loot.setHidden(true);
		value.setHidden(true);

		int w = 78;
		int h = 28;
		int x = back.getWidth() / 2 - w / 2; // of top left corner
		int y = back.getHeight() / 2 - h / 2;
		Widget[] graphics = new Widget[]{
			buildGraphicWidget(back, x, y, 9, 9, SPRITE_IDS_STANDARD[0]), // corners
			buildGraphicWidget(back, x + w - 9, y, 9, 9, SPRITE_IDS_STANDARD[1]),
			buildGraphicWidget(back, x, y + h - 9, 9, 9, SPRITE_IDS_STANDARD[2]),
			buildGraphicWidget(back, x + w - 9, y + h - 9, 9, 9, SPRITE_IDS_STANDARD[3]),
			buildGraphicWidget(back, x, y + 9, 9, h - 18, SPRITE_IDS_STANDARD[4]), // edges
			buildGraphicWidget(back, x + 9, y, w - 18, 9, SPRITE_IDS_STANDARD[5]),
			buildGraphicWidget(back, x + w - 9, y + 9, 9, h - 18, SPRITE_IDS_STANDARD[6]),
			buildGraphicWidget(back, x + 9, y + h - 9, w - 18, 9, SPRITE_IDS_STANDARD[7]),
		};

		Widget text = back.createChild(-1, WidgetType.TEXT)
			.setPos(x, y, WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_TOP)
			.setSize(w, h, WidgetSizeMode.ABSOLUTE, WidgetSizeMode.ABSOLUTE)
			.setTextShadowed(true)
			.setFontId(FontID.PLAIN_12)
			.setTextColor(0xff981f)
			.setText("Show Loot")
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)
			.setHasListener(true);
		text.revalidate();

		// on mouse hover and leave, update the sprite ids to simulate depth
		// also add a tooltip for the fake action
		text.setOnMouseOverListener((JavaScriptCallback) (ignored) ->
		{
			for (int i = 0; i < graphics.length; i++)
			{
				graphics[i].setSpriteId(SPRITE_IDS_HOVER[i]);
			}
		});
		text.setOnMouseLeaveListener((JavaScriptCallback) (ignored) ->
		{
			for (int i = 0; i < graphics.length; i++)
			{
				graphics[i].setSpriteId(SPRITE_IDS_STANDARD[i]);
			}
		});

		text.setAction(0, "Show");
		text.setOnOpListener((JavaScriptCallback) (ignored) ->
		{
			// queue the action for the next game tick just to simulate jagex widget delay ;)
			client.playSoundEffect(SoundEffectID.UI_BOOP);
			gameTickQueue.addLast(() -> unhide(loot, value, back, totalEarned, graphics, text));
		});
	}

	private Widget hideTotal()
	{
		Widget container = client.getWidget(InterfaceID.ColosseumIntermission2.LEFT);
		if (container == null)
		{
			return null;
		}

		Widget totalEarned = container.getChild(0);
		if (totalEarned != null)
		{
			totalEarned.setHidden(true);
			return totalEarned;
		}

		return null;
	}

	private void unhide(Widget loot, Widget value, Widget back, Widget totalEarned, Widget[] graphics, Widget text)
	{
		if (loot != null)
		{
			loot.setHidden(false);
		}

		if (value != null)
		{
			value.setHidden(false);
		}

		if (totalEarned != null)
		{
			totalEarned.setHidden(false);
		}

		if (back == null || graphics == null || text.isHidden())
		{
			return;
		}

		// clear out all the children we added
		// I tried actually removing them from the array but setChildren failed on a classcast due to separate classloaders?
		for (Widget graphic : graphics)
		{
			graphic.setHidden(true);
		}
		text.setHidden(true);
		text.setHasListener(false);
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		runGameTickQueuedActions();
	}

	private Widget buildGraphicWidget(Widget parent, int x, int y, int w, int h, int spriteId)
	{
		Widget graphic = parent.createChild(WidgetType.GRAPHIC)
			.setPos(x, y, WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_TOP)
			.setSize(w, h, WidgetSizeMode.ABSOLUTE, WidgetSizeMode.ABSOLUTE)
			.setSpriteId(spriteId);
		graphic.revalidate();
		return graphic;
	}

	private boolean shouldHide(LootHiderMode mode)
	{
		return mode == LootHiderMode.ALWAYS ||
			(mode == LootHiderMode.WAVE_12 && stateTracker.getCurrentState().getWaveNumber() != 12);
	}

	private void runGameTickQueuedActions()
	{
		if (!gameTickQueue.isEmpty())
		{
			clientThread.invoke(() ->
			{
				Iterator<Runnable> it = gameTickQueue.iterator();
				while (it.hasNext())
				{
					it.next().run();
					it.remove();
				}
			});
		}
	}
}
