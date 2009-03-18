/* ripple carry adder */
module addern(carryin, X, Y, S, carryout);
	parameter n = 32;
	input carryin;
	input [n-1:0] X, Y;
	output [n-1:0] S;
	output carryout;
	wire [n:0] C;
	wire [n-1:0] z1, z2, z3;

	assign C[0] = carryin;
	assign carryout = C[n];
	xor x0[n-1:0] (S, X, Y, C[n-1:0]);
	and a0[n-1:0] (z1, X, Y);
	and a1[n-1:0] (z2, X, C[n-1:0]);
	and a2[n-1:0] (z3, Y, C[n-1:0]);
	or o0[n-1:0] (C[n:1], z1, z2, z3);
endmodule
