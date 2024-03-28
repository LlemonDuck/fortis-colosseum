package com.duckblade.osrs.fortis.util;

import com.duckblade.osrs.fortis.util.spawns.WaveSpawns;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;

@RequiredArgsConstructor
@EqualsAndHashCode
public class ColosseumState
{

	@Getter
	private final boolean inLobby;

	@Getter
	private final boolean inColosseum;

	@Getter
	private final int waveNumber;

	@Getter
	private final boolean waveStarted;

	@Getter
	private final List<Modifier> modifiers;

	@EqualsAndHashCode.Exclude
	private WaveSpawns waveSpawns;

	@EqualsAndHashCode.Exclude
	private WaveSpawns nextWaveSpawns;

	public WaveSpawns getWaveSpawns(Client client)
	{
		if (this.waveSpawns != null)
		{
			return this.waveSpawns;
		}
		return this.waveSpawns = WaveSpawns.forWave(client, this, false);
	}

	public WaveSpawns getNextWaveSpawns(Client client)
	{
		if (this.nextWaveSpawns != null)
		{
			return this.nextWaveSpawns;
		}
		return this.nextWaveSpawns = WaveSpawns.forWave(client, this, true);
	}
}
