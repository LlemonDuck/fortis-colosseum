package com.duckblade.osrs.fortis.util.spawns;

import com.duckblade.osrs.fortis.util.ColosseumState;
import com.duckblade.osrs.fortis.util.Handicap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(access = AccessLevel.PRIVATE)
public class WaveSpawns
{

	@Singular
	List<WaveSpawn> spawns;

	@Singular
	List<WaveSpawn> reinforcements;

	public static WaveSpawns forWave(ColosseumState state, boolean next)
	{
		int wave = next ? state.getWaveNumber() + 1 : state.getWaveNumber();
		Map<Handicap, Integer> handicaps = state.getHandicaps();

		WaveSpawnsBuilder builder = WaveSpawns.builder();

		// handicap-only spawns
		if (handicaps.containsKey(Handicap.DOOM_SCORPION))
		{
			builder.spawn(new WaveSpawn(1, Enemy.DOOM_SCORPION));
		}

		if (handicaps.containsKey(Handicap.BEES))
		{
			builder.spawn(new WaveSpawn(handicaps.get(Handicap.BEES), Enemy.ANGRY_BEES));
		}

		// skip early for boss
		if (wave == 12)
		{
			builder.spawn(new WaveSpawn(1, Enemy.SOL_HEREDIT));
			return builder.build();
		}

		// frems every wave, 3 by default or 4 with quartet
		builder.spawn(new WaveSpawn(handicaps.containsKey(Handicap.QUARTET) ? 4 : 3, Enemy.FREMENNIK));

		if (wave <= 6)
		{
			// serpent shaman every wave up to 6
			builder.spawn(new WaveSpawn(1, Enemy.SERPENT_SHAMAN));
		}
		if ((wave >= 4 && wave <= 6) || (wave >= 10))
		{
			// and also as a reinforcement 4-6 and 10-11
			builder.reinforcement(new WaveSpawn(1, Enemy.SERPENT_SHAMAN));
		}

		// jaguar warrior is reinforcement only, all waves up to 6
		if (wave <= 6)
		{
			builder.reinforcement(new WaveSpawn(1, Enemy.JAGUAR_WARRIOR));
		}

		// javelins alternate 1 and 2 spawns, but skip waves 1 and 4
		if (wave == 2 || wave == 3)
		{
			builder.spawn(new WaveSpawn(wave - 1, Enemy.JAVELIN_COLOSSUS));
		}
		if (wave >= 5)
		{
			builder.spawn(new WaveSpawn(2 - (wave % 2), Enemy.JAVELIN_COLOSSUS));
		}

		// manticore every wave 4 and up, varying between 1 and 2 spawns
		if (wave >= 4)
		{
			// single spawn on wave 4-8, double thereafter
			boolean single = wave <= 8;
			builder.spawn(new WaveSpawn(single ? 1 : 2, Enemy.MANTICORE));
		}

		// shockwave waves 7, 8, and 11, and 2 spawns if dynamic duo is on
		if (wave == 7 || wave == 8 || wave == 11)
		{
			builder.spawn(new WaveSpawn(handicaps.containsKey(Handicap.DYNAMIC_DUO) ? 2 : 1, Enemy.SHOCKWAVE_COLOSSUS));
		}

		// minotaur replaces jaguar warrior in replacements wave 7 and up
		if (wave >= 7)
		{
			builder.reinforcement(new WaveSpawn(1, Enemy.MINOTAUR));
		}

		return builder.build();
	}

}
