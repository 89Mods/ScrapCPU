package theGhastModding.scrapCPU.simulator;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JCheckBox;

public class MemoryVisualizer extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8702134989929707139L;
	private volatile Simulator sim = null;
	private volatile BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
	
	private JPanel panel;
	private JSpinner spinner;
	private JSpinner spinner_1;
	private JCheckBox chckbxBw;
	
	@SuppressWarnings("serial")
	public MemoryVisualizer() {
		super("Memory Visualizer");
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.getContentPane().setLayout(null);
		this.getContentPane().setPreferredSize(new Dimension(350, 152));
		
		panel = new JPanel() {
			@Override
			public void paint(Graphics arg0) {
				super.paint(arg0);
				if(img != null) arg0.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), Color.BLACK, this);
			}
		};
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBounds(12, 12, 128, 128);
		getContentPane().add(panel);
		
		JLabel lblOffset = new JLabel("Offset:");
		lblOffset.setBounds(158, 12, 66, 15);
		getContentPane().add(lblOffset);
		
		JLabel lblLength = new JLabel("Length:");
		lblLength.setBounds(158, 39, 66, 15);
		getContentPane().add(lblLength);
		
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(0, 0, 62, 1));
		spinner.setBounds(242, 12, 96, 20);
		getContentPane().add(spinner);
		
		spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(64, 1, 64, 1));
		spinner_1.setBounds(242, 37, 96, 20);
		getContentPane().add(spinner_1);
		
		chckbxBw = new JCheckBox("B/W");
		chckbxBw.setBounds(158, 62, 126, 23);
		getContentPane().add(chckbxBw);
		
		this.pack();
	}
	
	public void showVisualizer(Simulator sim) {
		this.sim = sim;
		setVisible(true);
	}
	
	public void updateThingy() {
		if(sim == null) return;
		
		for(int i = 0; i < img.getWidth(); i++) {
			for(int j = 0; j < img.getHeight(); j++) {
				img.setRGB(i, j, 0);
			}
		}
		
		int offset = Integer.parseInt(spinner.getValue().toString());
		int len = Integer.parseInt(spinner_1.getValue().toString());
		int drawSize = (int)Math.round(Math.sqrt(len));
		double squareSize = (double)img.getWidth() / (double)drawSize;
		int drawStart = offset;
		for(int i = 0; i < len; i++) {
			if(i + drawStart >= 64) break;
			int imgX = i % drawSize;
			int imgY = i / drawSize;
			int xstart = (int)(imgX * squareSize);
			int xend = (int)((imgX + 1) * squareSize);
			int ystart = (int)(imgY * squareSize);
			int yend = (int)((imgY + 1) * squareSize);
			int val = sim.RAM[offset + i];
			int red = ((val >> 4) & 0b11) * 85;
			int green = ((val >> 2) & 0b11) * 85;
			int blue = ((val >> 0) & 0b11) * 85;
			if(chckbxBw.isSelected()) {
				if(val > 0) {
					red = green = blue = 0xFF;
				}else {
					red = green = blue = 0;
				}
			}
			for(int i2 = xstart; i2 < xend; i2++) {
				if(i2 >= img.getWidth()) break;
				for(int j2 = ystart; j2 < yend; j2++) {
					if(j2 >= img.getHeight()) break;
					img.setRGB(i2, j2, blue | (green << 8) | (red << 16));
				}
			}
		}
		panel.repaint();
	}
	
}