package theGhastModding.scrapCPU.assembler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
	
	private static final List<Instruction> opcodes = new ArrayList<Instruction>();
	
	static {
		opcodes.add(new Instruction("NOP", 0x00, false));
		opcodes.add(new Instruction("LOAD", 0x01, true));
		opcodes.add(new Instruction("LOADi", 0x02, true));
		opcodes.add(new Instruction("STORE", 0x03, true));
		opcodes.add(new Instruction("STOREA", 0x04, true));
		opcodes.add(new Instruction("ADD", 0x05, true));
		opcodes.add(new Instruction("ADDc", 0x06, true));
		opcodes.add(new Instruction("SUB", 0x07, true));
		opcodes.add(new Instruction("EQL", 0x08, true));
		opcodes.add(new Instruction("MAG", 0x09, true));
		opcodes.add(new Instruction("JMP", 0x0A, true));
		opcodes.add(new Instruction("JZ", 0x0B, true));
		opcodes.add(new Instruction("JNZ", 0x0C, true));
		opcodes.add(new Instruction("qADD", 0x0D, true));
		opcodes.add(new Instruction("qADDc", 0x0E, true));
		opcodes.add(new Instruction("qSUB", 0x0F, true));
		opcodes.add(new Instruction("qEQL", 0x10, true));
		opcodes.add(new Instruction("qMAG", 0x11, true));
	}
	
	private static class Instruction {
		
		public String name;
		public int opcode;
		public boolean hasArgument;
		
		public Instruction(String name, int opcode, boolean hasArgument) {
			super();
			this.name = name;
			this.opcode = opcode;
			this.hasArgument = hasArgument;
		}
		
	}
	
	private static class Label {
		
		public String name;
		public int startLine;
		public int endLine;
		public int startAddr;
		
		public Label(String name, int startLine, int endLine, int startAddr) {
			this.name = name;
			this.startLine = startLine;
			this.endLine = endLine;
			this.startAddr = startAddr;
		}
		
	}
	
	private static List<Label> labels = new ArrayList<Label>();
	
	private static List<String> unwrapped = new ArrayList<String>();
	private static Map<Integer, Integer> unwrappedSources = new HashMap<Integer, Integer>();
	
	private static Map<String, String> symbols = new HashMap<String, String>();
	
	private static int getNextArgument(String[] s, int offset) {
		for(int j = offset + 1; j < s.length; j++) {
			if(!s[j].isBlank()) {
				return j;
			}
		}
		return -1;
	}
	
	private static boolean isKeyword(String s) {
		
		return false;
	}
	
	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
			List<String> lines = new ArrayList<String>();
			while(true) {
				String line = br.readLine();
				if(line == null) break;
				lines.add(line);
			}
			br.close();
			
			for(int i = 0; i < lines.size(); i++) {
				if(lines.get(i).contains("#")) {
					int loc = lines.get(i).indexOf('#');
					lines.set(i, lines.get(i).substring(0, loc - 1));
				}
			}
			
			for(int i = 0; i < lines.size(); i++) {
				if(lines.get(i).isBlank()) continue;
				if(!(lines.get(i).startsWith("\t") || lines.get(i).startsWith(" "))) {
					String name = lines.get(i).split("[ \t]")[0].trim();
					if(name.equals("SYMBOL")) { //It's a symbol, not a label!
						String[] s = lines.get(i).split("[ \t]");
						int indx = getNextArgument(s, 0);
						if(indx == -1) {
							System.err.println(String.format("Error on line %d: Missing argument 1 for symbol definition.", i));
							System.exit(1);
						}
						String symName = s[indx];
						indx = getNextArgument(s, indx);
						if(indx == -1) {
							System.err.println(String.format("Error on line %d: Missing argument 2 for symbol definition.", i));
							System.exit(1);
						}
						if(isKeyword(symName)) {
							System.err.println(String.format("Error on line %d: Invalid argument for symbol name.", i));
							System.exit(1);
						}
						String symValue = s[indx];
						if(symName.isBlank() || symValue.isBlank()) {
							System.err.println(String.format("Error on line %ld: Invalid symbol definition.", i));
							System.exit(1);
						}
						symbols.put(symName.trim(), symValue.trim());
					} else {
						if(isKeyword(name)) {
							System.err.println(String.format("Error on line %d: Invalid label name.", i));
							System.exit(1);
						}
						labels.add(new Label(name, i, 0, 0));
					}
				}
			}
			
			for(int i = 0; i < labels.size(); i++) {
				for(int j = 0; j < labels.size(); j++) {
					if(i == j) continue;
					if(labels.get(i).name.equals(labels.get(j).name)) {
						System.err.println("Error: duplicate label name " + labels.get(i).name + " on lines " + Integer.toString(labels.get(i).startLine) + " and " + Integer.toString(labels.get(j).startLine) + "!");
						System.exit(1);
					}
				}
			}
			
			//Find entry point
			Label entryPoint = null;
			for(int i = 0; i < labels.size(); i++) {
				if(labels.get(i).name.equals("MAIN")) entryPoint = labels.get(i);
			}
			if(entryPoint == null) {
				System.err.println("Fatal error: Entry Point not found!");
				System.exit(1);
			}
			
			unwrapped.clear();
			for(int i = 0; i < labels.size(); i++) {
				Label label = labels.get(i);
				Label smallest = null;
				int smallestLoc = Integer.MAX_VALUE;
				for(int j = 0; j < labels.size(); j++) {
					Label label2 = labels.get(j);
					if(label2.startLine > label.startLine && label2.startLine < smallestLoc) {
						smallestLoc = label2.startLine;
						smallest = label2;
					}
				}
				if(i == labels.size() - 1) {
					label.endLine = lines.size() - 1;
					continue;
				}
				label.endLine = smallest.startLine - 1;
			}
			
			entryPoint.startAddr = 0;
			Pattern p = Pattern.compile("[ \t]");
			for(int j = -1; j < labels.size(); j++) {
				Label label;
				if(j == -1) {
					label = entryPoint;
					label.startAddr = 0;
				}else {
					if(labels.get(j).name.equals(entryPoint.name)) continue;
					label = labels.get(j);
					label.startAddr = unwrapped.size() * 2;
				}
				for(int i = label.startLine; i <= label.endLine; i++) {
					String line = lines.get(i);
					if(line.isEmpty() || line.isBlank()) continue;
					if(line.startsWith("\t") || line.startsWith(" ")) {
						line = line.trim();
					}else {
						//Line has a label on it, so the label needs to be removed
						Matcher m = p.matcher(line);
						if(m.find()) {
							line = line.substring(m.start() + 1).trim();
							if(line.isEmpty() || line.isBlank()) continue;
						}else continue;
					}
					unwrapped.add(line);
					unwrappedSources.put(unwrapped.size() - 1, i);
				}
			}
			
			for(int i = 0; i < unwrapped.size(); i++) {
				String line = unwrapped.get(i);
				
				if(line.startsWith("OUT")) {
					unwrapped.set(i, "STORE\t63");
					continue;
				}
				if(line.startsWith("SYMBOL")) {
					unwrapped.remove(i);
					i--;
					continue;
				}
				
				String[] instr_args = line.split("[ \t]");
				int indx = getNextArgument(instr_args, 0);
				if(indx != -1) {
					for(String s:symbols.keySet()) {
						if(instr_args[indx].equals(s)) {
							String newLine = line.replace(s, symbols.get(s));
							unwrapped.set(i, newLine);
							line = newLine;
							break;
						}
					}
				}
				
				if(line.startsWith("JMP")) {
					//It's a jump instruction. Let's resolve its label into an actual ROM address.
					String[] jmp_args = line.split("[ \t]");
					if(jmp_args.length < 2) {
						System.err.println(String.format("Error on line %d: Missing argument for instruction.", unwrappedSources.get(i)));
						System.exit(1);
					}
					indx = getNextArgument(jmp_args, 0);
					if(indx == -1) {
						System.err.println(String.format("Error on line %d: Missing argument for instruction.", unwrappedSources.get(i)));
						System.exit(1);
					}
					String labelName = jmp_args[indx];
					if(isKeyword(labelName)) {
						System.err.println(String.format("Error on line %d: Invalid label name.", unwrappedSources.get(i)));
						System.exit(1);
					}
					Label label = null;
					for(int j = 0; j < labels.size(); j++) {
						if(labels.get(j).name.equals(labelName)) {
							label = labels.get(j);
							break;
						}
					}
					if(label == null) {
						System.err.println("Invalid label name: " + labelName + "!");
						System.exit(1);
					}
					line = line.replace(labelName, Integer.toString(label.startAddr));
					unwrapped.set(i, line);
				}
			}
			
			int cntr = 0;
			for(String s:unwrapped) {
				for(Label l:labels) if(cntr * 2 == l.startAddr && cntr != 0) System.out.println();
				System.out.println(s);
				cntr++;
			}
			
			byte[] bytecode = new byte[unwrapped.size() * 2];
			
			for(int i = 0; i < unwrapped.size(); i++) {
				String line = unwrapped.get(i);
				Instruction ins = null;
				for(Instruction in:opcodes) {
					if(line.split("[ \t]")[0].equals(in.name)) {
						ins = in;
						break;
					}
				}
				if(ins == null) {
					System.err.println(String.format("Error on line %d: Invalid Instruction!", unwrappedSources.get(i)));
					System.exit(1);
				}
				bytecode[i * 2] = (byte)ins.opcode;
				if(ins.hasArgument) {
					String[] instr_args = line.split("[ \t]");
					int indx = getNextArgument(instr_args, 0);
					if(indx == -1) {
						System.err.println(String.format("Error on line %d: Missing Argument!", unwrappedSources.get(i)));
						System.exit(1);
					}
					String arg = instr_args[indx];
					int argnum = 0;
					try {
						if(arg.startsWith("0x")) {
							argnum = Integer.parseInt(arg.substring(2), 16);
						} else if(arg.startsWith("0b")) {
							argnum = Integer.parseInt(arg.substring(2), 2);
						} else if(arg.startsWith("0") && arg.length() > 1) {
							argnum = Integer.parseInt(arg.substring(1), 8);
						} else {
							argnum = Integer.parseInt(arg, 10);
						}
					} catch(NumberFormatException e) {
						System.err.println(String.format("Error on line %d: Invalid Argument!", unwrappedSources.get(i)));
						System.exit(1);
					}
					if(argnum > 63) {
						System.err.println(String.format("Warning on line %d: Value overflow!", unwrappedSources.get(i)));
					}
					bytecode[i * 2 + 1] = (byte)argnum;
				} else {
					bytecode[i * 2 + 1] = 0;
				}
			}
			
			for(int i = 0; i < bytecode.length; i++) bytecode[i] &= 0b00111111;
			
			FileOutputStream fos = new FileOutputStream(args[0] + ".bin");
			fos.write(bytecode);
			fos.close();
			
		}catch(Exception e) {
			System.err.println("Exception during compilation: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}