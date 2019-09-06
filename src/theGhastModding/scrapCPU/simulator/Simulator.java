package theGhastModding.scrapCPU.simulator;

import java.util.Arrays;

public class Simulator {
	
	public int A;
	public int B;
	public int M;
	public int PC;
	public int P;
	public int ins;
	public int STEP;
	public boolean carry;
	public boolean zero;
	
	public int[] RAM = new int[64];
	public int[] ROM = new int[512];
	
	public boolean mode = false;
	public boolean qMode = false;
	
	public Simulator() {
		reset(false);
	}
	
	public void reset(boolean keepRom) {
		A = B = M = PC = ins = STEP = P = 0;
		carry = false;
		zero = true;
		Arrays.fill(RAM, 0);
		//Random rng = new Random();
		//for(int i = 0; i != RAM.length; i++) RAM[i] = rng.nextInt() & 0b111111;
		if(!keepRom) Arrays.fill(ROM, 0);
	}
	
	public void step() {
		PC &= 0b111111111;
		if(STEP == 0) {
			//Nothing
			STEP++;
			return;
		}
		if(STEP == 1) {
			//Fetch Instruction
			STEP++;
			ins = ROM[PC] & 0b00111111;
			mode = (ins & 0b00100000) != 0;
			qMode = (ins & 0b00010000) != 0;
			if(!mode || (ins & 0b00011111) == 0x00 || ins == 0x3F) PC++;
			return;
		}
		if(ins == 0x3F) {
			if(STEP == 2) A = ROM[PC];
			if(STEP == 3) {
				STEP = 1;
				PC++;
				return;
			}
			STEP++;
			return;
		}
		if((ins & 0b00011111) == 0b010000) {
			if(!mode && STEP == 2) P = ROM[PC] & 0b111;
			if(mode && STEP == 2) P = RAM[M] & 0b111;
			if(STEP == 3) {
				STEP = 1;
				PC++;
				return;
			}
			STEP++;
			return;
		}
		switch(ins & 0b00001111) {
		default:
			STEP = 1;
			PC++;
			return;
		case 0x01: //LOAD
			if(!mode && STEP == 2) M = ROM[PC];
			if(STEP == 3) A = RAM[M];
			if(STEP == 4) {
				STEP = 1;
				PC++;
				return;
			}
			break;
		case 0x02: //STORE
			if(!mode && STEP == 2) M = ROM[PC];
			if(STEP == 3) RAM[M] = B;
			if(STEP == 4) {
				STEP = 1;
				PC++;
				return;
			}
			break;
		case 0x03: //STOREA
			if(STEP == 2) ALUstep(0b001, 0);
			if(!mode && STEP == 3) M = ROM[PC];
			if(STEP == 4) RAM[M] = B;
			if(STEP == 5) {
				STEP = 1;
				PC++;
				return;
			}
			break;
		case 0x04: //ADD
			if(!mode && STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b001, RAM[M]);
			if(!qMode && STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 1;
				PC++;
				return;
			}
			break;
		case 0x05: //ADDc
			if(!mode && STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b101, RAM[M]);
			if(!qMode && STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 1;
				PC++;
				return;
			}
			break;
		case 0x06: //SUB
			if(!mode && STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b011, RAM[M]);
			if(!qMode && STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 1;
				PC++;
				return;
			}
			break;
		case 0x07: //SUBc
			if(!mode && STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b111, RAM[M]);
			if(!qMode && STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 1;
				PC++;
				return;
			}
			break;
		case 0x08: //EQL
			if(!mode && STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b010, RAM[M]);
			if(!qMode && STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 1;
				PC++;
				return;
			}
			break;
		case 0x09: //MAG
			if(!mode && STEP == 2) M = ROM[PC];
			if(STEP == 3) ALUstep(0b000, RAM[M]);
			if(!qMode && STEP == 4) A = B;
			if(STEP == 5) {
				STEP = 1;
				PC++;
				return;
			}
			break;
		case 0x0C: //JMP
			if(!mode && STEP == 2) PC = ROM[PC] + (P << 6);
			if(mode && STEP == 2) PC = RAM[M] + (P << 6);
			if(STEP == 3) {
				STEP = 1;
				return;
			}
			break;
		case 0x0D: //JZ
			if(!mode && STEP == 2 && zero) PC = ROM[PC] + (P << 6);
			if(mode && STEP == 2 && zero) PC = RAM[M] + (P << 6);
			if(STEP == 3 && zero) {
				STEP = 1;
				return;
			}
			if(STEP == 2 && !zero) {
				STEP = 1;
				PC++;
				return;
			}
			break;
		case 0x0E: //JNZ
			if(STEP == 2 && zero) {
				STEP = 1;
				PC++;
				return;
			}
			if(!mode && STEP == 2 && !zero) PC = ROM[PC] + (P << 6);
			if(mode && STEP == 2 && !zero) PC = RAM[M] + (P << 6);
			if(STEP == 3 && !zero) {
				STEP = 1;
				return;
			}
			break;
		case 0x0F: //LOADm
			if(!mode && STEP == 2) M = ROM[PC];
			if(mode && STEP == 2) M = RAM[M];
			if(STEP == 3) {
				STEP = 1;
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
			B = A + (~busval & 0b00111111) + 1;
		}
		if(alusel == 0b010) {
			B = A == busval ? 1 : 0;
		}
		if(alusel == 0b000) {
			B = A > busval ? 1 : 0;
		}
		if(alusel == 0b111) {
			B = A + (~busval & 0b00111111) + (carry ? 1 : 0);
		}
		
		carry = B > 0b00111111;
		B &= 0b00111111;
		zero = B == 0;
	}
	
}