package com.duckblade.osrs.fortis;

import com.duckblade.osrs.fortis.features.loot.LootHiderMode;
import com.duckblade.osrs.fortis.features.loot.LootTotalMode;
import com.duckblade.osrs.fortis.features.modifiers.ModifierSpriteStyle;
import com.duckblade.osrs.fortis.features.timetracking.SplitsFileWriter;
import com.duckblade.osrs.fortis.features.timetracking.SplitsOverlayMode;
import com.duckblade.osrs.fortis.features.waves.EnemyNameMode;
import com.duckblade.osrs.fortis.features.waves.WaveOverlayMode;
import com.duckblade.osrs.fortis.util.TimerMode;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.ui.overlay.components.ComponentOrientation;

@ConfigGroup(FortisColosseumConfig.CONFIG_GROUP)
public interface FortisColosseumConfig extends Config
{

	String CONFIG_GROUP = "fortiscolosseum";

	@ConfigSection(
		name = "Waves Overlay",
		description = "Show which enemies are in the current and next wave.",
		position = 100
	)
	String SECTION_WAVES_OVERLAY = "wavesOverlay";

	@ConfigItem(
		keyName = "wavesOverlayMode",
		name = "Display",
		description = "Enable or disable the waves display components.",
		position = 101,
		section = SECTION_WAVES_OVERLAY
	)
	default WaveOverlayMode wavesOverlayMode()
	{
		return WaveOverlayMode.BOTH;
	}

	@ConfigItem(
		keyName = "wavesOverlayNames",
		name = "Names",
		description = "Whether to use official NPC names (e.g. Serpent Shaman), or colloquial (e.g. Mage)",
		position = 102,
		section = SECTION_WAVES_OVERLAY
	)
	default EnemyNameMode wavesOverlayNames()
	{
		return EnemyNameMode.COLLOQUIAL;
	}

	@ConfigItem(
		keyName = "wavesOverlayShowModifierSpawns",
		name = "Show Modifier-Only Spawns",
		description = "Include Angry Bees in the list of spawns for each wave.<br>Dynamic Duo and Quartet are always shown.",
		position = 103,
		section = SECTION_WAVES_OVERLAY
	)
	default boolean wavesOverlayShowModifierSpawns()
	{
		return false;
	}

	@ConfigSection(
		name = "Modifiers",
		description = "Modifiers overlay and selection options",
		position = 200
	)
	String SECTION_MODIFIERS = "modifiers";

	@ConfigItem(
		keyName = "modifiersOverlayEnabled",
		name = "Overlay Enabled",
		description = "Shows the current active modifiers as an overlay of icons",
		position = 201,
		section = SECTION_MODIFIERS
	)
	default boolean modifiersOverlayEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "modifiersOverlayOrientation",
		name = "Overlay Orientation",
		description = "Whether to render vertically or horizontally",
		position = 202,
		section = SECTION_MODIFIERS
	)
	default ComponentOrientation modifiersOverlayOrientation()
	{
		return ComponentOrientation.HORIZONTAL;
	}

	@ConfigItem(
		keyName = "modifiersOverlayOrientation",
		name = "Overlay Orientation",
		description = "Whether to render vertically or horizontally",
		position = 202,
		section = SECTION_MODIFIERS
	)
	void setModifiersOverlayOrientation(ComponentOrientation orientation);

	@ConfigItem(
		keyName = "modifiersOverlayStyle",
		name = "Style",
		description = "Whether to render vertically or horizontally",
		position = 203,
		section = SECTION_MODIFIERS
	)
	default ModifierSpriteStyle modifiersOverlayStyle()
	{
		return ModifierSpriteStyle.COMPACT;
	}

	@ConfigItem(
		keyName = "modifiersOverlayStyle",
		name = "Style",
		description = "Whether to render vertically or horizontally",
		position = 203,
		section = SECTION_MODIFIERS
	)
	void setModifiersOverlayStyle(ModifierSpriteStyle style);

	@ConfigItem(
		keyName = "modifiersNamesOnSelectWidget",
		name = "Show Names on Selector",
		description = "Show the modifier names on the pre-wave selector panel.",
		position = 204,
		section = SECTION_MODIFIERS
	)
	default boolean modifiersNamesOnSelectWidget()
	{
		return true;
	}

	@ConfigItem(
		keyName = "volatilityReminder",
		name = "Volatility Reminder",
		description = "Shows an icon overlay on dying NPCs to remind of their explosion.<br>Does NOT indicate the explosion radius.",
		position = 205,
		section = SECTION_MODIFIERS
	)
	default boolean volatilityReminder()
	{
		return false;
	}

	@ConfigSection(
		name = "Splits",
		description = "Time tracking and splits",
		position = 300
	)
	String SECTION_SPLITS = "splits";

	@ConfigItem(
		keyName = "splitsOverlayMode",
		name = "Overlay Panel",
		description = "Show splits as an overlay panel.",
		position = 301,
		section = SECTION_SPLITS
	)
	default SplitsOverlayMode splitsOverlayMode()
	{
		return SplitsOverlayMode.OFF;
	}

	@ConfigItem(
		keyName = "splitsOverlayLines",
		name = "Overlay Wave Count",
		description = "Show the last N waves on the overlay panel.<br>" +
			"Set to 1 to only show the active wave.<br>" +
			"Set to 0 to only show the total time.",
		position = 302,
		section = SECTION_SPLITS
	)
	@Range(min = 0, max = 12)
	default int splitsOverlayLines()
	{
		return 12;
	}

	@ConfigItem(
		keyName = "splitsFileCondition",
		name = "Save to File",
		description = "Save splits to files in .runelite/fortis-colosseum/splits/",
		position = 303,
		section = SECTION_SPLITS
	)
	default SplitsFileWriter.WriteCondition splitsFileCondition()
	{
		return SplitsFileWriter.WriteCondition.NEVER;
	}

	@ConfigItem(
		keyName = "splitsTimerMode",
		name = "File Timer Mode",
		description = "Whether to use human times or ticks when writing to the file.<br>" +
				"'Precise' is minutes, seconds, and milliseconds.<br>" +
				"'Lax' is minutes and seconds.<br>" +
				"'Ticks' is an unconverted server tick count.",
		position = 304,
		section = SECTION_SPLITS
	)
	default TimerMode splitsFileTimerMode()
	{
		return TimerMode.TICKS;
	}

	String KEY_LIVESPLIT_PORT = "splitsLivesplitPort";
	@ConfigItem(
		keyName = KEY_LIVESPLIT_PORT,
		name = "LiveSplit Port",
		description = "Send splits events to LiveSplit. Set to 0 to disable.<br>Requires LiveSplit Server. See the plugin README for more details.",
		position = 305,
		section = SECTION_SPLITS
	)
	@Range(min = 0, max = 65535)
	default int splitsLivesplitPort()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "splitsLivesplitAutoReset",
		name = "LiveSplit Auto-Reset",
		description = "Automatically restart the timer at Wave 1 when a new run is started.",
		position = 306,
		section = SECTION_SPLITS
	)
	default boolean splitsLivesplitAutoReset()
	{
		return false;
	}

	@ConfigSection(
		name = "Loot Interface",
		description = "Options for the loot shown between waves and on completion.",
		position = 400
	)
	String SECTION_LOOT_INTERFACE = "lootInterface";

	@ConfigItem(
		keyName = "leftClickBankAll", // legacy keyName
		name = "Left-Click Bank-All",
		description = "Switch the two-click Bank-All to a single click in the loot chest interface.",
		position = 401,
		section = SECTION_LOOT_INTERFACE
	)
	default boolean leftClickBankAll()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lootInterfaceHideNextWave",
		name = "Hide Next Wave",
		description = "Hide potential next wave loot behind an extra click.",
		position = 402,
		section = SECTION_LOOT_INTERFACE
	)
	default LootHiderMode lootInterfaceHideNextWave()
	{
		return LootHiderMode.OFF;
	}

	@ConfigItem(
		keyName = "lootInterfaceShowTotal",
		name = "Show Loot Total",
		description = "",
		position = 403,
		section = SECTION_LOOT_INTERFACE
	)
	default LootTotalMode lootInterfaceShowTotal()
	{
		return LootTotalMode.EARNED;
	}

	@ConfigSection(
		name = "LoS Links Panel",
		description = "Captures wave spawns, reinforcements, and current NPC locations and generates links to los.colosim.com in the side panel",
		position = 500
	)
	String SECTION_LOS_LINKS = "losLinks";

	@ConfigItem(
		keyName = "losLinksEnabled",
		name = "Panel Enabled",
		description = "Captures wave spawns, reinforcements, and current NPC locations and generates links to los.colosim.com in the side panel",
		position = 501,
		section = SECTION_LOS_LINKS
	)
	default boolean losLinksEnabled()
	{
		return true;
	}

}
