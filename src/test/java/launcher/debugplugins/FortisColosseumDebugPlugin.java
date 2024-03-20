package launcher.debugplugins;

import ch.qos.logback.classic.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import ch.qos.logback.classic.Logger;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.LoggerFactory;

@Singleton
@PluginDescriptor(
	name = "Fortis Colosseum Debug"
)
public class FortisColosseumDebugPlugin extends Plugin
{

	@Inject
	private OverlayManager overlayManager;

	@Override
	protected void startUp()
	{
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.WARN);
		((Logger) LoggerFactory.getLogger("com.duckblade.osrs.fortis")).setLevel(Level.DEBUG);
	}

	@Override
	protected void shutDown() throws Exception
	{
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
	}
}
