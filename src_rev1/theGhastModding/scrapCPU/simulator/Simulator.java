package theGhastModding.scrapCPU.simulator;

import java.util.Arrays;

public class Simulator {
	
	public int A;
	public int B;
	public int M;
	public int PC;
	public int ins;
	public int STEP;
	public boolean carry;
	public boolean zero;
	
	public int[] RAM = new int[64];
	public int[] ROM = new int[64];
	
	public Simulator() {
		reset(false);
	}
	
	public void reset(boolean keepRom) {
		A = B = M = PC = ins = STEP = 0;
		carry = false;
		zero = true;
		Arrays.fill(RAM, 0);
		if(!keepRom) Arrays.fill(ROM, 0);
	}
	
	public void step() {
		PC &= 0b00111111;
		if(STEP == 0) {
			//Nothing
			STEP++;
			return;
		}
		if(STEP == 1) {
			//Fetch Instruction
			STEP++;
			ins = ROM[PC];
			PC++;
			return;
		}
		switch(ins) {
		default:
			STEP = 0;
			PC++;
			return;
		case 0x01: //LOAD
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) A = RAM[M];
			if(STEP == 4) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x02: //LOADi
			if(STEP == 2) A = ROM[PC];
			if(STEP == 3) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x03: //STORE
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) RAM[M] = B;
			if(STEP == 4) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x04: //STOREA
			if(STEP == 2) ALUstep(0b001, 0);
			if(STEP == 3) M = ROM[PC];
			if(STEP == 4) RAM[M] = B;
			if(STEP == 5) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x05: //ADD
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b001, RAM[M]);
			if(STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x06: //ADDc
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b101, RAM[M]);
			if(STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x07: //SUB
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b011, RAM[M]);
			if(STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x08: //EQL
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b010, RAM[M]);
			if(STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x09: //MAG
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b000, RAM[M]);
			if(STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x0A: //JMP
			if(STEP == 2) PC = ROM[PC];
			if(STEP == 3) {
				STEP = 0;
				return;
			}
			break;
		case 0x0B: //JMPA
			if(STEP == 2 && zero) PC = ROM[PC];
			if(STEP == 3 && zero) {
				STEP = 0;
				return;
			}
			if(STEP == 2 && !zero) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x0C: //JMPB
			if(STEP == 2 && zero) {
				STEP = 0;
				PC++;
				return;
			}
			if(STEP == 2 && !zero) PC = ROM[PC];
			if(STEP == 3 && !zero) {
				STEP = 0;
				return;
			}
			break;
		case 0x0D: //qADD
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b001, RAM[M]);
			if(STEP == 4) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x0E: //qADDc
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b101, RAM[M]);
			if(STEP == 4) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x0F: //qSUB
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b011, RAM[M]);
			if(STEP == 4) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x10: //qEQL
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b010, RAM[M]);
			if(STEP == 4) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		case 0x11: //qMAG
			if(STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b000, RAM[M]);
			if(STEP == 4) {
				STEP = 0;
				PC++;
				return;
			}
			break;
		}
		STEP++;
	}
	
	private void ALUstep(int alusel, int busval) {
		if(alusel == 0b001) {
			B = A + busval;
		}
		if(alusel == 0b101) {
			B = A + busval + (carry ? 1 : 0);
		}
		if(alusel == 0b011) {
			B = A - busval;
		}
		if(alusel == 0b010) {
			B = A == busval ? 1 : 0;
		}
		if(alusel == 0b000) {
			B = A > busval ? 1 : 0;
		}
		
		carry = B > 0b00111111;
		B &= 0b00111111;
		zero = B == 0;
	}
	
}