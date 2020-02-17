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
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;
	private JTextField textField_7;
	private JLabel lblW_1;
	private JLabel lblW_2;
	private JLabel lblW_3;
	private JLabel lblLongWord;
	private JTextField textField_8;
	private JLabel lblFixedPoint_1;
	private JTextField textField_9;

	public TypeConverter(JComponent parent) {
		super("Type Converter");
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.getContentPane().setLayout(null);
		this.getContentPane().setPreferredSize(new Dimension(517, 185));
		
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
					int wordVal = (int)Math.round(floatVal * 64.0f) & 0b111111111111;
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
		
		textField_4 = new JTextField();
		textField_4.setText("0");
		textField_4.setColumns(10);
		textField_4.setBounds(271, 39, 49, 19);
		getContentPane().add(textField_4);
		
		textField_5 = new JTextField();
		textField_5.setText("0");
		textField_5.setColumns(10);
		textField_5.setBounds(332, 39, 50, 19);
		getContentPane().add(textField_5);
		
		textField_6 = new JTextField();
		textField_6.setText("0");
		textField_6.setColumns(10);
		textField_6.setBounds(394, 39, 50, 19);
		getContentPane().add(textField_6);
		
		textField_7 = new JTextField();
		textField_7.setText("0");
		textField_7.setColumns(10);
		textField_7.setBounds(456, 39, 49, 19);
		getContentPane().add(textField_7);
		
		JLabel lblW = new JLabel("W1");
		lblW.setBounds(271, 12, 49, 15);
		getContentPane().add(lblW);
		
		lblW_1 = new JLabel("W2");
		lblW_1.setBounds(332, 12, 41, 15);
		getContentPane().add(lblW_1);
		
		lblW_2 = new JLabel("W3");
		lblW_2.setBounds(394, 12, 41, 15);
		getContentPane().add(lblW_2);
		
		lblW_3 = new JLabel("W4");
		lblW_3.setBounds(456, 12, 49, 15);
		getContentPane().add(lblW_3);
		
		lblLongWord = new JLabel("Long Word");
		lblLongWord.setBounds(271, 70, 234, 15);
		getContentPane().add(lblLongWord);
		
		textField_8 = new JTextField();
		textField_8.setText("0");
		textField_8.setColumns(10);
		textField_8.setBounds(271, 97, 234, 19);
		getContentPane().add(textField_8);
		
		lblFixedPoint_1 = new JLabel("Fixed point");
		lblFixedPoint_1.setBounds(271, 128, 234, 15);
		getContentPane().add(lblFixedPoint_1);
		
		textField_9 = new JTextField();
		textField_9.setText("0");
		textField_9.setColumns(10);
		textField_9.setBounds(271, 155, 234, 19);
		getContentPane().add(textField_9);
		this.setLocationRelativeTo(parent);
		
		KeyListener al = new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					int wordVal1,wordVal2,wordVal3,wordVal4;
					try {
						wordVal1 = Integer.parseInt(textField_4.getText().toString());
						wordVal2 = Integer.parseInt(textField_5.getText().toString());
						wordVal3 = Integer.parseInt(textField_6.getText().toString());
						wordVal4 = Integer.parseInt(textField_7.getText().toString());
					}catch(NumberFormatException e) {
						return;
					}
					wordVal1 &= 0b111111;
					textField_4.setText(Integer.toString(wordVal1));
					wordVal2 &= 0b111111;
					textField_5.setText(Integer.toString(wordVal2));
					wordVal3 &= 0b111111;
					textField_6.setText(Integer.toString(wordVal3));
					wordVal4 &= 0b111111;
					textField_7.setText(Integer.toString(wordVal4));
					int wordVal = wordVal1 | (wordVal2 << 6) | (wordVal3 << 12) | (wordVal4 << 18);
					textField_8.setText(Integer.toString(wordVal));
					textField_9.setText(Float.toString(wordVal / 4096.0f));
				}
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {}
			
			@Override
			public void keyTyped(KeyEvent arg0) {}
			
		};
		textField_4.addKeyListener(al);;
		textField_5.addKeyListener(al);
		textField_6.addKeyListener(al);
		textField_7.addKeyListener(al);
		
		textField_8.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					int wordVal;
					try {
						wordVal = Integer.parseInt(textField_8.getText());
					}catch(NumberFormatException e) {
						return;
					}
					wordVal &= 0b111111111111111111111111;
					textField_8.setText(Integer.toString(wordVal));
					int wordVal1 = (wordVal >> 0) & 0b111111;
					int wordVal2 = (wordVal >> 6) & 0b111111;
					int wordVal3 = (wordVal >> 12) & 0b111111;
					int wordVal4 = (wordVal >> 18) & 0b111111;
					textField_4.setText(Integer.toString(wordVal1));
					textField_5.setText(Integer.toString(wordVal2));
					textField_6.setText(Integer.toString(wordVal3));
					textField_7.setText(Integer.toString(wordVal4));
					textField_9.setText(Float.toString(wordVal / 4096.0f));
				}
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {}
			
			@Override
			public void keyTyped(KeyEvent arg0) {}
			
		});
		
		textField_9.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					float floatVal;
					try {
						floatVal = Float.parseFloat(textField_9.getText());
					}catch(NumberFormatException e) {
						return;
					}
					if(floatVal < 0) return;
					int wordVal = (int)Math.round(floatVal * 4096.0f) & 0b111111111111111111111111;
					textField_9.setText(Float.toString(wordVal / 4096.0f));
					int wordVal1 = (wordVal >> 0) & 0b111111;
					int wordVal2 = (wordVal >> 6) & 0b111111;
					int wordVal3 = (wordVal >> 12) & 0b111111;
					int wordVal4 = (wordVal >> 18) & 0b111111;
					textField_4.setText(Integer.toString(wordVal1));
					textField_5.setText(Integer.toString(wordVal2));
					textField_6.setText(Integer.toString(wordVal3));
					textField_7.setText(Integer.toString(wordVal4));
					textField_8.setText(Integer.toString(wordVal));
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