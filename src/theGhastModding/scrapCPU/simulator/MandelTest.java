package theGhastModding.scrapCPU.simulator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class MandelTest {
	
	public static void main(String[] args) {
		
		BufferedImage res = new BufferedImage(63, 63, BufferedImage.TYPE_INT_RGB);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("numbers.txt")));
			
			int cntr = 0;
			while(true) {
				String line = br.readLine();
				if(line == null) break;
				int value = Integer.parseInt(line);
				if(value == 63) continue;
				int color = (int)((62.0 - value) / 62.0 * 255.0);
				color = color | (color << 8) | (color << 16);
				res.setRGB(cntr % 63, cntr / 63, color);
				cntr++;
			}
			JOptionPane.showMessageDialog(null, new ImageIcon(res));
			ImageIO.write(res, "png", new File("1.png"));
			
			br.close();
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
		
		//int c1 = fixed_div(4 << 6, res.getWidth() << 6);
		//int c2 = fixed_mul(c1, fixed_div(res.getWidth() << 6, 2 << 6));
		//int c3 = fixed_mul(c1, fixed_div(res.getHeight() << 6, 2 << 6));
		int c1 = 1;
		int c2 = 2 << 6;
		int c3 = c2;
		
		int x,y,yy,xx;
		int iteration = 0;
		for(int row = 0; row < res.getHeight(); row++) {
			int c_im = fixed_mul(row << 6, c1);
			c_im += (3 << 5);
			if(c_im >= c3) {
				c_im -= c3;
			}else {
				c_im = c3 - c_im;
			}
			for(int col = 0; col < res.getWidth(); col++) {
				int c_re = fixed_mul(col << 6, c1);
				c_re += (3 << 5);
				if(c_re >= c2) {
					c_re -= c2;
				}else {
					c_re = c2 - c_re;
				}
				x = c_re; y = c_im;
				iteration = 0;
				do {
					xx = fixed_mul(x, x);
					yy = fixed_mul(y, y);
					y = fixed_mul(x, y);
					y += y;
					y += c_im;
					x = xx - yy;
					x += c_re;
					iteration++;
				}while(xx+yy <= (4 << 6) && iteration < 62);
				
				//System.out.print(Integer.toHexString(iteration) + " ");
				int color = (int)((62 - iteration) / 62.0D * 255.0);
				res.setRGB(col, row, new Color(color, color, color).getRGB());
			}
		}
		
		JOptionPane.showMessageDialog(null, new ImageIcon(res));
		try {
			ImageIO.write(res, "png", new File("2.png"));
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	static int fixed_mul(int x, int y) {
		x &= 0b111111111111;
		y &= 0b111111111111;
		int res = (int) (((long)x * (long)y)) / (1 << 6);
		return res & 0b111111111111;
	}
	
	static int fixed_div(int x, int y) {
		x &= 0b111111111111;
		y &= 0b111111111111;
		return (int)(((long)x * (1 << 6)) / y) & 0b111111111111;
	}
	
}