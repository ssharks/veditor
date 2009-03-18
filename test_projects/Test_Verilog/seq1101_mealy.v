module seq1101_mealy(x, y, CLK, RESET);
	input x;
	input CLK;
	input RESET;
	output y;
	reg y;

	parameter start = 2'b00, got1 = 2'b01, got11 = 2'b11, got110 = 2'b10;

	reg [1:0] Q;	// state variables
	reg [1:0] D;	// next state logic output

	// next state logic
	always @(x or Q)
	begin
		y = 0;
		case(Q)
		start: D = x ? got1  : start;
		got1:  D = x ? got11 : start;
		got11: D = x ? got11 : got110;
		got110: if (x)
		begin
			D = got1;
			y = 1;
		end
		else
		begin
			D = start;
			y = 0;
		end
		default: D = 2'bxx;
		endcase
	end

	// state variables
	always @(posedge CLK or negedge RESET)
	begin
		if (RESET)
			Q = D;
		else
			Q = 0;
	end
endmodule