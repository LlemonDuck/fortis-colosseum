package com.duckblade.osrs.fortis.debugplugins;

import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Singleton
@PluginDescriptor(
	name = "Fortis Colosseum Debug"
)
public class FortisColosseumDebugPlugin extends Plugin
{

	private FortisColosseumDebugPanel debugPanel;

	@Override
	protected void startUp()
	{
		SwingUtilities.invokeLater(() -> debugPanel = injector.getInstance(FortisColosseumDebugPanel.class));
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted e)
	{
		if (e.getCommand().equals("fortis"))
		{
			if (debugPanel.isVisible())
			{
				debugPanel.close();
			}
			else
			{
				debugPanel.open();
			}
		}
	}
}
