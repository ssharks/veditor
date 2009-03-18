module seq1101(x, y, CLK, RESET);
	input x;
	input CLK;
	input RESET;
	output y;
	reg y;

	parameter start = 3'b000, got1 = 3'b001, got11 = 3'b011,
		  got110 = 3'b010, got1101 = 3'b110;

	reg [2:0] Q;	// state variables
	reg [2:0] D;	// next state logic output

	// next state logic
	always @(x or Q)
	begin
		case(Q)
		start: D = x ? got1  : start;
		got1:  D = x ? got11 : start;
		got11: D = x ? got11 : got110;
		got110: D = x ? got1101 : start;
		got1101: D = x ? got11 : start;
		default: D = 3'bxxx;
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

	// output logic
	always @(Q)
		y = Q[2];

endmodule