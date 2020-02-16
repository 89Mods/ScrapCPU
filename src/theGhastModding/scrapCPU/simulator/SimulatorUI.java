package theGhastModding.scrapCPU.simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.exbin.bined.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import javax.swing.JCheckBox;

public class SimulatorUI extends JPanel implements Runnable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2610128612675756693L;
	
	private byte[] RAM_mirror = new byte[64];
	private ByteArrayEditableData dat = new ByteArrayEditableData(RAM_mirror);
	
	private byte[] ROM_mirror = new byte[512];
	private ByteArrayEditableData dat2 = new ByteArrayEditableData(ROM_mirror);
	
	private BufferedImage registerImage = new BufferedImage(477, 28, BufferedImage.TYPE_INT_RGB);
	private BufferedImage segmentDisplayImage = new BufferedImage(100, 45, BufferedImage.TYPE_INT_RGB);
	private Color lightColor = new Color(0, 255, 0);
	private boolean running = false;
	private boolean simulate = false;
	
	private BufferedImage[] segmentImages = new BufferedImage[10];
	
	private JSlider slider;
	private JPanel panelINSIN;
	private JPanel panelA;
	private JPanel panelB;
	private JPanel panelM;
	private JPanel panelPC;
	private JPanel panelP;
	
	private JLabel lvalueDINSIN; private String DINSINtext = "Value (Decimal): ";
	private JLabel lvalueDA; private String DAtext = "Value (Decimal): ";
	private JLabel lvalueDB; private String DBtext = "Value (Decimal): ";
	private JLabel lvalueDM; private String DMtext = "Value (Decimal): ";
	private JLabel lvalueDPC; private String DPCtext = "Value (Decimal): ";
	private JLabel lvalueDP; private String DPtext = "Value (Decimal): ";
	
	private JLabel lvalueHINSIN; private String HINSINtext = "Value (Hexadecimal): ";
	private JLabel lvalueHA; private String HAtext = "Value (Hexadecimal): ";
	private JLabel lvalueHB; private String HBtext = "Value (Hexadecimal): ";
	private JLabel lvalueHM; private String HMtext = "Value (Hexadecimal): ";
	private JLabel lvalueHPC; private String HPCtext = "Value (Hexadecimal): ";
	private JLabel lvalueHP; private String HPtext = "Value (Hexadecimal): ";
	
	private JLabel lblflag; private String flagtext = "0-flag: ";
	private JLabel lblCarryFlag; private String lblCarrytext = "Carry Flag: ";
	
	private JLabel lblOutputHexValue; private String outputHexValuetext = "Hex Value: ";
	
	private JPanel panelOutput;
	
	private JCheckBox chckbxNewCheckBox;
	
	private CodeArea txtpnA;
	private CodeArea txtpnB;
	
	private Thread t;
	
	private Simulator simulator = new Simulator();
	private MemoryVisualizer mv;
	
	public SimulatorUI(JFrame parrent) {
		super();
		
		/*Random rng = new Random();
		String s = "";
		for(int i = 0; i < 4; i++) {
			s += Long.toString(rng.nextLong(), 32);
		}
		System.out.println(s);
		String s2 = "";
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(rng.nextBoolean()) {
				s2 += Character.toString(c).toUpperCase();
			}else {
				s2 += Character.toString(c).toLowerCase();
			}
		}
		System.out.println(s2);*/
		
		setPreferredSize(new Dimension(1050, 711));
		setLayout(null);
		
		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				simulate = true;
			}
		});
		btnStart.setBounds(12, 12, 114, 25);
		add(btnStart);
		
		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				simulate = false;
			}
		});
		btnStop.setBounds(138, 12, 114, 25);
		add(btnStop);
		
		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				simulator.reset(true);
			}
		});
		btnReset.setBounds(390, 12, 114, 25);
		add(btnReset);
		
		JLabel lblSpeed = new JLabel("Speed:");
		lblSpeed.setHorizontalAlignment(SwingConstants.CENTER);
		lblSpeed.setBounds(22, 49, 66, 15);
		add(lblSpeed);
		
		slider = new JSlider();
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(25);
		slider.setBounds(106, 48, 272, 24);
		add(slider);
		
		JButton btnStep = new JButton("Step");
		btnStep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				simulator.step();
			}
		});
		btnStep.setBounds(264, 12, 114, 25);
		add(btnStep);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "CPU Registers", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(12, 80, 501, 609);
		add(panel);
		panel.setLayout(null);
		
		JLabel lblINSIN = new JLabel("INSIN");
		lblINSIN.setBounds(12, 116, 66, 15);
		panel.add(lblINSIN);
		
		panelINSIN = new JPanel();
		panelINSIN.setBounds(12, 143, 477, 28);
		panel.add(panelINSIN);
		
		lvalueDINSIN = new JLabel(DINSINtext);
		lvalueDINSIN.setBounds(12, 183, 205, 15);
		panel.add(lvalueDINSIN);
		
		lvalueHINSIN = new JLabel(HINSINtext);
		lvalueHINSIN.setBounds(174, 183, 182, 15);
		panel.add(lvalueHINSIN);
		
		lvalueHA = new JLabel(HAtext);
		lvalueHA.setBounds(174, 277, 182, 15);
		panel.add(lvalueHA);
		
		lvalueDA = new JLabel(DAtext);
		lvalueDA.setBounds(12, 277, 205, 15);
		panel.add(lvalueDA);
		
		panelA = new JPanel();
		panelA.setBounds(12, 237, 477, 28);
		panel.add(panelA);
		
		JLabel lblA = new JLabel("A");
		lblA.setBounds(12, 210, 66, 15);
		panel.add(lblA);
		
		lvalueHB = new JLabel(HBtext);
		lvalueHB.setBounds(174, 371, 182, 15);
		panel.add(lvalueHB);
		
		lvalueDB = new JLabel(DBtext);
		lvalueDB.setBounds(12, 371, 205, 15);
		panel.add(lvalueDB);
		
		panelB = new JPanel();
		panelB.setBounds(12, 331, 477, 28);
		panel.add(panelB);
		
		JLabel lblB = new JLabel("B");
		lblB.setBounds(12, 304, 66, 15);
		panel.add(lblB);
		
		lvalueHM = new JLabel(HMtext);
		lvalueHM.setBounds(174, 465, 182, 15);
		panel.add(lvalueHM);
		
		lvalueDM = new JLabel(DMtext);
		lvalueDM.setBounds(12, 465, 205, 15);
		panel.add(lvalueDM);
		
		panelM = new JPanel();
		panelM.setBounds(12, 425, 477, 28);
		panel.add(panelM);
		
		JLabel lblM = new JLabel("M");
		lblM.setBounds(12, 398, 66, 15);
		panel.add(lblM);
		
		lblflag = new JLabel(flagtext);
		lblflag.setBounds(12, 586, 115, 15);
		panel.add(lblflag);
		
		lblCarryFlag = new JLabel(lblCarrytext);
		lblCarryFlag.setBounds(139, 586, 188, 15);
		panel.add(lblCarryFlag);
		
		lvalueHPC = new JLabel(HPCtext);
		lvalueHPC.setBounds(174, 89, 182, 15);
		panel.add(lvalueHPC);
		
		lvalueDPC = new JLabel(DPCtext);
		lvalueDPC.setBounds(12, 89, 205, 15);
		panel.add(lvalueDPC);
		
		panelPC = new JPanel();
		panelPC.setBounds(12, 49, 477, 28);
		panel.add(panelPC);
		
		JLabel lblPc = new JLabel("PC");
		lblPc.setBounds(12, 22, 66, 15);
		panel.add(lblPc);
		
		lvalueHP = new JLabel("Value (Hexadecimal): ");
		lvalueHP.setBounds(174, 559, 182, 15);
		panel.add(lvalueHP);
		
		lvalueDP = new JLabel("Value (Decimal): ");
		lvalueDP.setBounds(12, 559, 205, 15);
		panel.add(lvalueDP);
		
		panelP = new JPanel();
		panelP.setBounds(12, 519, 477, 28);
		panel.add(panelP);
		
		JLabel labelP = new JLabel("P");
		labelP.setBounds(12, 492, 66, 15);
		panel.add(labelP);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(null, "Memory Contents", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_5.setBounds(525, 174, 501, 214);
		add(panel_5);
		panel_5.setLayout(null);
		
		txtpnA = new CodeArea();
		txtpnA.setContentData(dat);
		txtpnA.setBounds(12, 24, 477, 178);
		txtpnA.setBorder(new LineBorder(Color.BLUE));
		panel_5.add(txtpnA);
		txtpnA.notifyDataChanged();
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Output Display", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(525, 80, 491, 89);
		add(panel_1);
		panel_1.setLayout(null);
		
		panelOutput = new JPanel();
		panelOutput.setBounds(12, 26, 100, 45);
		panel_1.add(panelOutput);
		
		lblOutputHexValue = new JLabel(outputHexValuetext);
		lblOutputHexValue.setBounds(130, 56, 128, 15);
		panel_1.add(lblOutputHexValue);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "ROM Contents", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBounds(525, 400, 501, 213);
		add(panel_2);
		panel_2.setLayout(null);
		
		txtpnB = new CodeArea();
		txtpnB.setContentData(dat2);
		txtpnB.setBounds(12, 24, 477, 177);
		txtpnB.setBorder(new LineBorder(Color.BLUE));
		panel_2.add(txtpnB);
		txtpnB.notifyDataChanged();
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("BIN files", "bin", "BIN"));
		fileChooser.setCurrentDirectory(new File("."));
		
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fileChooser.showOpenDialog(parrent);
				File f = fileChooser.getSelectedFile();
				if(!f.exists()) {
					JOptionPane.showMessageDialog(parrent, "Error: File does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					simulate = false;
					simulator.reset(false);
					Arrays.fill(simulator.ROM, 0);
					FileInputStream fis = new FileInputStream(f);
					for(int i = 0; i < 512; i++) {
						if(fis.available() <= 0) break;
						simulator.ROM[i] = (fis.read() & 0xFF) & 0b00111111;
					}
					for(int i = 0; i < 512; i++) ROM_mirror[i] = (byte)simulator.ROM[i];
					txtpnB.notifyDataChanged();
					fis.close();
				} catch(Exception e) {
					JOptionPane.showMessageDialog(parrent, "Error reading file: " + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					System.exit(1);
				}
			}
		});
		fileMenu.add(openItem);
		
		JMenuItem dumpItem = new JMenuItem("Dump RAM");
		dumpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean prevSimulate = simulate;
				simulate = false;
				try {
					FileOutputStream fos = new FileOutputStream("dump.dat");
					for(int i = 0; i < 64; i++) {
						fos.write((byte)simulator.RAM[i]);
					}
					fos.close();
				}catch(Exception e) {
					JOptionPane.showMessageDialog(parrent, "Error doing RAM dump: " + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					System.exit(1);
				}
				simulate = prevSimulate;
			}
		});
		fileMenu.add(dumpItem);
		
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		fileMenu.add(exitItem);
		
		TypeConverter tc = new TypeConverter(this);
		mv = new MemoryVisualizer();
		
		chckbxNewCheckBox = new JCheckBox("Hyperspeed");
		chckbxNewCheckBox.setBounds(390, 45, 126, 23);
		add(chckbxNewCheckBox);
		
		JMenu toolsMenu = new JMenu("Tools");
		menuBar.add(toolsMenu);
		JMenuItem converterItem = new JMenuItem("Type Converter");
		converterItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tc.setVisible(true);
			}
		});
		toolsMenu.add(converterItem);
		JMenuItem randomnizeItem = new JMenuItem("Randomnize Memory Contents");
		randomnizeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Random rng = new Random();
				for(int i = 0; i < 64; i++) {
					simulator.RAM[i] = rng.nextInt(64);
				}
			}
		});
		toolsMenu.add(randomnizeItem);
		JMenuItem visualizerItem = new JMenuItem("Memory visualizer");
		visualizerItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				mv.showVisualizer(simulator);
			}
		});
		toolsMenu.add(visualizerItem);
		
		parrent.setJMenuBar(menuBar);
		
		File f = null;
		try {
			for(int i = 0; i < 10; i++) {
				f = new File("res/7Seg-" + Integer.toString(i) + ".png");
				segmentImages[i] = ImageIO.read(f);
			}
		} catch(Exception e) {
			JOptionPane.showMessageDialog(parrent, "Error loading resource " + f.getPath() + ": " + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(1);
		}
		
		t = new Thread(this);
		t.start();
	}
	
	public void run() {
		
		running = true;
		long startTime = System.currentTimeMillis();
		int prevDisplay = 0;
		FileOutputStream busOut = null;
		try {
			busOut = new FileOutputStream("busdump.dat");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		while(running) {
			
			if(simulate) {
				simulator.step();
				try {
					busOut.write(simulator.busState);
					busOut.flush();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			if(simulator.RAM[63] != prevDisplay) {
				prevDisplay = simulator.RAM[63];
				System.out.println(Integer.toString(simulator.RAM[63]));
			}
			
			if(!chckbxNewCheckBox.isSelected() || (System.currentTimeMillis() - startTime >= 100)) {
				renderRegisterValue(simulator.PC, panelPC, 9);
				lvalueDPC.setText(DPCtext + Integer.toString(simulator.PC));
				lvalueHPC.setText(HPCtext + Integer.toHexString(simulator.PC));
				renderRegisterValue(simulator.ins, panelINSIN, 6);
				lvalueDINSIN.setText(DINSINtext + Integer.toString(simulator.ins));
				lvalueHINSIN.setText(HINSINtext + Integer.toHexString(simulator.ins));
				renderRegisterValue(simulator.A, panelA, 6);
				lvalueDA.setText(DAtext + Integer.toString(simulator.A));
				lvalueHA.setText(HAtext + Integer.toHexString(simulator.A));
				renderRegisterValue(simulator.B, panelB, 6);
				lvalueDB.setText(DBtext + Integer.toString(simulator.B));
				lvalueHB.setText(HBtext + Integer.toHexString(simulator.B));
				renderRegisterValue(simulator.M, panelM, 6);
				lvalueDM.setText(DMtext + Integer.toString(simulator.M));
				lvalueHM.setText(HMtext + Integer.toHexString(simulator.M));
				renderRegisterValue(simulator.P, panelP, 3);
				lvalueDP.setText(DPtext + Integer.toString(simulator.P));
				lvalueHP.setText(HPtext + Integer.toHexString(simulator.P));
				mv.updateThingy();
				
				lblflag.setText(flagtext + (simulator.zero ? "1" : "0"));
				lblCarryFlag.setText(lblCarrytext + (simulator.carry ? "1" : "0"));
				
				renderSegmentDisplay(simulator.RAM[63], panelOutput);
				lblOutputHexValue.setText(outputHexValuetext + Integer.toHexString(simulator.RAM[63]));
				
				if(System.currentTimeMillis() - startTime >= 100) {
					startTime = System.currentTimeMillis();
					for(int i = 0; i < 64; i++) RAM_mirror[i] = (byte)simulator.RAM[i];
					txtpnA.notifyDataChanged();
					txtpnB.notifyDataChanged();
				}
			}
			
			try {
				if(simulate) {
					if(!chckbxNewCheckBox.isSelected()) Thread.sleep(Math.max(1000 - (slider.getValue() * 10), 1));
				}
				else Thread.sleep(100);
			}catch(Exception e) {e.printStackTrace();}
		}
		try {
			busOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void renderRegisterValue(int val, JPanel panel, int bits) {
		Graphics g2 = panel.getGraphics();
		if(g2 == null) return;
		Graphics2D g = (Graphics2D)registerImage.getGraphics();
		g.setColor(panel.getBackground());
		g.fillRect(0, 0, registerImage.getWidth(), registerImage.getHeight());
		double rDist = 477.0 / (double)bits;
		for(int i = 0; i < bits; i++) {
			int bit = (val >> (bits - 1 - i)) & 1;
			if(bit == 1) g.setColor(lightColor);
			else g.setColor(Color.DARK_GRAY);
			g.fillRect((int)((double)i * rDist), 0, 28, 28);
		}
		g2.drawImage(registerImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);
	}
	
	private void renderSegmentDisplay(int val, JPanel panel) {
		Graphics g2 = panel.getGraphics();
		if(g2 == null) return;
		Graphics2D g = (Graphics2D)segmentDisplayImage.getGraphics();
		g.setColor(panel.getBackground());
		g.fillRect(0, 0, segmentDisplayImage.getWidth(), segmentDisplayImage.getHeight());
		int ones = (val % 100) % 10;
		int tens = (val % 100) / 10;
		g.drawImage(segmentImages[tens], 0, 0, 50, 45, null);
		g.drawImage(segmentImages[ones], 50, 0, 50, 45, null);
		g2.drawImage(segmentDisplayImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Scrap-CPU Emulator");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SimulatorUI ui = new SimulatorUI(frame);
		frame.setContentPane(ui);
		
		frame.pack();
		frame.setVisible(true);
	}
}