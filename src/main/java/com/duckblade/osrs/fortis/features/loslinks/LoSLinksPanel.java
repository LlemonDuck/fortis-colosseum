/*
 * Copyright (c) 2025, Will Ediger
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.duckblade.osrs.fortis.features.loslinks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

public class LoSLinksPanel extends PluginPanel
{
	private static final int COMPONENT_HEIGHT = 30;
	private static final int GAP = 5;
	private static final int WAVE_NUMBER_WIDTH = 42;
	private static final int SPAWN_BUTTON_WIDTH = 62;
	private static final int REINFORCEMENTS_BUTTON_WIDTH = 118;
	private static final Dimension FULL_WIDTH = new Dimension(Integer.MAX_VALUE, COMPONENT_HEIGHT);
	private static final Color BG_COLOR = ColorScheme.DARK_GRAY_COLOR;
	private static final Color BTN_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color HOVER_COLOR = new Color(52, 52, 52);

	private final JPanel wavesContainer;
	private final List<WavePanel> wavePanels = new ArrayList<>();

	@Inject
	public LoSLinksPanel(final LoSLinksManager loSLinksManager, final ClientThread clientThread)
	{
		super(false);

		setBackground(BG_COLOR);
		setLayout(new BorderLayout());

		JPanel header = new JPanel();
		header.setOpaque(false);
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBorder(new EmptyBorder(GAP, GAP, GAP, GAP));

		JButton currentLoS = createButton("Current LoS", FULL_WIDTH);
		currentLoS.addActionListener(e -> clientThread.invokeLater(() ->
		{
			String url = loSLinksManager.generateCurrentLoSLink();
			if (url != null)
			{
				SwingUtilities.invokeLater(() -> LinkBrowser.browse(url));
			}
		}));

		JLabel wavesLabel = createLabel("Waves");
		setFixedSize(wavesLabel, FULL_WIDTH);

		header.add(currentLoS);
		header.add(Box.createRigidArea(new Dimension(0, GAP)));
		header.add(wavesLabel);

		add(header, BorderLayout.NORTH);

		wavesContainer = new JPanel();
		wavesContainer.setLayout(new BoxLayout(wavesContainer, BoxLayout.Y_AXIS));
		wavesContainer.setBackground(BG_COLOR);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(BG_COLOR);
		wrapper.add(wavesContainer, BorderLayout.NORTH);
		add(wrapper, BorderLayout.CENTER);
	}

	public void addWave(int waveNumber)
	{
		SwingUtilities.invokeLater(() ->
		{
			WavePanel panel = new WavePanel(waveNumber);
			wavePanels.add(panel);
			wavesContainer.add(panel);
			wavesContainer.add(Box.createRigidArea(new Dimension(0, GAP)));
			wavesContainer.revalidate();
		});
	}

	public void setWaveSpawnUrl(int waveNumber, String url)
	{
		runOnWavePanel(waveNumber, panel -> panel.setSpawnUrl(url));
	}

	public void setWaveReinforcementUrl(int waveNumber, String url)
	{
		runOnWavePanel(waveNumber, panel -> panel.setReinforcementUrl(url));
	}

	private void runOnWavePanel(int waveNumber, java.util.function.Consumer<WavePanel> action)
	{
		SwingUtilities.invokeLater(() ->
		{
			if (waveNumber > 0 && waveNumber <= wavePanels.size())
			{
				action.accept(wavePanels.get(waveNumber - 1));
			}
		});
	}

	public void reset()
	{
		SwingUtilities.invokeLater(() ->
		{
			wavePanels.clear();
			wavesContainer.removeAll();
			wavesContainer.revalidate();
		});
	}

	private static JLabel createLabel(String text)
	{
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setOpaque(true);
		label.setBackground(BTN_COLOR);
		label.setForeground(Color.WHITE);
		return label;
	}

	private static final class WavePanel extends JPanel
	{
		private final JLabel numberLabel;
		private final JButton spawnButton;
		private final JButton reinfButton;

		WavePanel(int wave)
		{
			setOpaque(false);
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(0, GAP, 0, GAP));

			JPanel row = new JPanel();
			row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
			row.setOpaque(false);
			setFixedSize(row, FULL_WIDTH);

			numberLabel = createLabel(String.valueOf(wave));
			setFixedSize(numberLabel, WAVE_NUMBER_WIDTH, COMPONENT_HEIGHT);

			spawnButton = createButton("Spawn", new Dimension(SPAWN_BUTTON_WIDTH, COMPONENT_HEIGHT));
			spawnButton.setEnabled(false);

			reinfButton = createButton("Reinforcements", new Dimension(REINFORCEMENTS_BUTTON_WIDTH, COMPONENT_HEIGHT));
			reinfButton.setEnabled(false);
			reinfButton.setVisible(false);

			row.add(numberLabel);
			row.add(Box.createRigidArea(new Dimension(GAP, 0)));
			row.add(spawnButton);
			row.add(Box.createRigidArea(new Dimension(GAP, 0)));
			row.add(reinfButton);

			add(row, BorderLayout.CENTER);
		}


		void setSpawnUrl(String url)
		{
			enableButton(spawnButton, () -> LinkBrowser.browse(url));
		}

		void setReinforcementUrl(String url)
		{
			reinfButton.setVisible(true);
			enableButton(reinfButton, () -> LinkBrowser.browse(url));
		}

		private void enableButton(JButton b, Runnable action)
		{
			for (ActionListener l : b.getActionListeners())
			{
				b.removeActionListener(l);
			}
			b.addActionListener(e -> action.run());
			b.setEnabled(true);
		}
	}

	private static JButton createButton(String text, Dimension size)
	{
		JButton button = new JButton(text);
		button.setFocusPainted(false);
		button.setBackground(BTN_COLOR);
		button.setForeground(Color.WHITE);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setFixedSize(button, size);
		button.setBorder(BorderFactory.createEmptyBorder());

		button.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				button.setBackground(HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				button.setBackground(BTN_COLOR);
			}
		});

		return button;
	}

	private static void setFixedSize(JComponent component, Dimension size)
	{
		component.setPreferredSize(size);
		component.setMaximumSize(size);
		component.setMinimumSize(size);
	}

	private static void setFixedSize(JComponent component, int width, int height)
	{
		setFixedSize(component, new Dimension(width, height));
	}
}