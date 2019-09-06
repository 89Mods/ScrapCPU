package theGhastModding.scrapCPU.simulator;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class TypeConverter extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5363580446998180924L;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;

	public TypeConverter(JComponent parent) {
		super("Type Converter");
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.getContentPane().setLayout(null);
		this.getContentPane().setPreferredSize(new Dimension(256, 185));
		
		JLabel lblSingleWord = new JLabel("Low Word");
		lblSingleWord.setBounds(12, 12, 111, 15);
		getContentPane().add(lblSingleWord);
		
		JLabel lblSingleWord_1 = new JLabel("High Word");
		lblSingleWord_1.setBounds(135, 12, 124, 15);
		getContentPane().add(lblSingleWord_1);
		
		textField = new JTextField();
		textField.setText("0");
		textField.setBounds(12, 39, 111, 19);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		textField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					int wordVal = 0;
					int word1 = 0;
					int word2 = 0;
					try {
						word1 = Integer.parseInt(textField.getText());
						word2 = Integer.parseInt(textField_1.getText());;
					}catch(NumberFormatException e) {
						return;
					}
					word1 &= 0b00111111;
					word2 &= 0b00111111;
					wordVal = word1 + (word2 << 6);
					textField.setText(Integer.toString(word1));
					textField_1.setText(Integer.toString(word2));
					textField_2.setText(Integer.toString(wordVal));
					textField_3.setText(Float.toString(wordVal / 64.0f));
				}
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {}
			
			@Override
			public void keyTyped(KeyEvent arg0) {}
			
		});
		
		textField_1 = new JTextField();
		textField_1.setText("0");
		textField_1.setBounds(135, 39, 111, 19);
		getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		textField_1.addKeyListener(textField.getKeyListeners()[0]);
		
		JLabel lblDoubleWord = new JLabel("Double Word");
		lblDoubleWord.setBounds(12, 70, 150, 15);
		getContentPane().add(lblDoubleWord);
		
		textField_2 = new JTextField();
		textField_2.setText("0");
		textField_2.setBounds(12, 97, 234, 19);
		getContentPane().add(textField_2);
		textField_2.setColumns(10);
		
		textField_2.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					int wordVal = 0;
					try {
						wordVal = Integer.parseInt(textField_2.getText());
					}catch(NumberFormatException e) {
						return;
					}
					wordVal &= 0b111111111111;
					textField_2.setText(Integer.toString(wordVal));
					textField.setText(Integer.toString(wordVal & 0b00111111));
					textField_1.setText(Integer.toString((wordVal >> 6) & 0b00111111));
					textField_3.setText(Float.toString(wordVal / 64.0f));
				}
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {}
			
			@Override
			public void keyTyped(KeyEvent arg0) {}
			
		});
		
		JLabel lblFixedPoint = new JLabel("Fixed point");
		lblFixedPoint.setBounds(12, 128, 124, 15);
		getContentPane().add(lblFixedPoint);
		
		textField_3 = new JTextField();
		textField_3.setText("0");
		textField_3.setBounds(12, 155, 234, 19);
		getContentPane().add(textField_3);
		textField_3.setColumns(10);
		this.setLocationRelativeTo(parent);
		
		textField_3.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					float floatVal = 0;
					try {
						floatVal = Float.parseFloat(textField_3.getText());
					}catch(Exception e) {
						return;
					}
					if(floatVal < 0) return;
					int wordVal = (int)(floatVal * 64.0f) & 0b111111111111;
					int word1 = wordVal & 0b00111111;
					int word2 = (wordVal >> 6) & 0b00111111;
					textField.setText(Integer.toString(word1));
					textField_1.setText(Integer.toString(word2));
					textField_2.setText(Integer.toString(wordVal));
					textField_3.setText(Float.toString(wordVal / 64.0f));
				}
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {}
			
			@Override
			public void keyTyped(KeyEvent arg0) {}
			
		});
		
		this.pack();
	}
}