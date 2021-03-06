package theGhastModding.scrapCPU.assembler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
	
	private static final List<Instruction> opcodes = new ArrayList<Instruction>();
	
	static {
		opcodes.add(new Instruction("NOP", 0x00, false, true));
		opcodes.add(new Instruction(new String[] {"LOAD", "LDA"}, 0x01, true, true));
		opcodes.add(new Instruction(new String[] {"STORE", "STB"}, 0x02, true, true));
		opcodes.add(new Instruction(new String[] {"STOREA", "STA"}, 0x03, true, true));
		opcodes.add(new Instruction("ADD", 0x04, true, true));
		opcodes.add(new Instruction(new String[] {"ADDc", "ADC"}, 0x05, true, true));
		opcodes.add(new Instruction("SUB", 0x06, true, true));
		opcodes.add(new Instruction(new String[] {"SUBc", "SUC"}, 0x07, true, true));
		opcodes.add(new Instruction("EQL", 0x08, true, true));
		opcodes.add(new Instruction("MAG", 0x09, true, true));
		//opcodes.add(new Instruction("LSH", 0x0A, true, true));
		//opcodes.add(new Instruction("RSH", 0x0B, true, true));
		opcodes.add(new Instruction("JMP", 0x0C, true, true));
		opcodes.add(new Instruction("JZ", 0x0D, true, true));
		opcodes.add(new Instruction("JNZ", 0x0E, true, true));
		opcodes.add(new Instruction(new String[] {"LOADm", "LDM"}, 0x0F, true, true));
		opcodes.add(new Instruction(new String[] {"LOADp", "LDP"}, 0b010000, true, true));
		opcodes.add(new Instruction(new String[] {"LOADi", "LDI"}, 0x3F, true, false));
		
		opcodes.add(new Instruction("qADD", 0x04 | (1 << 4), true, true));
		opcodes.add(new Instruction(new String[] {"qADDc", "qADC"}, 0x05 | (1 << 4), true, true));
		opcodes.add(new Instruction("qSUB", 0x06 | (1 << 4), true, true));
		opcodes.add(new Instruction(new String[] {"qSUBc", "qSUC"}, 0x07 | (1 << 4), true, true));
		opcodes.add(new Instruction("qEQL", 0x08 | (1 << 4), true, true));
		opcodes.add(new Instruction("qMAG", 0x09 | (1 << 4), true, true));
		opcodes.add(new Instruction("qLSH", 0x0A | (1 << 4), true, true));
		opcodes.add(new Instruction("qRSH", 0x0B | (1 << 4), true, true));
	}
	
	private static class Instruction {
		
		public String[] mnemonics;
		public int opcode;
		public boolean hasArgument;
		public boolean hasIndirectMode;
		
		public Instruction(String mnemonic, int opcode, boolean hasArgument, boolean hasIndirectMode) {
			this(new String[] {mnemonic}, opcode, hasArgument, hasIndirectMode);
		}
		
		public Instruction(String[] mnemonics, int opcode, boolean hasArgument, boolean hasIndirectMode) {
			super();
			this.mnemonics = mnemonics;
			this.opcode = opcode;
			this.hasArgument = hasArgument;
			this.hasIndirectMode = hasIndirectMode;
		}
		
		public boolean checkMnemonic(String s) {
			for(String s2:mnemonics) {
				if(s2.equalsIgnoreCase(s)) return true;
			}
			return false;
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
	
	private static boolean isNumber(String s) {
		String s2 = s;
		if(s.startsWith("0b")) {
			s2 = s.substring(2);
			for(char c:s2.toCharArray()) {
				if(c != '0' && c != '1') return false;
			}
			return true;
		}
		if(s.startsWith("0x")) {
			s2 = s.substring(2);
			for(char c:s2.toCharArray()) {
				if(c != '0' && c != '1' && c != '2' && c != '3' && c != '4' && c != '5' && c != '6' && c != '7' && c != '8' && c != '9' && c != 'A' && c != 'B' && c != 'C' && c != 'D' && c != 'E' && c != 'F' && c != 'a' && c != 'b' && c != 'c' && c != 'd' && c != 'e' && c != 'f') {
					return false;
				}
			}
			return true;
		}
		for(char c:s2.toCharArray()) {
			if(c != '0' && c != '1' && c != '2' && c != '3' && c != '4' && c != '5' && c != '6' && c != '7' && c != '8' && c != '9') {
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		try {
			
			/*
			 * 				if(line.startsWith("RETURN")) {
					unwrapped.set(i, "LOADm 61");
					unwrapped.add(i, "LOADp");
					unwrapped.add(i, "LOADm 62");
					unwrapped.add(i, "JMP");
				}
			 * 
			 * 
			 * Random rng = new Random();
			String brokenProgram = "";
			for(int i = 0; i != 32; i++) {
				Instruction ins = opcodes.get(rng.nextInt(opcodes.size()));
				if(ins.name.contains("RSH") || ins.name.contains("LSH") || ins.name.equals("JMP") || ins.name.contains("SUBc") || ins.name.equals("JZ") || ins.name.equals("JNZ")) {
					i--;
					continue;
				}
				brokenProgram += ins.name;
				if(rng.nextBoolean() || ins.opcode == 0x3F) {
					brokenProgram += "\t\t" + Integer.toString(rng.nextInt(64));
				}
				brokenProgram += "\n";
			}
			System.out.println(brokenProgram);*/
			
			BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
			List<String> lines = new ArrayList<String>();
			while(true) {
				String line = br.readLine();
				if(line == null) break;
				lines.add(line);
			}
			br.close();
			
			for(int i = 0; i < lines.size(); i++) {
				if(lines.get(i).contains("#") || lines.get(i).contains("//")) {
					int loc = Integer.MAX_VALUE;
					if(lines.get(i).contains("#")) loc = lines.get(i).indexOf('#');
					if(lines.get(i).contains("//")) loc = Math.min(loc, lines.get(i).indexOf("//"));
					if(loc <= 0) lines.set(i, "");
					else lines.set(i, lines.get(i).substring(0, loc - 1));
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
			int labelCnt = labels.size();
			for(int j = -1; j < labelCnt; j++) {
				Label label;
				if(j == -1) {
					label = entryPoint;
					label.startAddr = 0;
				}else {
					if(labels.get(j).name.equals(entryPoint.name)) continue;
					label = labels.get(j);
					label.startAddr = unwrapped.size();
				}
				for(int i = label.startLine; i <= label.endLine; i++) {
					String line = lines.get(i);
					if(line.isEmpty() || line.isBlank()) continue;
					if(line.startsWith("SYMBOL")) continue;
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
					
					//Macros parsed here
					if(line.startsWith("OUT")) {
						line = "STORE\t63";
					}
					
					if(line.startsWith("RETURN")) {
						unwrapped.add("LOADm 61");
						unwrappedSources.put(unwrapped.size() - 1, i);
						unwrapped.add("LOADp");
						unwrappedSources.put(unwrapped.size() - 1, i);
						unwrapped.add("LOADm 62");
						unwrappedSources.put(unwrapped.size() - 1, i);
						unwrapped.add("JMP");
						unwrappedSources.put(unwrapped.size() - 1, i);
					}else if(line.startsWith("CALL")) {
						UUID callID = UUID.randomUUID();
						unwrapped.add("LOADi CALL_" + callID.toString() + "_hi");
						unwrappedSources.put(unwrapped.size() - 1, i);
						unwrapped.add("STOREA 61");
						unwrappedSources.put(unwrapped.size() - 1, i);
						unwrapped.add("LOADi CALL_" + callID.toString() + "_lo");
						unwrappedSources.put(unwrapped.size() - 1, i);
						unwrapped.add("STOREA 62");
						unwrappedSources.put(unwrapped.size() - 1, i);
						unwrapped.add(line.replaceFirst("CALL", "JMP"));
						unwrappedSources.put(unwrapped.size() - 1, i);
						labels.add(new Label("CALL_" + callID.toString(), i + 5, i + 5, unwrapped.size()));
					}else{
						unwrapped.add(line);
						unwrappedSources.put(unwrapped.size() - 1, i);
					}
				}
			}
			
			//Figure out the actual address of each label
			int[] instructionAddr = new int[unwrapped.size()];
			int addrCntr = 0;
			for(int i = 0; i < unwrapped.size(); i++) {
				String line = unwrapped.get(i);
				
				Instruction ins = null;
				for(Instruction in:opcodes) {
					if(in.checkMnemonic(line.split("[ \t]")[0])) {
						ins = in;
						break;
					}
				}
				if(ins == null) {
					System.err.println(String.format("Error on line %d: Invalid Instruction (" + line.split("[ \t]")[0] + ")!", unwrappedSources.get(i)));
					System.exit(1);
				}
				instructionAddr[i] = addrCntr;
				if(ins.mnemonics[0].equals("JMP") || ins.mnemonics[0].equals("JZ") || ins.mnemonics[0].equals("JNZ")) {
					String[] instr_args = line.split("[ \t]");
					int indx = getNextArgument(instr_args, 0);
					if(indx == -1) {
						addrCntr++;
					}
					else if(isNumber(instr_args[indx])) {
						addrCntr+=2;
					}else {
						addrCntr += 4;
					}
				}
				else if(ins.mnemonics[0].equals("NOP")) {
					addrCntr += 2;
				}
				else if(ins.hasArgument) {
					String[] instr_args = line.split("[ \t]");
					int indx = getNextArgument(instr_args, 0);
					if(indx == -1 && !ins.hasIndirectMode) {
						System.err.println(String.format("Error on line %d: Missing Argument!", unwrappedSources.get(i)));
						System.exit(1);
					}
					if(indx == -1 && ins.hasIndirectMode) {
						addrCntr++;
					}else addrCntr += 2;
				}else addrCntr++;
			}
			for(Label l:labels) {
				l.startAddr = instructionAddr[l.startAddr];
				symbols.put(l.name + "_hi", Integer.toString((l.startAddr >>> 6) & 0b111));
				symbols.put(l.name + "_lo", Integer.toString(l.startAddr & 0b00111111));
			}
			
			int indx;
			for(int i = 0; i < unwrapped.size(); i++) {
				String line = unwrapped.get(i);
				
				String[] instr_args = line.split("[ \t]");
				indx = getNextArgument(instr_args, 0);
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
				
				if(line.startsWith("JMP") || line.startsWith("JZ") || line.startsWith("JNZ")) {
					//It's a jump instruction. Let's resolve its label into an actual ROM address.;
					String[] jmp_args = line.split("[ \t]");
					if(jmp_args.length < 2) {
						//Is indirect
						continue;
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
					
					int addr = label.startAddr;
					String pCmd = "LOADp " + Integer.toString((addr >>> 6) & 0b111);
					addr &= 0b00111111;
					line = line.replace(labelName, Integer.toString(addr));
					unwrapped.set(i, line);
					unwrapped.add(i, pCmd);
					i++;
				}
			}
			
			int cntr = 0;
			for(String s:unwrapped) {
				for(Label l:labels) if(cntr == l.startAddr && cntr != 0) System.out.println();
				System.out.println(s);
				cntr++;
			}
			
			byte[] bytecode = new byte[unwrapped.size() * 2];
			int bytecodeSize = 0;
			
			for(int i = 0; i < unwrapped.size(); i++) {
				String line = unwrapped.get(i);
				Instruction ins = null;
				for(Instruction in:opcodes) {
					if(in.checkMnemonic(line.split("[ \t]")[0])) {
						ins = in;
						break;
					}
				}
				if(ins == null) {
					System.err.println(String.format("Error on line %d: Invalid Instruction (" + line.split("[ \t]")[0] + ")!", unwrappedSources.get(i)));
					System.exit(1);
				}
				bytecode[bytecodeSize] = (byte)ins.opcode;
				bytecodeSize++;
				if(ins.hasArgument) {
					String[] instr_args = line.split("[ \t]");
					indx = getNextArgument(instr_args, 0);
					if(indx == -1 && !ins.hasIndirectMode) {
						System.err.println(String.format("Error on line %d: Missing Argument!", unwrappedSources.get(i)));
						System.exit(1);
					}
					if(indx == -1 && ins.hasIndirectMode) {
						bytecode[bytecodeSize - 1] = (byte)(ins.opcode | (1 << 5));
					}else {
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
						bytecode[bytecodeSize] = (byte)(argnum & 0b00111111);
						bytecodeSize++;
					}
				}else if(ins.mnemonics[0].equals("NOP")) {
					bytecode[bytecodeSize] = 0;
					bytecodeSize++;
				}/* else {
					bytecode[bytecodeSize] = 0;
					bytecodeSize++;
				}*/
			}
			
			for(int i = 0; i < bytecode.length; i++) {
				bytecode[i] &= 0b00111111;
			}
			
			FileOutputStream fos = new FileOutputStream(args[0] + ".bin");
			fos.write(bytecode, 0, bytecodeSize);
			fos.close();
			
		}catch(Exception e) {
			System.err.println("Exception during compilation: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}